import game2D.TileMap;

public class LevelTwo extends Level
{

	public LevelTwo(Player player, Boss boss, TileMap tmap,Game gct)
	{
		super(player, boss, tmap, gct);
		setUpLevel();
	}
	
	void setUpLevel()
	{
		player.show();
		setUpThugs();
		setUpCrates();
		setUpTurrets();
        //TODO set up boss spawn
		
	}
	/**
	 * Scan through the tile map and replace every occurrence of 'o'
	 * with an according tile char. Instantiate a new Turret and add it
	 * along with the coordinates of the tile to the ArrayList.
	 * */
    private void setUpTurrets()
    {
    	for(int y=0; y<tmap.getMapHeight(); y++)
    		for(int x=0; x<tmap.getMapWidth(); x++)
    			if(tmap.getTileChar(x, y)=='o')
    			{
    				float pixelX = x*tmap.getTileWidth();
    				float pixelY = y*tmap.getTileHeight();
    				Turret turret = new Turret("turret");
    				//add thug to array list
    				Pair<Float, Float> location = new Pair<Float,Float>(pixelX,pixelY);
    				Pair<Turret,Pair<Float,Float>> p = new Pair<Turret ,Pair<Float,Float>>(turret,location);
    				turretSpawnPositions.add(p);
    				//set the x,y for each thug
    				turret.setX(pixelX);
    				turret.setY(pixelY);
    				//set tile according to the one above it
    				tmap.setTileChar(tmap.getTileChar(x, y-1), x, y);
    			}
    }
}
