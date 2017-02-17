import java.awt.Graphics2D;

import game2D.*;

public class EnemyProjectile extends Sprite
{
	Game gct;
	public EnemyProjectile(Animation anim, Game gct) 
	{
		super(anim);
		this.gct = gct;
	}
	public void handleTileMapCollision()
	{
		if(this.isVisible())
        {
        	if(gct.checkLeftSideForCollision(this))
        		this.hide();
        	if(gct.checkRightSideForCollision(this))
        		this.hide();
        }
	}
	public void draw(Graphics2D g)
	{
		setOffsets(gct.getXOffset(),gct.getYOffset());
		drawTransformed(g);
	}
}
