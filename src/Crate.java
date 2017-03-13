import game2D.*;

public class Crate extends SpriteExtension
{
	private boolean crateHit = false;
	private float crateHitX;
	
	public Crate(String tag)
	{
		super();
		this.tag = tag;
		loadAssets();
	}
	public void setHit() { crateHit = true; }
	public void setHitX(float x) { crateHitX = x;}
	public boolean isHit() { return crateHit;}
	public float getHitX() { return crateHitX;}
	/**
	 * Update the crate if it has been hit by the Grapple Hook.
	 * Rotates and moves the crate until it is in the needed position.
	 * Replaces the crate with a 'c' tile char in the tile map.
	 */
	public void update(float elapsed, Player player, TileMap tmap)
	{
		collider = new Collision(tmap);
        if(crateHit)
        {
			if(this.getRotation()>-90)
				this.setRotation(this.getRotation()-2.0);
	    	if(this.getX()>getHitX()-32)
	    		this.setX(this.getX()-2);
	    	else if(!collider.checkBottomSideForCollision(this))
	    		this.setY(this.getY()+2);
	    	else
	    	{
	    		crateHit=false;
	    		tmap.setTileChar('c', ((int)this.getX()+5)/tmap.getTileWidth(), ((int)this.getY()+5)/tmap.getTileHeight());
	    		//adding 5 to both X and Y to make sure we're in the correct tile 
	    		this.hide();
	    	}
        }
	}
	/**
	 * Reset the position of the Crate and its' hit flag.
	 * */
	public void reset(float x, float y)
	{
		setX(x);
		setY(y);
		crateHit=false; 
	}
}