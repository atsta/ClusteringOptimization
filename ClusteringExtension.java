import java.io.BufferedReader;  
import java.io.FileReader;  
import java.io.IOException;  
import java.util.*; 
import java.time.Duration;
import java.time.Instant;

public class ClusteringExtension
{   
    final static int NUM_PARTITIONS = 4;
    final static int VERTICES_COUNT = 4000000;
    final static int EDGES_COUNT = 117185084;
    public static int MAX_COM_VOLUME = 2 * EDGES_COUNT/NUM_PARTITIONS;
    public static Integer[] degrees;
    public static Node[] nodes;
    public static Integer[] externalDegrees;
    public static Integer[] internalDegrees;
    public static double[] qualityScores = new double[VERTICES_COUNT];
    public static Integer[] communities = new Integer[VERTICES_COUNT];
    public static Integer[] communityVolumes = new Integer[VERTICES_COUNT];
    public static int maxCommunityId = 1;
    public static int totalCommunities = 0;

    public static void main(String args[])   
    {   
        System.out.println("Edges count: "+ EDGES_COUNT);
        System.out.println("Vertices count: "+ VERTICES_COUNT);
        System.out.println("Max comminity volume: "+ MAX_COM_VOLUME);

        Instant start = Instant.now();

        initEdgeNodes();
        findCommunities();

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();    
        System.out.println("Total duration: "+ timeElapsed/1000 + " seconds");
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
            nodes[u].communityDegree[maxCommunityId] = 1;
            communityVolumes[maxCommunityId] += nodes[u].communityDegree[maxCommunityId];
            maxCommunityId++;
        }
        if(communities[v] == null)
        {
            if (communityVolumes[maxCommunityId] == null)
                communityVolumes[maxCommunityId] = 0;
            communities[v] = maxCommunityId;
            nodes[v].communityDegree[maxCommunityId] = 1;
            communityVolumes[maxCommunityId] += nodes[v].communityDegree[maxCommunityId];
            maxCommunityId++;
        }

        var volCommU = communityVolumes[communities[u]];
        var volCommV = communityVolumes[communities[v]];

        var trueVolCommU = volCommU - nodes[u].communityDegree[communities[u]];
        var trueVolCommV = volCommV - nodes[v].communityDegree[communities[v]];

        if((volCommU <= MAX_COM_VOLUME) && (volCommV <= MAX_COM_VOLUME))
        {
            if(trueVolCommU <= trueVolCommV && volCommV + nodes[u].communityDegree[communities[v]] <= MAX_COM_VOLUME)
            {
                communityVolumes[communities[u]] -= nodes[u].communityDegree[communities[u]];
                //nodes[u].communityDegree[communities[u]]--;
                communityVolumes[communities[v]]++;
                nodes[u].communityDegree[communities[v]]++;

                communities[u] = communities[v];
            }
            else if (trueVolCommV < trueVolCommU && volCommU + nodes[v].communityDegree[communities[u]] <= MAX_COM_VOLUME) 
            {
                communityVolumes[communities[v]]-=nodes[v].communityDegree[communities[v]]--;
                nodes[v].communityDegree[communities[v]]--;
                
                communityVolumes[communities[u]]++;
                nodes[v].communityDegree[communities[u]]++;

                communities[v] = communities[u];
            }
        }
    }

    private static void initEdgeNodes()
    {
        nodes = new Node[VERTICES_COUNT];
        for (int i = 1; i <= VERTICES_COUNT; i++) 
        {
            nodes[i] =  new Node(i, VERTICES_COUNT);
        }
    }
}  