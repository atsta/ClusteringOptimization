import java.io.BufferedReader;  
import java.io.FileReader;  
import java.io.IOException;  
import java.util.*; 

public class Clustering2PSL  
{   
    final static int COMM_BOUND = 5;
    final static int VERTICES_COUNT = 4000000;
    final static int EDGES_COUNT = 117185084;
    public static List<List<Integer>> edgeList;
    public static Integer[] degrees;

    public static void main(String args[])   
    {   
        // Graph randomGraph = new Graph();
        
        // randomGraph.printGraph();
        // randomGraph.printEdgeList();

        // randomGraph.calculateDegrees();
        // randomGraph.printEdgeDegrees();
        calcDegrees();
        printEdgeDegrees();
        // findCommunities(randomGraph);
    }   

    private static void readEdgeList()
    {
        edgeList = new ArrayList<>(EDGES_COUNT);
        String line = "";  
        String splitBy = ",";  
        try   
        {  
            BufferedReader br = new BufferedReader(new FileReader("dataset.csv"));  
            while ((line = br.readLine()) != null) 
            {  
                String[] edge = line.split(splitBy);   
                edgeList.add(Arrays.asList(Integer.parseInt(edge[0]),  Integer.parseInt(edge[1])));
            }  
        }   
        catch (IOException e)   
        {  
            e.printStackTrace();  
        }   
    }

    private static void initDegrees()
    {
        degrees = new Integer[VERTICES_COUNT];
        for (int i = 0; i < VERTICES_COUNT; i++) 
        {
            degrees[i] = 0;
        }
    }

    private static void calcDegrees()
    {
        initDegrees();
        String line = "";  
        String splitBy = ",";  
        try   
        {  
            BufferedReader br = new BufferedReader(new FileReader("dataset.csv"));  
            while ((line = br.readLine()) != null) 
            {  
                String[] edge = line.split(splitBy);   
                var w = Integer.parseInt(edge[0]);
                var v = Integer.parseInt(edge[1]);
                degrees[w]++;
                degrees[v]++;
            }  
        }   
        catch (IOException e)   
        {  
            e.printStackTrace();  
        }   
    }

    private static void printEdgeDegrees() 
    {
        System.out.println("The input edge degrees (top 1000):");
        for (int i = 0; i < degrees.length; i++) {
            System.out.println(i + " -> " + degrees[i]);
            if (i > 999)
                break;
        }
    }

    private static void printEdgeList() 
    {
        System.out.println("The input edge list (top 1000 edges)");
        for (int i = 0; i < edgeList.size(); i++) {
            var list = edgeList.get(i);
            System.out.println("[ " + list.get(0) + " , " + list.get(1) + " ]");
            if (i > 999)
                break;
        }
    }

    public static void findCommunities(Graph randomGraph)
    {
        Integer[] communities = new Integer[randomGraph.vertices];
        Integer[] communityVolumes = new Integer[randomGraph.vertices];
        int maxCommunityId = 0;
        
        for (int i = 0; i < randomGraph.edgeList.size(); i++) 
        {
            var edge = randomGraph.edgeList.get(i);
            var u = edge.get(0);
            var v = edge.get(1);

            if(communities[u] == null)
            {
                if (communityVolumes[maxCommunityId] == null)
                    communityVolumes[maxCommunityId] = 0;
                communities[u] = maxCommunityId;
                communityVolumes[maxCommunityId] += randomGraph.degrees[u];
                maxCommunityId++;
            }
            if(communities[v] == null)
            {
                if (communityVolumes[maxCommunityId] == null)
                    communityVolumes[maxCommunityId] = 0;
                communities[v] = maxCommunityId;
                communityVolumes[maxCommunityId] += randomGraph.degrees[v];
                maxCommunityId++;
            }

            var volCommU = communityVolumes[communities[u]];
            var volCommV = communityVolumes[communities[v]];

            var trueVolCommU = volCommU - randomGraph.degrees[u];
            var trueVolCommV = volCommV - randomGraph.degrees[v];

            if((volCommU <= COMM_BOUND) && (volCommV <= COMM_BOUND))
            {
                if(trueVolCommU <= trueVolCommV && volCommV + randomGraph.degrees[u] <= COMM_BOUND)
                {
                    communityVolumes[communities[u]] -= randomGraph.degrees[u];
                    communityVolumes[communities[v]] += randomGraph.degrees[u];
                    communities[u] = communities[v];
                }
                else if (trueVolCommV < trueVolCommU && volCommU + randomGraph.degrees[v] <= COMM_BOUND) 
                {
                    communityVolumes[communities[v]] -= randomGraph.degrees[v];
                    communityVolumes[communities[u]] += randomGraph.degrees[v];
                    communities[v] = communities[u];
                }
            }
        }
        for (int i = 0; i < randomGraph.vertices; i++) 
        {
            System.out.println("Community with id " + communities[i] + " has volume " + communityVolumes[communities[i]]);
        }
    }
}  