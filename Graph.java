import java.util.*;
import java.io.*;
  
public class Graph {
  
    public int vertices;
    public int edges;
  
    final int MAX_LIMIT = 20;
    Random random = new Random();
    public List<List<Integer> > adjacencyList;
  
    public Graph()
    {
        this.vertices = random.nextInt(MAX_LIMIT) + 1;
        this.edges = random.nextInt(computeMaxEdges(vertices)) + 1;
        adjacencyList = new ArrayList<>(vertices);
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
            adjacencyList.get(w).add(v);
    }
  
    public static void main(String[] args)
    {
        Graph randomGraph = new Graph();
  
        System.out.println("The generated random graph :");
        for (int i = 0;
             i < randomGraph.adjacencyList.size(); i++) {
            System.out.print(i + " -> { ");
  
            List<Integer> list
                = randomGraph.adjacencyList.get(i);
  
            if (list.isEmpty())
                System.out.print(" No adjacent vertices ");
            else {
                int size = list.size();
                for (int j = 0; j < size; j++) {
  
                    System.out.print(list.get(j));
                    if (j < size - 1)
                        System.out.print(" , ");
                }
            }
            System.out.println("}");
        }
    }
}