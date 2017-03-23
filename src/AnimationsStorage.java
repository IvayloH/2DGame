import java.awt.Image;
import java.util.HashMap;

import javax.swing.ImageIcon;

import game2D.Animation;

/**
 * @author Ivo
 *
 */
public class AnimationsStorage
{
	private HashMap<String,Animation> storage;
	
	
	public AnimationsStorage() 
	{ 
		storage = new HashMap<String,Animation>();
		loadAssets();
	}
	
	public Animation getAnim(String name)
	{
		Animation a = storage.get(name);
		if(a==null){ System.out.println(name);}
		return a;
	}
	/**
	 * Load the necessary assets for the sprite to work.
	 * */
	private void loadAssets()
	{
		Animation anim = new Animation();
		
		anim.addFrame(loadImage("assets/maps/crate.png"), 60);
		storage.put("crateAnim", anim);
		
		anim = new Animation();
		anim.addFrame(loadImage("assets/images/Projectiles/GrappleHook.png"), 60);
		storage.put("grappleHookAnim", anim);

		anim = new Animation();
		anim.addFrame(loadImage("assets/images/Projectiles/thugProjectile.png"), 60);
		storage.put("thugProjectile", anim);

		anim = new Animation();
		anim.addFrame(loadImage("assets/images/BatmanGadgets/batarang.gif"), 60);
		storage.put("batarang", anim);

		anim = new Animation();
		anim.addFrame(loadImage("assets/images/BatmanStates/BatmanFacingRight.gif"), 60);
		storage.put("playerStandRight", anim);

		anim = new Animation();
		anim.addFrame(loadImage("assets/images/BatmanStates/BatmanFacingLeft.gif"), 60);
		storage.put("playerStandLeft", anim);
		
		anim= new Animation();
		anim.addFrame(loadImage("assets/images/BatmanStates/BatmanMoveRight.gif"), 60);
		storage.put("playerMoveRight", anim);
		
		anim = new Animation();
		anim.addFrame(loadImage("assets/images/BatmanStates/BatmanMoveLeft.gif"),60);
		storage.put("playerMoveLeft", anim);
		
		anim = new Animation();
        anim.addFrame(loadImage("assets/images/BatmanStates/BatmanJumpRight.png"),60);
        storage.put("playerJumpRight", anim);
        
        anim = new Animation();
        anim.addFrame(loadImage("assets/images/BatmanStates/BatmanJumpLeft.png"),60);
        storage.put("playerJumpLeft", anim);

        anim = new Animation();
        anim.addFrame(loadImage("assets/images/BatmanStates/transparent64.png"), 60);
        storage.put("transparent64", anim);
	    
        anim = new Animation();
        anim.addFrame(loadImage("assets/images/BatmanStates/transparent32.png"), 60);
        storage.put("transparent32", anim);
        
        anim = new Animation();
        anim.addFrame(loadImage("assets/images/BatmanStates/crouch_R.gif"), 60);
        storage.put("playerCrouchRight", anim);
        
        anim = new Animation();
        anim.addFrame(loadImage("assets/images/BatmanStates/crouch_L.gif"), 60);
        storage.put("playerCrouchLeft", anim);

        anim = new Animation();
        anim.addFrame(loadImage("assets/images/BatmanStates/crouch_move_R.gif"), 60);
        storage.put("playerCrouchingMoveRight", anim);
        
        anim = new Animation();
        anim.addFrame(loadImage("assets/images/BatmanStates/crouch_move_L.gif"), 60);
        storage.put("playerCrouchingMoveLeft", anim);
        
		anim = new Animation();
		anim.addFrame(loadImage("assets/images/BatmanWithGadgets/BatmanGrappleHookGunRight.gif"), 60);
		storage.put("grappleHookRight", anim);
		
		anim = new Animation();
		anim.addFrame(loadImage("assets/images/BatmanWithGadgets/BatmanGrappleHookGunLeft.gif"), 60);
		storage.put("grappleHookLeft", anim);
		
		anim = new Animation();
		anim.addFrame(loadImage("assets/images/Enemies/Thug/thug_sl.gif"), 60);
		storage.put("thugStandLeft", anim);
		
		anim = new Animation();
		anim.addFrame(loadImage("assets/images/Enemies/Thug/thug_sr.gif"), 60);
		storage.put("thugStandRight", anim);
		
		anim = new Animation();
		anim.addFrame(loadImage("assets/images/Enemies/Thug/thug_fire_right.gif"), 60);
		storage.put("thugFireRight", anim);
		
		anim = new Animation();
		anim.addFrame(loadImage("assets/images/Enemies/Thug/thug_fire_left.gif"), 60);
		storage.put("thugFireLeft", anim);
		
		anim = new Animation();
		anim.addFrame(loadImage("assets/images/Enemies/Turret/turret_idle_l.gif"), 60);
		storage.put("turretIdleLeft", anim);

		
		anim = new Animation();
		anim.addFrame(loadImage("assets/images/Enemies/Turret/turret_idle_r.gif"), 	60);
		storage.put("turretIdleRight", anim);
		
		anim = new Animation();
		anim.addFrame(loadImage("assets/images/Enemies/Turret/turret_sh_l.gif"), 60);
		storage.put("turretFireLeft", anim);
		
		anim = new Animation();
		anim.addFrame(loadImage("assets/images/Enemies/Turret/turret_sh_r.gif"), 60);
		storage.put("turretFireRight", anim);
		
		//for(String s: storage.keySet())
		//	System.out.println(s);
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
