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
		loadAnim();
	}
	public void setHit() { crateHit = true; }
	public void setHitX(float x) { crateHitX = x;}
	public boolean isHit() { return crateHit;}
	public float getHitX() { return crateHitX;}
	
    public void reset()
    {
    	crateHit=false;
    }
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
	    	}
        }
	}

    private void loadAnim()
    {
        crateAnim = new Animation();
        crateAnim.addFrame(gct.loadImage("assets/maps/crate.png"), 60);
        setAnimation(crateAnim);
    }
}
