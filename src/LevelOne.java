
public class LevelOne extends Game
{
	private static final long serialVersionUID = 1L;
	
	Player player;
	Crate crates;
	Thug thugs;
	Boss boss;
	
	public LevelOne(Player player, Crate crates, Thug thugs, Boss boss)
	{
		this.player = player;
		this.crates = crates;
		this.thugs = thugs;
		this.boss = boss;
	}
	
	public void restartLevel()
	{
		thugs.reset();
		crates.reset();
		boss.reset();
		player.reset();
		player.show();
	}
	
	public void setUpLevel()
	{
		player.show();
		setUpThugSpawnPositionsLevelOne();
        setUpCrateSpawnPositionsLevelOne();
        boss.setSpawn(1945f, 50f);
	}
    private void setUpCrateSpawnPositionsLevelOne()
    {
    	crates.addCrateSpawnPoint(704.f, 160.f);
    	crates.addCrateSpawnPoint(1408.f, 160.f);
    }
    private void setUpThugSpawnPositionsLevelOne()
    {
    	thugs.addThugSpawnPoint(450.f, 100.f);
    	thugs.addThugSpawnPoint(894.f, 100.f);
    	thugs.addThugSpawnPoint(1440.f, 100.f);
    }
	
}
