import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;

/**
 * Created by blac2410 on 2/27/2015.
 */
public class GoBackNSender {

    public static void main(String[] args) throws IOException {
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
        sender.SendFile(fileName);
    }

    public static class Sender{
        String destinationAddress;
        int destinationPort;
        int senderPort;
        int reliabilityNumber;
        int windowSize;

        public Sender(String destinationAddress,int destinationPort,int senderPort, int reliabilityNumber, int windowSize){
            this.destinationAddress = destinationAddress;
            this.destinationPort = destinationPort;
            this.senderPort = senderPort;
            this.reliabilityNumber = reliabilityNumber;
            this.windowSize = windowSize;
        }

        public void SendFile(String fileName) throws IOException {
            DatagramSocket socket = new DatagramSocket(this.senderPort);

            File fileHandle = new File(fileName);
            FileInputStream fileData = new FileInputStream(fileHandle);

            byte[] buffer = new byte[124];
            int bytesRead = 0;
            int totalBytes = (int) fileHandle.length();
            while (bytesRead<=totalBytes){
                int read = fileData.read(buffer, bytesRead, totalBytes-bytesRead);
                InetAddress receiver = InetAddress.getByName(this.destinationAddress);
                DatagramPacket packet = new DatagramPacket(buffer,buffer.length,receiver,this.destinationPort);
                socket.send(packet);
            }
            socket.close();
        }
    }
}
