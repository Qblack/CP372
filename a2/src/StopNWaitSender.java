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
	        int rn = Integer.parseInt(args[3]);
			
			//open file
			File sendFile = new File(filename);
			int len = (int) sendFile.length();
			
			//read file
			int readBytes = 124;
			byte[] msg = new byte[readBytes];
			FileInputStream readFile = new FileInputStream(sendFile);
			
			//create Datagram sockets
			DatagramSocket sendToRecv = new DatagramSocket(recUDPPort,hostAddr);
			DatagramSocket recvToSend = new DatagramSocket(sendUDPPort,hostAddr);
			int time = 1000;				//time of timeout for packets in milliseconds
			
			//set start time for transmission
			long start = System.nanoTime();
			
			int offset = 0;
			int num = 0;
			int sequence = 0;
			byte pkt = 1;
			while ((offset<len)&&(num!=-1)){
				//clear message and set to needed size, then read in data
				if ((offset+readBytes)>len){
					msg = new byte[len-offset];
					num = readFile.read(msg,1,len-offset);
				}else{
					msg = new byte[readBytes];
					num = readFile.read(msg,1,readBytes);
				}
				offset = offset + readBytes;
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
				if (sequence % rn != 0) {
					sendToRecv.send(packet);
				}
				//wait for response from receiver
				sendToRecv.setSoTimeout(time);				//in milliseconds
				boolean resp = false;
				
				while(resp == false){
					byte[] ack = new byte[1];
					DatagramPacket p_ack = new DatagramPacket(ack, 1);
					try{
						recvToSend.receive(p_ack);
						ack = p_ack.getData();
						//check if valid ACK and every packet not lost
						if ( msg[0] == ack[p_ack.getLength() - 1]){
							resp = true;
						}
						//if not, resend packet
						else {
							sendToRecv.send(packet);
							sendToRecv.setSoTimeout(time);
						}
					}
					//in case of dropped packet
					catch (SocketTimeoutException e){
						//resend packet
						sendToRecv.send(packet);
						sendToRecv.setSoTimeout(time);
					}
				}
			}
			readFile.close();
			
			DatagramPacket EOT = new DatagramPacket("FIN".getBytes(),3,hostAddr,recUDPPort);
			sendToRecv.send(EOT);
			
			//close sockets
			sendToRecv.close();
			recvToSend.close();
			
			//report total transmission time
			System.out.println("\nTime to transmit in Milliseconds: " + (System.nanoTime() - start)/1000000);
		}
		else{
			System.out.println("Invalid number of arguments.");
		}
	}
}
