import java.io.* ;
import java.io.InputStream;
import java.lang.Exception;
import java.lang.System;
import java.net.* ;
import java.util.* ;

public final class Server {

    private enum ShapeType {
        Triangle, Quadrilateral, Invalid;
    }

    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;
    private static final String PORT_ERROR = "Invalid port please choose a port between: "+ MIN_PORT +" and " + MAX_PORT;
    private static Vector<Shape> m_shapes;
    final static String CRLF = "\r\n";

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

            while(requestLine!=null){
                System.out.println(requestLine);
                StringTokenizer tokens = new StringTokenizer(requestLine);
                String method = tokens.nextToken();
                if (Objects.equals(method, "GET")){
                    System.out.print("GET request made");
                    handleGet(tokens);
                    outputStream.writeBytes("POTATO");
                    outputStream.writeBytes(CRLF);
                }else if (Objects.equals(method, "POST")){
                    System.out.print("POST request made");
                    ShapeType shapeType = handlePost(tokens);
                    outputStream.writeBytes("OK "+ shapeType);
                    outputStream.writeBytes(CRLF);
                }else{
                    throw new Exception(String.valueOf(405));
                }
                requestLine = reader.readLine();
            }


        }

        private void handleGet(StringTokenizer tokens) throws Exception {
            String shapeType = tokens.nextToken();
            switch (shapeType) {
                case "T":
                    handleTriangleGets(tokens);
                    break;
                case "Q":
                    System.out.print("Quad request received");
                    break;
                default:
                    throw new Exception("400 Invalid request");
            }
        }

        private void handleTriangleGets(StringTokenizer tokens) {
            String request = tokens.nextToken();

            if(request.toLowerCase().equals("right")){
                m_shapes.stream().filter(shape -> shape instanceof Triangle)
                        .map(t -> (Triangle) t)
                        .filter(Triangle::isRightAngled).forEach(System.out::println);
            }
        }

        private ShapeType handlePost(StringTokenizer tokens) throws Exception {
            ShapeType shapeType = ShapeType.Invalid;
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
                    shapeType = ShapeType.Triangle;
                }else{
                    throw new Exception("400 Not a triangle");
                }
            }else if(points.size()==4){
                shapeType = ShapeType.Quadrilateral;
            }
            return shapeType;
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
    private static abstract class Shape{
        public Vector<Point> points;
        public int count = 0;


        public void incrementCount() {
            this.count++;
        }

        public int getCount() {
            return count;
        }

        public abstract boolean equals(Triangle other);
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

            int a2 = first.distanceSquared(second);
            int b2 = first.distanceSquared(third);
            int c2 = second.distanceSquared(third);

            ArrayList<Integer> distances = new ArrayList<>();
            distances.add(a2);
            distances.add(b2);
            distances.add(c2);
            Collections.sort(distances);
            c2 = distances.remove(2);
            b2 = distances.remove(1);
            a2 = distances.remove(0);

            if((a2+b2)>=c2&&(a2+c2)>=b2&&(b2+c2)>=a2){
                if(a2 == b2 && a2 == c2){
                    this.isEquilateral = true;
                    this.isIsosceles = true;
                }else if (a2==b2 || a2==c2|| c2==b2){
                    this.isIsosceles = true;
                }else if(c2==(a2+b2)){
                    this.isRightAngled = true;
                    this.isScalene = true;
                }else{
                    this.isScalene = true;
                }
            }else{
                isTriangle = false;
            }
        }

        @Override
        public boolean equals(Triangle other){
            Boolean equal = true;
            for (Point point : other.points) {
                if(!super.points.contains(point)){
                    equal = false;
                }
            }
            return equal;
        }

        public Boolean isEquilateral() {
            return this.isEquilateral;
        }
        public Boolean isRightAngled() {
            return this.isRightAngled;
        }
        public Boolean isIsosceles() {
            return this.isIsosceles;
        }

        public Boolean isScalene() {
            return this.isScalene;
        }



        public Boolean isTriangle() {
            return this.isTriangle;
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
            return (int) (Math.pow((other.getX()- this.x),2) + Math.pow((other.getY() - this.y),2));
        }
    }
}

