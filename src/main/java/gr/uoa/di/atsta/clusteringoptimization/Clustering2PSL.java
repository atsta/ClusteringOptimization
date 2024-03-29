package gr.uoa.di.atsta.clusteringoptimization;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;  
import java.time.Duration;
import java.time.Instant;

public class Clustering2PSL  
{       
    public static int MAX_COM_VOLUME;
    public static Integer[] degrees;
    public static Integer[] communities;
    public static Integer[] communityVolumes;
    public static int maxCommunityId = 1;
    public static long communitiesCalcDuration;
    public static long totalDuration;
    public static long degreeCalcDuration;
    public static Integer[] NUM_PARTITIONS = {4, 6, 12, 16, 32, 50, 80, 128, 200, 256};
    public static void main(String args[]) throws IOException   
    {   
        for (int i = 0; i < NUM_PARTITIONS.length; i++) 
        {   
            maxCommunityId = 1;
            Instant start = Instant.now();
            MAX_COM_VOLUME = 2*Utils.EDGES_COUNT/NUM_PARTITIONS[i];
            initCommunities();
            Instant startDegreeCalc = Instant.now();
            calcDegrees();
            Instant finishDegreeCalc = Instant.now();
            degreeCalcDuration = Duration.between(startDegreeCalc, finishDegreeCalc).toMillis(); 
            Instant startCommunitiesCalc = Instant.now();
            findCommunities();
            Instant finishCommunitiesCalc = Instant.now();
            communitiesCalcDuration = Duration.between(startCommunitiesCalc, finishCommunitiesCalc).toMillis();  
            Instant finish = Instant.now();
            totalDuration = Duration.between(start, finish).toMillis();    
            Utils utils = new Utils("results_2psl", 2*Utils.EDGES_COUNT, totalDuration, degreeCalcDuration, communities, degrees, NUM_PARTITIONS[i]);
            utils.Evaluate();
        }
    }   

    private static void initCommunities() {
        communities = new Integer[Utils.VERTICES_COUNT];
        communityVolumes = new Integer[Utils.VERTICES_COUNT];
        for (int i = 0;i < Utils.VERTICES_COUNT; i++)
        {
            communities[i]=0;
            communityVolumes[i] = 0;
        }
    }

    private static void findCommunities()
    {
        String line = "";  
        String splitBy = ",";  
        try   
        {  
            BufferedReader br = new BufferedReader(new FileReader(Utils.DATASET));  
            while ((line = br.readLine()) != null) 
            {  
                String[] edge = line.split(splitBy);   
                var w = Integer.parseInt(edge[0]);
                var v = Integer.parseInt(edge[1]);
                findEdgeCommunity(w, v);
            }  
            br.close();
        }   
        catch (IOException e)   
        {  
            e.printStackTrace();  
        }  
    }

    public static void findEdgeCommunity(int u, int v)
    {
        if(communities[u] == 0)
        {
            communities[u] = maxCommunityId;
            communityVolumes[maxCommunityId] += degrees[u];
            ++maxCommunityId;
        }
        if(communities[v] == 0)
        {
            communities[v] = maxCommunityId;
            communityVolumes[maxCommunityId] += degrees[v];
            ++maxCommunityId;
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
   
    private static void initDegrees()
    {
        degrees = new Integer[Utils.VERTICES_COUNT];
        for (int i = 0; i < Utils.VERTICES_COUNT; i++) 
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
            BufferedReader br = new BufferedReader(new FileReader(Utils.DATASET));  
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
}  