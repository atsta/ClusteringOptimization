package gr.uoa.di.atsta.clusteringoptimization;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Utils 
{
	public final static int NUM_PARTITIONS = 5000;
    public final static int VERTICES_COUNT = 600000;
    public final static int EDGES_COUNT = 925872;
    public static String DATASET = "Datasets/amazon_dataset.csv";
	//public static String DATASET = "Datasets/amazon_dataset_shuffled.csv";

	// public final static int NUM_PARTITIONS = 5000;
    // public final static int VERTICES_COUNT = 430000;
    // public final static int EDGES_COUNT = 1049866;
    // //public static String DATASET = "Datasets/dblp_dataset.csv";
    // public static String DATASET = "Datasets/dblp_dataset_shuffled.csv";

	public String resultsFile;
	public int denominatorFactor;
	public long totalDuration;
	public long degreeCalcDuration;
	public Integer[] communities;
    private Integer[] communityVolumes;
    public Utils(String resultsFile, int denominatorFactor, long totalDuration, long degreeCalcDuration, Integer[] communities, Integer[] communityVolumes) 
	{
		this.resultsFile = resultsFile;
		this.denominatorFactor = denominatorFactor;
		this.totalDuration = totalDuration;
		this.degreeCalcDuration = degreeCalcDuration;
		this.communities = communities;
		this.communityVolumes = communityVolumes;
	}

	public void Evaluate() throws IOException  
	{
		this.evaluateCommunities();
        this.writeResultsToFile();
	}

    private double[] coverageScores;
    private double[] conductanceScores;
	private Integer[] externalDegrees;
    private Integer[] internalDegrees;
    private int totalCommunities = 0;
	private Set<Integer> validCommunities;
    private Map<Integer, List<String>> members; 
	
	private void evaluateCommunities() 
    {
        initExternalDegrees();
        String line = "";  
        String splitBy = ",";  
        try   
        {  
            BufferedReader br = new BufferedReader(new FileReader(DATASET));  
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
        filterValidComnmunities();
        calculateQualityScores();
    }

	private void initExternalDegrees()
    {
        externalDegrees = new Integer[VERTICES_COUNT];
        internalDegrees = new Integer[VERTICES_COUNT];
		conductanceScores = new double[VERTICES_COUNT];
		coverageScores = new double[VERTICES_COUNT];
        for (int i = 0; i < VERTICES_COUNT; i++) 
        {
            externalDegrees[i] = 0;
            internalDegrees[i] = 0;
            conductanceScores[i] = 0;
            coverageScores[i] = 0;      
        }
    }

	private void calculateExternalDegree(int u, int v)
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

	private void filterValidComnmunities()
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

	private void calculateQualityScores() 
    {
        //conductance score
        for (Integer community : validCommunities) 
    	{
    		int denominator = Math.min(communityVolumes[community], denominatorFactor - communityVolumes[community]);
            if (denominator != 0)
			{
                double res = ((double)externalDegrees[community]) / denominator;
				conductanceScores[community] = res;
				// System.out.println(externalDegrees[community]);
				// System.out.println(denominator);
				// System.out.println(res);
			}
    	}

        //coverage score 
        for (Integer community : validCommunities) 
    	{
            var totalDegree = internalDegrees[community] + externalDegrees[community];
            if (totalDegree != 0)
            {
                double res = ((double)internalDegrees[community]) / totalDegree;
				coverageScores[community] = res;
				// System.out.println(internalDegrees[community]);
				// System.out.println(totalDegree);
            }
    	}
    }

    private void writeResultsToFile() throws IOException
    {
		String file = resultsFile;
    	File txtfile = new File(file);
    	FileWriter fileWriter = new FileWriter(txtfile);
    	
    	StringBuilder line = new StringBuilder();
    	//Basic
    	line.append("Edges count: "+ EDGES_COUNT + "\n");
    	line.append("Vertices count: "+ VERTICES_COUNT + "\n");
        line.append("Number of partitions: "+ NUM_PARTITIONS + "\n");
    	line.append("Total " + totalCommunities + " communities found"+ "\n\n");
    	
    	line.append("--------------------------------------------------------------------------\n\n");
    	
    	//Time
		line.append("Degree calculation duration: "+ degreeCalcDuration + " milliseconds"+ "\n");
		line.append("Degree calculation duration: "+ degreeCalcDuration/1000 + " seconds"+ "\n\n");

		line.append("Community detection duration: "+ (totalDuration - degreeCalcDuration) + " milliseconds"+ "\n");
    	line.append("Community detection duration: "+ (totalDuration - degreeCalcDuration)/1000 + " seconds"+ "\n\n");

		line.append("Total duration: "+ totalDuration + " milliseconds"+ "\n");
    	line.append("Total duration: "+ totalDuration/1000 + " seconds"+ "\n\n");

    	line.append("--------------------------------------------------------------------------\n\n");
    	
    	//Quality
    	double sumCoverage = 0.0;
    	double sumConductance = 0.0;
    	var sumInternalDegrees = 0;
    	var sumExternalDegrees = 0;
        for (Integer community : validCommunities) 
    	{
        	sumCoverage += coverageScores[community];
        	sumConductance += conductanceScores[community];
        	sumInternalDegrees += internalDegrees[community];
        	sumExternalDegrees += externalDegrees[community];
    	}
    	line.append("Average coverage: "+ sumCoverage/totalCommunities + "\n");
    	line.append("Average conductance: "+ sumConductance/totalCommunities + "\n");
    	line.append("Sum internal degrees: "+ sumInternalDegrees + "\n");
    	line.append("Sum external degrees: "+ sumExternalDegrees + "\n\n");

    	line.append("--------------------------------------------------------------------------\n\n");
    	
    	fileWriter.write(line.toString());    
    	for (Integer i : members.keySet()) 
    	{
    		line = new StringBuilder();
        	line.append("Community id: " + i + "\n");
        	line.append("Size: " + communityVolumes[i] + "\n");
			line.append("Conductance: " + conductanceScores[i] + "\n");
        	line.append("Coverage: " + coverageScores[i] + "\n");
			line.append("Internal degrees: " + internalDegrees[i] + "\n");
        	line.append("External degrees: " + externalDegrees[i] + "\n");
        	line.append("Members: ");
        	line.append(String.join(",", members.get(i)));
    	    line.append("\n\n");
    	    fileWriter.write(line.toString());
		}
    	fileWriter.close();
	}
}