/**
 * Quinton Black
 * Colin Hagerman
 * CP372: Networks
 * Assignment 2:  Sender Java file for Stop-and-Wait
 */

import java.io.*;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class StopNWaitSender {
	
	public static void main (String[] args) throws IOException {
		// Get inputs 
		if (args.length == 5){
			InetAddress hostAddr = null;
			hostAddr = InetAddress.getByName(args[0]);

	        int recUDPPort = Integer.parseInt(args[1]);
	        int sendUDPPort=Integer.parseInt(args[2]);
	        String filename = args[3];
	        int rn = Integer.parseInt(args[4]);
			
			//open file
			File sendFile = new File(filename);
			int len = (int) sendFile.length();
			
			//read file
			int readBytes = 124;
			FileInputStream readFile = new FileInputStream(sendFile);
			
			//create Datagram sockets
			DatagramSocket sock = new DatagramSocket(sendUDPPort,hostAddr);
			int time = 1000;				//time of timeout for packets in milliseconds
			
			//set start time for transmission
			long start = System.nanoTime();
			
			int bytesRead = 0;
			int num = 0;
			int sequence = 0;
			byte pkt = 1;
			int bytesToRead = 0;
			while ((bytesRead<len)&&(num!=-1)){
				byte[] msg = new byte[readBytes];
				bytesToRead = Math.min(len - bytesRead, msg.length - 1);
				num = readFile.read(msg,1,bytesToRead);
				bytesRead = bytesRead + num;
				sequence = sequence + 1;
				
				//set sequence acknowledgement code
				if (pkt == 0){
					pkt = 1;
				}else{
					pkt = 0;
				}
				//add ACK number to packet
				msg[0] = pkt;
				
				//create & send packet to receiver
				DatagramPacket packet = new DatagramPacket(msg,msg.length,hostAddr,recUDPPort);
				//check for dropped packets
				if ((rn == 0)||(sequence % rn != 0)) {
					sock.send(packet);
				}
				//wait for response from receiver
				sock.setSoTimeout(time);				//in milliseconds
				boolean resp = false;
				
				while(resp == false){ 
					byte[] ack = new byte[4];
					DatagramPacket p_ack = new DatagramPacket(ack, 4);
					try{
						sock.receive(p_ack);
						ack = p_ack.getData();
						//check if valid ACK and every packet not lost
						if ( msg[0] == (ack[p_ack.getLength() - 1] % 2)){
							resp = true;
						}
						//if not, resend packet
						else {
							//System.out.println("resend");
							sock.send(packet);
							sock.setSoTimeout(time);
						}
					}
					//in case of dropped packet
					catch (SocketTimeoutException e){
						//resend packet
						sock.send(packet);
						sock.setSoTimeout(time);
					}
				}
			}
			readFile.close();
			
			DatagramPacket EOT = new DatagramPacket("EOF".getBytes(),3,hostAddr,recUDPPort);
			sock.send(EOT);
			
			//close sockets
			sock.close();
			
			//report total transmission time
			System.out.println("\nTime to transmit in Milliseconds: " + (System.nanoTime() - start)/1000000);
		}
		else{
			System.out.println("Invalid number of arguments.");
		}
	}
}
