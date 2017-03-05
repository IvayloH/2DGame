import game2D.*;

public class GrappleHook extends SpriteExtension
{
	private boolean grappleHookRetracting = false;
	private float HOOKLIMIT;
	
	public GrappleHook(float hookLimit, String tag)
	{
		super();
		HOOKLIMIT = hookLimit;
		this.tag = tag;
		loadAssets();
	}
	public void setGrappleHookRetracting(boolean retract) { grappleHookRetracting = retract;}
	public boolean isGrappleHookRetracting() { return grappleHookRetracting; }
	public float getHookLimit() { return HOOKLIMIT; }
	  /**
     * Simulate the effect that the grapple hook retracts back into the grapple gun.
     */
  	public void retractGrappleHook(Player player)
    {
  		Velocity v = null;
  		if(player.isFacingRight())
			v = new Velocity(0.5f, this.getX(), this.getY(), player.getX()+player.getWidth(), player.getY()+20);
		else
			v = new Velocity(0.5f,  this.getX(), this.getY(),player.getX(), player.getY()+player.getHeight()/2);
  		
  		this.setVelocityX((float)v.getdx());
  		this.setVelocityY((float)v.getdy());
		grappleHookRetracting = true;
    }
}
