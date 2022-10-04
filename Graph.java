import java.util.*;   

public class Graph 
{
    public int vertices;
    public int edges;
  
    final int MAX_LIMIT = 20;
  
    Random random = new Random();

    public List<List<Integer>> adjacencyList;
    public List<List<Integer>> edgeList;
    public Integer[] degrees;

    public Graph()
    {
        this.vertices = random.nextInt(MAX_LIMIT) + 1;
        this.edges = random.nextInt(computeMaxEdges(vertices)) + 1;
  
        adjacencyList = new ArrayList<>(vertices);
        edgeList = new ArrayList<>(vertices);

        for (int i = 0; i < vertices; i++)
            adjacencyList.add(new ArrayList<Integer>());
  
        for (int i = 0; i < edges; i++) {
            int v = random.nextInt(vertices);
            int w = random.nextInt(vertices);

            if((v == w ) || adjacencyList.get(v).contains(w)) {
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
        adjacencyList.get(w).add(v);
        edgeList.add(Arrays.asList(v, w));
    }

    void calculateDegrees()
    {
        degrees = new Integer[vertices];
        for (int i = 0; i < edgeList.size(); i++) 
        {
            var list = edgeList.get(i);
            int w = list.get(0);
            if (degrees[w] == null)
                degrees[w] = 0;
            
            int v = list.get(1);
            if (degrees[v] == null)
                degrees[v] = 0;

            degrees[w]++;
            degrees[v]++;
        }
    }

    void printGraph() 
    {
        System.out.println("The generated random graph :");
        for (int i = 0; i < this.adjacencyList.size(); i++) {
            System.out.print(i + " -> { ");
            var list = this.adjacencyList.get(i);
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
            System.out.println(" }");
        }
    }

    void printEdgeList() 
    {
        System.out.println("The generated edge list from the random graph :");
        for (int i = 0; i < this.edgeList.size(); i++) {
            var list = this.edgeList.get(i);
            System.out.println("[ " + list.get(0) + " , " + list.get(1) + " ]");
        }
    }

    void printEdgeDegrees() 
    {
        System.out.println("The edge degrees from the random graph :");
        for (int i = 0; i < this.degrees.length; i++) {
            System.out.println(i + " -> " + this.degrees[i]);
        }
    }
}