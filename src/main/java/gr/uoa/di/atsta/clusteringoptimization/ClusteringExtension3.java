package gr.uoa.di.atsta.clusteringoptimization;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;  
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ClusteringExtension3
{   
    public static int MAX_COM_VOLUME;
    public static List<Community> comms;
    public static Integer[] communities;
    public static Integer[] dummyCommunities;
    public static Integer[] degrees;
    public static Map<Integer, Double> communityScores;
    public static long totalDuration;

    public static void main(String args[]) throws IOException   
    {   
        Instant start = Instant.now();
        MAX_COM_VOLUME = Utils.VERTICES_COUNT/Utils.NUM_PARTITIONS;
        communities = new Integer[Utils.VERTICES_COUNT];
        initComms();
        calcDegrees();
        calculateCommunityScores();
        findCommunities();
        Instant finish = Instant.now();
        totalDuration = Duration.between(start, finish).toMillis();    
        Utils utils = new Utils("results_extension_3", 2*Utils.EDGES_COUNT, totalDuration, 0, communities, null);
        utils.Evaluate();
    }   
    
    private static void initComms()
    {
        comms = new ArrayList<>(); 	
        for (int i = 0; i < Utils.VERTICES_COUNT; i++) 
        {      
            comms.add(new Community(i));      
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
                var u = Integer.parseInt(edge[0]);
                var v = Integer.parseInt(edge[1]);
                degrees[u]++;
                degrees[v]++;
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
    }

    private static void updatePartialDegrees()
    {
        comms.parallelStream().forEach(
						(community) -> {
							community.updateDegrees(degrees);
						});
    }

    private static void calculateCommunityScores() 
    {
        communityScores = new HashMap<Integer, Double>();
        for (int i = 0; i < Utils.VERTICES_COUNT; i++) 
        {    
            var comm = comms.get(i);
            comm.calculateCommunityScore(MAX_COM_VOLUME);     
            communityScores.put(i, comm.score);
            //System.out.println(communityScores.get(i));
        }
    }
	
    private static List<Entry<Integer, Double>> sortCommunityScores() 
    {
        return communityScores.entrySet().stream()
        .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    private static void findCommunities() 
    {
        var sortCommunityScores = sortCommunityScores();
        for (Entry<Integer, Double> entry : sortCommunityScores) {
            //System.out.println(entry.getValue());
            var commId = entry.getKey();
            var comm = comms.get(commId);
            var commSize = comm.size();
            if (MAX_COM_VOLUME < commSize)
            {
                List<Entry<Integer, Double>> sortedComm = comm.getSortedCommunity();
                for (int i = 0; i < MAX_COM_VOLUME; i++) {
                    Entry<Integer, Double> memberEntry = sortedComm.get(i);
                    var memberKey = memberEntry.getKey();
                    if (communities[memberKey] == null) 
                        communities[memberKey] = commId;
                }
            }
            else 
            {
                for(Integer memberKey : comm.members.keySet()) {
                    if (communities[memberKey] == null) 
                        communities[memberKey] = commId;
                }
            }
		}
    }
}  