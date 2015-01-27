//package src;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.PlainDocument;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
	static Socket m_socket = null;
	static PrintWriter m_out = null;
	static BufferedReader m_in = null;
	static JTextArea m_display = new JTextArea(5,10);
    static JButton m_connectButton = new JButton("Connect");
    static JButton m_disconnectButton = new JButton("Disconnect");
    static JButton m_postButton = new JButton("POST");
    static JButton m_getButton = new JButton("GET");
	
	
    private static void createAndShowGUI() {
    	final MainView view = new Client.MainView();

        JFrame frame = new JFrame("The Shaper");
        frame.setContentPane(view);
        frame.setSize(500, 195);
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
        private JLabel m_ipLabel = new JLabel("IP: ");
        private JLabel m_portLabel = new JLabel("PORT: ");
        private JTextField m_ipText = new JTextField(IP_FORMAT.length());
        private JTextField m_portText = new JTextField(5);

        public ConnectionView(){
            this.layoutView();
        }

        private void layoutView() {
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            this.add(this.m_ipLabel);
            this.add(this.m_ipText);
            this.add(this.m_portLabel);
            this.add(this.m_portText);
            this.add(m_connectButton);
            this.add(m_disconnectButton);
            m_disconnectButton.setEnabled(false);
        
	        m_connectButton.addActionListener(new ActionListener() {
	    	  public void actionPerformed(ActionEvent evt) {
	    	    // do this on Connect press
	    		String ipAddr = m_ipText.getText();
	    		int portNum = Integer.parseInt(m_portText.getText());
	    		
	    		try {
    				m_socket = new Socket(ipAddr, portNum);
    				m_out = new PrintWriter (m_socket.getOutputStream(), true);
    				m_in = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));

                	String single_out = m_in.readLine();
                	m_display.replaceRange(single_out, 0, m_display.getText().length());
	    		}
	    		
	    		catch (IndexOutOfBoundsException e) {
	    		    System.err.println("IndexOutOfBoundsException: " + e.getMessage());
	    		} catch (IOException e) {
	    		    System.err.println("Caught IOException: " + e.getMessage());
	    		}
	    		//on successful connect, disable connect button and show disconnect as well as POST/GET
	    		m_connectButton.setEnabled(false);
	    		m_disconnectButton.setEnabled(true);
	    		m_postButton.setEnabled(true);
	    		m_getButton.setEnabled(true);
 	    	  }
	    	  
	    	});
	        
	        m_disconnectButton.addActionListener(new ActionListener() {
		    	  public void actionPerformed(ActionEvent evt) { 
		    		  try{
							m_out.close();
							m_in.close();
							m_socket.close();
						}catch (IOException e){
							System.out.println(e);
						}
		    		//on successful disconnect, disable disconnect & POST/GET button and show connect
		    		m_connectButton.setEnabled(true);
		    		m_disconnectButton.setEnabled(false);
		    		m_postButton.setEnabled(false);
		    		m_getButton.setEnabled(false);
		    	  }
		    });
        }
    }

    public static class RequestView extends JPanel {
        private JTextField m_inputText = new JTextField(1);
        private JLabel m_inputLabel = new JLabel("Input: ");

        public RequestView() {
            this.layoutView();
        }

        private void layoutView() {
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            this.add(m_inputLabel);
            this.add(m_inputText);
            this.add(m_postButton);
            this.add(m_getButton);
            m_postButton.setEnabled(false);
            m_getButton.setEnabled(false);
                       
            m_postButton.addActionListener(new ActionListener() {
          	  public void actionPerformed(ActionEvent evt) {
                m_out.println("POST " + m_inputText.getText());

                try {
                	String single_out = m_in.readLine();
                	m_display.replaceRange(single_out, 0, m_display.getText().length());
					}catch (IOException e) {
						System.out.println(e);
					}
            	}
            });
            
            m_getButton.addActionListener(new ActionListener() {
          	  public void actionPerformed(ActionEvent evt) {
                  m_out.println("GET " + m_inputText.getText());
                  try {
                	  String delims = "&";
                	  String single_out = "";
                	  String[] line_out = m_in.readLine().split(delims);
                	  for (int i=0; i<line_out.length; i++) {
                		  single_out = single_out + line_out[i].toString() + "\n";
                	  }
                	  m_display.replaceRange(single_out, 0, m_display.getText().length());
                  }catch (IOException e) {
            	 		System.out.println(e);
                  }
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
            m_display.setEditable(false);
            m_outputPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
            m_outputPane.setViewportView(m_display);
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
            this.m_connectionView.setBorder(new EmptyBorder(5,5,5,5));
            this.add(this.m_connectionView, BorderLayout.NORTH);

            this.add(this.m_requestView,BorderLayout.CENTER);
            this.add(this.m_responseView,BorderLayout.SOUTH);
        }
    }
}
