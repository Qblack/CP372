package src;
import java.io.* ;
import java.io.InputStream;
import java.lang.Exception;
import java.lang.System;
import java.net.* ;
import java.util.* ;
import java.util.stream.Stream;

public final class Server {
	
	//---ProtocolExceptions----------
	public static class ProtocolException extends Exception{
		public ProtocolException (String msg){
			super (msg);
		}
	}


    private enum ShapeType {
        Triangle, Quadrilateral, Invalid
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
        ServerSocket sock = new ServerSocket(port);
        m_shapes = new Vector<>();

        // Process HTTP service requests in an infinite loop.
        while (true) {
            // Listen for a TCP connection request.
            Socket connection = sock.accept();

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

        public ShapeRequest(Socket sock) throws Exception{
            m_socket = sock;
        }
        // Implement the run() method of the Runnable interface.

        public void run() {
            try {
                processRequest();
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        //need custom exception handling
        private void processRequest() throws Exception{
            InputStream inputStream = m_socket.getInputStream();
            DataOutputStream outputStream = new DataOutputStream(m_socket.getOutputStream());
            // Set up input stream filters.
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            // Get the request line of the HTTP request message.
            String requestLine = reader.readLine();
            
            try{
	            while(requestLine!=null){
	                StringTokenizer tokens = new StringTokenizer(requestLine);
	                String method = tokens.nextToken();
	                if (Objects.equals(method, "GET")){
	                    Vector<Shape> results = handleGet(tokens);
	                    for (Shape result : results) {
	                        outputStream.writeBytes(result.toString()+"\n");
	                    }
	                    outputStream.writeBytes(CRLF);
	                }else if (Objects.equals(method, "POST")){
	                    ShapeType shapeType = handlePost(tokens);
	                    if (shapeType != shapeType.Invalid){
		                    outputStream.writeBytes("OK "+ shapeType);
		                    outputStream.writeBytes(CRLF);
	                    }else{
	                    	throw new ProtocolException("403: Invalid Number of Points");
	                    }
	                }else{
	                    throw new ProtocolException("400: Bad Request");
	                }
	                requestLine = reader.readLine();
	            }
            }catch(ProtocolException err){
            	outputStream.writeBytes(err.getMessage());
                outputStream.writeBytes(CRLF);
            }
        }

        private Vector<Shape> handleGet(StringTokenizer tokens) throws ProtocolException {
            String shapeType = tokens.nextToken();
            Vector<Shape> resultShapes = new Vector<>();
            switch (shapeType) {
                case "T":
					handleTriangleGets(tokens,resultShapes);
                    break;
                case "Q":
                    handleQuadrilateralGets(tokens, resultShapes);
                    break;
                default:
                    throw new ProtocolException("401: Invalid Shape Query");
            }
            return resultShapes;
        }

        private void handleQuadrilateralGets(StringTokenizer tokens, Vector<Shape> resultShapes) throws ProtocolException {
            String request = tokens.nextToken();

            Stream<Quadrilateral> quadrilateralStream = m_shapes.stream()
                    .filter(shape -> shape instanceof Quadrilateral)
                    .map(q -> (Quadrilateral) q);

            if(request.toLowerCase().equals("square")){
                quadrilateralStream.filter(Quadrilateral::isSquare).forEach(resultShapes::add);
            }else if(request.toLowerCase().equals("rectangle")) {
                quadrilateralStream.filter(Quadrilateral::isRectangle).forEach(resultShapes::add);
            }else if(request.toLowerCase().equals("rhombus")) {
                quadrilateralStream.filter(Quadrilateral::isRhombus).forEach(resultShapes::add);
            }else if(request.toLowerCase().equals("parallelogram")) {
                quadrilateralStream.filter(Quadrilateral::isParallelogram).forEach(resultShapes::add);
            }else if(request.toLowerCase().equals("convex")) {
                quadrilateralStream.filter(Quadrilateral::isConvex).forEach(resultShapes::add);
            }else if(request.toLowerCase().equals("concave")) {
                quadrilateralStream.filter(q-> !q.isConvex()).forEach(resultShapes::add);
            }else if(request.toLowerCase().equals("trapezoid")) {
                quadrilateralStream.filter(Quadrilateral::isTrapezoid).forEach(resultShapes::add);
            }else if(request.matches("^\\d+$")){
                int numberOf = Integer.parseInt(request);
                quadrilateralStream.filter(t -> t.getCount() >= numberOf).forEach(resultShapes::add);
            }else{
                throw new ProtocolException("402: Invalid Quadrilateral");
            }
        }

        private void handleTriangleGets(StringTokenizer tokens, Vector<Shape> resultShapes) throws ProtocolException {
            String request = tokens.nextToken();

            Stream<Triangle> triangleStream = m_shapes.stream()
                    .filter(shape -> shape instanceof Triangle)
                    .map(t -> (Triangle) t);

            if(request.toLowerCase().equals("right")){
                triangleStream.filter(Triangle::isRightAngled).forEach(resultShapes::add);
            }else if(request.toLowerCase().equals("isosceles")) {
                triangleStream.filter(Triangle::isIsosceles).forEach(resultShapes::add);
            }else if(request.toLowerCase().equals("equilateral")) {
                triangleStream.filter(Triangle::isEquilateral).forEach(resultShapes::add);
            }else if(request.toLowerCase().equals("scalene")) {
                triangleStream.filter(Triangle::isScalene).forEach(resultShapes::add);
            }else if(request.matches("^\\d+$")){
                int numberOf = Integer.parseInt(request);
                triangleStream.filter(t->t.getCount()>=numberOf).forEach(resultShapes::add);
            }else{
                throw new ProtocolException("402: Invalid Triangle");
            }
        }

        private ShapeType handlePost(StringTokenizer tokens) throws ProtocolException {
            ShapeType shapeType = ShapeType.Invalid;
            int numberPoints = tokens.countTokens();
            if (numberPoints%2!=0 && (numberPoints/3!=2 || numberPoints/4!=2)){
                throw new ProtocolException("403: Invalid Number of Points");
            }
            if (tokens.toString().matches("^\\d+$")){
            	throw new ProtocolException("405: POST request accepts integers only");
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
                    throw new ProtocolException("404: Point/Line Segment Error in Triangle");
                }
            }else if(points.size()==4){
                //TODO Check for reflexive
                Quadrilateral quad = new Quadrilateral(points);
                if(quad.isQuadrilateral()){
                    int indexOfAlready = m_shapes.indexOf(quad);
                    if(indexOfAlready>=0){
                        m_shapes.elementAt(indexOfAlready).incrementCount();
                    }else{
                        m_shapes.add(quad);
                    }
                    shapeType = ShapeType.Quadrilateral;
                }else{
                    throw new ProtocolException("404: Point/Line Segment Error in Quadrilateral");
                }
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

        public abstract boolean equals(Object other);
        public abstract String toString();
    }


    /* Currently must be given in counter clock wise order from farthest left and lowest point */
    private static class Quadrilateral extends Shape {
        private final boolean isQuadrilateral;
        private boolean m_trapezoid = false;
        private boolean m_rectangle = false;
        private boolean m_square = false;
        private boolean m_rhombus = false;
        private boolean m_parallelogram = false;
        private boolean m_concave = false;

        public Quadrilateral(Vector<Point> points){
            super.points=points;
            this.isQuadrilateral = checkIfPointsAreEqual();

            orderPoints();
            Point a = super.points.get(0);
            Point b = super.points.get(1);
            Point c = super.points.get(2);
            Point d = super.points.get(3);

            Line bottom = new Line(a,b);
            Line right = new Line(b,c);
            Line top = new Line(c,d);
            Line left = new Line(d,a);
            int minDiagonal = a.distanceSquared(d) + a.distanceSquared(b);

            if(a.distanceSquared(c)<minDiagonal){
                this.m_concave = true;
            }else if(bottom.lengthSquared==right.lengthSquared&&right.lengthSquared==top.lengthSquared&&top.lengthSquared==left.lengthSquared){
                this.m_rhombus = true;
                if(bottom.slope==0 && left.slope==Integer.MAX_VALUE){
                    this.m_square = true;
                    this.m_rectangle = true;
                    this.m_parallelogram = true;
                }
            }else if(bottom.lengthSquared==top.lengthSquared && left.lengthSquared==right.lengthSquared){
                if(bottom.areParellel(top) && left.areParellel(right)){
                    this.m_parallelogram = true;
                    if(bottom.slope==0&&left.slope==Integer.MAX_VALUE){
                        this.m_rectangle=true;
                    }
                }
            }else if(bottom.areParellel(top)||left.areParellel(right)){
                this.m_trapezoid = true;
            }
        }

        private boolean checkIfPointsAreEqual() {
            if(super.points.get(0) == super.points.get(1)||
                    super.points.get(0) == super.points.get(2)||
                    super.points.get(0) == super.points.get(3)){
                return true;
            }else if(super.points.get(1) == super.points.get(2)||
                    super.points.get(1) == super.points.get(3)) {
                return true;
            }else return super.points.get(2) == super.points.get(3);
        }

        /**
         * Orders point counter clockwise, a b c d
         */
        private void orderPoints() {
            int bottomLeftIndex = getBottomLeft();
            Point pointA = super.points.remove(bottomLeftIndex);
            ArrayList<Line> vertices = new ArrayList<>();
            for(Point point : super.points){
                vertices.add(new Line(pointA, point));
            }
            super.points.removeAllElements();
            vertices.sort(new LineSlopeComparator());

            points.add(pointA);
            for (Line vertice : vertices) {
                points.add(vertice.pair[1]);
            }
        }

        public boolean isTrapezoid() {
            return m_trapezoid;
        }

        public boolean isQuadrilateral() {
            return isQuadrilateral;
        }


        public class Line {
            public Point[] pair = new Point[2];
            public double slope = 0;
            public int lengthSquared = 0;
            public Line(Point point1, Point point2){
                this.pair[0] = point1;
                this.pair[1] = point2;
                this.lengthSquared = point1.distanceSquared(point2);
                this.slope = point1.slopeBetweenPoints(point2);
            }

            public boolean areParellel(Line other){
                Point p1 = this.pair[0];
                Point p2 = this.pair[1];
                Point p3 = other.pair[0];
                Point p4 = other.pair[1];
                int firstHalf = (p4.getX()-p3.getX())*(p2.getY()-p1.getY());
                int secondHalf = (p4.getY()-p3.getY())*(p2.getX()-p1.getX());
                return (firstHalf-secondHalf)==0;
            }

        }
        public class LineSlopeComparator implements Comparator<Line> {
            /**
             * @param line1
             * @param line2
             * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
             */
            @Override
            public int compare(Line line1, Line line2) {
                if(line1.slope== line2.slope){
                    return 0;
                }else if(line1.slope< line2.slope){
                    return -1;
                }
                return 1;
            }
        }
        private int getBottomLeft() {
            Point bottomLeft = super.points.elementAt(0);
            int bottomLeftIndex = 0;
            for(int position =1; position< super.points.size() ;position++){
                Point point = super.points.elementAt(position);
                if( point.getX()==bottomLeft.getX()){
                    if(point.getY() < bottomLeft.getY()){
                        bottomLeft = point;
                        bottomLeftIndex = position;
                    }
                }else if( point.getX()<bottomLeft.getX()){
                    bottomLeft = point;
                    bottomLeftIndex = position;
                }
            }
            return bottomLeftIndex;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }else if (getClass() != obj.getClass()) {
                return false;
            }
            final Quadrilateral other = (Quadrilateral) obj;
            Boolean equal = true;
            for (Point point : other.points) {
                if(!super.points.contains(point)){
                    equal = false;
                }
            }
            return equal;
        }

        @Override
        public String toString() {
            String output = "";
            for (Point point : this.points) {
                output+= point.toString()+",";
            }
            return output;
        }

        public boolean isConvex() {
            return m_concave;
        }

        public boolean isParallelogram() {
            return m_parallelogram;
        }

        public boolean isRhombus() {
            return m_rhombus;
        }

        public boolean isSquare() {
            return m_square;
        }

        public boolean isRectangle() {
            return m_rectangle;
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
                }else if (c2==(a2+b2)){
                    this.isIsosceles = true;
                    this.isRightAngled = true;
                }else if(a2==b2 || a2==c2|| c2==b2){
                    this.isIsosceles = true;
                }else{
                    this.isScalene = true;
                }
            }else{
                isTriangle = false;
            }
        }

        @Override
        public boolean equals(Object obj){
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Triangle other = (Triangle) obj;

            Boolean equal = true;
            for (Point point : other.points) {
                if(!super.points.contains(point)){
                    equal = false;
                }
            }
            return equal;
        }

        @Override
        public String toString() {
            String output = "";
            for (Point point : this.points) {
                output+= point.toString()+",";
            }
            return output;
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

        public double slopeBetweenPoints(Point other){
            int rise = other.getY() - this.getY();
            int run = other.getX() - this.getX();
            double slope;
            if (run==0){
                slope = Integer.MAX_VALUE;
            }else{
                slope = rise/run;
            }
            return slope;
        }

        @Override
        public boolean equals(Object obj){
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Point other = (Point) obj;
            return this.x == other.x && this.y == other.y;
        }

        @Override
        public String toString(){
            return "("+this.x+","+this.y+")";
        }

    }
}

