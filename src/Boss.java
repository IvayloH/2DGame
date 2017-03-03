import game2D.*;
public class Boss extends SpriteExtension
{
	private int HP;
	private int maxHP=10;
	private boolean invulnerable = false;
	private long invulnerableTime = 0;
	private boolean dead = false;

	public Boss(String tag)
	{
		super();
		this.tag = tag;
		loadAssets();
		projectile = new SpriteExtension("projectile");
		HP=maxHP;
	}
	public void setSpawn(float x, float y)
	{
		setX(x);
		setY(y);
	}
	public boolean isDead() { return dead; }
	public int getCurrentHP() { return HP; }
	public int getMaxHP() { return maxHP; }
	public void reset() { HP = maxHP; }
	public SpriteExtension getProjectile() { return projectile; }
	public void kill() { dead=true; }
	public void lookLeft() { setAnimation(standLeft); }
	public void lookRight() { setAnimation(standRight); }
	public boolean isInvulnerable() { return invulnerable; }
	public long getInvulnerableTimer() { return invulnerableTime; }
	public void setInvulnerable(boolean state) { invulnerable = state;}
	public void setInvulnerableTimer(long time) { invulnerableTime = time; }
	/**
	 * Decrease boss HP and make invulnerable.
	 */
	public void takeDamage() 
	{
		if(!invulnerable) 
		{ 
			HP--;
			invulnerable = true;
		}
	}
	/**
	 * Handles the shooting animation and action.
	 */
	public void shoot(Player player)
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
					//shoot directly at the player
					v = new Velocity(0.7f, projectile.getX(),projectile.getY(),
							player.getX()+player.getWidth()/2,player.getY()+player.getHeight()/2);
					projectile.setRotation(180);
				}
				else
				{
					this.setAnimation(fireLeft);
					projectile.setX(this.getX());
					projectile.setY(this.getY()+15);
					v = new Velocity(0.7f, projectile.getX(),projectile.getY(),
							player.getX()+player.getWidth()/2,player.getY()+player.getHeight()/2);
					projectile.setRotation(0);
				}
	
				projectile.setVelocityX((float)v.getdx());
				projectile.setVelocityY((float)v.getdy());
				projectile.setRotation(v.getAngle());
				
		        playShootSound();
				projectile.show();
	    	}
		}
	}
}