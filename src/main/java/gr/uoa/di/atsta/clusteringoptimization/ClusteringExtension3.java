package gr.uoa.di.atsta.clusteringoptimization;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;  
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ClusteringExtension3
{   
    public static int MAX_COM_VOLUME;
    public static List<Community> comms;
    public static Integer[] communities;
    public static Integer[] degrees;
    public static long totalDuration;
    public static Integer[] communityVolumes;

    public static void main(String args[]) throws IOException   
    {   
        initComms();
        Instant start = Instant.now();
        MAX_COM_VOLUME = Utils.VERTICES_COUNT/Utils.NUM_PARTITIONS;
        communities = new Integer[Utils.VERTICES_COUNT];

        Instant finish = Instant.now();
        totalDuration = Duration.between(start, finish).toMillis();    
        Utils utils = new Utils("results_extension_3.txt", 2*Utils.EDGES_COUNT, totalDuration, 0, communities, communityVolumes);
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

    private static void updatePartialDegree()
    {
        comms = new ArrayList<>(); 	
        for (int i = 0; i < Utils.VERTICES_COUNT; i++) 
        {      
            //comms[i].members    
        }
    }
	
}  