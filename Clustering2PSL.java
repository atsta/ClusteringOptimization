public class Clustering2PSL  
{   
    public static void main(String args[])   
    {   
        Graph randomGraph = new Graph();
        
        randomGraph.printGraph();
        randomGraph.printEdgeList();

        randomGraph.calculateDegrees();
        randomGraph.printEdgeDegrees();
    }   
}  