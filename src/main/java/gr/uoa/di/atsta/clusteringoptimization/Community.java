package gr.uoa.di.atsta.clusteringoptimization;
import java.util.Map.Entry;
import java.util.*;   

public class Community 
{
    public int id;
    public int volume;
    public double score;
    public Map<Integer, Double> members;

    public Community(int id)
    {
        this.id = id;
        this.volume = 0;
        this.score = 0;
        this.members = new HashMap<Integer, Double>();
    }

    public boolean contains(Integer member) {
		return this.members.containsKey(member);
	}

    public int size() {
		return members.size();
	}

	public void updateDegrees(Integer member, Double value) {
		if (value < 0)
			value = 0.0;
		this.members.put(member, value);
	}

    public void updateDegrees(Integer[] degrees)
    {
        this.members.forEach((key, value) -> this.updateDegrees(key, value/degrees[key]));
    }

	public double getDegrees(Integer member) {
        var degreeInComm = this.members.get(member);
        if (degreeInComm == null)
            return 0;
        return degreeInComm;   
	}

    public List<Entry<Integer, Double>> getSortedCommunity() 
    {
		final List<Entry<Integer, Double>> sorted = this.members.entrySet().stream()
				.sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
				.collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
		return sorted;
	}

	public double getTotalValue() 
    {
		double total = 0.0;
		for(double value : this.members.values()) {
			total += value;
		}
		return total;
	}

    public void calculateCommunityScore(int MAX_COM_VOLUME) 
    {
        var commSize = this.size();
        double total = 0.0;
        if (MAX_COM_VOLUME < commSize)
        {
            List<Entry<Integer, Double>> sortedComm = getSortedCommunity();
            for (int i = 0; i < MAX_COM_VOLUME; i++) {
                Entry<Integer, Double> entry = sortedComm.get(i);
                total += entry.getValue();
            }
        }
        else 
        {
            for(double value : this.members.values()) {
                total += value;
            }
        }
        this.score = total;
	}
}