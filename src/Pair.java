public class Pair<First,Second>
{
	private final First first;
	private final Second second;
	
	public Pair(First x, Second y)
	{
		this.first = x;
		this.second = y;
	}
	
	public First getFirst() { return first; }
	public Second getSecond() { return second; }
}