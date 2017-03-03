import java.util.ArrayList;
import game2D.TileMap;

public abstract class Level
{
	protected ArrayList<Pair<Crate,Pair<Float,Float>>> crateSpawnPositions;
	protected ArrayList<Pair<Thug,Pair<Float,Float>>> thugSpawnPositions;
	protected ArrayList<Pair<Turret,Pair<Float,Float>>> turretSpawnPositions;
	
	protected Player player;
	protected Boss boss;
	protected TileMap tmap = null;
	protected SpriteExtension projectile;
	
	public Level(Player player, Boss boss, TileMap tmap,Game gct)
	{
		crateSpawnPositions = new ArrayList<Pair<Crate,Pair<Float,Float>>>();
		thugSpawnPositions = new ArrayList<Pair<Thug,Pair<Float,Float>>>();
		turretSpawnPositions = new ArrayList<Pair<Turret,Pair<Float,Float>>>();
		this.player = player;
		this.tmap = tmap;
		this.boss = boss;
	}
	
	public ArrayList<Pair<Crate,Pair<Float,Float>>> getCrateSpawnPositions() { return crateSpawnPositions; }
	public ArrayList<Pair<Thug,Pair<Float,Float>>> getThugSpawnPositions() { return thugSpawnPositions; }
	
	/**
	 * Handles calling all other methods needed to set up the level. (empty)
	 * */
	void setUpLevel() { }
	

	/**
	 * Reset the Crate and Thug Lists by calling their reset methods.
	 * */
	protected void resetCurrentLevel()
	{
		int i=0;
		for(i=0; i<thugSpawnPositions.size(); i++)
		{
			Thug th = thugSpawnPositions.get(i).getFirst();
			Pair<Float,Float> location = thugSpawnPositions.get(i).getSecond();
			th.reset(location.getFirst(), location.getSecond());
		}
		for(i=0; i<crateSpawnPositions.size(); i++)
		{
			Crate c = crateSpawnPositions.get(i).getFirst();
			Pair<Float,Float> location = crateSpawnPositions.get(i).getSecond();
			resetCratesOnTileMap();
			c.reset(location.getFirst(), location.getSecond());
		}
	}
	/**
	 * Scan through the tile map and replace every occurrence of 'a'
	 * with an according tile char. Instantiate a new Thug and add it
	 * along with the coordinates of the tile to the ArrayList.
	 * */
    protected void setUpThugs()
    {
    	for(int y=0; y<tmap.getMapHeight(); y++)
    		for(int x=0; x<tmap.getMapWidth(); x++)
    			if(tmap.getTileChar(x, y)=='a')
    			{
    				float pixelX = x*tmap.getTileWidth();
    				float pixelY = y*tmap.getTileHeight();
    				Thug th = new Thug("thug");
    				//add thug to array list
    				Pair<Float, Float> location = new Pair<Float,Float>(pixelX,pixelY);
    				Pair<Thug,Pair<Float,Float>> p = new Pair<Thug ,Pair<Float,Float>>(th,location);
    				thugSpawnPositions.add(p);
    				//set the x,y for each thug
    				th.setX(pixelX);
    				th.setY(pixelY);
    				//set tile according to the one above it
    				tmap.setTileChar(tmap.getTileChar(x, y-1), x, y);
    			}
    }
	/**
	 * Scan through the tile map and replace every occurrence of 'c'
	 * with an according tile char. Instantiate a new Crate and add it
	 * along with the coordinates of the tile to the ArrayList.
	 * */
    protected void setUpCrates()
    {
    	for(int y=0; y<tmap.getMapHeight(); y++)
    		for(int x=0; x<tmap.getMapWidth(); x++)
    			if(tmap.getTileChar(x, y)=='c')
    			{
    				float pixelX = x*tmap.getTileWidth();
    				float pixelY = y*tmap.getTileHeight();
    				//add spawn position to the array list
    		    	Crate c = new Crate("crate");

    		    	Pair<Float,Float> location = new Pair<Float,Float>(pixelX,pixelY);
    		      	Pair<Crate, Pair<Float,Float>> p = new Pair<Crate, Pair<Float,Float>>(c,location);
    		      	crateSpawnPositions.add(p);
    		      	//set x,y for the crate
    	    		c.setX(pixelX);
    	    		c.setY(pixelY);
    	    		//set tile according to the one above it
    		      	tmap.setTileChar(tmap.getTileChar(x, y-1), x, y);
    			}
    }
	/**
	 * Scan through the tile map and replace every occurrence of 'c'
	 * with an according tile char. This will reset all of the crates
	 * that have been hit and have been dropped down.
	 * */
    private void resetCratesOnTileMap()
    {
    	for(int y=0; y<tmap.getMapHeight(); y++)
    		for(int x=0; x<tmap.getMapWidth(); x++)
    			if(tmap.getTileChar(x, y)=='c') //find the tile with 'c' -> reset it
    				tmap.setTileChar(tmap.getTileChar(x, y-1), x, y);
    }
}