package gr.uoa.di.atsta.clusteringoptimization;
import java.io.BufferedReader;  
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;  
import java.time.Duration;
import java.time.Instant;
import java.io.File;  
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClusteringExtension
{   
	/*
    final static int NUM_PARTITIONS = 1000;
    final static int VERTICES_COUNT = 4000000;
    final static int EDGES_COUNT = 117185084;
    public static String filename = "dataset.csv";
    final static int WINDOW_SIZE = 1000;
    */
    
	/*
    final static int NUM_PARTITIONS = 4;
    final static int VERTICES_COUNT = 40;
    final static int EDGES_COUNT = 560;
    final static int WINDOW_SIZE = 50;
    public static String filename = "small_dataset.csv";
    */
	
    final static int NUM_PARTITIONS = 5;
    final static int VERTICES_COUNT = 29;
    final static int EDGES_COUNT = 165;
    public static String filename = "small_dataset.csv";
    
    public static int MAX_COM_VOLUME = EDGES_COUNT/NUM_PARTITIONS;
   // public static int MAX_COM_VOLUME = 2 * VERTICES_COUNT/NUM_PARTITIONS;
    public static Integer[] degrees;
    public static Node[] nodes;
    public static Integer[] externalDegrees;
    public static Integer[] internalDegrees;
    public static double[] coverageScores = new double[VERTICES_COUNT];
    public static double[] conductanceScores = new double[VERTICES_COUNT];
    public static Integer[] communities = new Integer[VERTICES_COUNT];
    public static Integer[] communityVolumes = new Integer[VERTICES_COUNT];
    public static int maxCommunityId = 1;
    public static Set<Integer> validCommunities;
    public static Map<Integer, List<String>> members; 
    public static int totalCommunities = 0;
    public static long totalDuration;

    public static void main(String args[]) throws IOException   
    {   
        Instant start = Instant.now();
        initEdgeNodes();
        findPartialDegrees();
        findCommunities();
        Instant finish = Instant.now();
        totalDuration = Duration.between(start, finish).toMillis();    
        
        evaluateCommunities();        
        writeResultsToFile();
    }   
    
    private static void findPartialDegrees()
    {
        String line = "";  
        String splitBy = ",";  
        try   
        {  
            //var edgesProcessed = 0;
            BufferedReader br = new BufferedReader(new FileReader(filename));  
            while ((line = br.readLine()) != null) 
            {  
                String[] edge = line.split(splitBy);   
                var w = Integer.parseInt(edge[0]);
                var v = Integer.parseInt(edge[1]);
                updatePartialDegree(w, v);
            }  
        }   
        catch (IOException e)   
        {  
            e.printStackTrace();  
        }   
    }
    

    private static void findCommunities()
    {
        String line = "";  
        String splitBy = ",";  
        try   
        {  
            //var edgesProcessed = 0;
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
    
    public static void updatePartialDegree(int u, int v)
    {
        var nodeU = nodes[u];
        var nodeV = nodes[v];
        
        if(communities[u] == null)
        {
            communities[u] = maxCommunityId;
            maxCommunityId++;
        }
        if(communities[v] == null)
        {
            communities[v] = maxCommunityId;
            maxCommunityId++;
        }
        
        var degreeUinCommU = nodeU.getDegrees(communities[u]);
        var degreeVinCommV = nodeV.getDegrees(communities[v]);
        var degreeUinCommV = nodeU.getDegrees(communities[v]);
        var degreeVinCommU = nodeV.getDegrees(communities[u]);

        nodeU.updateDegrees(communities[v], degreeUinCommV + 1);
        nodeV.updateDegrees(communities[v], degreeVinCommV + 1);
        nodeV.updateDegrees(communities[u], degreeVinCommU + 1);
        nodeU.updateDegrees(communities[u], degreeUinCommU + 1);
    }
    

    public static void findEdgeCommunity(int u, int v)
    {
        var nodeU = nodes[u];
        var nodeV = nodes[v];
       
        var degreeUinCommU = nodeU.getDegrees(communities[u]);
        var degreeVinCommV = nodeV.getDegrees(communities[v]);
        var degreeUinCommV = nodeU.getDegrees(communities[v]);
        var degreeVinCommU = nodeV.getDegrees(communities[u]);
        
        if(communityVolumes[communities[u]] == null)
        {
            communityVolumes[communities[u]] = degreeUinCommU;
        }
        if(communityVolumes[communities[v]] == null)
        {
            communityVolumes[communities[v]] = degreeVinCommV;
        }
        
        var volCommU = communityVolumes[communities[u]];
        var volCommV = communityVolumes[communities[v]];
        
        var trueVolCommU = volCommU - degreeUinCommU;
        var trueVolCommV = volCommV - degreeVinCommV;
        
        if((0 <= trueVolCommU && volCommU < MAX_COM_VOLUME) && (0 <= trueVolCommV && volCommV < MAX_COM_VOLUME))
        {
            if(trueVolCommU <= trueVolCommV && volCommV + degreeUinCommV <= MAX_COM_VOLUME)
            {
            	communityVolumes[communities[u]] -= degreeUinCommU;
                communityVolumes[communities[v]] += degreeUinCommV;
                communities[u] = communities[v];
            }
            else if (trueVolCommV < trueVolCommU && volCommU + degreeVinCommU <= MAX_COM_VOLUME) 
            {
            	communityVolumes[communities[v]] -= degreeVinCommV;
                communityVolumes[communities[u]] += degreeVinCommU;                
                communities[v] = communities[u];
            }
        }
    }

    /*
    private static void pruneCommunities() 
    {
    	 for (int i = 0; i < VERTICES_COUNT; i++) 
         {
             nodes[i].pruneCommunities(4);
         }
    }*/

    private static void initEdgeNodes()
    {
        nodes = new Node[VERTICES_COUNT];
        for (int i = 0; i < VERTICES_COUNT; i++) 
        {
            nodes[i] = new Node(i);
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

    public static void calculateQualityScores() 
    {
        //conductance score

         for (int i = 0; i < VERTICES_COUNT; i++)
         {
             if (communityVolumes[i] == null)
                 continue;

             var denominator = Math.min(communityVolumes[i], 2*VERTICES_COUNT - communityVolumes[i]);
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
    
    public static void writeResultsToFile() throws IOException
    {
    	filterValidComnmunities();
    	File txtfile = new File("results_extension.txt");
    	FileWriter fileWriter = new FileWriter(txtfile);
    	
    	StringBuilder line = new StringBuilder();
    	//Basic
    	line.append("Edges count: "+ EDGES_COUNT + "\n");
    	line.append("Vertices count: "+ VERTICES_COUNT + "\n");
        line.append("Max comminity volume: "+ MAX_COM_VOLUME + "\n");
    	line.append("Total " + totalCommunities + " communities found"+ "\n");
    	
    	line.append("--------------------------------------------------------------------------\n\n");
    	
    	//Time
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
    	line.append("Average coverage: "+ sumCoverage + "\n");
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
}  