public class Clustering2PSL  
{   
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
        int maxCommunityId = 1;
        
        for (int i = 0; i < randomGraph.edgeList.size(); i++) 
        {
            var edge = randomGraph.edgeList.get(i);
            var u = edge.get(0);
            var v = edge.get(1);

            if(communities[u] == null)
            {
                if (communityVolumes[maxCommunityId - 1] == null)
                    communityVolumes[maxCommunityId - 1] = 0;
                communities[u] = maxCommunityId;
                communityVolumes[maxCommunityId - 1] += randomGraph.degrees[u];
                maxCommunityId++;
            }
            if(communities[v] == null)
            {
                if (communityVolumes[maxCommunityId - 1] == null)
                    communityVolumes[maxCommunityId - 1] = 0;
                communities[v] = maxCommunityId;
                communityVolumes[maxCommunityId - 1] += randomGraph.degrees[v];
                maxCommunityId++;
            }
        }
    }
}  