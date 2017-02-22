import game2D.*;

public class Crate extends Sprite
{
	private Game gct;
	private boolean crateHit = false;
	private float crateHitX;
	private Animation crateAnim;
	
	public Crate(Game gct)
	{
		super();
		this.gct = gct;
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
	    		//adding 5 to both X and Y to make sure we're in the correct tile 
	    		this.hide();
	    	}
        }
	}
	public void reset(float x, float y)
	{
		setX(x);
		setY(y);
		crateHit=false; 
	}
	/**
	 * Load the necessary assets for the sprite to work.
	 * Also sets default animation.
	 * */
    private void loadAssets()
    {
        crateAnim = new Animation();
        crateAnim.addFrame(gct.loadImage("assets/maps/crate.png"), 60);
        setAnimation(crateAnim);
    }
}