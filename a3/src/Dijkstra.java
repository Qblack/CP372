//package blac_hage_a3;

import java.util.Scanner;
import java.lang.Math;
import java.util.ArrayList;


public class Dijkstra {
	/*
	 * Classes
	 */
	
	//create class for each vertex in graph
	public class Vertex{
		//define class variables
		public int node;
		public ArrayList<Edge> adjacent;
		public int minDist = 2147483647;
		public Vertex prev;
		
		//define class functions
		public int getNode(){
			return node;
		}
		
		public int getMin(Vertex v){
			return Math.min(minDist,v.minDist);
		}
	}
	
	//create class for each edge in graph
	public class Edge{
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
		ArrayList<Vertex> ordered = new ArrayList<Vertex>();
		//check each node in array
		for (int i = 0; i<vArr.size();i++){
			//specify starting minV
			Vertex minV = vArr.get(0);
			
			for (Vertex v:vArr){
				if (minV.minDist>v.minDist){
					minV = vArr.get(vArr.indexOf(v));
				}
			}
			//remove minV from future checks as it is the current lowest
			vArr.remove(minV);
			ordered.add(i,minV);
		}
		return ordered;
	}
	
	
	public static void determinePaths(ArrayList<Vertex> graph, Vertex start){
		start.minDist = 0;
		
		//visit each adjacent vertex of start in order of smallest to largest
		ArrayList<Vertex> orderedV = orderByMin(graph);
		for(Vertex v:orderedV){
			//visit all edges of start vertex
			for (Edge e:start.adjacent){
				//distance to v = distance to u + distance from u to v
				int dist = start.minDist + e.weight;
	

			}
		}	
	}
	
	/*
	 * Main
	 */
	
	public static void main(String[] args) {
		
		//get user input for n by n matrix
		Scanner in = new Scanner(System.in);
		System.out.println("What is the n by n size of this graph?");
		int n = in.nextInt();
		
		//create 2D array (graph)
		int[][] g = new int[n][n];
		
		//get each row from user and place in matrix
		for (int i=1; i<=n;i++){
			String row = in.nextLine();
			String[] vlist = row.split(" ");
			//place every edge in graph
			for (int c=1;c<=n;i++){
				g[i][c] = Integer.parseInt(vlist[c]);
			}
		}
		//run Dijkstra's algorithm from every node to find shortest path
		

	}

}
