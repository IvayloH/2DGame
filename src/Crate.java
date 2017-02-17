import java.awt.Graphics2D;
import java.util.ArrayList;

import game2D.*;

public class Crate extends Sprite
{
	ArrayList<SpawnPosition<Float,Float>> crateSpawnPositions = new ArrayList<SpawnPosition<Float,Float>>();
	int currCrate=0;
	boolean crateHit = false;
	float crateHitX;
	
	public Crate(Animation anim)
	{
		super(anim);
	}
	public void setHit() { crateHit = true; }
	public void setHitX(float x) { crateHitX = x;}
	public boolean isHit() { return crateHit;}
	public float getHitX() { return crateHitX;}
	
	
	public void update(float elapsed, TileMap tmap, Game gct)
	{
		if(this.getRotation()>-90)
			this.setRotation(this.getRotation()-2.0);
    	if(this.getX()>getHitX()-32)
    		this.setX(this.getX()-2);
    	else if(!gct.checkBottomSideForCollision(this))
    		this.setY(this.getY()+2);
    	else
    	{
    		crateHit=false;
    		tmap.setTileChar('c', ((int)this.getX()+5)/tmap.getTileWidth(), ((int)this.getY()+5)/tmap.getTileHeight());
    		this.hide();
    		currCrate++;
    		this.setRotation(0);
    	}
	}
	/**
	 * Draws the crate at the designated spawn location when the player gets close enough.
	 */
	public void drawCrate(Player player, Game gct, Graphics2D g)
	{
        this.drawTransformed(g);
        this.setOffsets(gct.getXOffset(), gct.getYOffset());
        //setup next spawn position
        if(currCrate<crateSpawnPositions.size() && !crateHit)
        {
	        SpawnPosition<Float, Float> p = crateSpawnPositions.get(currCrate);
	        if(player.getX()+gct.getWidth()>p.getX())
	        {
	        	this.setX(p.getX());
	            this.setY(p.getY());	            
	            this.show();
	        }
        }
	}
  	/**
  	 * Create and add all the (x,y) locations to spawn a crate there
       */
    public void initialiseCrateSpawnPoints()
    {
      	SpawnPosition<Float,Float> p = new SpawnPosition<Float,Float>(704.f,160.f);
      	crateSpawnPositions.add(p);
      	p = new SpawnPosition<Float,Float>(1408.f,160.f);
      	crateSpawnPositions.add(p);
    }
}
