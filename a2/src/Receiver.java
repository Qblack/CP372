import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

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
        InetAddress senderAddress;
        int senderPort;
        int receiverPort;
        String fileName;



        public ReceiverThread(String senderAddress,int sPort,int rPort, String fileName) throws UnknownHostException {
            this.senderAddress =  InetAddress.getByName(senderAddress);
            this.senderPort = sPort;
            this.receiverPort = rPort;
            this.fileName = fileName;

        }

        public void start() throws IOException, InterruptedException {
            File fileHandler = new File(this.fileName);
            FileOutputStream outFile = new FileOutputStream(fileHandler);
            DatagramSocket socket = new DatagramSocket(this.receiverPort);

            int expectedSeqNumber = 0;
            DatagramPacket packet = new DatagramPacket(new byte[61],61);
            while("FIN".getBytes()!=packet.getData()){
                socket.receive(packet);
                byte[] packetData = packet.getData();
                int actualSeqNumber = packetData[0];
                DatagramPacket ackPacket;
                if(actualSeqNumber==expectedSeqNumber){
                    outFile.write(packet.getData());
                    ackPacket = makeACK(expectedSeqNumber);
                    expectedSeqNumber++;
                }else{
                    ackPacket = makeACK(expectedSeqNumber);
                }
                socket.send(ackPacket);
            }
            outFile.close();
            socket.close();
        }


        private DatagramPacket makeACK(int expectedSeqNumber){
            byte[] byteData = ("ack"+String.valueOf(expectedSeqNumber)).getBytes();
            return new DatagramPacket(byteData,byteData.length,this.senderAddress,this.senderPort);
        }
    }
}
