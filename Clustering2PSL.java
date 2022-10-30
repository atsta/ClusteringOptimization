import java.io.BufferedReader;  
import java.io.FileReader;  
import java.io.IOException;  
import java.util.*; 
import java.time.Duration;
import java.time.Instant;

public class Clustering2PSL  
{   
    final static int NUM_PARTITIONS = 4;
    final static int VERTICES_COUNT = 4000000;
    final static int EDGES_COUNT = 117185084;
    public static List<List<Integer>> edgeList;
    public static Integer[] degrees;
    public static Integer[] externalDegrees;
    public static Integer[] internalDegrees;
    public static double[] qualityScores = new double[VERTICES_COUNT];
    public static int MAX_COM_VOLUME = 2 * EDGES_COUNT/NUM_PARTITIONS;
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

        Instant startDegreeCalc = Instant.now();
        calcDegrees();
        //printEdgeDegrees();
        Instant finishDegreeCalc = Instant.now();
        long timeElapsedDegreeCalc = Duration.between(startDegreeCalc, finishDegreeCalc).toMillis(); 
        System.out.println("Degree calculation: "+ timeElapsedDegreeCalc/1000 + " seconds");

        Instant startCommunitiesCalc = Instant.now();
        findCommunities();
        findCommunities();
        //printCommunities();
        findTotalCommunities();
        Instant finishCommunitiesCalc = Instant.now();
        long timeElapsedCommunitiesCalc = Duration.between(startCommunitiesCalc, finishCommunitiesCalc).toMillis();  
        System.out.println("Communities detection: "+ timeElapsedCommunitiesCalc/1000 + " seconds");

        System.out.println("Total " + totalCommunities + " communities found");
        
        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();    
        System.out.println("Total duration: "+ timeElapsed/1000 + " seconds");

        double degreePercentage = (double) Math.round(timeElapsedDegreeCalc * 100 / timeElapsed);
        double communityPercentage = (double) Math.round(timeElapsedCommunitiesCalc * 100 / timeElapsed);
        System.out.println("Degree calculation in total duration: " + degreePercentage + " %");
        System.out.println("Community detection in total duration: " + communityPercentage + " %");

        evaluateCommunities();
        printQualityScores();
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
            while (i < VERTICES_COUNT - 1 && communities[i] == communities[i + 1])
                i++;
            if (i < 999)
                System.out.println("Community with id " + communities[i] + " has volume " + communityVolumes[communities[i]]);
        }
    }

    private static void findTotalCommunities() 
    {
        for (int i = 0; i < VERTICES_COUNT; i++) 
        {
            if (communityVolumes[i] == null || communityVolumes[i] <= 0)
                continue;
            totalCommunities++;
        }
    }

    private static void evaluateCommunities() 
    {
        initExternalDegrees();
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
                calculateExternalDegree(w, v);
            }  
        }   
        catch (IOException e)   
        {  
            e.printStackTrace();  
        }   
        calculateQualityScores();
    }

    private static void initExternalDegrees()
    {
        externalDegrees = new Integer[VERTICES_COUNT];
        internalDegrees = new Integer[VERTICES_COUNT];
        for (int i = 0; i < VERTICES_COUNT; i++) 
        {
            externalDegrees[i] = 0;
            internalDegrees[i] = 0;
            qualityScores[i] = 0;
        }
    }

    public static void calculateExternalDegree(int u, int v)
    {
        var comU = communities[u];
		var comV = communities[v];

		if (comU != comV)
        {
			externalDegrees[comU]++;
			externalDegrees[comV]++;
        }
        else
        {
            internalDegrees[comU]+=2;
        }
    }

    public static void calculateQualityScores() 
    {
        //conductance score

        // for (int i = 0; i < VERTICES_COUNT; i++)
        // {
        //     if (communityVolumes[i] == null)
        //         continue;

        //     var denominator = Math.min(communityVolumes[i], 2*EDGES_COUNT - communityVolumes[i]);
        //     if (denominator != 0)
        //     {
        //         qualityScores[i] = (double) externalDegrees[i] / denominator;
        //     }
        // }

        //coverage score 

        for (int i = 0; i < VERTICES_COUNT; i++)
        {
            var totalDegree = internalDegrees[i] + externalDegrees[i];
            if (totalDegree != 0)
            {
                qualityScores[i] = (double) internalDegrees[i] / totalDegree;
            }
        }
    }

    public static void printQualityScores()
    {
        int k = 0;
        for (int i = 0; i < VERTICES_COUNT; i++)
        {
            if (qualityScores[i] != 0)
            {
                System.out.println("Quality score of community " + i + ":" + qualityScores[i]);
                k++;
            }
            if (k > 999)
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