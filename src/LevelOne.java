import game2D.TileMap;

public class LevelOne extends Level
{	
	public LevelOne(Player player, Boss boss, TileMap tmap,Game gct)
	{
		super(player, boss, tmap, gct);
		setUpLevel();
	}
	void setUpLevel()
	{
		player.show();
		setUpThugs();
		setUpCrates();
        boss.setSpawn(1945f, 50f);
	}
	public void restartLevel()
	{
		resetCurrentLevel();
		boss.reset();
		player.reset();
		player.show();
	}
}
