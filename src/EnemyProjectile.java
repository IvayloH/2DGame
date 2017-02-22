import java.awt.Graphics2D;

import game2D.*;

public class EnemyProjectile extends Sprite
{
	private Game gct;
	private Animation projectile;
	public EnemyProjectile(Game gct) 
	{
		super();
		this.gct = gct;
		loadAssets();
	}
	public void updateProjectile(long elapsed)
	{
		//handle TileMap collisions
		if(this.isVisible())
        {
        	if(gct.checkLeftSideForCollision(this))
        		this.hide();
        	if(gct.checkRightSideForCollision(this))
        		this.hide();
        }
		this.update(elapsed);
	}
	public void draw(Graphics2D g)
	{
		setOffsets(gct.getXOffset(),gct.getYOffset());
		drawTransformed(g);
	}
	/**
	 * Load the necessary assets for the sprite to work.
	 * Also sets default animation.
	 * */
	private void loadAssets()
	{
        projectile = new Animation();
        projectile.addFrame(gct.loadImage("assets/images/Projectiles/thugProjectile.png"), 60);
        setAnimation(projectile);
	}
}
