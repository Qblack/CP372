import javax.swing.*;
import java.awt.*;

public class Client {

    private static void createAndShowGUI() {
        final MainView view = new MainView();

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
