import game2D.*;

public class Thug extends SpriteExtension
{
	private boolean killed = false;
	private float gravity = 0.01f;
	private final float PATROLSPEED = .04f;
	private boolean walkingRight = false;

	public Thug(String tag)
	{
		super();
		this.tag = tag;
		projectile = new SpriteExtension("projectile");
		loadAssets();
	}
	public SpriteExtension getProjectile() { return projectile; }
	public boolean isKilled() { return killed; }
	
	
	public void update(long elapsed, Player player, TileMap tmap)
	{
		collider = new Collision(tmap);
		if (this.getY() + this.getHeight() > tmap.getPixelHeight())
			this.kill();
		
		if(!collider.checkBottomSideForCollision(this))
		{
			this.setVelocityY(.5f);
    		this.setVelocityY(this.getVelocityY()+(gravity*elapsed)); // gravity adjustment
		}
		else
		{
			if(player.getX()>this.getX() && !projectile.isVisible())
				this.setAnimation(standRight);
			else if(player.getX()<this.getX() && !projectile.isVisible())
				this.setAnimation(standLeft);
			this.setVelocityY(.0f);
			collider.recoverSpriteStuckInBottomTile(this);
    		if(Math.random()>0.6)
    			if(isPlayerClose(player))	
    					shootHorizontal(player);
		}
		this.update(elapsed);
		//if(gct.checkBottomSideForCollision(this))
			patrol(player, tmap); // patrol the rooftops
	}
	/**
	 * Reset the position to the thug's original position
	 * and set the killed flag to false.
	 **/
	public void reset(float x, float y) 
	{
		setX(x);
		setY(y);
		killed=false; 
	}
	/**
	 * Sets the killed flag to true and hides the sprite.
	 **/
	public void kill()
	{
		if(!killed)
		{
			killed = true;
			this.hide(); //TODO Replace with a new animation.
		}
	}
	/**
	 * Handles the shooting animation and action.
	 */
	private void shootHorizontal(Player player)
	{
		if(!player.isGameOver())//stop shooting after game is over
		{
	    	if(!projectile.isVisible() || projectile.getX()+screenWidth<this.getX() || projectile.getX()-screenWidth>this.getX())
	    	{
	    		projectile.setScale(0.8f);
				Velocity v;
				if(player.getX()>this.getX())
				{
					this.setAnimation(fireRight);
					projectile.setX(this.getX()+this.getWidth());
					projectile.setY(this.getY()+15);
					v = new Velocity(0.7f,projectile.getX(),projectile.getY(),this.getX()+50,this.getY()+26);
					projectile.setRotation(180);
				}
				else
				{
					this.setAnimation(fireLeft);
					projectile.setX(this.getX());
					projectile.setY(this.getY()+15);
					v = new Velocity(0.7f,projectile.getX(),projectile.getY(),this.getX()-50,this.getY()+26);
					projectile.setRotation(0);
				}
				projectile.setVelocityY(.0f);
				projectile.setVelocityX((float)v.getdx());
				
				playShootSound();
				projectile.show();
	    	}
		}
	}
	private void patrol(Player player, TileMap tmap)//FIXME
	{
		if(!isPlayerClose(player))
		{
			if(walkingRight)
			{
				if(!modifiedThugCheckBottomSideForCollision(tmap) || collider.checkRightSideForCollision(this))
				{
					this.setVelocityX(.0f);
					walkingRight=false;
				}
				else
				{
					this.setAnimation(standRight);
					this.setVelocityX(PATROLSPEED);
				}
			}
			else
			{
				if(!modifiedThugCheckBottomSideForCollision(tmap) || collider.checkLeftSideForCollision(this))
				{	
					this.setVelocityX(.0f);
					walkingRight=true;
				}
				else
				{
					this.setAnimation(standLeft);
					this.setVelocityX(-PATROLSPEED);
				}
			}
		}
		else
			this.setVelocityX(.0f);
	}
	private boolean modifiedThugCheckBottomSideForCollision(TileMap tmap)
	{
		boolean hit = false;
		char tileCharBottom;
		
		if(walkingRight)
			tileCharBottom = tmap.getTileChar(((int)this.getX()+this.getWidth()+this.getWidth()/2)/tmap.getTileWidth(), (int)(this.getY()+this.getHeight())/tmap.getTileHeight());
		else
			tileCharBottom = tmap.getTileChar(((int)this.getX()-this.getWidth()/2)/tmap.getTileWidth(), (int)(this.getY()+this.getHeight())/tmap.getTileHeight());
		if(tileCharBottom == 'r')
			hit = true;
		return hit;
	}
	private boolean isPlayerClose(Player player)
	{
		return ((player.getX()+screenWidth>this.getX() || this.getX()<player.getX()-screenWidth)
				&&(player.getY()+player.getHeight()+50>this.getY() || player.getY()-player.getHeight()+20>this.getY())); 
		// check to see if player is close or not on the X axis
		//then on the Y axis
	}
}