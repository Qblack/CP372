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

public class StopNWaitReciever {
	
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
			byte[] msg;						//stores read data
			msg=new byte[123];
			FileInputStream readFile = new FileInputStream(sendFile);
			
			int offset = 0;
			int num;
			do {
				num = readFile.read(msg,offset,len-offset);
				offset = offset + num;
			} while((offset<len)&&(num!=-1));
			readFile.close();
			
			//create Datagram sockets
			DatagramSocket sendToRecv = new DatagramSocket(recUDPPort,hostAddr);
			DatagramSocket recvToSend = new DatagramSocket(sendUDPPort,hostAddr);
			
			//send packets
			byte[] buf = new byte[1];
			DatagramPacket ack = new DatagramPacket(buf,buf.length);
			int pkt = 1;
			for (int i=0; num < msg.length; i++){
				//assign packet number
				if (pkt == 0){
					pkt = 1;
				}
				else{
					pkt = 0;
				}
				/*
				 * Need to add sequence number to packet
				 * Need to add RN (Reliability Number)
				 * Need to only send one packet at a time (potentially move into above do-while loop)
				 */
				
				//create & send packet to receiver
				DatagramPacket packet = new DatagramPacket(msg,msg.length,hostAddr,recUDPPort);
				sendToRecv.send(packet);
				
				//wait for response from receiver
				sendToRecv.setSoTimeout(1000);
				boolean resp = false;
				while(resp==false){
					try{
						recvToSend.receive(ack);
						//check if valid ACK
						if (Integer.parseInt(ack.toString())==pkt){
							resp = true;
						}
						//if not, resend packet
						else{
							sendToRecv.send(packet);
							sendToRecv.setSoTimeout(1000);
						}
					}
					//in case of dropped packet
					catch (SocketTimeoutException e){
						//resend packet
						sendToRecv.send(packet);
						sendToRecv.setSoTimeout(1000);
					}
				}
			}
			DatagramPacket EOT = new DatagramPacket("FIN".getBytes(),3,hostAddr,recUDPPort);
			sendToRecv.send(EOT);
			
			//close sockets
			sendToRecv.close();
			recvToSend.close();
		}
	}
}
