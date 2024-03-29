// package gr.uoa.di.atsta.clusteringoptimization;
// import java.io.BufferedReader;
// import java.io.FileReader;
// import java.io.IOException;  
// import java.time.Duration;
// import java.time.Instant;
// import java.util.ArrayList;
// import java.util.List;

// public class ClusteringExtension2Streams
// {   
//     public static int MAX_COM_VOLUME;
//     public static List<Node> nodes;
//     public static Integer[] communities;
//     public static Integer[] communityVolumes;
//     public static int maxCommunityId = 1;
//     public static long totalDuration;
//     public static long degreeCalcDuration;
//     public static Integer[] degrees;

//     public static void main(String args[]) throws IOException   
//     {   
//         calcDegrees();
//         Instant start = Instant.now();
//         MAX_COM_VOLUME = Utils.VERTICES_COUNT/Utils.NUM_PARTITIONS;
//         communities = new Integer[Utils.VERTICES_COUNT];
//         communityVolumes = new Integer[Utils.VERTICES_COUNT];
//         initEdgeNodes();
//         Instant startDegreeCalc = Instant.now();
//         findPartialDegrees();
//         Instant finishDegreeCalc = Instant.now();
//         degreeCalcDuration = Duration.between(startDegreeCalc, finishDegreeCalc).toMillis(); 
//         findCommunities();
//         Instant finish = Instant.now();
//         totalDuration = Duration.between(start, finish).toMillis();    
//         Utils utils = new Utils("results_extension_2", 2*Utils.EDGES_COUNT, totalDuration, degreeCalcDuration, communities, degrees);
//         utils.Evaluate();
//     }   
    
//     private static void initDegrees()
//     {
//         degrees = new Integer[Utils.VERTICES_COUNT];
//         for (int i = 0; i < Utils.VERTICES_COUNT; i++) 
//         {
//             degrees[i] = 0;
//         }
//     }
    
//     private static void calcDegrees()
//     {
//         initDegrees();
//         String line = "";  
//         String splitBy = ",";  
//         try   
//         {  
//             BufferedReader br = new BufferedReader(new FileReader(Utils.DATASET));  
//             while ((line = br.readLine()) != null) 
//             {  
//                 String[] edge = line.split(splitBy);   
//                 var w = Integer.parseInt(edge[0]);
//                 var v = Integer.parseInt(edge[1]);
//                 degrees[w]++;
//                 degrees[v]++;
//             }  
//         }   
//         catch (IOException e)   
//         {  
//             e.printStackTrace();  
//         }   
//     }

//     private static void initEdgeNodes()
//     {
//         nodes = new ArrayList<>(); 	
//         for (int i = 0; i < Utils.VERTICES_COUNT; i++) 
//         {      
//             nodes.add(new Node(i));      
//         }
//     }

//     private static void findPartialDegrees()
//     {
//         String line = "";  
//         String splitBy = ",";  
//         try   
//         {  
//             //var edgesProcessed = 0;
//             BufferedReader br = new BufferedReader(new FileReader(Utils.DATASET));  
//             while ((line = br.readLine()) != null) 
//             {  
//                 String[] edge = line.split(splitBy);   
//                 var w = Integer.parseInt(edge[0]);
//                 var v = Integer.parseInt(edge[1]);
//                 updatePartialDegree(w, v);
//                 //edgesProcessed++;
//                 // if (edgesProcessed%WINDOW_SIZE == 0)
//                 //   pruneCommunities();
//             }  
//         }   
//         catch (IOException e)   
//         {  
//             e.printStackTrace();  
//         }   
//     }
    
//     // private static void pruneCommunities() 
//     // {
//     // 	    nodes.parallelStream().forEach(
//     //             (node) -> {
//     //                 Node.findGreatest(node, 4);
//     //             });
//     // }
    
//     private static void findCommunities()
//     {
//         String line = "";  
//         String splitBy = ",";  
//         try   
//         {  
//             //var edgesProcessed = 0;
//             BufferedReader br = new BufferedReader(new FileReader(Utils.DATASET));  
//             while ((line = br.readLine()) != null) 
//             {  
//                 String[] edge = line.split(splitBy);   
//                 var w = Integer.parseInt(edge[0]);
//                 var v = Integer.parseInt(edge[1]);
//                 findEdgeCommunity(w, v);
//             }  
//         }   
//         catch (IOException e)   
//         {  
//             e.printStackTrace();  
//         }   
//     }
    
//     public static void updatePartialDegree(int u, int v)
//     {
//         var nodeU = nodes.get(u);
//         var nodeV = nodes.get(v);

//         if(communities[u] == null)
//         {
//             communities[u] = maxCommunityId;
//             maxCommunityId++;
//         }
//         if(communities[v] == null)
//         {
//             communities[v] = maxCommunityId;
//             maxCommunityId++;
//         }
        
//         var degreeUinCommU = nodeU.getDegrees(communities[u]);
//         var degreeVinCommV = nodeV.getDegrees(communities[v]);
//         var degreeUinCommV = nodeU.getDegrees(communities[v]);
//         var degreeVinCommU = nodeV.getDegrees(communities[u]);

//         nodeU.updateDegrees(communities[v], degreeUinCommV + 1);
//         nodeV.updateDegrees(communities[v], degreeVinCommV + 1);
//         nodeV.updateDegrees(communities[u], degreeVinCommU + 1);
//         nodeU.updateDegrees(communities[u], degreeUinCommU + 1);    
//     }
    
//     public static void findEdgeCommunity(int u, int v)
//     {
//         var nodeU = nodes.get(u);
//         var nodeV = nodes.get(v);
    
//         var degreeUinCommU = nodeU.getDegrees(communities[u]);
//         var degreeVinCommV = nodeV.getDegrees(communities[v]);
//         var degreeUinCommV = nodeU.getDegrees(communities[v]);
//         var degreeVinCommU = nodeV.getDegrees(communities[u]);
        
//         if(communityVolumes[communities[u]] == null)
//         {
//             communityVolumes[communities[u]] = degreeUinCommU;
//         }
//         if(communityVolumes[communities[v]] == null)
//         {
//             communityVolumes[communities[v]] = degreeVinCommV;
//         }
        
//         var volCommU = communityVolumes[communities[u]];
//         var volCommV = communityVolumes[communities[v]];
        
//         var trueVolCommU = volCommU - degreeUinCommU;
//         var trueVolCommV = volCommV - degreeVinCommV;
        
//         if((0 <= trueVolCommU && volCommU < MAX_COM_VOLUME) && (0 <= trueVolCommV && volCommV < MAX_COM_VOLUME))
//         {
//             if(trueVolCommU <= trueVolCommV && volCommV + degreeUinCommV <= MAX_COM_VOLUME)
//             {
//             	communityVolumes[communities[u]] -= degreeUinCommU;
//                 communityVolumes[communities[v]] += degreeUinCommV;
//                 communities[u] = communities[v];
//             }
//             else if (trueVolCommV < trueVolCommU && volCommU + degreeVinCommU <= MAX_COM_VOLUME) 
//             {
//             	communityVolumes[communities[v]] -= degreeVinCommV;
//                 communityVolumes[communities[u]] += degreeVinCommU;                
//                 communities[v] = communities[u];
//             }
//         }
//     }
// }  