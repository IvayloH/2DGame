public class Boss extends Enemy
{
	private int[] difficultyScale = {4,8,12};
	private boolean invulnerable = false;
	private long invulnerableTime = 0;

	public Boss(String tag)
	{
		super(tag);
		loadAssets();
		projectile = new SpriteExtension("projectile");
		maxHP = 10; // default
		lifeBars = maxHP;
	}
	
	public void reset() { lifeBars = maxHP; }
	public SpriteExtension getProjectile() { return projectile; }
	public void lookLeft() { setAnimation(standLeft); }
	public void lookRight() { setAnimation(standRight); }
	public boolean isInvulnerable() { return invulnerable; }
	public long getInvulnerableTimer() { return invulnerableTime; }
	public void setInvulnerable(boolean state) { invulnerable = state;}
	public void setInvulnerableTimer(long time) { invulnerableTime = time; }
	
	/**
	* Sets health based on the difficulty level
	* @param difficulty (0-Easy, 1 - Medium, 2 - Hard)
	*/
	public void setHpBasedOnDifficulty(int difficulty)
	{
		maxHP = difficultyScale[difficulty];
		lifeBars=maxHP;
	}
	
	/**
	* Sets the spawn Point(x,y) for the boss.
	*/
	public void setSpawn(float x, float y)
	{
		setX(x);
		setY(y);
	}

	/**
	 * Decrease boss HP and make invulnerable.
	 */
	public void takeDamage() 
	{
		if(!invulnerable) 
		{ 
			lifeBars--;
			invulnerable = true;
		}
	}
	
	/**
	 * Handles the shooting animation and action.
	 */
	public void shoot(Player player)
	{
		//call the shoot method from superclass
		if(isVisible())
			shoot(player, true);
	}
}