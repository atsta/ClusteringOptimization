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
import java.util.Map.Entry;

public final class Utils 
{
	public final static int NUM_PARTITIONS = 4;
    public final static int VERTICES_COUNT = 334822;
    public final static int EDGES_COUNT = 925872;
	public final static int EDGES_COUNT_REAL = 924923;
	public static String DATASET_NAME = "amazon_dataset";

	// public final static int NUM_PARTITIONS = 4;
    // public final static int VERTICES_COUNT = 430000;
    // public final static int EDGES_COUNT = 1049866;
	// public static String DATASET_NAME = "dblp_dataset_shuffled";

	public String resultsFile;
	public int denominatorFactor;
	public long totalDuration;
	public long degreeCalcDuration;
	public Integer[] communities;
    public Integer[] degrees;
    public Utils(String resultsFile, int denominatorFactor, long totalDuration, long degreeCalcDuration, Integer[] communities, Integer[] degrees) 
	{
		this.resultsFile = resultsFile;
		this.denominatorFactor = denominatorFactor;
		this.totalDuration = totalDuration;
		this.degreeCalcDuration = degreeCalcDuration;
		this.communities = communities;
		this.degrees = degrees;
	}

	public void Evaluate() throws IOException  
	{
		this.evaluateCommunities();
        this.writeResultsToFile();
		this.writeCommunitiesToFile();
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
	private Integer[] communityVolumes;
	public static String DATASET = "Datasets/Converted/" + DATASET_NAME+ ".csv";
	
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
		communityVolumes = new Integer[VERTICES_COUNT];
    	for (Integer community : validCommunities) 
    	{
			communityVolumes[community] = 0;
    		members.put(community, new ArrayList<>());
    	}
    	for (Integer i = 1; i < VERTICES_COUNT; i++) 
        {
            if (communities[i] == null)
                continue;
			communityVolumes[communities[i]] += degrees[i];
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

	public List<Entry<Integer, Integer>> getSortedDistributionOccurencesDesc() 
    {
		final List<Entry<Integer, Integer>> sorted = this.communityDistribution.entrySet().stream()
				.sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
				.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
		return sorted;
	}

    private void writeResultsToFile() throws IOException
    {
		String file = "Results/" + DATASET_NAME + "/" + resultsFile + ".csv";
    	File csvfile = new File(file);
		FileWriter fileWriter = new FileWriter(csvfile);
    	StringBuilder line = new StringBuilder();

		String[] row11 = {"Edges count", Integer.toString(EDGES_COUNT) };
		fileWriter.append(String.join(",", row11));
		fileWriter.append("\n");
		String[] row12 = {"Vertices count", Integer.toString(VERTICES_COUNT) };
		fileWriter.append(String.join(",", row12));
		fileWriter.append("\n");
		String[] row13 = {"Number of partitions", Integer.toString(NUM_PARTITIONS) };
		fileWriter.append(String.join(",", row13));
		fileWriter.append("\n");
		String[] row14 = {"Total", Integer.toString(totalCommunities) };
		fileWriter.append(String.join(",", row14));
		fileWriter.append("\n\n");

		String[] row00 = {"Total duration (ms)", Long.toString(totalDuration) };
		fileWriter.append(String.join(",", row00));
		fileWriter.append("\n");
		String[] row01 = {"Total duration (s)", Long.toString(totalDuration/1000) };
		fileWriter.append(String.join(",", row01));
		fileWriter.append("\n");
		String[] row15 = {"Degree calculation duration (ms)", Long.toString(degreeCalcDuration) };
		fileWriter.append(String.join(",", row15));
		fileWriter.append("\n");
		String[] row16 = {"Degree calculation duration (s)", Long.toString(degreeCalcDuration/1000) };
		fileWriter.append(String.join(",", row16));
		fileWriter.append("\n");
		String[] row17 = {"Community calculation duration (ms)", Long.toString(totalDuration - degreeCalcDuration) };
		fileWriter.append(String.join(",", row17));
		fileWriter.append("\n");
		String[] row18 = {"Community calculation duration (s)", Long.toString((totalDuration - degreeCalcDuration)/1000) };
		fileWriter.append(String.join(",", row18));
		fileWriter.append("\n\n");

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

		String[] row19 = {"Average coverage", Double.toString(sumCoverage/totalCommunities) };
		fileWriter.append(String.join(",", row19));
		fileWriter.append("\n");
		String[] row20 = {"Average conductance", Double.toString(sumConductance/totalCommunities) };
		fileWriter.append(String.join(",", row20));
		fileWriter.append("\n");
		String[] row21 = {"Sum internal degrees", Double.toString(sumInternalDegrees) };
		fileWriter.append(String.join(",", row21));
		fileWriter.append("\n");
		String[] row22 = {"Sum external degrees", Double.toString(sumExternalDegrees) };
		fileWriter.append(String.join(",", row22));
		fileWriter.append("\n");
		String[] row27 = {"Avg community size", Double.toString(avgCommMembers) };
		fileWriter.append(String.join(",", row27));
		fileWriter.append("\n");
		String[] row23 = {"Max community size", Double.toString(maxCommSize) };
		fileWriter.append(String.join(",", row23));
		fileWriter.append("\n");
		String[] row24 = {"Min community size", Double.toString(minCommSize) };
		fileWriter.append(String.join(",", row24));
		fileWriter.append("\n\n");

		String[] row25 = {"Community", "Size", "Members"};
		fileWriter.append(String.join(",", row25));
		fileWriter.append("\n");
    	for (Integer i : membersSorted.keySet()) 
    	{
			var commMembers = membersSorted.get(i);
			String[] row = { i.toString(), Integer.toString(commMembers.size()), String.join(",", commMembers)};
			fileWriter.append(String.join(",", row));
			fileWriter.append("\n");
		}
    	fileWriter.close();
		
/////////////////////////////////////////////////////////////////////////////////////////////

		String distrinbutionFile = "Results/" + DATASET_NAME + "/" + "distribution_"+resultsFile + ".csv";
    	File distcsvfile = new File(distrinbutionFile);
    	FileWriter distributionFileWriter = new FileWriter(distcsvfile);
		String[] row1 = {"Average community size", Double.toString(avgCommMembers) };
		distributionFileWriter.append(String.join(",", row1));
		distributionFileWriter.append("\n");
		String[] row2 = {"Max community size", Integer.toString(maxCommSize) };
		distributionFileWriter.append(String.join(",", row2));
		distributionFileWriter.append("\n");
		String[] row3 = {"Min community size", Integer.toString(minCommSize) };
		distributionFileWriter.append(String.join(",", row3));
		distributionFileWriter.append("\n\n");
		String[] row4 = {"Size", "Occurrences" };
		distributionFileWriter.append(String.join(",", row4));
		distributionFileWriter.append("\n");
		var dist = getSortedDistributionOccurencesDesc();
    	for (Integer i : communityDistribution.keySet()) 
    	{
			String[] row = { i.toString(), communityDistribution.get(i).toString() };
			distributionFileWriter.append(String.join(",", row));
			distributionFileWriter.append("\n");
		}
    	distributionFileWriter.close();

/////////////////////////////////////////////////////////////////////////////////////////////

		String commFile = "Results/" + DATASET_NAME + "/" + "communities_"+resultsFile + ".csv";
    	File cmmcsvfile = new File(commFile);
    	FileWriter commFileWriter = new FileWriter(cmmcsvfile);
		String[] row6 = {"Node", "Community" };
		commFileWriter.append(String.join(",", row6));
		commFileWriter.append("\n");
		for (int i = 1; i < VERTICES_COUNT; i++) 
        {
			var comm = "";
			if (communities[i] != null)
				comm = communities[i].toString();
			String[] row = { Integer.toString(i), comm };
			commFileWriter.append(String.join(",", row));
			commFileWriter.append("\n");
        }
    	commFileWriter.close();
	}

	private void writeCommunitiesToFile() throws IOException 
	{
		String commsFile = "Input/" + DATASET_NAME + "/" + "comms_"+resultsFile + ".csv";
    	File distcsvfile = new File(commsFile);
    	FileWriter commFileWriter = new FileWriter(distcsvfile);
		for (int i = 0; i < VERTICES_COUNT; i++) 
        {
			if (communities[i] == null)
				continue;
			String[] row = { Integer.toString(i), Integer.toString(communities[i]) };
			commFileWriter.append(String.join(",", row));
			commFileWriter.append("\n");
        }
    	commFileWriter.close();

		String commVolsFile = "Input/" + DATASET_NAME + "/" + "comm_vols_"+resultsFile + ".csv";
    	File volsCsvFile = new File(commVolsFile);
    	FileWriter commVolsFileWriter = new FileWriter(volsCsvFile);
		for (Integer community : validCommunities) 
    	{
			String[] row = { Integer.toString(community), Integer.toString(communityVolumes[community]) };
			commVolsFileWriter.append(String.join(",", row));
			commVolsFileWriter.append("\n");
    	}
    	commVolsFileWriter.close();
	}
}