import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

public class Receiver{

    public static void main(String args[]) throws IOException, InterruptedException {
        String senderHost = args[0];
        int senderPort = Integer.parseInt(args[1]);
        int receiverPort = Integer.parseInt(args[2]);
        String fileName = args[3];
        ReceiverThread receiver = new ReceiverThread(senderHost,senderPort,receiverPort,fileName);
        receiver.start();
    }


    public static class ReceiverThread {
        private static final String ACK = "ack";
        public static final String EOF = "EOF";
        InetAddress senderAddress;
        int senderPort;
        int receiverPort;
        String fileName;
        private FileOutputStream outFile;
        private int expectedseqnum =0;
        private DatagramSocket socket;
        private DatagramPacket sndpkt;


        public ReceiverThread(String senderAddress,int sPort,int rPort, String fileName) throws UnknownHostException {
            this.senderAddress =  InetAddress.getByName(senderAddress);
            this.senderPort = sPort;
            this.receiverPort = rPort;
            this.fileName = fileName;

        }

        public void start() throws IOException, InterruptedException {
            File fileHandler = new File(this.fileName);
            outFile = new FileOutputStream(fileHandler);
            socket = new DatagramSocket(this.receiverPort);

            this.expectedseqnum = 0;
            this.sndpkt = make_pkt(0,ACK);

            default_receive();
            byte[] buffer = new byte[125];
            DatagramPacket rcvpkt = new DatagramPacket(buffer,buffer.length);
            boolean eof = false;
            while(!eof){
                this.socket.receive(rcvpkt);
                eof = rdt_rcv(rcvpkt);
            }
            outFile.close();
            socket.close();

        }

        private boolean rdt_rcv(DatagramPacket rcvpkt) throws IOException {
            boolean eof = false;
            byte[] data = extract(rcvpkt);
            if(Arrays.equals(data, EOF.getBytes())){
                eof=true;
                sndpkt = makeEOFAcKPacket();
                udt_send(sndpkt);
            }else{
                deliver_data(data);
                sndpkt = make_pkt(this.expectedseqnum,ACK);
                udt_send(sndpkt);
                this.expectedseqnum++;
            }
            return eof;
        }

        private void default_receive() throws IOException {
            udt_send(sndpkt);
        }


        private void udt_send(DatagramPacket sndpkt) throws IOException {
            this.socket.send(sndpkt);
        }

        private void deliver_data(byte[] data) throws IOException {
            if(data.length>1){
                this.outFile.write(data,1,data.length-1);
            }
        }

        private byte[] extract(DatagramPacket rcvpkt) {
            byte[] data = new byte[rcvpkt.getLength()];
            byte[] rcvpktData = rcvpkt.getData();
            for(int i =0;i<data.length;i++){
                data[i] = rcvpktData[i];
            }
            return data;
        }


        private DatagramPacket make_pkt(int expectedSeqNumber,String ack){
            byte[] byteData = new byte[ack.length()+1];
            int i=0;
            for(byte b : ack.getBytes()){
                byteData[i]=b;
                i++;
            }
            byteData[byteData.length-1] = (byte) (expectedSeqNumber%128);
            return new DatagramPacket(byteData,byteData.length,this.senderAddress,this.senderPort);
        }

        private DatagramPacket makeEOFAcKPacket(){
            byte[] byteData = new byte[ACK.length()+EOF.length()];
            int i=0;
            for(byte b : ACK.getBytes()){
                byteData[i]=b;
                i++;
            }
            for(byte b : EOF.getBytes()){
                byteData[i]=b;
                i++;
            }
            return new DatagramPacket(byteData,byteData.length,this.senderAddress,this.senderPort);
        }

    }
}
