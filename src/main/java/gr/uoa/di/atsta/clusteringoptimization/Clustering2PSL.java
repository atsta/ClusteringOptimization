package gr.uoa.di.atsta.clusteringoptimization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;  
import java.util.*; 
import java.time.Duration;
import java.time.Instant;

public class Clustering2PSL  
{   
    final static int NUM_PARTITIONS = 1000;
    final static int VERTICES_COUNT = 4000000;
    final static int EDGES_COUNT = 117185084;
    public static String filename = "dataset.csv";
    
    /*
    final static int NUM_PARTITIONS = 4;
    public static String filename = "small_dataset.csv";
    final static int VERTICES_COUNT = 40;
    final static int EDGES_COUNT = 560;
    */
    
    public static int MAX_COM_VOLUME = 2 * EDGES_COUNT/NUM_PARTITIONS;
    public static Integer[] degrees;
    public static Integer[] externalDegrees;
    public static Integer[] internalDegrees;
    public static double[] coverageScores = new double[VERTICES_COUNT];
    public static double[] conductanceScores = new double[VERTICES_COUNT];
    public static Integer[] communities = new Integer[VERTICES_COUNT];
    public static Integer[] communityVolumes = new Integer[VERTICES_COUNT];
    public static int maxCommunityId = 1;
    public static Set<Integer> validCommunities;
    public static int totalCommunities = 0;
    public static Map<Integer, List<String>> members; 
    public static long communitiesCalcDuration;
    public static long totalDuration;
    public static long degreeCalcDuration;

    public static void main(String args[]) throws IOException   
    {   
        Instant start = Instant.now();
        Instant startDegreeCalc = Instant.now();
        calcDegrees();
        Instant finishDegreeCalc = Instant.now();
        degreeCalcDuration = Duration.between(startDegreeCalc, finishDegreeCalc).toMillis(); 

        Instant startCommunitiesCalc = Instant.now();
        findCommunities();
        findCommunities(); 
        Instant finishCommunitiesCalc = Instant.now();
        communitiesCalcDuration = Duration.between(startCommunitiesCalc, finishCommunitiesCalc).toMillis();  
        Instant finish = Instant.now();
        totalDuration = Duration.between(start, finish).toMillis();    

        evaluateCommunities();
        writeResultsToFile();
    }   

    private static void findCommunities()
    {
        String line = "";  
        String splitBy = ",";  
        try   
        {  
            BufferedReader br = new BufferedReader(new FileReader(filename));  
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
   
    private static void evaluateCommunities() 
    {
        initExternalDegrees();
        String line = "";  
        String splitBy = ",";  
        try   
        {  
            BufferedReader br = new BufferedReader(new FileReader(filename));  
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
            conductanceScores[i] = 0;
            coverageScores[i] = 0;      
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

         for (int i = 0; i < VERTICES_COUNT; i++)
         {
             if (communityVolumes[i] == null)
                 continue;

             var denominator = Math.min(communityVolumes[i], 2*EDGES_COUNT - communityVolumes[i]);
             if (denominator != 0)
             {
                conductanceScores[i] = (double) externalDegrees[i] / denominator;
             }
         }

        //coverage score 
    	
        for (int i = 0; i < VERTICES_COUNT; i++)
        {
            var totalDegree = internalDegrees[i] + externalDegrees[i];
            if (totalDegree != 0)
            {
                coverageScores[i] = (double) internalDegrees[i] / totalDegree;
            }
        }
    }
    
    private static void filterValidComnmunities()
    {
    	validCommunities = new HashSet<>();
        for (int i = 1; i < VERTICES_COUNT; i++) 
        {
           if (communities[i] == null)
                continue;
           
           if (validCommunities.contains(communities[i]))
        	   continue;
           
           validCommunities.add(communities[i]);
           totalCommunities++;
        }
        members = new HashMap<>();
    	for (Integer community : validCommunities) 
    	{
    		members.put(community, new ArrayList<>());
    	}
    	for (Integer i = 1; i < VERTICES_COUNT; i++) 
        {
            if (communities[i] == null)
                continue;
    		members.get(communities[i]).add(i.toString());
        }
    }
    
    public static void writeResultsToFile() throws IOException
    {
    	filterValidComnmunities();
    	File txtfile = new File("results_2psl.txt");
    	FileWriter fileWriter = new FileWriter(txtfile);
    	
    	StringBuilder line = new StringBuilder();
    	//Basic
    	line.append("Edges count: "+ EDGES_COUNT + "\n");
    	line.append("Vertices count: "+ VERTICES_COUNT + "\n");
        line.append("Max comminity volume: "+ MAX_COM_VOLUME + "\n");
    	line.append("Total " + totalCommunities + " communities found"+ "\n");
    	
    	line.append("--------------------------------------------------------------------------\n\n");

    	//Time
    	line.append("Degree calculation: "+ degreeCalcDuration/1000 + " seconds"+ "\n");
    	line.append("Communities detection: "+ communitiesCalcDuration/1000 + " seconds"+ "\n");
    	line.append("Total duration: "+ totalDuration/1000 + " seconds"+ "\n");

    	line.append("--------------------------------------------------------------------------\n\n");
    	
    	//Quality
    	var sumCoverage = 0;
    	var sumConductance = 0;
        for (int i = 0; i < VERTICES_COUNT; i++)
        {
        	sumCoverage += coverageScores[i];
        	sumConductance += conductanceScores[i];
        }
    	line.append("Average covergae: "+ sumCoverage + "\n");
    	line.append("Average conductance: "+ sumConductance + "\n");
    	
    	line.append("--------------------------------------------------------------------------\n\n");

    	fileWriter.write(line.toString());
    	for (Integer i : members.keySet()) 
    	{
    		line = new StringBuilder();
        	line.append("Community id: " + i + "\n");
        	line.append("Size: " + communityVolumes[i] + "\n");
        	line.append("Members: ");
        	line.append(String.join(",", members.get(i)));
    	    line.append("\n\n");
    	    fileWriter.write(line.toString());
		}
    	fileWriter.close();
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
            BufferedReader br = new BufferedReader(new FileReader(filename));  
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