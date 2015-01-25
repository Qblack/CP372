package src;	//be sure to delete this line
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
    private static Vector<Shape> m_shapes;

    //Main Server
    public static void main(String argv[]) throws Exception {
        // Get the port number from the command line.

        int port = new Integer(argv[0]).intValue();

        if (port< MIN_PORT || port > MAX_PORT){
            throw new Exception(PORT_ERROR);
        }
        // Establish the listen socket.
        ServerSocket socket = new ServerSocket(port);
        m_shapes = new Vector<>();

        // Process HTTP service requests in an infinite loop.
        while (true) {
            // Listen for a TCP connection request.
            Socket connection = socket.accept();

            // Construct an object to process the HTTP request message.
            ShapeRequest request = new ShapeRequest(connection);

            // Create a new thread to process the request.
            Thread thread = new Thread(request);

            // Start the thread.
            thread.start();
        }
    }


    //Handle Requests
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


        // Extract the filename from the request line.
            if (requestLine != null && !requestLine.isEmpty()){
            	System.out.print("DATA");
                StringTokenizer tokens = new StringTokenizer(requestLine);
                String method = tokens.nextToken();

                if (Objects.equals(method, "GET")){
                    System.out.print("GET request made");
                }else if (Objects.equals(method, "POST")){
                    System.out.print("POST request made");
                }else{
                    throw new Exception(String.valueOf(405));
                }
            }
            else {
                System.out.print("Connected");
            }
        }

        private void handleGet(StringTokenizer tokens) {
            
        }

        private void handlePost(StringTokenizer tokens) throws Exception {
            int numberPoints = tokens.countTokens();
            if (numberPoints%2!=0 && (numberPoints/3!=2 || numberPoints/4!=2)){
                throw new Exception("400, invalid number of points");
            }

            Vector<Point> points = getPointVector(tokens);

            if (points.size()==3){
                Triangle triangle = new Triangle(points);
                if(triangle.isTriangle()){
                    int indexOfAlready = m_shapes.indexOf(triangle);
                    if(indexOfAlready>=0){
                        m_shapes.elementAt(indexOfAlready).incrementCount();
                    }else{
                        m_shapes.add(triangle);
                    }
                }else{
                    throw new Exception("400 Not a triangle");
                }
            }

        }

        private Vector<Point> getPointVector(StringTokenizer tokens) {
            Vector<Point> points = new Vector<>();
            while(tokens.hasMoreTokens()){
                int xCoordinate = Integer.parseInt(tokens.nextToken());
                int yCoordinate = Integer.parseInt(tokens.nextToken());
                Point point = new Point(xCoordinate,yCoordinate);
                points.add(point);
            }
            return points;
        }
    }

    //CLASSES
    private static class Shape{
        public Vector<Point> points;
        public int count = 0;
        public void incrementCount() {
            this.count++;
        }
    }

    private static class Triangle extends Shape {

        private Boolean isIsosceles =false;
        private Boolean isRightAngled = false;
        private Boolean isEquilateral = false;
        private Boolean isScalene = false;
        private Boolean isTriangle = true;

        private Triangle(Vector<Point> points){
            super.points = points;
            Point first = super.points.get(0);
            Point second =super.points.get(1);
            Point third = super.points.get(2);

            int a = first.distanceSquared(second);
            int b = first.distanceSquared(third);
            int c = second.distanceSquared(third);

            ArrayList<Integer> distances = new ArrayList<>();
            distances.add(a);
            distances.add(b);
            distances.add(c);
            Collections.sort(distances);
            c = distances.remove(2);
            b = distances.remove(1);
            a = distances.remove(0);

            if((a+b)>c&&(a+c)>b&&(b+c)>a){
                if(a == b && a == c){
                    this.isEquilateral = true;
                    this.isIsosceles = true;
                }else if (a==b || a==c|| c==b){
                    this.isIsosceles = true;
                }else if(c==(a+b)){
                    this.isRightAngled = true;
                    this.isScalene = true;
                }else{
                    this.isScalene = true;
                }
            }else{
                isTriangle = false;
            }
        }

        public boolean Equals(Triangle other){
            Boolean equal = true;
            for (Point point : other.points) {
                if(!super.points.contains(point)){
                    equal = false;
                }
            }
            return equal;
        }

        public Boolean isEquilateral() {
            return isEquilateral;
        }
        public Boolean isRightAngled() {
            return isRightAngled;
        }
        public Boolean isIsosceles() {
            return isIsosceles;
        }

        public Boolean isScalene() {
            return isScalene;
        }

        public int getCount() {
            return count;
        }



        public Boolean isTriangle() {
            return isTriangle;
        }
    }

    private static class Point{
        private final int x;
        private final int y;

        public Point(int x,int y){
            this.x = x;
            this.y = y;
        }

        public int getY() {
            return this.y;
        }

        public int getX() {
            return this.x;
        }

        public int distanceSquared(Point other){
            return (other.getX()- this.x)^2 + (other.getY()- this.y)^2;
        }
    }
}

