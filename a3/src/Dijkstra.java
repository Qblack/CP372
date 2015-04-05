//package blac_hage_a3;

import java.util.Scanner;
import java.lang.Math;
import java.util.ArrayList;


public class Dijkstra {
	/*
	 * Classes
	 */
	
	//create class for each vertex in graph
	static public class Vertex{
		//define class variables
		public int node;
		public ArrayList<Edge> adjacent = new ArrayList();
		public int minDist = 1111111;
		public Vertex prev;
	
		//define constructor
		public Vertex(int n){
			node = n;
		}
		
		//define class functions
		public int getNode(){
			return node;
		}
		
		public int getMin(Vertex v){
			return Math.min(minDist,v.minDist);
		}
		public String toString(){
			return "Node: " + (node+1);
		}
	}
	
	//create class for each edge in graph
	static public class Edge{
		public Vertex endNode;
		public int weight;
		
		//constructor
		public Edge(Vertex v, int w){
			endNode = v;
			weight = w;
		}
	}
	
	
	/*
	 * Helper Functions
	 */
	
	//Orders a list of vertices from smallest to largest based on their minimum distances
	public static ArrayList<Vertex> orderByMin(ArrayList<Vertex> vArr){
		//initialize ordered list
		ArrayList<Vertex> vArrCopy = (ArrayList<Vertex>) vArr.clone();
		ArrayList<Vertex> ordered = new ArrayList<Vertex>();
		//check each node in array
		for (int i = 0; i<vArrCopy.size();i++){
			//specify starting minV
			Vertex minV = vArrCopy.get(0);
			
			for (Vertex v:vArrCopy){
				if (minV.minDist > v.minDist){
					minV = vArrCopy.get(vArrCopy.indexOf(v));
				}
			}
			//remove minV from future checks as it is the current lowest
			vArrCopy.remove(minV);
			ordered.add(i,minV);
		}
		return ordered;
	}
	
	//uses path costs to set what each vertices predecessor node is
	public static void calcPath(ArrayList<Vertex> graph, Vertex start){
		start.minDist = 0;
		ArrayList<Vertex> vCopy = (ArrayList<Vertex>) graph.clone();
		//visit each adjacent vertex of start in order of smallest to largest
		ArrayList<Vertex> orderedV = orderByMin(vCopy);
		for(Vertex v:orderedV){
			//visit all edges of start vertex
			for (Edge e:start.adjacent){
				//distance to v = distance to u + distance from u to v
				int dist = v.minDist + e.weight;
				if (dist < e.endNode.minDist){
					e.endNode.minDist = dist;
					e.endNode.prev = v;
				}
			}
		}	
	}
	
	//Displays vertex list to get to last node - use for forwarding table
	public static ArrayList<Vertex> displayPathTo(Vertex lastNode){
		//initialize path of vertices to take
		ArrayList<Vertex> pathBack = new ArrayList();
		Vertex v = lastNode;
		while (v!=null) {
			pathBack.add(v);
			v = v.prev;
		}
		
		//reverse order of back and return
		ArrayList<Vertex> path = new ArrayList();
		int num = pathBack.size();
		for (int i=0;i<num;i++){
			v = pathBack.remove(num-i-1);
			path.add(i, v);
		}
		return path;
	}
	
	/*
	 * Main
	 */
	
	public static void main(String[] args) {
		
		//get user input for n by n matrix
		Scanner in = new Scanner(System.in);
		System.out.println("Enter Directed Graph");
		int n = Integer.parseInt(in.nextLine());
		
		//create 2D array (graph)
		int[][] g = new int[n][n];
		ArrayList<Vertex> vertices = new ArrayList();
		
		//get each row from user and place initialize corresponding vertex
		for (int i=0; i<n;i++){
			Vertex v = new Vertex(i);
			vertices.add(v);
			//get vertex corresponding edges from user input
			String row = in.nextLine();
			String[] vlist = row.split(" ");
			
			//place every edge in graph
			for (int c=0;c<n;c++){
				g[i][c] = Integer.parseInt(vlist[c]);
			}
		}
		in.close();
		
		//initialize edge objects
		for(int i=0; i<n;i++){
			Vertex v = vertices.get(i);
			for (int c=0;c<n;c++){
				if (g[i][c]!=-1 && g[i][c]!=0){
					Edge e = new Edge(vertices.get(c),g[i][c]);
					v.adjacent.add(e);
				}
			}
		}
		
		for(Vertex v:vertices){
			System.out.println("Node: "+(v.node+1));
			for(Edge e:v.adjacent){
				System.out.println("Edge: " + (v.node+1) + " to " + (e.endNode.node+1) + " weight=" + e.weight);
			}
			System.out.println();
		}
		
		/*
		//make function calls
		ArrayList<Vertex> vert2 = (ArrayList<Vertex>) vertices.clone();
		
		for (Vertex x:vertices){
			calcPath(vert2, x);
			for (Vertex nLast:vert2){
				ArrayList<Vertex> p = new ArrayList();
				p = displayPathTo(nLast);
				System.out.println("Path To: " + (nLast.node+1) + " From " + (x.node+1));
				int t = 0;
				for (Vertex v:p){
					t++;
					System.out.println("Step "+ t +": " + v.toString());
				}
				System.out.println();
			}
		}
		*/
	}

}
