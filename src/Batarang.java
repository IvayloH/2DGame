import java.awt.Graphics2D;

import game2D.*;

public class Batarang extends Sprite
{
	Game gct;
	Animation batarangAnim;
	Sound throwing;
	public Batarang(Game gct)
	{
		super();
		this.gct = gct;
		loadAnim();
	}
	
	public void draw(Graphics2D g)
	{
		setOffsets(gct.getXOffset(), gct.getYOffset());
		drawTransformed(g);
	}
	
	public void update(long elapsed, Thug enemy, Sprite player, Crate crate, Boss boss)
	{
		if(gct.boundingBoxCollision(this,enemy))
			enemy.kill();
		if(gct.boundingBoxCollision(this,crate))
			this.hide();
		if(gct.boundingBoxCollision(this,boss))
		{
			boss.takeDamage();
			this.hide();
		}
		if(gct.checkBottomSideForCollision(this) || gct.checkRightSideForCollision(this) || gct.checkLeftSideForCollision(this))
		{
			this.hide();
		}
		if(this.getX()>player.getX()+gct.getWidth() || this.getX()<player.getX()-gct.getWidth())
			this.hide();
		update(elapsed);
	}
	
	public void Throw(Player player, float mouseX, float mouseY)
	{
		if(!this.isVisible() && !player.isCrouching())
		{
			Velocity v;
			if(player.isLookingRight())
			{
				this.setX(player.getX()+player.getWidth());
				this.setY(player.getY()+26);
			}
			else
			{
				this.setX(player.getX());
				this.setY(player.getY()+26);	
			}
			v = new Velocity(.5f,this.getX()+gct.getXOffset(),this.getY()+gct.getYOffset(), mouseX+10, mouseY+10);
			this.setVelocityX((float)v.getdx());
			this.setVelocityY((float)v.getdy());
			throwing = new Sound("assets/sounds/grunt_throw.wav");
			throwing.start();
			this.show();
		}
	}
	
	private void loadAnim()
	{
        batarangAnim = new Animation();
        batarangAnim.addFrame(gct.loadImage("assets/images/Projectiles/thugProjectile.png"), 60);
        setAnimation(batarangAnim);
	}
}