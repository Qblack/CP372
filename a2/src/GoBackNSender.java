import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

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

        Sender sender = new Sender(destinationAddress,destinationPort,senderPort,reliabilityNumber,windowSize);
        sender.start(fileName);

    }

    public static class Sender{
        private  static final int FILE_DATA_SIZE = 60;
        private  static final int PACKET_SIZE = FILE_DATA_SIZE+1;
        public static final int TIMEOUT = 4*100000000;
        private String destinationAddress;
        private int destinationPort;
        private int senderPort;
        private int reliabilityNumber;
        private int windowSize;
        private int nextSeqNum = 0;
        private int sendBase=0;
        private DatagramPacket[] sndpkt;
        private InetAddress receiver;
        private DatagramPacket eot;
        private DatagramSocket socket;
        private Timer timer = new Timer();
        private final TimerTask start_timer = new TimerTask() {
            @Override
            public void run() {
                try {
                    timeout();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };


        public Sender(String destinationAddress,int destinationPort,int senderPort, int reliabilityNumber, int windowSize) throws UnknownHostException {
            this.destinationAddress = destinationAddress;
            this.destinationPort = destinationPort;
            this.senderPort = senderPort;
            this.reliabilityNumber = reliabilityNumber;
            this.windowSize = windowSize;
            this.receiver = InetAddress.getByName(this.destinationAddress);
            this.eot = new DatagramPacket("EOF".getBytes(),3, this.receiver,this.destinationPort);
        }

        public void start(String fileName) throws IOException, InterruptedException {
            socket = new DatagramSocket(this.senderPort);

            File fileHandle = new File(fileName);
            FileInputStream fileData = new FileInputStream(fileHandle);

            this.sndpkt = new DatagramPacket[(int) Math.ceil(fileHandle.length()/ FILE_DATA_SIZE)+1];

            int bytesRead = 0;
            int totalBytes = (int) fileHandle.length();
            int read = 0;
            byte[] data;
            while (bytesRead<totalBytes && read!=-1) {
                data = new byte[PACKET_SIZE];
                int bytesToRead = Math.min(totalBytes - bytesRead, data.length - 1);
                read = fileData.read(data, 1, bytesToRead);
                bytesRead += read;

                boolean sent;
                do{
                    sent = rdt_send(data);
                    byte[] tobugg = new byte[PACKET_SIZE];
                    DatagramPacket receivedPacket = new DatagramPacket(tobugg,tobugg.length);
                    socket.receive(receivedPacket);
                    rdt_rcv(receivedPacket);
                }while(!sent);

            }

            socket.send(this.eot);
            socket.close();

        }

        private boolean rdt_send(byte[] data) throws IOException, InterruptedException {
            if(this.nextSeqNum<this.sendBase+this.windowSize){
                this.sndpkt[this.nextSeqNum] = make_pkt(this.nextSeqNum, data);
                this.socket.send(this.sndpkt[this.nextSeqNum]);
                if(this.sendBase==this.nextSeqNum){
                    timer.schedule(start_timer, TIMEOUT);
                }
                this.nextSeqNum= (this.nextSeqNum+1)%128;
                return true;
            }
            return false;
        }

        private void timeout() throws IOException {
            timer.schedule(start_timer,TIMEOUT);
            for(int i=this.sendBase;i<this.nextSeqNum;i++){
                this.socket.send(this.sndpkt[i]);
            }
        }

        private void rdt_rcv(DatagramPacket rcvpkt){
            this.sendBase = getacknum(rcvpkt)+1;
            if(this.sendBase==this.nextSeqNum){
                this.timer.cancel();
            }else{
//                this.timer.cancel();
//                this.timer.purge();
//                this.timer.schedule(this.start_timer,TIMEOUT);
            }
        }

        private int getacknum(DatagramPacket rcvpkt) {
            byte[] ack = rcvpkt.getData();
            int sequenceNumber = Integer.parseInt(String.valueOf(ack[rcvpkt.getLength() - 1]));
            return sequenceNumber % 128;
        }


        private DatagramPacket make_pkt(int nextSequenceNumber, byte[] data){
            data[0] = (byte) nextSequenceNumber;
            return new DatagramPacket(data,data.length, receiver, this.destinationPort);
        }


    }
}
