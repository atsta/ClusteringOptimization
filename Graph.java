import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
  
public class Graph {
  
    public int vertices;
    public int edges;
    public static List<List<Integer>> edgeList;
    
    Random random = new Random();
    public List<List<Integer> > adjacencyList;
  
    final int MAX_LIMIT = 40;
    public static String filename = "small_dataset.csv";
    
   /*final int MAX_LIMIT = 1000;
    public static String filename = "small_dataset1.csv";*/
    
    public Graph()
    {
        this.vertices = random.nextInt(MAX_LIMIT) + 1;
        this.edges = random.nextInt(computeMaxEdges(vertices)) + 1;
        adjacencyList = new ArrayList<>(vertices);
        edgeList = new ArrayList<>(vertices);
        for (int i = 0; i < vertices; i++)
            adjacencyList.add(new ArrayList<>());
  
        for (int i = 0; i < edges; i++) {
            int v = random.nextInt(vertices);
            int w = random.nextInt(vertices);
            if (adjacencyList.get(v).contains(w)) {
                i = i - 1;
                continue;
            }
            addEdge(v, w);
        }
    }
  
    int computeMaxEdges(int numOfVertices)
    {
        return numOfVertices * ((numOfVertices - 1) / 2);
    }
  
    void addEdge(int v, int w)
    {
        adjacencyList.get(v).add(w);
        if (v != w)
        {
            adjacencyList.get(w).add(v);
            edgeList.add(Arrays.asList(v, w));
        }        
    }
  
    static void printEdgeList() 
    {
        System.out.println("The generated edge list from the random graph :");
        for (int i = 0; i < edgeList.size(); i++) {
            var list = edgeList.get(i);
            //System.out.println("[ " + list.get(0) + " , " + list.get(1) + " ]");
        }
    }
    
    public static void writeEdgeListToCSV() throws IOException
    {
    	File csvFile = new File(filename);
    	FileWriter fileWriter = new FileWriter(csvFile);
    	
    	for (int i = 0; i < edgeList.size(); i++) {
    	    StringBuilder line = new StringBuilder();
    	    var list = edgeList.get(i);
	        line.append(list.get(0));  
	        line.append(',');
	        line.append(list.get(1));  
    	    line.append("\n");
    	    fileWriter.write(line.toString());
    	}
    	fileWriter.close();
    }
    
    public static void main(String[] args)
    {
        Graph randomGraph = new Graph();
  
        //System.out.println("The generated random graph :");
        for (int i = 0;
             i < randomGraph.adjacencyList.size(); i++) {
           // System.out.print(i + " -> { ");
  
            List<Integer> list
                = randomGraph.adjacencyList.get(i);
  
            if (list.isEmpty())
                System.out.print(" No adjacent vertices ");
            else {
                int size = list.size();
                for (int j = 0; j < size; j++) {
  
                    //System.out.print(list.get(j));
                   // if (j < size - 1)
                      //  System.out.print(" , ");
                }
            }
            //System.out.println("}");
        }
        //printEdgeList();
        try {
			writeEdgeListToCSV();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}