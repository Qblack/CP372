import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class GoBackNSender {

    public static void main(String[] args) throws IOException, InterruptedException {
        if(args.length!=6){
            System.out.format("Expected 6 arguments got: %d", args.length);
        }

        String destinationAddress = args[0];
        int destinationPort = Integer.parseInt(args[1]);
        int senderPort = Integer.parseInt(args[2]);
        String fileName = args[3];
        int reliabilityNumber = Integer.parseInt(args[4]);
        int windowSize = Integer.parseInt(args[5]);
        long startTime = System.nanoTime();
        new GoBackNClient(destinationAddress,destinationPort,senderPort,reliabilityNumber,windowSize, fileName);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime)/1000000;
        System.out.format("Total Transmission Time: %d milliseconds",duration);
    }

    public static class GoBackNClient {
        public  static final int FILE_DATA_SIZE = 60;
        public  static final int PACKET_SIZE = FILE_DATA_SIZE+1;
        public static final int TIMEOUT = 400;
        public String destinationAddress;
        public int destinationPort;
        public int senderPort;
        public int reliabilityNumber;
        public int windowSize;
        public int m_nextSeqNum = 0;
        public int attemptedSent = 1;
        public int m_sendBase =0;
        public Vector<DatagramPacket> m_sndpkt;
        public InetAddress receiver;
        public DatagramPacket m_eot;
        public DatagramSocket m_socket;
        public Timer timer = null;
        public String m_fileName = "";
        public boolean m_eofSent = false;

        public GoBackNClient(String destinationAddress, int destinationPort, int senderPort, int reliabilityNumber, int windowSize, String fileName) throws UnknownHostException, SocketException {
            this.destinationAddress = destinationAddress;
            this.destinationPort = destinationPort;
            this.senderPort = senderPort;
            this.reliabilityNumber = reliabilityNumber;
            this.windowSize = windowSize;
            this.receiver = InetAddress.getByName(this.destinationAddress);
            m_eot = new DatagramPacket("EOF".getBytes(),3, this.receiver,this.destinationPort);
            m_fileName = fileName;
            m_socket = new DatagramSocket(senderPort);
            m_sndpkt = new Vector<DatagramPacket>();

            SendingManager sendingManager = new SendingManager();
            ReceiverManager receiverManager = new ReceiverManager();
            sendingManager.run();
            receiverManager.run();
            m_socket.close();
        }



        public class SendingManager implements Runnable{

            @Override
            public void run() {
                File fileHandle = new File(m_fileName);
                FileInputStream fileData = null;
                try {
                    fileData = new FileInputStream(fileHandle);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                int bytesRead = 0;
                int totalBytes = (int) fileHandle.length();
                int read = 0;
                byte[] data;

                while (bytesRead<totalBytes && read!=-1) {
                    data = new byte[PACKET_SIZE];
                    int bytesToRead = Math.min(totalBytes - bytesRead, data.length - 1);
                    assert fileData != null;
                    try {
                        read = fileData.read(data, 1, bytesToRead);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    bytesRead += read;

                    boolean sent = false;
                    do{
                        try {
                            sent = rdt_send(data);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }while(!sent);

                }
                try {
                    
                    m_socket.send(m_eot);
                    m_eofSent = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    assert fileData != null;
                    fileData.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                stop_timer();
            }
            private boolean rdt_send(byte[] data) throws IOException, InterruptedException {
                if(getNextSequenceNumber() < m_sendBase +windowSize){
                    m_sndpkt.insertElementAt(make_pkt(getNextSequenceNumber(), data),m_nextSeqNum);
                    send_pkt(m_sndpkt.elementAt(getNextSequenceNumber()));
                    if(m_sendBase == m_nextSeqNum){
                        start_timer();
                    }
                    m_nextSeqNum++;
                    return true;
                }
                return false;
            }
        }



        public class ReceiverManager implements Runnable{

            private boolean m_eofNotAcked;

            @Override
            public void run() {
                m_eofNotAcked = true;
                while(!m_eofSent && m_eofNotAcked){
                    byte[] tobuff = new byte[PACKET_SIZE];
                    DatagramPacket receivedPacket = null;
                    try {
                        receivedPacket = make_pkt(getNextSequenceNumber(), tobuff);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        assert receivedPacket != null;
                        m_socket.receive(receivedPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    rdt_rcv(receivedPacket);
                }

            }

            private void rdt_rcv(DatagramPacket rcvpkt){
                if(rcvpkt.getLength()<4){
                    if(m_sendBase == m_nextSeqNum){
                        stop_timer();
                    }else{
                        start_timer();
                    }
                    m_sendBase = getacknum(rcvpkt)+1;
                }else{
                    m_eofNotAcked = false;
                    stop_timer();

                }
            }
        }

        //Utility
        public int getacknum(DatagramPacket rcvpkt) {
            byte[] ack = rcvpkt.getData();
            return Integer.parseInt(String.valueOf(ack[rcvpkt.getLength() - 1]));
        }

        public DatagramPacket make_pkt(int nextSequenceNumber, byte[] data) throws IOException {
            data[0] = (byte) nextSequenceNumber;
            return new DatagramPacket(data,data.length, receiver, this.destinationPort);
        }

        public void send_pkt(DatagramPacket packet) throws IOException {
            if(this.reliabilityNumber==0 || this.attemptedSent %this.reliabilityNumber!=0){
                this.m_socket.send(packet);
            }
            this.attemptedSent++;
        }

        private int getNextSequenceNumber(){
            return m_nextSeqNum % 128;
        }


        //Timer Stuff
        public void timeout() throws IOException {
            start_timer();
            for(int i=m_sendBase;i<getNextSequenceNumber();i++){
                send_pkt(m_sndpkt.elementAt(i));
            }
        }

        public void start_timer(){
            stop_timer();
            this.timer = new Timer();
            this.timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        timeout();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            },TIMEOUT);
        }


        public void stop_timer(){
            if(this.timer!=null){
                this.timer.cancel();
                this.timer.purge();
            }
        }


    }
}
