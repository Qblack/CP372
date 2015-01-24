import java.io.* ;
import java.io.InputStream;
import java.lang.Exception;
import java.lang.System;
import java.net.* ;
import java.util.* ;

public final class Server {

    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;
    private static final String PORT_ERROR = "Invalid port please choose a port between: "+ MIN_PORT +" and " + MAX_PORT;

    public static void main(String argv[]) throws Exception {
        // Get the port number from the command line.

        int port = new Integer(argv[0]).intValue();

        if (port< MIN_PORT || port > MAX_PORT){
            throw new Exception(PORT_ERROR);
        }
        // Establish the listen socket.
        ServerSocket socket = new ServerSocket(port);

        // Process HTTP service requests in an infinite loop.
        while (true) {
            // Listen for a TCP connection request.
            Socket connection = socket.accept();

            // Construct an object to process the HTTP request message.
            ShapeRequest request = new ShapeRequest(connection);

            // Create a new thread to process the request.
            Thread thread = new Thread((Runnable) request);

            // Start the thread.
            thread.start();
        }
    }

    public static class ShapeRequest implements Runnable {
        Socket m_socket;

        public ShapeRequest(Socket socket) throws Exception{
            m_socket = socket;
        }
        // Implement the run() method of the Runnable interface.

        public void run() {
            try {
                processRequest();
            } catch (Exception e) {
                System.out.println(e);
            }
        }

    private void processRequest() throws Exception{
        InputStream inputStream = m_socket.getInputStream();
        DataOutputStream outputStream = new DataOutputStream(m_socket.getOutputStream());

        // Set up input stream filters.
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        // Get the request line of the HTTP request message.
        String requestLine = reader.readLine();

        // Extract the filename from the request line.
        StringTokenizer tokens = new StringTokenizer(requestLine);


        String method = tokens.nextToken();

        if (Objects.equals(method, "GET")){
            System.out.print("GET request made");
        }else if (Objects.equals(method, "POST")){
            System.out.print("POST request made");
        }else{
            throw new Exception(String.valueOf(405));
        }
        System.out.print("Hello World");
        System.out.print("Hello World");
        String fileName = tokens.nextToken();


    }
}

}

