package gr.uoa.di.atsta.clusteringoptimization;
import java.io.IOException;

public class Utils 
{
    public string resultsFileName;
    public 
    public Utils(string resultsFileName)
    {
        this.resultsFileName = resultsFileName;
    }

    private static double[] coverageScores;
    private static double[] conductanceScores;
    private static Integer[] communities = new Integer[VERTICES_COUNT];
    private static Integer[] communityVolumes = new Integer[VERTICES_COUNT];

    public static void writeResultsToFile() throws IOException
    {
    	File txtfile = new File("results_extension.txt");
    	FileWriter fileWriter = new FileWriter(txtfile);
    	
    	StringBuilder line = new StringBuilder();
    	//Basic
    	line.append("Edges count: "+ EDGES_COUNT + "\n");
    	line.append("Vertices count: "+ VERTICES_COUNT + "\n");
        line.append("Max comminity volume: "+ MAX_COM_VOLUME + "\n");
        line.append("Number of partitions: "+ NUM_PARTITIONS + "\n");
    	line.append("Total " + totalCommunities + " communities found"+ "\n");
    	
    	line.append("--------------------------------------------------------------------------\n\n");
    	
    	//Time
    	line.append("Total duration: "+ totalDuration/1000 + " seconds"+ "\n");

    	line.append("--------------------------------------------------------------------------\n\n");
    	
    	//Quality
    	var sumCoverage = 0;
    	var sumConductance = 0;
    	var sumInternalDegrees = 0;
    	var sumExternalDegrees = 0;
        for (Integer community : validCommunities) 
    	{
        	sumCoverage += coverageScores[community];
        	sumConductance += conductanceScores[community];
        	sumInternalDegrees += internalDegrees[community];
        	sumExternalDegrees += externalDegrees[community];
    	}
    	line.append("Average coverage: "+ sumCoverage + "\n");
    	line.append("Average conductance: "+ sumConductance + "\n");
    	line.append("Sum internal degrees: "+ sumInternalDegrees + "\n");
    	line.append("Sum external degrees: "+ sumExternalDegrees + "\n");

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
