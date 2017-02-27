import game2D.*;
public class Thug extends Sprite
{
	private boolean killed = false;
	private Game gct;
	private float gravity = 0.01f;
	private final float PATROLSPEED = .04f;
	private boolean walkingRight = false;
	private EnemyProjectile projectile;
	private Animation animLeft;
	private Animation animRight;
	private Animation shootLeft;
	private Animation shootRight;
	private Sound shooting = null;
	
	public Thug(Game gct)
	{
		super();
		this.gct = gct;
		projectile = new EnemyProjectile(gct);
		loadAssets();
	}
	public EnemyProjectile getProjectile() { return projectile; }
	public boolean isKilled() { return killed; }
	
	
	public void update(long elapsed, Player player, TileMap tmap)
	{
		if (this.getY() + this.getHeight() > tmap.getPixelHeight())
			this.kill();
		
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
	private void patrol(Player player, TileMap tmap)//FIXME
	{
		if(!isPlayerClose(player))
		{
			if(walkingRight)
			{
				if(!modifiedThugCheckBottomSideForCollision(tmap) || gct.checkRightSideForCollision(this))
				{
					this.setVelocityX(.0f);
					walkingRight=false;
				}
				else
				{
					this.setAnimation(animRight);
					this.setVelocityX(PATROLSPEED);
				}
			}
			else
			{
				if(!modifiedThugCheckBottomSideForCollision(tmap) || gct.checkLeftSideForCollision(this))
				{	
					this.setVelocityX(.0f);
					walkingRight=true;
				}
				else
				{
					this.setAnimation(animLeft);
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
		return ((player.getX()+gct.getWidth()/2>this.getX() || this.getX()<player.getX()-gct.getWidth()/2)
				&&(player.getY()+player.getHeight()+50>this.getY() || player.getY()-player.getHeight()+20>this.getY())); 
		// check to see if player is close or not on the X axis
		//then on the Y axis
	}
	/**
	 * Load the necessary assets for the sprite to work.
	 * Also sets default animation.
	 * */
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