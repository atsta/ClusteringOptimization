package gr.uoa.di.atsta.clusteringoptimization;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Set;

public class Node 
{
    public int id;
    public Map<Integer, Integer> communityDegrees;

    public Node(int id)
    {
        this.id = id;
        this.communityDegrees = new HashMap<Integer, Integer>();
    }

    public boolean contains(Integer community) {
		return this.communityDegrees.containsKey(community);
	}

	public void updateDegrees(Integer community, Integer value) {
		if (value < 0)
			value = 0;
		this.communityDegrees.put(community, value);
	}

	public Integer getDegrees(Integer community) {
        var degreeInComm = this.communityDegrees.get(community);
        if (degreeInComm == null)
            return 0;
        return degreeInComm;   
	}

    public void pruneCommunities(int size) {
		this.communityDegrees = findGreatest(this.communityDegrees, size);
    	//this.communityDegrees = new HashMap<Integer, Integer>();
	}

    public static <K, V extends Comparable<? super V>> Map<K, V> 
    findGreatest(Map<K, V> map, int size)
    {
        Comparator<? super Entry<K, V>> comparator = 
            new Comparator<Entry<K, V>>()
        {
            @Override
            public int compare(Entry<K, V> e0, Entry<K, V> e1)
            {
                V v0 = e0.getValue();
                V v1 = e1.getValue();
                return v0.compareTo(v1);
            }
        };
        PriorityQueue<Entry<K, V>> highest = 
            new PriorityQueue<Entry<K,V>>(size, comparator);
        for (Entry<K, V> entry : map.entrySet())
        {
            highest.offer(entry);
            while (highest.size() > size)
            {
                highest.poll();
            }
        }

        Map<K, V> result = new HashMap<K,V>();
        while (highest.size() > 0)
        {
        	Entry<K, V> entry = highest.poll();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}