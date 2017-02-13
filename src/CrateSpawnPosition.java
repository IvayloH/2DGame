public class CrateSpawnPosition<X,Y>
{
	private final X x;
	private final Y y;
	
	public CrateSpawnPosition(X x, Y y)
	{
		this.x = x;
		this.y = y;
	}
	
	public X getX() { return x; }
	public Y getY() { return y; }
}