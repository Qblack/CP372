package src;	//be sure to delete this line
import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
	static Socket socket = null;
	static PrintWriter out = null;
	static BufferedReader in = null;
	
    private static void createAndShowGUI() {
    	final MainView view = new Client.MainView();

        JFrame frame = new JFrame("The Shaper");
        frame.setContentPane(view);
        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Display the window.
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(Client::createAndShowGUI);
    }

    public static class ConnectionView extends JPanel {
        final private String IP_FORMAT = "###.###.###.###";
        final private String PORT_FORMAT = "######";
        private JButton m_connectButton = new JButton("Connect");
        private JButton m_disconnectButton = new JButton("Disconnect");
        private JLabel m_ipLabel = new JLabel("IP: ");
        private JLabel m_portLabel = new JLabel("PORT: ");
        private JTextField m_ipText = new JTextField(IP_FORMAT.length());
        private JTextField m_portText = new JTextField(PORT_FORMAT.length());

        public ConnectionView(){
            this.layoutView();
        }

        private void layoutView() {
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            this.add(this.m_ipLabel);
            this.add(this.m_ipText);
            this.add(this.m_portLabel);
            this.add(this.m_portText);
            this.add(this.m_connectButton);
            this.add(this.m_disconnectButton);
        
	        m_connectButton.addActionListener(new ActionListener() {
	    	  public void actionPerformed(ActionEvent evt) {
	    	    // do this on Connect press
	    		String ipAddr = m_ipText.getText();
	    		int portNum = Integer.parseInt(m_portText.getText());
	    		
	    		try {
	    				Socket socket = new Socket(ipAddr, portNum);
	    				out = new PrintWriter (socket.getOutputStream(), true);
	    				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	    				//BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
	    		}
	    		
	    		catch (IndexOutOfBoundsException e) {
	    		    System.err.println("IndexOutOfBoundsException: " + e.getMessage());
	    		} catch (IOException e) {
	    		    System.err.println("Caught IOException: " + e.getMessage());
	    		}
	    		
	    		finally {
	    		    if (out != null) { 
	    		        System.out.println("Closing PrintWriter");
	    		        out.close(); 
	    		    } else { 
	    		        System.out.println("PrintWriter not open");
	    		    } 
	    		} 
	    	  }
	    	});
	        
	        m_disconnectButton.addActionListener(new ActionListener() {
		    	  public void actionPerformed(ActionEvent evt) {
		    	    // do this on Disconnect press
		    		  
		    	  }
		    });
        }
    }

    public static class RequestView extends JPanel {
        private JTextField m_inputText = new JTextField(20);
        private JLabel m_inputLabel = new JLabel("Input: ");
        private JButton m_postButton = new JButton("POST");
        private JButton m_getButton = new JButton("GET");

        public RequestView() {
            this.layoutView();
        }

        private void layoutView() {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.add(m_inputLabel);
            this.add(m_inputText);
            this.add(m_postButton);
            this.add(m_getButton);
            
            m_postButton.addActionListener(new ActionListener() {
          	  public void actionPerformed(ActionEvent evt) {
          	    // do this on POST press
          		  System.out.print("post ...");
          	  }
            });
            
            m_getButton.addActionListener(new ActionListener() {
          	  public void actionPerformed(ActionEvent evt) {
          	    // do this on GET press
          		  System.out.print("get ...");
          	  }
            });
        }
    }

    public static class ResponseView extends JPanel {
        private JLabel m_outputLabel = new JLabel("Output: ");
        private JScrollPane m_outputPane = new JScrollPane();

        public ResponseView(){
            this.layoutView();
        }

        private void layoutView() {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.add(this.m_outputLabel);
            this.add(this.m_outputPane);
        }
    }

    private static class MainView extends JPanel {
        private ResponseView m_responseView = null;
        private ConnectionView m_connectionView = null;
        private RequestView m_requestView = null;

        private MainView(){
            this.m_requestView = new RequestView();
            this.m_connectionView = new ConnectionView();
            this.m_responseView = new ResponseView();
            this.layoutView();
        }

        private void layoutView() {
            this.setLayout(new BorderLayout());
            this.add(this.m_connectionView,BorderLayout.NORTH);
            this.add(this.m_requestView,BorderLayout.CENTER);
            this.add(this.m_responseView,BorderLayout.SOUTH);
        }
    }
}
