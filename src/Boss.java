import java.awt.Color;
import java.awt.Graphics2D;

import game2D.*;
public class Boss extends Sprite
{
	Game gct;
	private float gravity = 0.01f;
	private int HP;
	private int maxHP=10;
	private boolean invulnerable = false;
	private long invulnerableTime = 0;
	private boolean dead = false;
	private Animation standLeft;
	private Animation standRight;
	private Animation shootLeft;
	private Animation shootRight;
	private EnemyProjectile projectile;
	
	public Boss(EnemyProjectile p, Game gct)
	{
		super();
		this.gct = gct;
		loadAnims();
		projectile = p;
		HP=maxHP;
	}
	public void setSpawn(float x, float y)
	{
		setX(x);
		setY(y);
	}
	private void loadAnims() 
	{
        standLeft = new Animation();
        standLeft.addFrame(gct.loadImage("assets/images/Enemies/Thug/thug_sl.gif"), 60);
        
        //set starting animation
        setAnimation(standLeft);
        
        standRight = new Animation();
        standRight.addFrame(gct.loadImage("assets/images/Enemies/Thug/thug_sr.gif"), 60);

        shootRight = new Animation();
        shootRight.addFrame(gct.loadImage("assets/images/Enemies/Thug/thug_fire_right.gif"), 60);

        shootLeft = new Animation();
        shootLeft.addFrame(gct.loadImage("assets/images/Enemies/Thug/thug_fire_left.gif"), 60);
	}

	public void update(long elapsed, Player player)
	{	
		if(HP<1)
		{
			hide();
			dead=true;
		}
		else
		{
			if(!gct.checkBottomSideForCollision(this))
			{
				this.setVelocityY(.5f);
				this.setVelocityY(this.getVelocityY()+(gravity*elapsed)); // gravity adjustment
			}
			else
			{
				if(invulnerable)
				{
					if(invulnerableTime>1000)
						invulnerable = false;
					else
						invulnerableTime+=elapsed;
				}
				this.setVelocityY(.0f);
				if(player.getX()>this.getX() && !projectile.isVisible())
					this.setAnimation(standRight);
				else if(player.getX()<this.getX() && !projectile.isVisible())
					this.setAnimation(standLeft);
				gct.recoverSpriteStuckInBottomTile(this);
				if(Math.random()>0.3)
					if(player.getX()+gct.getWidth()>this.getX() || this.getX()<player.getX()-gct.getWidth()) // check to see if player is close or not
	    				if(player.getY()+player.getHeight()-20>this.getY() || player.getY()-player.getHeight()+20>this.getY()) //player is near the same height
	    					shoot(player);
			}
		}
		update(elapsed);
	}
	
	public void draw(Graphics2D g)
	{
		setOffsets(gct.getXOffset(), gct.getYOffset());
		drawTransformed(g);
		show();
        g.setColor(Color.green);
        int i=0, j=gct.getWidth()-95;
        for(; i<HP; i++,j+=8)
        {
        	g.fillRoundRect(j,40, 5, 25, 6, 6);
        }
        for(i=0, j=gct.getWidth()-95; i<maxHP; i++, j+=8)
        {
        	g.drawString("--", j, 40); //top line
        	g.drawString("--", j, 72); //bottom line
        }
        g.drawLine(j, 36, j, 68); // side line
	}
	
	private void shoot(Player player)
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
					//shoot directly at the player
					v = new Velocity(0.7f, projectile.getX()+gct.getXOffset(),projectile.getY()+gct.getYOffset(),
							player.getX()+player.getWidth()/2+gct.getXOffset(),player.getY()+player.getHeight()/2+gct.getYOffset());
					projectile.setRotation(180);
				}
				else
				{
					this.setAnimation(shootLeft);
					projectile.setX(this.getX());
					projectile.setY(this.getY()+15);
					v = new Velocity(0.7f, projectile.getX()+gct.getXOffset(),projectile.getY()+gct.getYOffset(),
							player.getX()+player.getWidth()/2+gct.getXOffset(),player.getY()+player.getHeight()/2+gct.getYOffset());
					projectile.setRotation(0);
				}
	
				projectile.setVelocityX((float)v.getdx());
				projectile.setVelocityY((float)v.getdy());
				projectile.setRotation(v.getAngle());
				projectile.show();
	    	}
		}
	}
	
	public void takeDamage()
	{
		if(!invulnerable) 
			HP--;
	}
	public boolean isDead()
	{
		return dead;
	}
	public void reset()
	{
		HP = maxHP;
	}
}
