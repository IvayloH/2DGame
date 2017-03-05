public class Boss extends Enemy
{
	private int[] difficultyScale = {4,8,12};
	private int HP;
	private int maxHP=8; // default value
	private boolean invulnerable = false;
	private long invulnerableTime = 0;
	private boolean dead = false;

	public Boss(String tag)
	{
		super(tag);
		loadAssets();
		projectile = new SpriteExtension("projectile");
		HP = maxHP;
	}
	public void setDifficulty(int difficulty)
	{
		maxHP = difficultyScale[difficulty];
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
		//call the shoot method from superclass
		shoot(player, true);
	}
}