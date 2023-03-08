package gr.uoa.di.atsta.clusteringoptimization;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Utils 
{
	public final static int NUM_PARTITIONS = 5000;
    public final static int VERTICES_COUNT = 600000;
    public final static int EDGES_COUNT = 925872;
	public static String DATASET_NAME = "amazon_dataset_shuffled";
	public static String DATASET = "Datasets/" + DATASET_NAME+ ".csv";

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
	private Map<Integer, List<String>> membersSorted; 
	private Map<Integer, Integer> communityDistribution; 
	private double avgCommMembers = 0;
	private int maxCommSize = 0;
	private int minCommSize = 0;
	
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
		calculateCommunityStats();
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

	private void sortCommunitiesBasedOnSize() 
    {
		membersSorted = new LinkedHashMap<>();

		this.members.entrySet()
				.stream()
				.sorted((e1, e2) -> e2.getValue().size() - e1.getValue().size())
				.forEachOrdered(x -> membersSorted.put(x.getKey(), x.getValue()));
	}

	private void calculateCommunityStats() 
	{
		sortCommunitiesBasedOnSize();
		communityDistribution = new HashMap<Integer, Integer>();
		var totalMembers = 0;
		var minSizeIndex = -1;
    	for (Integer i : membersSorted.keySet()) 
    	{
			var commSize = membersSorted.get(i).size();
			totalMembers += commSize;
			var count = communityDistribution.get(commSize);
			if (count == null)
				count = 0;
			communityDistribution.put(commSize, count + 1);
			minSizeIndex = i;
		}
		avgCommMembers = totalMembers/totalCommunities;
		maxCommSize = membersSorted.entrySet().iterator().next().getValue().size();
		if (minSizeIndex != -1)
			minCommSize = membersSorted.get(minSizeIndex).size();
	}

	private void calculateQualityScores() 
    {
        //conductance score
        for (Integer community : validCommunities) 
    	{
			var totalDegree = internalDegrees[community] + externalDegrees[community];
    		//int denominator = Math.min(communityVolumes[community], denominatorFactor - communityVolumes[community]);
            if (totalDegree != 0)
			{
                double res = ((double)externalDegrees[community]) / totalDegree;
				conductanceScores[community] = res;
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
            }
    	}
    }

    private void writeResultsToFile() throws IOException
    {
		String file = "Results/" + DATASET_NAME + "/" + resultsFile;
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
    	line.append("Average community size: "+ avgCommMembers + "\n\n");
    	line.append("Max community size: "+ maxCommSize + "\n\n");
    	line.append("Min community size: "+ minCommSize + "\n\n");

    	line.append("--------------------------------------------------------------------------\n\n");
    	
		line.append("*sum of members degrees or partial degrees (depending on algorithm)" +"\n");

    	fileWriter.write(line.toString());    
    	for (Integer i : membersSorted.keySet()) 
    	{
    		line = new StringBuilder();
        	line.append("Community id: " + i + "\n");
			line.append("Conductance: " + conductanceScores[i] + "\n");
        	line.append("Coverage: " + coverageScores[i] + "\n");
			line.append("Internal degrees: " + internalDegrees[i] + "\n");
        	line.append("External degrees: " + externalDegrees[i] + "\n");
			if (communityVolumes != null)
				line.append("*Volume: " + communityVolumes[i] + "\n");
			var commMembers = membersSorted.get(i);
			line.append("Size: " + commMembers.size() + "\n");
        	line.append("Members: ");
        	line.append(String.join(",", commMembers));
    	    line.append("\n\n");
    	    fileWriter.write(line.toString());
		}
    	fileWriter.close();
		
/////////////////////////////////////////////////////////////////////////////////////////////

		String distrinbutionFile = "Results/" + DATASET_NAME + "/" + "distribution_"+resultsFile;
    	File disttxtfile = new File(distrinbutionFile);
    	FileWriter distributionFileWriter = new FileWriter(disttxtfile);
    	line = new StringBuilder();
    	line.append("Distribution results:"+"\n\n");    	
    	distributionFileWriter.write(line.toString());    
    	for (Integer i : communityDistribution.keySet()) 
    	{
    		line = new StringBuilder();
        	line.append("Size: " + i + "\n");
			line.append("Occurences: " + communityDistribution.get(i) + "\n\n");
    	    distributionFileWriter.write(line.toString());
		}
    	distributionFileWriter.close();
	}
}