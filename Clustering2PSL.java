public class Clustering2PSL  
{   
    final static int COMM_BOUND = 10;

    public static void main(String args[])   
    {   
        Graph randomGraph = new Graph();
        
        randomGraph.printGraph();
        randomGraph.printEdgeList();

        randomGraph.calculateDegrees();
        randomGraph.printEdgeDegrees();

        findCommunities(randomGraph);
    }   

    public static void findCommunities(Graph randomGraph)
    {
        Integer[] communities = new Integer[randomGraph.vertices];
        Integer[] communityVolumes = new Integer[randomGraph.vertices];
        int maxCommunityId = 0;
        
        for (int i = 0; i < randomGraph.edgeList.size(); i++) 
        {
            var edge = randomGraph.edgeList.get(i);
            var u = edge.get(0);
            var v = edge.get(1);

            if(communities[u] == null)
            {
                if (communityVolumes[maxCommunityId] == null)
                    communityVolumes[maxCommunityId] = 0;
                communities[u] = maxCommunityId;
                communityVolumes[maxCommunityId] += randomGraph.degrees[u];
                maxCommunityId++;
            }
            if(communities[v] == null)
            {
                if (communityVolumes[maxCommunityId] == null)
                    communityVolumes[maxCommunityId] = 0;
                communities[v] = maxCommunityId;
                communityVolumes[maxCommunityId] += randomGraph.degrees[v];
                maxCommunityId++;
            }

            var volCommU = communityVolumes[communities[u]];
            var volCommV = communityVolumes[communities[v]];

            var trueVolCommU = volCommU - randomGraph.degrees[u];
            var trueVolCommV = volCommV - randomGraph.degrees[v];

            if((volCommU <= COMM_BOUND) && (volCommV <= COMM_BOUND))
            {
                if(trueVolCommU <= trueVolCommV && volCommV + randomGraph.degrees[u] <= COMM_BOUND)
                {
                    communityVolumes[communities[u]] -= randomGraph.degrees[u];
                    communityVolumes[communities[v]] += randomGraph.degrees[u];
                    communities[u] = communities[v];
                }
                else if (trueVolCommV < trueVolCommU && volCommU + randomGraph.degrees[v] <= COMM_BOUND) 
                {
                    communityVolumes[communities[v]] -= randomGraph.degrees[v];
                    communityVolumes[communities[u]] += randomGraph.degrees[v];
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