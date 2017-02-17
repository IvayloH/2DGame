import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import game2D.*;
public class GrappleHook extends Sprite
{
	Game gct;
	boolean grappleHookRetracting = false;
	float HOOKLIMIT;
	
	public GrappleHook(Animation anim, float limit, Game gct)
	{
		super(anim);
		HOOKLIMIT = limit;
		this.gct = gct;
	}
	public void update(Player player, Crate crate)
	{
		checkForCollision(player);
    	if((this.getX()>player.getX()+player.getWidth()+HOOKLIMIT)
				|| (this.getX()<player.getX()-HOOKLIMIT)
				|| (this.getY()>player.getY()+player.getHeight()/2+HOOKLIMIT)
				|| (this.getY()<player.getY()-HOOKLIMIT))
			retractGrappleHook(player);

    	if(gct.boundingBoxCollision(this, crate))
    	{
    		retractGrappleHook(player);
    		if(!gct.checkRightSideForCollision(crate))
    		{//if next to a tile, then crate already fallen
    			crate.setHit();
    			crate.setHitX(crate.getX());
    		}
    	}
    	
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
}
