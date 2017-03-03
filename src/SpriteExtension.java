import java.awt.Image;
import javax.swing.ImageIcon;

import game2D.*;

public class SpriteExtension extends Sprite
{
	//Animations
	protected Animation mainAnimation;
	protected Animation standLeft;
	protected Animation standRight;
	protected Animation moveLeft;
	protected Animation moveRight;
	protected Animation fireLeft;
	protected Animation fireRight;
	
	//Sounds
	Sound fire;
	
	protected SpriteExtension projectile;
	
	protected String tag = "";
	protected float gravity = 0.01f;
	protected boolean walkingRight = false;
	protected Collision collider;
	static final int screenWidth = 512;   //512
	static final int screenHeight = 384;  //384
	
	public SpriteExtension(String tag)
	{
		super();
		this.tag = tag;
		loadAssets();
	}
	/**
	 * If using this constructor, set tag manually before use!!
	 * */
	public SpriteExtension() { }
	
	public void setTag(String tag){ this.tag = tag; }
	public String getTag() { return tag; }
	/**
	 * Load the appropriate sound according to the Tag and calls the start() method.
	 * */
	protected void playShootSound()
	{
		switch(tag)
		{
		case "batarang":
		{
			fire = new Sound("assets/sounds/grunt_throw.wav");
			break;
		}
		case "thug":
		{
			fire = new Sound("assets/sounds/shoot.wav");
			break;
		}
		case "turret":
		{
			fire = new Sound("assets/sounds/shoot.wav");
			break;
		}
		case "boss":
		{
			fire = new Sound("assets/sounds/shoot.wav");
			break;
		}
			default:
				break;
		}
		fire.start();
	}
	/**
	 * Load the necessary assets for the sprite to work.
	 * Also sets default animation.
	 * */
	protected void loadAssets()
	{
		if(tag.equals(""))
			System.out.println("Animation for " + this.getClass().toString() + " failed to load or was not set.");
        mainAnimation = new Animation();
        switch(tag)
        {
        case "crate":
        {
	        mainAnimation.addFrame(loadImage("assets/maps/crate.png"), 60);
	        setAnimation(mainAnimation);
	        break;
        }
        case "grappleHook":
        {
			mainAnimation.addFrame(loadImage("assets/images/Projectiles/GrappleHook.png"), 60);
	        setAnimation(mainAnimation);
	        break;
        }
        case "projectile":
        {
			mainAnimation.addFrame(loadImage("assets/images/Projectiles/thugProjectile.png"), 60);
	        setAnimation(mainAnimation);
	        break;
        }
        case "batarang":
        {
			mainAnimation.addFrame(loadImage("assets/images/BatmanGadgets/batarang.gif"), 60);
	        setAnimation(mainAnimation);
	        break;
        }
        case "player":
        {
        	standRight = new Animation();
        	standRight.addFrame(loadImage("assets/images/BatmanStates/BatmanFacingRight.gif"), 60);
            
        	setAnimation(standRight);
        	
        	standLeft = new Animation();
        	standLeft.addFrame(loadImage("assets/images/BatmanStates/BatmanFacingLeft.gif"), 60);
    	        
    	    moveRight= new Animation();
    	    moveRight.addFrame(loadImage("assets/images/BatmanStates/BatmanMoveRight.gif"), 60);
    	        
            moveLeft = new Animation();
            moveLeft.addFrame(loadImage("assets/images/BatmanStates/BatmanMoveLeft.gif"),60);
            
            fireRight = new Animation();
            fireRight.addFrame(loadImage("assets/images/BatmanWithGadgets/BatmanGrappleHookGunRight.gif"), 60);
            
            fireLeft = new Animation();
            fireLeft.addFrame(loadImage("assets/images/BatmanWithGadgets/BatmanGrappleHookGunLeft.gif"), 60);
            break;
        }
        case "boss":
        {
            standLeft = new Animation();
            standLeft.addFrame(loadImage("assets/images/Enemies/Thug/thug_sl.gif"), 60);
            
            //set starting animation
            setAnimation(standLeft);
            
            standRight = new Animation();
            standRight.addFrame(loadImage("assets/images/Enemies/Thug/thug_sr.gif"), 60);

            fireRight = new Animation();
            fireRight.addFrame(loadImage("assets/images/Enemies/Thug/thug_fire_right.gif"), 60);

            fireLeft = new Animation();
            fireLeft.addFrame(loadImage("assets/images/Enemies/Thug/thug_fire_left.gif"), 60);
        	break;
        }
        case "thug":
        {
        	standLeft = new Animation();
        	standLeft.addFrame(loadImage("assets/images/Enemies/Thug/thug_sl.gif"), 60);
            
            //set starting animation
            setAnimation(standLeft);
            
            standRight = new Animation();
            standRight.addFrame(loadImage("assets/images/Enemies/Thug/thug_sr.gif"), 60);

            fireRight= new Animation();
            fireRight.addFrame(loadImage("assets/images/Enemies/Thug/thug_fire_right.gif"), 60);

            fireLeft = new Animation();
            fireLeft.addFrame(loadImage("assets/images/Enemies/Thug/thug_fire_left.gif"), 60);
            break;
        }
        case "turret":
        {
	   		standLeft = new Animation();
	   		standLeft.addFrame(loadImage("assets/images/Enemies/Turret/turret_idle_l.gif"), 60);
		    setAnimation(standLeft);
		     
		    standRight = new Animation();
		    standRight.addFrame(loadImage("assets/images/Enemies/Turret/turret_idle_r.gif"), 	60);
		     
		    fireLeft = new Animation();
		    fireLeft.addFrame(loadImage("assets/images/Enemies/Turret/turret_sh_l.gif"), 60);
		     
		    fireRight = new Animation();
		    fireRight.addFrame(loadImage("assets/images/Enemies/Turret/turret_sh_r.gif"), 60);
        	break;
        }
	    default:
	        	break;
        }

		
	}
    /**
     * Loads an image with the given 'fileName'
     * 
     * @param fileName The file path to the image file that should be loaded 
     * @return A reference to the Image object that was loaded
     */
    protected Image loadImage(String fileName) 
    { 
    	return new ImageIcon(fileName).getImage(); 
    }
}
