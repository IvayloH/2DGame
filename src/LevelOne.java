import java.util.ArrayList;
import game2D.TileMap;

public class LevelOne extends Level
{	
	public LevelOne(Player player, Boss boss, TileMap tmap,Game gct)
	{
		crateSpawnPositions = new ArrayList<Pair<Crate,Pair<Float,Float>>>();
		thugSpawnPositions = new ArrayList<Pair<Thug,Pair<Float,Float>>>();
		this.player = player;
		this.tmap = tmap;
		this.boss = boss;
		this.gct = gct;	
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
