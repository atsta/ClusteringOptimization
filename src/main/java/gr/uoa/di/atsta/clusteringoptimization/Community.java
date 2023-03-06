package gr.uoa.di.atsta.clusteringoptimization;
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
        this.members = new HashMap<>();
    }

    public boolean contains(Integer member) {
		return this.members.containsKey(member);
	}

	public void updateDegrees(Integer member, Double value) {
		if (value < 0)
			value = 0.0;
		this.members.put(member, value);
	}

	public double getDegrees(Integer member) {
        var degreeInComm = this.members.get(member);
        if (degreeInComm == null)
            return 0;
        return degreeInComm;   
	}
}