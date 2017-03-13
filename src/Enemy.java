import game2D.*;

public class Enemy extends SpriteExtension
{
	private final float PATROLSPEED = .04f;
	private boolean walkingRight = false;
	
	public Enemy(String tag)
	{
		super();
		this.tag = tag;
		projectile = new SpriteExtension("projectile");
		loadAssets();
	}
	
	public SpriteExtension getProjectile() { return projectile; }
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
	 * @param elapsed Time that has elapsed.
	 * @param player The player sprite.
	 * @param tmap   The current TileMap, for collision checks.
	 * @param aimAtPlayer If true, thugs will aim at the player when shooting.
	 */
	public void update(long elapsed, Player player, TileMap tmap, boolean aimAtPlayer)
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
    					shoot(player, aimAtPlayer);
		}
		this.update(elapsed);
		if(tag.equals("thug"))
			if(collider.checkBottomSideForCollision(this))
				patrol(player, tmap); // patrol the rooftops
	}
	/**
	 * Handles the shooting animation and action.
	 */
	protected void shoot(Player player, boolean aimAtPlayer)
	{
		if(!player.isKilled())//stop shooting after game is over
		{
	    	if(!projectile.isVisible() || projectile.getX()+screenWidth<this.getX() || projectile.getX()-screenWidth>this.getX())
	    	{
				Velocity v;
				projectile.setVelocityY(.0f);
				if(player.getX()>this.getX())
				{
					this.setAnimation(fireRight);
					projectile.setX(this.getX()+this.getWidth());
					projectile.setY(this.getY()+15);
					if(aimAtPlayer)
					{
						v = new Velocity(0.7f, projectile.getX(), projectile.getY(), 
								player.getX()+player.getWidth()/2, player.getY()+player.getHeight()/2);
						projectile.setRotation(v.getAngle());
						projectile.setVelocityY((float)v.getdy());
					}
					else
					{
						v = new Velocity(0.7f,projectile.getX(),projectile.getY(),this.getX()+this.getWidth()+5,this.getY()+26);
						projectile.setRotation(180);
					}
				}
				else
				{
					this.setAnimation(fireLeft);
					projectile.setX(this.getX());
					projectile.setY(this.getY()+15);
					if(aimAtPlayer)
					{
						v = new Velocity(0.7f, projectile.getX(),projectile.getY(),
								player.getX()+player.getWidth()/2,player.getY()+player.getHeight()/2);
						projectile.setRotation(v.getAngle());
						projectile.setVelocityY((float)v.getdy());
					}
					else
					{
						v = new Velocity(0.7f,projectile.getX(),projectile.getY(),this.getX()-50,this.getY()+26);
						projectile.setRotation(0);
					}
				}
				projectile.setVelocityX((float)v.getdx());
	    		switch(tag)
	    		{
	    		case "turret":
	    		{
	    			projectile.shiftY(5f);
	    			projectile.setScale(1.0f);
	    			projectile.setVelocityY(.0f);
	    			if(projectile.getVelocityX()<0)
	    				{
	    					projectile.setVelocityX(-1.5f);
	    					projectile.setRotation(180);
	    				}
	    			else
	    			{ 
	    				projectile.setVelocityX(1.5f);
	    				projectile.setRotation(0);
	    			}
	    			break;
	    		}
	    		case "thug":
	    		{
	    			if(projectile.getVelocityX()<0) projectile.setVelocityX(-.5f);
	    			else projectile.setVelocityX(.5f);
	    			projectile.setScale(.5f);
	    			break;
	    		}
	    		default:
	    			break;
	    		}
				playShootSound();
				projectile.show();
	    	}
		}
	}
	/**
	 * Makes the sprite move left/right when it reaches end of tile or any other obstacle.
	 * */
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
	/**
	 * Collision checking modified so the sprite does not walk off the tiles.
	 * */
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
	/**
	 * Determines if the player sprite is close.
	 * @param Player Sprite to compare with.
	 * @return true if Player Sprite is close on both X and Y axis
	 * */
	private boolean isPlayerClose(Player player)
	{
		boolean closeOnX = false;
		boolean closeOnY = false;
		if(player.getX()+screenWidth>this.getX())
		{
			if(this.getX()+screenWidth<player.getX())
				closeOnX = false;
			else
				closeOnX = true;
		}
		if(player.getY()+player.getHeight()+50>this.getY() || player.getY()-player.getHeight()+20>this.getY())
		{
			closeOnY = true;
		}
		else
			closeOnY = false;
		return (closeOnX && closeOnY);
	}
	
}
