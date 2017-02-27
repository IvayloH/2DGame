import java.awt.Graphics2D;
import java.util.ArrayList;

import game2D.TileMap;

public abstract class Level
{
	protected ArrayList<Pair<Crate,Pair<Float,Float>>> crateSpawnPositions;
	protected ArrayList<Pair<Thug,Pair<Float,Float>>> thugSpawnPositions;
	
	protected Player player;
	protected Boss boss;
	protected TileMap tmap = null;
	protected Game gct;
	protected EnemyProjectile projectile;
	
	public Level(Player player, Boss boss, TileMap tmap,Game gct)
	{
		crateSpawnPositions = new ArrayList<Pair<Crate,Pair<Float,Float>>>();
		thugSpawnPositions = new ArrayList<Pair<Thug,Pair<Float,Float>>>();
		this.player = player;
		this.tmap = tmap;
		this.boss = boss;
		this.gct = gct;	
	}
	
	public ArrayList<Pair<Crate,Pair<Float,Float>>> getCrateSpawnPositions() { return crateSpawnPositions; }
	public ArrayList<Pair<Thug,Pair<Float,Float>>> getThugSpawnPositions() { return thugSpawnPositions; }
	
	/**
	 * Handles calling all other methods needed to set up the level. (empty)
	 * */
	void setUpLevel() { }
	
	/**
	 * Update the crates/boss/thugs and their projectiles if visible.
	 * */
	public void update(long elapsed)
	{    
		int i=0;
		for(i=0; i<crateSpawnPositions.size(); i++)
        {
			if(crateSpawnPositions.get(i).getFirst().isHit())
				crateSpawnPositions.get(i).getFirst().update(elapsed, player, tmap);
        }
		for(i=0; i<thugSpawnPositions.size(); i++)
		{
			Thug th = thugSpawnPositions.get(i).getFirst();
			if(th.isVisible())
				th.update(elapsed, player, tmap);
			if(th.getProjectile().isVisible())
	    	{
	    		if(gct.boundingBoxCollision(th.getProjectile(),player) && !player.isInvincible())
	    			player.takeDamage();
	    		if(th.getProjectile().isVisible())
	    			th.getProjectile().updateProjectile(elapsed);
	    	}
		}
		//boss bullet collision
		if(gct.boundingBoxCollision(boss.getProjectile(),player) && !player.isInvincible())
			player.takeDamage();
	}
	/**
	 * Draw each crate/thug by going through their ArrayLists accordingly.
	 * */
	public void draw(Graphics2D g)
	{	//draw crates
		for(int i=0; i<crateSpawnPositions.size(); i++)
        {
        	Crate c =  crateSpawnPositions.get(i).getFirst();
    		c.drawTransformed(g);
    	    c.setOffsets(gct.getXOffset(), gct.getYOffset());
        	Pair<Float,Float> crateLocation = crateSpawnPositions.get(i).getSecond();
        	if(player.getX()+gct.getWidth()>crateLocation.getFirst() && !c.isHit())
        		c.show();
        }
		//draw thugs
		for(int i=0; i<thugSpawnPositions.size(); i++)
		{
			Thug th = thugSpawnPositions.get(i).getFirst();
			th.drawTransformed(g);
			th.setOffsets(gct.getXOffset(), gct.getYOffset());
			Pair<Float,Float> thugLocation = thugSpawnPositions.get(i).getSecond();
			if(player.getX()+gct.getWidth()>thugLocation.getFirst() && !th.isKilled())
				th.show();
			if(th.getProjectile().isVisible())
				th.getProjectile().draw(g);
		}
	}
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
    				Thug th = new Thug(gct);
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
    		    	Crate c = new Crate(gct);

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