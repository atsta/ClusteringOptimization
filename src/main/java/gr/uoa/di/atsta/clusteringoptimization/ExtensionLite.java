package gr.uoa.di.atsta.clusteringoptimization;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;  
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ExtensionLite
{   
    public final static Integer NUM_PARTITIONS = 4;
    public final static int VERTICES_COUNT = 3997697;
    public final static int EDGES_COUNT = 34680452;
	public static String DATASET_NAME = "journal_dataset";
    public static String DATASET = "Datasets/Converted/" + DATASET_NAME+ ".csv";
    public static int MAX_COM_VOLUME = VERTICES_COUNT/NUM_PARTITIONS;

    public static List<Community> comms;
    public static Integer[] degrees;
    public static Integer[] finalCommunities;
    public static Map<Integer, Integer> communitySize;
    public static void main(String args[]) throws IOException   
    {   
        finalCommunities = new Integer[VERTICES_COUNT];
        initComms();
        calcDegrees();
        calculateCommunitySizes();
        findCommunities();
    }   
    
    private static void initComms()
    {
        comms = new ArrayList<>(); 	
        for (int i = 0; i < VERTICES_COUNT; i++) 
        {      
            comms.add(new Community(i));      
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
            BufferedReader br = new BufferedReader(new FileReader(DATASET));  
            while ((line = br.readLine()) != null) 
            {  
                String[] edge = line.split(splitBy);   
                var u = Integer.parseInt(edge[0]);
                var v = Integer.parseInt(edge[1]);
                ++degrees[u];
                ++degrees[v];
                calcPartialDegree(u, v);
            }  
        }   
        catch (IOException e)   
        {  
            e.printStackTrace();  
        }   
        updatePartialDegrees();
    }

    public static void calcPartialDegree(int u, int v)
    {
        var commU = comms.get(u);
        var commV = comms.get(v);

        var degreeUinCommU = commU.getDegrees(u);
        var degreeVinCommU = commU.getDegrees(v);
        var degreeUinCommV = commV.getDegrees(u);
        var degreeVinCommV = commV.getDegrees(v);

        commU.updateDegrees(u, degreeUinCommU + 1.0);
        commU.updateDegrees(v, degreeVinCommU + 1.0);
        commV.updateDegrees(u, degreeUinCommV + 1.0);
        commV.updateDegrees(v, degreeVinCommV + 1.0);

        commU.edges +=2;
        commV.edges +=2;
    }

    private static void updatePartialDegrees()
    {
        comms.parallelStream().forEach(
						(community) -> {
							community.updateDegrees(degrees);
						});
    }

    private static void calculateCommunitySizes() 
    {
        communitySize = new HashMap<Integer, Integer>();
        for (int i = 0; i < VERTICES_COUNT; i++) 
        {    
            var comm = comms.get(i);
            communitySize.put(i, comm.edges);
        }
    }

    private static void sortCommMembers()
    {
        comms.parallelStream().forEach(
						(community) -> {
							community.sortCommMembers();
						});
    }
	
    private static List<Entry<Integer, Integer>> sortCommunitySizes() 
    {
        return communitySize.entrySet().stream()
        .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    private static void findCommunities() 
    {
        sortCommMembers();
        var sortCommunitySizes = sortCommunitySizes();
        for (Entry<Integer, Integer> entry : sortCommunitySizes) {
            var commId = entry.getKey();
            var comm = comms.get(commId);
            var comVol = 0;
            for (int i = 0; i < comm.size(); i++) {
                Entry<Integer, Double> memberEntry = comm.sortedmembers.get(i);
                var memberKey = memberEntry.getKey();
                if (finalCommunities[memberKey] == null) 
                {
                    finalCommunities[memberKey] = commId;
                    comVol += degrees[memberKey];
                }
                if (comVol >= MAX_COM_VOLUME/comm.edges)
                {
                    break;
                }
            }
        }
    }
}  