import game2D.*;

public class SpriteExtension extends Sprite
{
	final int screenWidth = 512;   //512
	final int screenHeight = 384;  //384
	
	AnimationsStorage storage;

	//Sounds
	protected Sound fire = null;
	protected FadingSound fadingFire = null;
	
	protected SpriteExtension projectile;
	protected int maxHP;
	protected int lifeBars;
	protected boolean killed;
	protected String tag = "";
	protected float gravity = 0.01f;
	protected boolean walkingRight = false;
	protected Collision collider;

	
	public SpriteExtension(String tag)
	{
		super();
		storage = new AnimationsStorage();
		this.tag = tag;
		setAnimationsAccordingToTag(tag);
	}
	
	/**
	 * If using this constructor, set tag manually before use!!
	 * */
	public SpriteExtension()
	{
		storage = new AnimationsStorage();
	}
	
	public void setTag(String tag){ this.tag = tag; }
	public String getTag() { return tag; }
	public int getCurrentHP() { return lifeBars; }
	public int getMaxHP() { return maxHP; }
	public boolean isKilled() { return killed; }
	
	/**
	 * Set killed to true and hide the sprite.
	 * */
	public void kill() 
	{ 
		if(!killed)
		{
			killed = true; 
			if(!tag.equals("turret"))//leave turrets showing
				this.hide();
		}
	}
	
	/**
	 * Load the appropriate sound according to the Tag and calls the start() method.
	 * */
	protected void playShootSound()
	{
		switch(tag)
		{
		case "batarang":
		{
			fadingFire = new FadingSound("assets/sounds/grunt_throw.wav");
			break;
		}
		case "thug":
		{
			fadingFire = new FadingSound("assets/sounds/shoot.wav");
			break;
		}
		case "turret":
		{
			fadingFire = new FadingSound("assets/sounds/shoot.wav");
			break;
		}
		case "boss":
		{
			fadingFire = new FadingSound("assets/sounds/shoot.wav");
			break;
		}
			default:
				break;
		}
		fadingFire.start();
	}
	
	protected void setAnimationsAccordingToTag(String tag)
	{
		switch(tag)
		{
		case "batarang":
		{
			setAnimation(storage.getAnim("batarang"));
			break;
		}
		case "projectile":
		{
			setAnimation(storage.getAnim("thugProjectile"));
			break;
		}
		case "grappleHook":
		{
			setAnimation(storage.getAnim("grappleHookAnim"));
			break;
		}
		case "crate":
		{
			setAnimation(storage.getAnim("crateAnim"));
			break;
		}
		case "thug":
		{
			if(Math.random()>0.5)
				setAnimation(storage.getAnim("thugStandLeft"));
			else
				setAnimation(storage.getAnim("thugStandRight"));
			break;
		}
		case "boss":
		{
			setAnimation(storage.getAnim("thugStandLeft"));
			break;
		}
		case "turret":
		{
			setAnimation(storage.getAnim("turretIdleLeft"));
			break;
		}
		default:	
			break;
		}
	}
}
