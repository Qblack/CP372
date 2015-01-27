//package src;
import java.io.* ;
import java.io.InputStream;
import java.lang.Exception;
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
        Triangle, Quadrilateral, Invalid, ExistingTriangle, ExistingQuadrilateral
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
            outputStream.writeBytes("OK: Connected" + CRLF);


            // Set up input stream filters.
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            // Get the request line of the HTTP request message.
            String requestLine = reader.readLine();
            
            while(requestLine!=null){
            	try{
		            StringTokenizer tokens = new StringTokenizer(requestLine);
		            String method = tokens.nextToken();
		            if (Objects.equals(method, "GET")){
		                Vector<Shape> results = handleGet(tokens);
                        if(results.size()>0){
                            int index =0;
                            StringBuilder output = new StringBuilder();
                            for (Shape result : results) {
                                output.append(result.toString());
                                if(index!=results.size()-1){
                                    output.append("&");
                                }
                                index+=1;
                            }
                            outputStream.writeBytes(output.toString());
                        }else{
                            outputStream.writeBytes("OK: Query Has No Results");
                        }
		                outputStream.writeBytes(CRLF);
		            }else if (Objects.equals(method, "POST")){
		                ShapeType shapeType = handlePost(tokens);
		                if (shapeType != ShapeType.Invalid){
		                    outputStream.writeBytes("OK "+ shapeType);
		                    outputStream.writeBytes(CRLF);
		                }else{
		                	throw new ProtocolException("403: Invalid Number of Points");
		                }
		            }else{
		                throw new ProtocolException("400: Bad Request");
		            }
		            
			        }catch(ProtocolException err){
		            	outputStream.writeBytes(err.getMessage());
		                outputStream.writeBytes(CRLF);
			        }
            	requestLine = reader.readLine();
            }
        }

        private Vector<Shape> handleGet(StringTokenizer tokens) throws ProtocolException {
            String shapeTypeCharacter = tokens.nextToken();
            Vector<Shape> resultShapes = new Vector<>();
            String request;
            switch (shapeTypeCharacter) {
                case "T":
                    Stream<Triangle> triangleStream = m_shapes.stream()
                            .filter(shape -> shape instanceof Triangle)
                            .map(q -> (Triangle) q);
                    do {
                        request = tokens.nextToken();
                        resultShapes = handleTriangleGets(request, triangleStream);
                        triangleStream = resultShapes.stream()
                                .filter(shape -> shape instanceof Triangle)
                                .map(q -> (Triangle) q);
                    } while (tokens.hasMoreTokens());
                    break;
                case "Q":
                    Stream<Quadrilateral> quadrilateralStream = m_shapes.stream()
                            .filter(shape-> shape instanceof Quadrilateral)
                            .map(q -> (Quadrilateral)q);
                    do {
                        request = tokens.nextToken();
                        resultShapes = handleQuadrilateralGets(request, quadrilateralStream);
                        quadrilateralStream = resultShapes.stream()
                                .filter(shape -> shape instanceof Quadrilateral)
                                .map(q -> (Quadrilateral) q);
                    } while (tokens.hasMoreTokens());
                    break;
                default:
                    throw new ProtocolException("401: Invalid Shape Query");
            }
            return resultShapes;
        }

        private Vector<Shape> handleQuadrilateralGets(String request,Stream<Quadrilateral> quadrilateralStream) throws ProtocolException {
            Vector<Shape> results = new Vector<>();
            if(request.toLowerCase().equals("square")){
                quadrilateralStream.filter(Quadrilateral::isSquare).forEach(results::add);
            }else if(request.toLowerCase().equals("rectangle")) {
                quadrilateralStream.filter(Quadrilateral::isRectangle).forEach(results::add);
            }else if(request.toLowerCase().equals("rhombus")) {
                quadrilateralStream.filter(Quadrilateral::isRhombus).forEach(results::add);
            }else if(request.toLowerCase().equals("parallelogram")) {
                quadrilateralStream.filter(Quadrilateral::isParallelogram).forEach(results::add);
            }else if(request.toLowerCase().equals("convex")) {
                quadrilateralStream.filter(Quadrilateral::isConvex).forEach(results::add);
            }else if(request.toLowerCase().equals("concave")) {
                quadrilateralStream.filter(q-> !q.isConvex()).forEach(results::add);
            }else if(request.toLowerCase().equals("trapezoid")) {
                quadrilateralStream.filter(Quadrilateral::isTrapezoid).forEach(results::add);
            }else if(request.matches("^\\d+$")){
                int numberOf = Integer.parseInt(request);
                quadrilateralStream.filter(t -> t.getCount() >= numberOf).forEach(results::add);
            }else{
                throw new ProtocolException("402: Invalid Quadrilateral");
            }
            return results;
        }

        private Vector<Shape> handleTriangleGets(String request, Stream<Triangle> triangleStream) throws ProtocolException {
            Vector<Shape> results = new Vector<>();

            if(request.toLowerCase().equals("right")){
                triangleStream.filter(Triangle::isRightAngled).forEach(results::add);
            }else if(request.toLowerCase().equals("isosceles")) {
                triangleStream.filter(Triangle::isIsosceles).forEach(results::add);
            }else if(request.toLowerCase().equals("equilateral")) {
                triangleStream.filter(Triangle::isEquilateral).forEach(results::add);
            }else if(request.toLowerCase().equals("scalene")) {
                triangleStream.filter(Triangle::isScalene).forEach(results::add);
            }else if(request.matches("^\\d+$")){
                int numberOf = Integer.parseInt(request);
                triangleStream.filter(t->t.getCount()>=numberOf).forEach(results::add);
            }else{
                throw new ProtocolException("402: Invalid Triangle");
            }
            return  results;
        }

        private ShapeType handlePost(StringTokenizer tokens) throws ProtocolException {
            ShapeType shapeType = ShapeType.Invalid;
            int numberPoints = tokens.countTokens();
            if (numberPoints%2!=0 && (numberPoints/3!=2 || numberPoints/4!=2)){
                throw new ProtocolException("403: Invalid Number of Points");
            }

            Vector<Point> points = constructPointVector(tokens);
            if (points.size()==3){
                shapeType = postTriangle(points);
            }else if(points.size()==4){
                shapeType = postQuadrilateral(points);

            }
            return shapeType;
        }

        private ShapeType postQuadrilateral(Vector<Point> points) throws ProtocolException {
            ShapeType shapeType;//TODO Check for reflexive
            Quadrilateral quad = new Quadrilateral(points);
            if(quad.isQuadrilateral()){
                int indexOfAlready = m_shapes.indexOf(quad);
                shapeType = ShapeType.Quadrilateral;
                if(indexOfAlready>=0){
                    m_shapes.elementAt(indexOfAlready).incrementCount();
                    shapeType = ShapeType.ExistingQuadrilateral;
                }else{
                    m_shapes.add(quad);
                }

            }else{
                throw new ProtocolException("404: Point/Line Segment Error in Quadrilateral");
            }
            return shapeType;
        }

        private ShapeType postTriangle(Vector<Point> points) throws ProtocolException {
            ShapeType shapeType;
            Triangle triangle = new Triangle(points);
            if(triangle.isTriangle()){
                int indexOfAlready = m_shapes.indexOf(triangle);
                shapeType = ShapeType.Triangle;
                if(indexOfAlready>=0){
                    m_shapes.elementAt(indexOfAlready).incrementCount();
                    shapeType = ShapeType.ExistingTriangle;
                }else{
                    m_shapes.add(triangle);
                }
            }else{
                throw new ProtocolException("404: Point/Line Segment Error in Triangle");
            }
            return shapeType;
        }

        private Vector<Point> constructPointVector(StringTokenizer tokens) throws ProtocolException{
            Vector<Point> points = new Vector<>();
            try {
            	while(tokens.hasMoreTokens()){
	                int xCoordinate = Integer.parseInt(tokens.nextToken());
	                int yCoordinate = Integer.parseInt(tokens.nextToken());
	                Point point = new Point(xCoordinate,yCoordinate);
	                points.add(point);
            	}
        	}catch (Exception err){
        		throw new ProtocolException("405: POST Requires Integers Only");
        	}	
            return points;
        }
    }

    //CLASSES
    private static abstract class Shape{
        public Vector<Point> points;
        public int count = 1;

        public void incrementCount() {
            this.count++;
        }

        public int getCount() {
            return count;
        }

        public double perimeter = 0;
        public abstract double findPerimeter();

        public double area = 0;
        public abstract double findArea();

        public abstract boolean equals(Object other);
        public abstract boolean hasLineSegment();
        public abstract boolean hasPointOverlap();

        public boolean crossProduct(Point a, Point b, Point c) {
            int crossProduct = (c.y - a.y) * (b.x - a.x) - (c.x - a.x) * (b.y - a.y);
            return crossProduct == 0;
        }

        public String toString() {
            StringBuilder output = new StringBuilder();
            boolean first = true;
            for (Point point : this.points) {
                if(!first){
                    output.append(",");
                }
                output.append(point.toString());
                first=false;
            }
            return output.toString();
        }

    }

    private static class Quadrilateral extends Shape {
        private boolean m_isQuadrilateral = false;
        private boolean m_isTrapezoid = false;
        private boolean m_isRectangle = false;
        private boolean m_isSquare = false;
        private boolean m_isRhombus = false;
        private boolean m_isParallelogram = false;
        private boolean m_isConcave = false;


        public Quadrilateral(Vector<Point> points){
            super.points = points;
            this.m_isQuadrilateral = !(hasPointOverlap() || hasLineSegment());
            if(m_isQuadrilateral){
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
                    this.m_isConcave = true;
                }else if(bottom.lengthSquared==right.lengthSquared&&right.lengthSquared==top.lengthSquared&&top.lengthSquared==left.lengthSquared){
                    this.m_isRhombus = true;
                    if(bottom.slope==0 && left.slope==Integer.MAX_VALUE){
                        this.m_isSquare = true;
                        this.m_isRectangle = true;
                        this.m_isParallelogram = true;
                        this.m_isTrapezoid = true;
                    }
                }else if(bottom.lengthSquared==top.lengthSquared && left.lengthSquared==right.lengthSquared){
                    if(bottom.areParellel(top) && left.areParellel(right)){
                        this.m_isParallelogram = true;
                        if(bottom.slope==0&&left.slope==Integer.MAX_VALUE){
                            this.m_isRectangle =true;
                            this.m_isTrapezoid = true;
                        }
                    }
                }else if(bottom.areParellel(top)||left.areParellel(right)){
                    this.m_isTrapezoid = true;
                }
            }
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
        public boolean hasLineSegment() {
            Point a = super.points.get(0);
            Point b = super.points.get(1);
            Point c = super.points.get(2);
            Point d = super.points.get(3);
            if(crossProduct(a, b, c)){
                return true;
            }else if(crossProduct(a, b, d)){
                return true;
            }else if (crossProduct(a, c, d)){
                return true;
            }else if(crossProduct(b, c, d)){
                return true;
            }
            return false;
        }

        @Override
        public boolean hasPointOverlap() {
            if(super.points.get(0) == super.points.get(1)||
                    super.points.get(0) == super.points.get(2)||
                    super.points.get(0) == super.points.get(3)){
                return true;
            }else if(super.points.get(1) == super.points.get(2)||
                    super.points.get(1) == super.points.get(3)) {
                return true;
            }else return super.points.get(2) == super.points.get(3);
        }

        @Override
        public double findPerimeter() {
            return 0;
        }

        @Override
        public double findArea() {
            return 0;
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



        public boolean isTrapezoid() {
            return m_isTrapezoid;
        }

        public boolean isQuadrilateral() {
            return m_isQuadrilateral;
        }

        public boolean isConvex() {
            return m_isConcave;
        }

        public boolean isParallelogram() {
            return m_isParallelogram;
        }

        public boolean isRhombus() {
            return m_isRhombus;
        }

        public boolean isSquare() {
            return m_isSquare;
        }

        public boolean isRectangle() {
            return m_isRectangle;
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
    }

    private static class Triangle extends Shape {

        private Boolean m_isIsosceles =false;
        private Boolean m_isRightAngled = false;
        private Boolean m_isEquilateral = false;
        private Boolean m_isScalene = false;
        private Boolean m_isTriangle = true;

        private Triangle(Vector<Point> points){
            super.points = points;

            m_isTriangle = !(this.hasLineSegment() || this.hasPointOverlap());
            if(m_isTriangle){
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
                if(a2 == b2 && a2 == c2){
                    this.m_isEquilateral = true;
                    this.m_isIsosceles = true;
                }else if (c2==(a2+b2)){
                    this.m_isIsosceles = true;
                    this.m_isRightAngled = true;
                }else if(a2==b2 || a2==c2|| c2==b2){
                    this.m_isIsosceles = true;
                }else{
                    this.m_isScalene = true;
                }
            }
        }

        @Override
        public double findPerimeter() {
            return 0;
        }

        @Override
        public double findArea() {
            return 0;
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
        public boolean hasLineSegment() {
            Point a = super.points.get(0);
            Point b = super.points.get(1);
            Point c = super.points.get(2);
            return super.crossProduct(a, b, c);
        }


        @Override
        public boolean hasPointOverlap() {
            if(super.points.get(0) == super.points.get(1)||
                    super.points.get(0) == super.points.get(2)){
                return true;
            }else if(super.points.get(1) == super.points.get(2)) {
                return true;
            }else{
                return false;
            }
        }

        public Boolean isEquilateral() {
            return this.m_isEquilateral;
        }
        public Boolean isRightAngled() {
            return this.m_isRightAngled;
        }
        public Boolean isIsosceles() {
            return this.m_isIsosceles;
        }

        public Boolean isScalene() {
            return this.m_isScalene;
        }

        public Boolean isTriangle() {
            return this.m_isTriangle;
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

