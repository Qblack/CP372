//package blac_hage_a3;

import java.util.Scanner;
import java.lang.Math;
import java.util.ArrayList;
import java.util.PriorityQueue;


public class Dijkstra {
	public static final int MAX = 1111111;
	/*
	 * Classes
	 */
	
	//create class for each vertex in graph
	static public class Vertex implements Comparable<Vertex>{
		//define class variables
		public int node;
		public ArrayList<Edge> adjacent = new ArrayList();
		public int minDist = MAX;
		public Vertex prev;
	
		//define constructor
		public Vertex(int n){
			node = n;
		}

		//Comparator for priority Queue
		public int compareTo(Vertex v){
			return Double.compare(minDist,v.minDist);
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
	
	//uses path costs to add vertices to PriorityQueue which stores them and reorganizes vertices
	//based on their minimum distance (compareTo function)
	public static void calcPath(Vertex start){
		//initialize path to starting node
		start.minDist = 0;
		PriorityQueue<Vertex> vert = new PriorityQueue<Vertex>();
		vert.add(start);
		
		//loop through every vertex, removing the minimum at the start each time
		Vertex v = vert.poll();
		while(v!=null){
			//loop through all outbound edges
			for (Edge e:v.adjacent){
				//distance to v = distance to u + distance from u to v
				//default distance is 1111111
				Vertex next = e.endNode;
				int dist = v.minDist + e.weight;
				if (dist < next.minDist){
					//next node has lower path cost so remove it, update costs, then reinsert next node 
					//so that it is properly ordered
					vert.remove(next);
					next.minDist = dist;
					next.prev = v;
					vert.add(next);
				}
			}
			v = vert.poll();
		}
	}
	
	//Displays vertex list to get to last node - use for forwarding table
	public static ArrayList<Vertex> displayPathTo(Vertex lastNode){
		//initialize vectors
		ArrayList<Vertex> backPath = new ArrayList();
		ArrayList<Vertex> fwdPath = new ArrayList();
		
		//get predecessor node and add to back Path, then repeat until none left
		while (lastNode!=null){
			backPath.add(lastNode);
			lastNode = lastNode.prev;
		}
		//reverse order of backPath and return result
		int num = backPath.size()-1;
		for (int i=0; i<=num; i++){
			fwdPath.add(i, backPath.get(num-i));
		}
		return fwdPath;
	}
	
	//resets vertices previouse links and min distances as to not interfere with future node
	//path comparisons
	public static void reset(ArrayList<Vertex> v){
		for(Vertex node:v){
			node.minDist = MAX;
			node.prev = null;
		}
	}
	
	//Dijkstras(g={v,e}, start)
	public static String doDijkstras (ArrayList<Vertex> vertices, Vertex start){
		ArrayList<Vertex> vert = (ArrayList<Vertex>) vertices.clone();
		String output = "";
		
		reset(vertices);
		calcPath(start);
		
		//loop through every possible end vertex
		for (Vertex end: vert){
			if (start.node == end.node){
				output = output + (start.node+1) + "\t\t" + " - " + "\t\t" + 0 + "\n";
			}
			else{
				ArrayList<Vertex> path = new ArrayList();
				path = displayPathTo(end);
				//print Fowarding Table and Cost
				if (path.size()>1){
					output = output + (end.node+1) + "\t\t" + (path.get(1).node+1) + "\t\t" + end.minDist + "\n";
				}
				else{
					output = output + (end.node+1) + "\t\t" + "-1" + "\t\t" + "NA" + "\n";
				}
			}
		}
		return output;
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
		//calculate path costs from every node
		String out = "";
		for (Vertex start: vertices){
			System.out.println("\nSource Node: " + (start.node+1));
			System.out.println("Node \t Next Hop \t Path Cost");
			out = doDijkstras(vertices, start);
			System.out.println(out);
		}
	}
}
