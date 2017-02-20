import java.awt.Graphics2D;
import java.util.ArrayList;

import game2D.*;
public class Thug extends Sprite
{
	ArrayList<Position<Float,Float>> thugSpawnPositions = new ArrayList<Position<Float,Float>>();
	int currThug=0;
	boolean killed = true;
	Game gct;
    float gravity = 0.01f;
	EnemyProjectile projectile;
	Animation animLeft;
	Animation animRight;
	Animation shootLeft;
	Animation shootRight;
	Sound shooting = null;
	
	public Thug(EnemyProjectile projectile ,Game gct)
	{
		super();
		this.gct = gct;
		this.projectile = projectile;
		loadAssets();
	}
	public void reset()
	{
		currThug = 0;
		killed=true;
	}
	public void drawThugAtNextPosition(Player player, Graphics2D g)
	{
        this.drawTransformed(g);
        this.setOffsets(gct.getXOffset(), gct.getYOffset());

		if(currThug<thugSpawnPositions.size() && killed)
		{
			Position<Float, Float> p = thugSpawnPositions.get(currThug);
			if(player.getX()+gct.getWidth()>p.getX())
		    {
				this.setX(p.getX());
				this.setY(p.getY());	            
				this.show();
				killed=false;
		    }
		}
	}
	public void update(long elapsed, Player player)
	{
		if(!gct.checkBottomSideForCollision(this))
		{
			this.setVelocityY(.5f);
    		this.setVelocityY(this.getVelocityY()+(gravity*elapsed)); // gravity adjustment
		}
		else
		{
			if(player.getX()>this.getX() && !projectile.isVisible())
				this.setAnimation(animRight);
			else if(player.getX()<this.getX() && !projectile.isVisible())
				this.setAnimation(animLeft);
			this.setVelocityY(.0f);
			gct.recoverSpriteStuckInBottomTile(this);
    		if(Math.random()>0.6)
    			if(player.getX()+gct.getWidth()/2>this.getX() || this.getX()<player.getX()-gct.getWidth()/2) // check to see if player is close or not
    				if(player.getY()+player.getHeight()-20>this.getY() || player.getY()-player.getHeight()+20>this.getY()) //player is near the same height
    						Shoot(player);

    		if(player.getX()>this.getX()+gct.getWidth()/2)
    		{
    			this.kill();
    		}
		}
		this.update(elapsed);
	}
	public void addThugSpawnPoint(float x, float y)
	{
		Position<Float,Float> p = new Position<Float,Float>(x,y);
		thugSpawnPositions.add(p);
	}
	public void kill()
	{
		if(!killed)
		{
			currThug++;
			killed = true;
			this.hide();
		}
	}
	/**
	 * Handles the shooting animation and action.
	 */
	private void Shoot(Player player)
	{
		if(!player.isGameOver())//stop shooting after game is over
		{
	    	if(!projectile.isVisible() || projectile.getX()+gct.getWidth()<this.getX() || projectile.getX()-gct.getWidth()>this.getX())
	    	{
	    		projectile.setScale(0.8f);
				Velocity v;
				if(player.getX()>this.getX())
				{
					this.setAnimation(shootRight);
					projectile.setX(this.getX()+this.getWidth());
					projectile.setY(this.getY()+15);
					v = new Velocity(0.7f,projectile.getX()+gct.getXOffset(),projectile.getY()+gct.getYOffset(),this.getX()+gct.getXOffset()+50,this.getY()+26+gct.getYOffset());
					projectile.setRotation(180);
				}
				else
				{
					this.setAnimation(shootLeft);
					projectile.setX(this.getX());
					projectile.setY(this.getY()+15);
					v = new Velocity(0.7f,projectile.getX()+gct.getXOffset(),projectile.getY()+gct.getYOffset(),this.getX()+gct.getXOffset()-50,this.getY()+26+gct.getYOffset());
					projectile.setRotation(0);
				}
				projectile.setVelocityY(.0f);
				projectile.setVelocityX((float)v.getdx());
				
		        shooting = new Sound("assets/sounds/shoot.wav");
				shooting.start();
				
				projectile.show();
	    	}
		}
	}
	
	private void loadAssets()
	{
        animLeft = new Animation();
        animLeft.addFrame(gct.loadImage("assets/images/Enemies/Thug/thug_sl.gif"), 60);
        
        //set starting animation
        setAnimation(animLeft);
        
        animRight = new Animation();
        animRight.addFrame(gct.loadImage("assets/images/Enemies/Thug/thug_sr.gif"), 60);

        shootRight = new Animation();
        shootRight.addFrame(gct.loadImage("assets/images/Enemies/Thug/thug_fire_right.gif"), 60);

        shootLeft = new Animation();
        shootLeft.addFrame(gct.loadImage("assets/images/Enemies/Thug/thug_fire_left.gif"), 60);
	}
}
