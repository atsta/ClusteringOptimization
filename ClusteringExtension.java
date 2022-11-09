import java.io.BufferedReader;  
import java.io.FileReader;  
import java.io.IOException;  
import java.time.Duration;
import java.time.Instant;
import java.util.*;  
import java.io.IOException;  
import java.io.File;  
import java.nio.file.Files;  
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

public class ClusteringExtension
{   
    final static int NUM_PARTITIONS = 4;
    final static int VERTICES_COUNT = 4000000;
    final static int EDGES_COUNT = 117185084;
    final static int WINDOW_SIZE = 10000;
    public static int MAX_COM_VOLUME = 2 * EDGES_COUNT/NUM_PARTITIONS;
    public static Integer[] degrees;
    public static List<Node> nodes;
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
            var edgesProcessed = 0;
            BufferedReader br = new BufferedReader(new FileReader("dataset.csv"));  
            while ((line = br.readLine()) != null) 
            {  
                String[] edge = line.split(splitBy);   
                var w = Integer.parseInt(edge[0]);
                var v = Integer.parseInt(edge[1]);
                findEdgeCommunity(w, v);
                edgesProcessed++;

                if (edgesProcessed%WINDOW_SIZE == 0)
                    pruneCommunities();
            }  
        }   
        catch (IOException e)   
        {  
            e.printStackTrace();  
        }   
    }

    public static void findEdgeCommunity(int u, int v)
    {
        var nodeU = nodes.get(u);
        var nodeV = nodes.get(v);

        if(communities[u] == null)
        {
            if (communityVolumes[maxCommunityId] == null)
                communityVolumes[maxCommunityId] = 0;
            communities[u] = maxCommunityId;
            nodeU.updateDegrees(maxCommunityId, 1);
            communityVolumes[maxCommunityId] += 1;
            maxCommunityId++;
        }
        if(communities[v] == null)
        {
            if (communityVolumes[maxCommunityId] == null)
                communityVolumes[maxCommunityId] = 0;
            communities[v] = maxCommunityId;
            nodeV.updateDegrees(maxCommunityId, 1);
            communityVolumes[maxCommunityId] += 1;
            maxCommunityId++;
        }

        var volCommU = communityVolumes[communities[u]];
        var volCommV = communityVolumes[communities[v]];

        var degreeUinCommU = nodeU.getDegrees(communities[u]);
        var degreeUinCommV = nodeU.getDegrees(communities[v]);

        var degreeVinCommV = nodeV.getDegrees(communities[v]);
        var degreeVinCommU = nodeV.getDegrees(communities[u]);

        var trueVolCommU = volCommU - degreeUinCommU;
        var trueVolCommV = volCommV - degreeVinCommV;

        if((volCommU <= MAX_COM_VOLUME) && (volCommV <= MAX_COM_VOLUME))
        {
            if(trueVolCommU <= trueVolCommV && volCommV + degreeUinCommV <= MAX_COM_VOLUME)
            {
                communityVolumes[communities[u]] -= degreeUinCommU;
                communityVolumes[communities[v]]++;
                nodeU.updateDegrees(communities[v], degreeUinCommV + 1);
                communities[u] = communities[v];
            }
            else if (trueVolCommV < trueVolCommU && volCommU + degreeVinCommU <= MAX_COM_VOLUME) 
            {
                communityVolumes[communities[v]] -= degreeVinCommV;                
                communityVolumes[communities[u]]++;
                nodeV.updateDegrees(communities[u], degreeVinCommU + 1);
                communities[v] = communities[u];
            }
        }
    }

    private static void pruneCommunities() 
    {
        nodes.parallelStream().forEach(
            (node) -> {
                node.pruneCommunities(4);
            });
    }

    private static void initEdgeNodes()
    {
        nodes = new ArrayList<Node>();
        for (int i = 0; i < VERTICES_COUNT; i++) 
        {
            nodes.add(new Node(i));
        }
    }
}  