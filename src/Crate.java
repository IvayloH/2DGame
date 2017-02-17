import java.awt.Graphics2D;
import java.util.ArrayList;

import game2D.*;

public class Crate extends Sprite
{
	Game gct;
	ArrayList<Position<Float,Float>> crateSpawnPositions = new ArrayList<Position<Float,Float>>();
	int currCrate=0;
	boolean crateHit = false;
	float crateHitX;
	
	public Crate(Animation anim, Game gct)
	{
		super(anim);
		this.gct = gct;
	}
	public void setHit() { crateHit = true; }
	public void setHitX(float x) { crateHitX = x;}
	public boolean isHit() { return crateHit;}
	public float getHitX() { return crateHitX;}
	
	
	public void update(float elapsed, Player player, TileMap tmap)
	{

        if(crateHit)
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
	}
	/**
	 * Draws the crate at the designated spawn location when the player gets close enough.
	 */
	public void drawCrate(Player player, Graphics2D g)
	{
        this.drawTransformed(g);
        this.setOffsets(gct.getXOffset(), gct.getYOffset());
        //setup next spawn position
        if(currCrate<crateSpawnPositions.size() && !crateHit)
        {
	        Position<Float, Float> p = crateSpawnPositions.get(currCrate);
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
    public void addCrateSpawnPoint(float x, float y)
    {
      	Position<Float,Float> p = new Position<Float,Float>(x,y);
      	crateSpawnPositions.add(p);
    }
    
    public void reset()
    {
    	currCrate = 0;
    	crateHit=false;
    }
}
