import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import game2D.*;
public class GrappleHook extends Sprite
{
	private Game gct;
	private boolean grappleHookRetracting = false;
	private float HOOKLIMIT;
	private Animation grapple;
	
	public GrappleHook( float hookLimit, Game gct)
	{
		super();
		HOOKLIMIT = hookLimit;
		this.gct = gct;
		loadAnim();
	}
	public void update(long elapsed, Player player,  Level lvl)
	{
		for(int i=0; i<lvl.getCrateSpawnPositions().size(); i++)
		{
			Crate crate = lvl.getCrateSpawnPositions().get(i).getFirst();
	    	if(gct.boundingBoxCollision(this, crate))
	    	{
	    		retractGrappleHook(player);
	    		if(!gct.checkRightSideForCollision(crate))
	    		{//if next to a tile, then crate already fallen
	    			crate.setHit();
	    			crate.setHitX(crate.getX());
	    		}
	    	}
		}
				
		checkForCollision(player);
    	if((this.getX()>player.getX()+player.getWidth()+HOOKLIMIT)
				|| (this.getX()<player.getX()-HOOKLIMIT)
				|| (this.getY()>player.getY()+player.getHeight()/2+HOOKLIMIT)
				|| (this.getY()<player.getY()-HOOKLIMIT))
			retractGrappleHook(player);
    	
		if(grappleHookRetracting)
		{
			if(gct.boundingBoxCollision(player, this))
			{
				this.setVelocityX(.0f);
				this.setVelocityY(.0f);
				this.hide();
				grappleHookRetracting=false;
			}
		}
		update(elapsed);
	}
	
	  /**
     * Simulate the effect that the grapple hook retracts back into the grapple gun.
     */
  	public void retractGrappleHook(Player player)
    {
  		Velocity v = null;
  		if(player.isLookingRight())
			v = new Velocity(0.5f, this.getX(), this.getY(), player.getX()+player.getWidth(), player.getY()+20);
		else
			v = new Velocity(0.5f,  this.getX(), this.getY(),player.getX(), player.getY()+player.getHeight()/2);
  		
  		this.setVelocityX((float)v.getdx());
  		this.setVelocityY((float)v.getdy());
		grappleHookRetracting = true;
    }
  	/**
  	 * Draws the Grapple Hook and a line behind.
  	 */
  	public void drawGrappleHook(Player player, Graphics2D g)
  	{
        if(this.isVisible())
        {
            this.setRotation(this.getRotation());
            this.drawTransformed(g);
            this.setOffsets(gct.getXOffset(),gct.getYOffset());
        	g.setColor(Color.black);
        	g.setStroke(new BasicStroke(3));
        	//TODO adjust the line so it follows the hook properly when rotated
        	if(player.isLookingRight())
        		g.drawLine(	(int)player.getX()+(int)player.getWidth()+gct.getXOffset(),
        					(int)player.getY()+26+gct.getYOffset(),
        					(int)this.getX()+gct.getXOffset(),
        					(int)this.getY()+(int)(this.getHeight()/2)+gct.getYOffset());
        	else
            	g.drawLine(	(int)player.getX()+gct.getXOffset(),
        					(int)player.getY()+26+gct.getYOffset(),
        					(int)this.getX()+gct.getXOffset(),
        					(int)this.getY()+(int)(this.getHeight()/2)+gct.getYOffset());
        	//reset stroke
        	g.setStroke(new BasicStroke(0));
  	      }
  	}
  	public void shoot(Player player, float mouseX, float mouseY)
  	{
  		if(!this.isVisible() && !player.isCrouching())
		{
			Velocity v;
			if(player.isLookingRight())
			{
				this.setX(player.getX()+player.getWidth());
				this.setY(player.getY()+20);
			}
			else
			{
				this.setX(player.getX());
				this.setY(player.getY()+20);
			}
			v = new Velocity(0.5f, this.getX()+gct.getXOffset(), this.getY()+gct.getYOffset(), mouseX+10, mouseY+10);
			this.setVelocityX((float)v.getdx());
			this.setVelocityY((float)v.getdy());
			this.setRotation(v.getAngle());
			this.show();
		}
  	}
  	
  	/**
  	 * Check for collision against the tile maps on the Left and Right side of the grappleHook.
  	 */
  	private void checkForCollision(Player player)
  	{
    	//check for right/left collision depending on which way the hook is going
    	if(this.getVelocityX()>0)
    	{
    		if(gct.checkRightSideForCollision(this))
    			retractGrappleHook(player);
    	}
    	else if(gct.checkLeftSideForCollision(this))
    			retractGrappleHook(player);
  	}
  	private void loadAnim()
  	{
        grapple = new Animation();
        grapple.addFrame(gct.loadImage("assets/images/Projectiles/GrappleHook.png"), 60);
        setAnimation(grapple);
  	}
}
