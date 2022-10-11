import java.util.*;   

public class Node 
{
    public int id;
    public Integer[] communityDegree;

    public Node(int id, int vertices)
    {
        this.id = id;
        this.communityDegree = new Integer[vertices];
        for (int i = 0; i < vertices; i++)
            this.communityDegree[i] = 0;
    }
}