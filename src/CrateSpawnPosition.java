public class CrateSpawnPosition<L,R>
{
	private final L x;
	private final R y;
	
	public CrateSpawnPosition(L x, R y)
	{
		this.x = x;
		this.y = y;
	}
	
	public L getX() { return x; }
	public R getY() { return y; }
}