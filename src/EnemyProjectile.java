import java.awt.Graphics2D;

import game2D.*;

public class EnemyProjectile extends Sprite
{
	Game gct;
	Animation projectile;
	public EnemyProjectile(Game gct) 
	{
		super();
		this.gct = gct;
		loadAnim();
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
	private void loadAnim()
	{
        projectile = new Animation();
        projectile.addFrame(gct.loadImage("assets/images/Projectiles/thugProjectile.png"), 60);
        setAnimation(projectile);
	}
}
