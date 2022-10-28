import java.io.BufferedReader;  
import java.io.FileReader;  
import java.io.IOException;  
import java.util.*; 

public class Clustering2PSL  
{   
    final static int NUM_PARTITIONS = 5000;
    final static int VERTICES_COUNT = 4000000;
    final static int EDGES_COUNT = 117185084;
    public static List<List<Integer>> edgeList;
    public static Integer[] degrees;
    public static int MAX_COM_VOLUME = 2 * EDGES_COUNT/NUM_PARTITIONS;
    public static Integer[] communities = new Integer[VERTICES_COUNT];
    public static Integer[] communityVolumes = new Integer[VERTICES_COUNT];
    public static int maxCommunityId = 0;

    public static void main(String args[])   
    {   
        // Graph randomGraph = new Graph();
        
        // randomGraph.printGraph();
        // randomGraph.printEdgeList();

        // randomGraph.calculateDegrees();
        // randomGraph.printEdgeDegrees();
        calcDegrees();
        printEdgeDegrees();
        findCommunities();
        printCommunities();
    }   

    private static void findCommunities()
    {
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
                findEdgeCommunity(w, v);
            }  
        }   
        catch (IOException e)   
        {  
            e.printStackTrace();  
        }   
    }

    public static void findEdgeCommunity(int u, int v)
    {
        if(communities[u] == null)
        {
            if (communityVolumes[maxCommunityId] == null)
                communityVolumes[maxCommunityId] = 0;
            communities[u] = maxCommunityId;
            communityVolumes[maxCommunityId] += degrees[u];
            maxCommunityId++;
        }
        if(communities[v] == null)
        {
            if (communityVolumes[maxCommunityId] == null)
                communityVolumes[maxCommunityId] = 0;
            communities[v] = maxCommunityId;
            communityVolumes[maxCommunityId] += degrees[v];
            maxCommunityId++;
        }

        var volCommU = communityVolumes[communities[u]];
        var volCommV = communityVolumes[communities[v]];

        var trueVolCommU = volCommU - degrees[u];
        var trueVolCommV = volCommV - degrees[v];

        if((volCommU <= MAX_COM_VOLUME) && (volCommV <= MAX_COM_VOLUME))
        {
            if(trueVolCommU <= trueVolCommV && volCommV + degrees[u] <= MAX_COM_VOLUME)
            {
                communityVolumes[communities[u]] -= degrees[u];
                communityVolumes[communities[v]] += degrees[u];
                communities[u] = communities[v];
            }
            else if (trueVolCommV < trueVolCommU && volCommU + degrees[v] <= MAX_COM_VOLUME) 
            {
                communityVolumes[communities[v]] -= degrees[v];
                communityVolumes[communities[u]] += degrees[v];
                communities[v] = communities[u];
            }
        }
    }

    private static void printCommunities()
    {
        for (int i = 0; i < VERTICES_COUNT; i++) 
        {
            if (communities[i] == null)
                continue;
            System.out.println("Community with id " + communities[i] + " has volume " + communityVolumes[communities[i]]);
            if (i > 999)
                break;
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
}  