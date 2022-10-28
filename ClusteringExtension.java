import java.util.*;   

public class ClusteringExtension
{   
    final static int COMM_BOUND = 5;
    final static int VERTICES_COUNT = 4000000;
    final static int EDGES_COUNT = 117185084;
    public static List<List<Integer>> edgeList;
    public static Integer[] degrees;

    public static void main(String args[])   
    {   
        //Graph randomGraph = new Graph();
        //randomGraph.printGraph();
        //randomGraph.printEdges();
        
        //readEdgeList();
        //printEdgeList();
        calcDegrees();
        printEdgeDegrees();
        //findCommunities(randomGraph);
    }   

    public static void findCommunities(Graph randomGraph)
    {
        Integer[] communities = new Integer[randomGraph.vertices];
        Integer[] communityVolumes = new Integer[randomGraph.vertices];
        int maxCommunityId = 0;
        
        for (int i = 0; i < randomGraph.edgeNodes.size(); i++) 
        {
            var edge = randomGraph.edgeNodes.get(i);
            var nodeU = edge.get(0);
            var nodeV = edge.get(1);

            var u = nodeU.id;
            var v = nodeV.id;

            if(communities[u] == null)
            {
                if (communityVolumes[maxCommunityId] == null)
                    communityVolumes[maxCommunityId] = 0;
                communities[u] = maxCommunityId;
                nodeU.communityDegree[maxCommunityId] = 1;
                communityVolumes[maxCommunityId] += nodeU.communityDegree[maxCommunityId];
                maxCommunityId++;
            }
            if(communities[v] == null)
            {
                if (communityVolumes[maxCommunityId] == null)
                    communityVolumes[maxCommunityId] = 0;
                communities[v] = maxCommunityId;
                nodeV.communityDegree[maxCommunityId] = 1;
                communityVolumes[maxCommunityId] += nodeV.communityDegree[maxCommunityId];
                maxCommunityId++;
            }

            var volCommU = communityVolumes[communities[u]];
            var volCommV = communityVolumes[communities[v]];

            var trueVolCommU = volCommU - nodeU.communityDegree[communities[u]];
            var trueVolCommV = volCommV - nodeV.communityDegree[communities[v]];

            if((volCommU <= COMM_BOUND) && (volCommV <= COMM_BOUND))
            {
                if(trueVolCommU <= trueVolCommV && volCommV + nodeU.communityDegree[communities[v]] <= COMM_BOUND)
                {
                    communityVolumes[communities[u]]--;
                    nodeU.communityDegree[communities[u]]--;
                    
                    communityVolumes[communities[v]]++;
                    nodeU.communityDegree[communities[v]]++;

                    communities[u] = communities[v];
                }
                else if (trueVolCommV < trueVolCommU && volCommU + nodeV.communityDegree[communities[u]] <= COMM_BOUND) 
                {
                    communityVolumes[communities[v]]--;
                    nodeV.communityDegree[communities[v]]--;
                    
                    communityVolumes[communities[u]]++;
                    nodeV.communityDegree[communities[u]]++;

                    communities[v] = communities[u];
                }
            }
        }
        for (int i = 0; i < randomGraph.vertices; i++) 
        {
            System.out.println("Community with id " + communities[i] + " has volume " + communityVolumes[communities[i]]);
        }
    }
}  