import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;
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
        long startTime = System.nanoTime();
        Sender sender = new Sender(destinationAddress,destinationPort,senderPort,reliabilityNumber,windowSize);
        sender.start(fileName);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime)/1000000;
        System.out.format("Total Transmission Time: %d milliseconds",duration);
    }

    public static class Sender{
        private  static final int FILE_DATA_SIZE = 124;
        private  static final int PACKET_SIZE = FILE_DATA_SIZE+1;
        public static final int TIMEOUT = 1000;
        private String destinationAddress;
        private int destinationPort;
        private int senderPort;
        private int reliabilityNumber;
        private int windowSize;
        private int nextSeqNum = 0;
        private int MAX_SEQUENCE_NUMBER = 128;
        private int attemptedSent = 1;
        private int sendBase=0;
        private DatagramPacket[] sndpkt;
        private InetAddress receiver;
        private DatagramPacket eot;
        private DatagramSocket socket;
        private Timer timer = null;



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
            this.socket = new DatagramSocket(this.senderPort);

            File fileHandle = new File(fileName);
            FileInputStream fileData = new FileInputStream(fileHandle);

            this.sndpkt = new DatagramPacket[256];

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
                    byte[] tobuff = new byte[PACKET_SIZE];
                    DatagramPacket receivedPacket = make_pkt(this.nextSeqNum, tobuff);
                    this.socket.setSoTimeout(100000);
                    try {
                        this.socket.receive(receivedPacket);
                    }catch (SocketTimeoutException e){
                    }

                    rdt_rcv(receivedPacket);

                }while(!sent);

            }

            this.socket.send(this.eot);


            byte[] tobuff;
            do{
                tobuff = new byte[6];
                DatagramPacket receivedPacket = new DatagramPacket(tobuff,6);
                this.socket.receive(receivedPacket);
            }while(tobuff[5]==0);

            this.socket.close();
            fileData.close();
            stop_timer();

        }

        private boolean rdt_send(byte[] data) throws IOException, InterruptedException {
            if(this.nextSeqNum<this.sendBase+this.windowSize){
                this.sndpkt[this.nextSeqNum] = make_pkt(this.nextSeqNum, data);
                send_pkt(this.sndpkt[this.nextSeqNum]);
                if(this.sendBase==this.nextSeqNum){
                    start_timer();
                }
                this.nextSeqNum = (this.nextSeqNum+1)% MAX_SEQUENCE_NUMBER;
                return true;
            }
            return false;
        }

        private void timeout() throws IOException {
            start_timer();
            int i = this.sendBase;
            if(i<this.nextSeqNum){
                while(i < this.nextSeqNum){
                    send_pkt(this.sndpkt[i]);
                    i++;
                }
            }else{
                while(i < MAX_SEQUENCE_NUMBER){
                    send_pkt(this.sndpkt[i]);
                    i++;
                }
                i=0;
                while(i<this.nextSeqNum){
                    send_pkt(this.sndpkt[i]);
                    i++;
                }
            }
        }

        private void rdt_rcv(DatagramPacket rcvpkt){
            this.sendBase = getacknum(rcvpkt)+1;
            if(this.sendBase==this.nextSeqNum){
                stop_timer();
            }else{
                start_timer();
            }
        }

        private int getacknum(DatagramPacket rcvpkt) {
            byte[] ack = rcvpkt.getData();
            int sequenceNumber = Integer.parseInt(String.valueOf(ack[rcvpkt.getLength() - 1]));
            return sequenceNumber % MAX_SEQUENCE_NUMBER;
        }


        private DatagramPacket make_pkt(int nextSequenceNumber, byte[] data) throws IOException {
            data[0] = (byte) nextSequenceNumber;
            int i =1;
            boolean zero =false;
            while (i<data.length && !zero){
                if(data[i] == 0){
                    zero = true;
                }else{
                    i++;
                }
            }
            return new DatagramPacket(data,i, receiver, this.destinationPort);
        }

        private void send_pkt(DatagramPacket packet) throws IOException {
            if(this.reliabilityNumber==0 || this.attemptedSent %this.reliabilityNumber!=0){
                this.socket.send(packet);
            }
            this.attemptedSent++;
        }

        private void start_timer(){
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


        private void stop_timer(){
            if(this.timer!=null){
                this.timer.cancel();
                this.timer.purge();
            }
        }


    }
}
