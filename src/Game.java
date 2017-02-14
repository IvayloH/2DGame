import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import game2D.*;

// Game demonstrates how we can override the GameCore class
// to create our own 'game'. We usually need to implement at
// least 'draw' and 'update' (not including any local event handling)
// to begin the process. You should also add code to the 'init'
// method that will initialise event handlers etc. By default GameCore
// will handle the 'Escape' key to quit the game but you should
// override this with your own event handler.

/**
 * @author David Cairns
 *
 */


public class Game extends GameCore implements MouseListener, MouseWheelListener, MouseMotionListener
{
	private static final long serialVersionUID = 1L;
	// Useful game constants
	static final int screenWidth = 512;   //512
	static final int screenHeight = 384;  //384
	final float RUNSPEED = .07f;
	final float JUMPHEIGHT = 48;  // ??? tile.height()/2 + tile.height()/4
	final float HOOKLIMIT = 150; // in pixels
	final double crateRotation = 1.5708; //90 degrees
	final int amountOfDamageBeforeDeath = 6;
	final String[] gadgets = {"Batarang", "Grapple Hook"}; // holds all of batman's gadgets
	// list in which the location(x,y) of every crate is added in order to spawn it where needed, when needed
	final ArrayList<CrateSpawnPosition<Float,Float>> crateSpawnPositions = new ArrayList<CrateSpawnPosition<Float,Float>>(); 
	
	
    float lift = 0.005f;
    float gravity = 0.01f;
    float posY = .0f;  // keep track of jump start
    int lifeBars = 0;    // number of lives left
    String currentGadget = "Grapple Hook";  // keep track of the current gadget that has been selected
    int currCrate = 0; //keep track of the crate and which to spawn next from the list
    float cratePosX = 0;
    double grappleHookRotation = 0.0;
    float invincibleTime = .0f;

    int xOffset, yOffset;
    
    // Game state flags
    boolean collisionRIGHT = false;
    boolean collisionLEFT = false;
    boolean collisionABOVE = false;
    boolean collisionBELOW = false;
    boolean crateHit = false;
    boolean grappleHookRetracting = false;
    boolean cursorChanged = false;
    boolean invincible = false; // player becomes invincible after being hit
    boolean flashy = false; // for flashing effect while player is invincible

    //Pressed Key flags
    boolean leftKey = false;
    boolean rightKey = false;
    boolean jumpKey = false;
    boolean crouchKey = false;
    
    // Batman Direction
    boolean lookingRight = true;

    enum EPlayerState
    {
    	RUN_LEFT,
    	RUN_RIGHT,
    	JUMP,
    	JUMP_LEFT,
    	JUMP_RIGHT,
    	CROUCH,
    	CROUCH_MOVE_LEFT,
    	CROUCH_MOVE_RIGHT,
    	STANDING,
    	FALLING,
    	RESPAWN,
    	DEAD
    }
   //Sprite State
    EPlayerState playerState = EPlayerState.FALLING;
    
    // Game resources
    Animation standingFacingLeft = null;
    Animation standingFacingRight = null;
    Animation movement_Right = null;
    Animation movement_Left = null;
    Animation grapple = null;
    Animation jump_Right = null;
    Animation jump_Left = null;
    Animation crateAnim = null;
    Animation grappleHookGun_Right = null;
    Animation grappleHookGun_Left = null;
    Animation thugAnim = null;
    Animation thugProjectileAnim = null;
    Animation batarangAnim = null;
    Animation transparent = null;
    Animation crouch = null;
    Animation crouch_move_left=null;
    Animation crouch_move_right=null;
    
    Sprite	player = null;
    Sprite grappleHook = null;
    Sprite thug = null;
    Sprite crate = null;
    Sprite thugProjectile = null;
    Sprite batarang = null;
    
    Image bgImage = null;
    
    ArrayList<Sprite> clouds = new ArrayList<Sprite>();
    TileMap tmap = new TileMap();	// Our tile map, note that we load it in init()    
    AffineTransform crateTransform = new AffineTransform();

    /**
	 * The obligatory main method that creates
     * an instance of our class and starts it running
     * 
     * @param args	The list of parameters this program might use (ignored)
     */
    public static void main(String[] args)
    {
        Game gct = new Game();
        gct.init();
        // Start in windowed mode with the given screen height and width
        gct.run(false,screenWidth,screenHeight);
    }

    /**
     * Initialise the class, e.g. set up variables, load images,
     * create animations, register event handlers
     */
    public void init()
    {         
        loadAnimations();
        loadSprites();
        
        addMouseListener(this);
        addMouseWheelListener(this);
        addMouseMotionListener(this);
        
        initialiseCrateSpawnPoints();
        initialiseGame(); 		
        System.out.println(tmap);
    }

    /**
     * You will probably want to put code to restart a game in
     * a separate method so that you can call it to restart
     * the game.
     */
    public void initialiseGame()
    {
    	lifeBars = amountOfDamageBeforeDeath;
    	resetSpritePositionAndVelocity(player,75,50,0,0);
    	resetSpritePositionAndVelocity(thug,500,50,0,0);
        player.show();
        thug.show();
    }

    /**
     * Draw the current state of the game
     */
    public void draw(Graphics2D g)
    {
    	xOffset = screenWidth/2-(int)player.getX();
    	yOffset = screenHeight/2-(int)player.getY();
        int minOffsetX= screenWidth-tmap.getPixelWidth();
        int maxOffsetX = 0;
        int minOffsetY = screenHeight-tmap.getPixelHeight();
        int maxOffsetY = 0;
        
        if(xOffset>maxOffsetX)
        	xOffset = maxOffsetX;
        else if(xOffset<minOffsetX)
        	xOffset=minOffsetX;
        
        if(yOffset>maxOffsetY)
        	yOffset=maxOffsetY;
        else if(yOffset<minOffsetY)
        	yOffset=minOffsetY;
                 
        g.drawImage(bgImage,0,0,null);
        
        // Apply offsets to sprites then draw them
        for (Sprite s: clouds)
        {
        	s.setOffsets(xOffset,yOffset);
        	s.draw(g);
        }

        // Apply offsets to player and draw 
        player.setOffsets(xOffset, yOffset);
        player.draw(g);
        
        thug.setOffsets(xOffset, yOffset);
        thug.draw(g);
        
        thugProjectile.setOffsets(xOffset, yOffset);
        thugProjectile.draw(g);
        
        batarang.setOffsets(xOffset, yOffset);
        batarang.draw(g);
        
        // Apply offsets to tile map and draw  it
        tmap.draw(g,xOffset,yOffset);
        
        drawHUD(g);
        drawGrappleHook(g);
        drawCrate(g);
    }

    /**
     * Update any sprites and check for collisions
     * 
     * @param elapsed The elapsed time between this call and the previous call of elapsed
     */    
    public void update(long elapsed)
    {
    	//THUG UPD
    	if(thug.isVisible())
    	{
    		if(!checkBottomSideForCollision(thug))
    		{
    			thug.setVelocityY(.5f);
        		thug.setVelocityY(thug.getVelocityY()+(gravity*elapsed)); // gravity adjustment
    		}
    		else
    		{
    			if(player.getX()>thug.getX())
    			{
    				thug.setAnimation(grappleHookGun_Right);
    			}
    			else
    				thug.setAnimation(grappleHookGun_Left);
    			
    			thug.setVelocityY(.0f);
    			recoverSpriteStuckInBottomTile(thug);
    			//make sure thug is on the ground before shooting
        		if(Math.random()>0.3)
        			if(player.getX()+screenWidth<thug.getX() || thug.getX()>player.getX()-screenWidth) // check to see if player is close or not
        				thugShoot(thug);
    		}
    		thug.update(elapsed);
    	}
    	if(thugProjectile.isVisible())
    	{
    		thugProjectile.update(elapsed);
    		if(boundingBoxCollision(thugProjectile, player) && !invincible)
    			playerTakeDamage(elapsed);
    	}
    	if(batarang.isVisible())
    	{
    		batarang.update(elapsed);
    		if(boundingBoxCollision(batarang,thug))
    			thug.hide();
    		if(checkBottomSideForCollision(batarang) || checkRightSideForCollision(batarang) || checkLeftSideForCollision(batarang))
    		{
    			batarang.hide();
    		}
    		if(batarang.getX()>player.getX()+screenWidth || batarang.getX()<player.getX()-screenWidth)
    			batarang.hide();
    	}
    	//CRATE UPD
    	if(crateHit)
    	{
    		if(crate.getRotation()>-90)
        		crate.setRotation(crate.getRotation()-2.0);
        	if(crate.getX()>cratePosX-32)
        		crate.setX(crate.getX()-2);
        	else if(!checkBottomSideForCollision(crate))
        		crate.setY(crate.getY()+2);
        	else
        	{
        		crateHit=false;
        		tmap.setTileChar('c', ((int)crate.getX()+5)/tmap.getTileWidth(), ((int)crate.getY()+5)/tmap.getTileHeight());
        		crate.hide();
        		currCrate++;
        		crate.setRotation(0);
        	}
    	}
    	//HOOK UPD
    	if (grappleHook.isVisible())
    	{
    		if((grappleHook.getX()>player.getX()+player.getWidth()+HOOKLIMIT)
    				|| (grappleHook.getX()<player.getX()-HOOKLIMIT)
    				|| (grappleHook.getY()>player.getY()+player.getHeight()/2+HOOKLIMIT)
    				|| (grappleHook.getY()<player.getY()-HOOKLIMIT))
    			retractGrappleHook();

        	if(boundingBoxCollision(grappleHook, crate))
        	{
        		retractGrappleHook();
        		if(!checkRightSideForCollision(crate))
        		{//if next to a tile, then crate already fallen
        			crateHit=true;
        			cratePosX = crate.getX();
        		}
        	}
        	
    		if(grappleHookRetracting)
    		{
    			if(boundingBoxCollision(player, grappleHook))
    			{
    				grappleHook.setVelocityX(.0f);
    				grappleHook.setVelocityY(.0f);
    				grappleHook.hide();
    				grappleHookRetracting=false;
    			}
    		}
    		grappleHook.update(elapsed);
    	}
    	
    	//PLAYER UPD
    	if(invincible)
    	{
    		invincibleTime+=elapsed;
    		if(flashy)
    		{
    			player.setAnimation(getAppropriateAnimation());
    			flashy=false;
    		}
    		else
    		{
    			player.setAnimation(transparent);
    			flashy=true;
    		}
    		if(invincibleTime>2000f)
    		{
    			invincible=false;
    			invincibleTime=0f;
    		}
    	}
    	
    	if(!grappleHookRetracting && !invincible)
    		player.setAnimation(getAppropriateAnimation());
    	
    	if(playerState.equals(EPlayerState.RESPAWN)) 
    	{
    		if(lifeBars<1) 
    		{
    			resetSpritePositionAndVelocity(player,75,50,0,0);
    			playerState = EPlayerState.FALLING;
    			lifeBars = amountOfDamageBeforeDeath;
    			//stop(); // stop game if player loses all lives
    			//TODO add an end game state
    			//playerState = EPlayerState.DEAD;
    		}
    	}
    		
    	if(playerState.equals(EPlayerState.STANDING))
    	{
    		player.setVelocityX(.0f);
    		player.setVelocityY(.0f);
    	}
    	
    	if(playerState.equals(EPlayerState.CROUCH) || playerState.equals(EPlayerState.CROUCH_MOVE_LEFT) || playerState.equals(EPlayerState.CROUCH_MOVE_RIGHT))
    	{
			if(playerState.equals(EPlayerState.CROUCH_MOVE_RIGHT) && !collisionRIGHT)
				player.setVelocityX(RUNSPEED/2);
			else if(playerState.equals(EPlayerState.CROUCH_MOVE_LEFT) && !collisionLEFT)
				player.setVelocityX(-RUNSPEED/2);
			else
				player.setVelocityX(.0f);
    	}
    	
    	if(playerState.equals(EPlayerState.JUMP) || playerState.equals(EPlayerState.JUMP_RIGHT) || playerState.equals(EPlayerState.JUMP_LEFT))
    	{
    		if(!collisionABOVE)
    		{
    			player.setVelocityY(-gravity*elapsed);
    			if(playerState.equals(EPlayerState.JUMP_RIGHT) && !collisionRIGHT) player.setVelocityX(RUNSPEED);
    			else if(playerState.equals(EPlayerState.JUMP_LEFT) && !collisionLEFT) player.setVelocityX(-RUNSPEED);
    			else player.setVelocityX(.0f);
    			if(posY-player.getY()>JUMPHEIGHT)
    				playerState = EPlayerState.FALLING;
    		}
    	}
    	
    	//change the playerState and animation after jump has ended
    	if(collisionBELOW && playerState.equals(EPlayerState.FALLING))
		{
			playerState = EPlayerState.STANDING;
			if(rightKey)
				playerState = EPlayerState.RUN_RIGHT;
			else if(leftKey)
				playerState = EPlayerState.RUN_LEFT;
			
			player.setAnimation(getAppropriateAnimation());
		}
    	
    	if(playerState.equals(EPlayerState.RUN_RIGHT))
    	{
    		if(collisionRIGHT) 
    		{
    			player.setVelocityX(.0f);
    			recoverSpriteStuckInRightTile(player);
    		}
    		else
    			player.setVelocityX(RUNSPEED);
    	}
    	
    	if(playerState.equals(EPlayerState.RUN_LEFT))
    	{
    		if(collisionLEFT)
    		{
    			player.setVelocityX(.0f);
    			recoverSpriteStuckInLeftTile(player);
    		}
    		else
    			player.setVelocityX(-RUNSPEED);
    	}
    	
    	if(collisionBELOW && !playerState.equals(EPlayerState.JUMP) && !playerState.equals(EPlayerState.JUMP_LEFT)&& !playerState.equals(EPlayerState.JUMP_RIGHT))
    		player.setVelocityY(.0f);
    	else if(!playerState.equals(EPlayerState.JUMP)&& !playerState.equals(EPlayerState.JUMP_LEFT) && !playerState.equals(EPlayerState.JUMP_RIGHT)) 
    	{
    		player.setVelocityY(.05f);
    		player.setVelocityY(player.getVelocityY()+(gravity*elapsed)); // gravity adjustment
    	}
    	
       	for (Sprite s: clouds)
       		s.update(elapsed);
       	
        // Now update the sprites animation and position
        player.update(elapsed);
        
        // Then check for any collisions that may have occurred
        handleTileMapCollisions(player,elapsed);
    }

	/**
     * Checks and handles collisions with the tile map for the
     * given sprite 's'. Initial functionality is limited...
     * 
     * @param s			The Sprite to check collisions for
     * @param elapsed	How time has gone by
     */
    public void handleTileMapCollisions(Sprite s, long elapsed)
    {
    	//Check if sprite has fallen off screen
        if (s.getY() + s.getHeight() > tmap.getPixelHeight())
        {
        	if(s.getTag().equals("player")) 
        	{
        		playerTakeDamage(elapsed);
        		playerState = EPlayerState.RESPAWN;
        	}
        	else
        		s.hide();
        }
        
    	//Check Tile underneath the sprite for collision
        collisionBELOW = checkBottomSideForCollision(s);
    	if(collisionBELOW)
    		recoverSpriteStuckInBottomTile(s);
        
        //Check Tile to the RIGHT of the sprite for collision
        if(playerState.equals(EPlayerState.RUN_RIGHT) || playerState.equals(EPlayerState.CROUCH_MOVE_RIGHT))
        	collisionRIGHT = checkRightSideForCollision(s);

        
        //Check Tile to the LEFT of the sprite for collision
        if(playerState.equals(EPlayerState.RUN_LEFT) || playerState.equals(EPlayerState.CROUCH_MOVE_LEFT))
        	collisionLEFT = checkLeftSideForCollision(s);

        
        //Check Tile ABOVE the sprite for collision
        if(playerState.equals(EPlayerState.JUMP) || playerState.equals(EPlayerState.JUMP_RIGHT) || playerState.equals(EPlayerState.JUMP_LEFT))
        {
        	collisionABOVE = checkTopSideForCollision(s);
        	collisionRIGHT = checkRightSideForCollision(s);
        	collisionLEFT = checkLeftSideForCollision(s);
        }       
        
        if(playerState.equals(EPlayerState.FALLING))
        {
        	collisionRIGHT = checkRightSideForCollision(s);
        	collisionLEFT = checkLeftSideForCollision(s);
        }
        
        if(grappleHook.isVisible())
        {
        	//check for right/left collision depending on which way the hook is going
        	if(grappleHook.getVelocityX()>0)
        	{
        		if(checkRightSideForCollision(grappleHook))
        			retractGrappleHook();
        	}
        	else if(checkLeftSideForCollision(grappleHook))
        			retractGrappleHook();
        }
        if(thugProjectile.isVisible())
        {
        	if(checkLeftSideForCollision(thugProjectile))
        		thugProjectile.hide();
        	if(checkRightSideForCollision(thugProjectile))
        		thugProjectile.hide();
        }
    }
    
    
    private void drawHUD(Graphics2D g)
    {
    	String msg = String.format("Equipped Gadget: %s", currentGadget); // TODO WILL BE REPLACED WITH AN IMAGE
        g.setColor(Color.red);
        g.drawString(msg, 20, 90);
        
        //Life Bars
        msg="------------";
        g.setColor(Color.black);
        g.drawString(msg, 20, 40);
        for(int i=0,j=20; i<lifeBars; i++,j+=8)
        {
        	g.fillRoundRect(j, 40, 5, 25, 6, 6);
        }
        g.drawLine(68, 36, 68, 68);
        g.drawString(msg, 20, 72);
        
    }
    private void drawGrappleHook(Graphics2D g)
    {
        if(grappleHook.isVisible())
        {
            grappleHook.setRotation(grappleHookRotation);
            grappleHook.drawTransformed(g);
        	grappleHook.setOffsets(xOffset, yOffset);
        	g.setColor(Color.black);
        	g.setStroke(new BasicStroke(3));
        	if(lookingRight)
        		g.drawLine(	(int)player.getX()+(int)player.getWidth()+xOffset,
        					(int)player.getY()+26+yOffset,
        					(int)grappleHook.getX()+xOffset,
        					(int)grappleHook.getY()+(int)(grappleHook.getHeight()/2)+yOffset);
        	else
            	g.drawLine(	(int)player.getX()+xOffset,
        					(int)player.getY()+26+yOffset,
        					(int)grappleHook.getX()+xOffset,
        					(int)grappleHook.getY()+(int)(grappleHook.getHeight()/2)+yOffset);
        	//reset stroke
        	g.setStroke(new BasicStroke(0));
        }
    }
    private void drawCrate(Graphics2D g)
    {
        crate.drawTransformed(g);
        crate.setOffsets(xOffset, yOffset);
        //setup next spawn position
        if(currCrate<crateSpawnPositions.size() && !crateHit)
        {
	        CrateSpawnPosition<Float, Float> p = crateSpawnPositions.get(currCrate);
	        if(player.getX()+screenWidth>p.getX())
	        {
	        	crate.setX(p.getX());
	            crate.setY(p.getY());	            
	            crate.show();
	        }
        }
    }
    /**
     * Used to determine which Animation should be played depending on the player state
      */
    private Animation getAppropriateAnimation()
    {
    	if(!grappleHook.isVisible())
    	{
    		if(playerState.equals(EPlayerState.CROUCH))
    			return crouch;
    		if(playerState.equals(EPlayerState.CROUCH_MOVE_LEFT))
    			return crouch_move_left;
    		if(playerState.equals(EPlayerState.CROUCH_MOVE_RIGHT))
    			return crouch_move_right;
	    	if(playerState.equals(EPlayerState.RUN_RIGHT))
	    		return movement_Right;
	    	if(playerState.equals(EPlayerState.RUN_LEFT))
	    		return movement_Left;
	    	if(playerState.equals(EPlayerState.JUMP_RIGHT))
	    		return jump_Right;
	    	if(playerState.equals(EPlayerState.JUMP_LEFT))
	    		return jump_Left;
	    	if(playerState.equals(EPlayerState.JUMP) || playerState.equals(EPlayerState.FALLING))
	    		if(lookingRight)
	    			return jump_Right;
	    		else 
	    			return jump_Left;
			if(lookingRight)
				return standingFacingRight;
			else 
				return standingFacingLeft;
    	}
    	else
    	{
	    	if(lookingRight)
				return grappleHookGun_Right;
			else 
				return grappleHookGun_Left;

    	}
    }
    /**
     * Occurs when the player takes damage from any source
     */
    private void playerTakeDamage(long elapsed)
    {
    	lifeBars--;
    	invincible=true;
    	if(lifeBars<1)
    		playerState = EPlayerState.RESPAWN;
    }
    /**
     * Handles Shooting by thugs
     */
    private void thugShoot(Sprite s) 
    {
    	if(!thugProjectile.isVisible() || thugProjectile.getX()+screenWidth<thug.getX() || thugProjectile.getX()-screenWidth>thug.getX())
    	{
			Velocity v;
			if(player.getX()>s.getX())
			{
				thugProjectile.setX(s.getX()+s.getWidth());
				thugProjectile.setY(s.getY()+20);
				v = new Velocity(0.3f,thugProjectile.getX()+xOffset,thugProjectile.getY()+yOffset,thug.getX()+xOffset+50,thug.getY()+26+yOffset);
			}
			else
			{
				thugProjectile.setX(s.getX());
				thugProjectile.setY(s.getY()+20);
				v = new Velocity(0.3f,thugProjectile.getX()+xOffset,thugProjectile.getY()+yOffset,thug.getX()+xOffset-50,thug.getY()+26+yOffset);
			}

			
			thugProjectile.setVelocityX((float)v.getdx());
			thugProjectile.show();
    	}
	}
  	/**
  	 * Create and add all the (x,y) locations to spawn a crate there
       */
    private void initialiseCrateSpawnPoints()
    {
      	CrateSpawnPosition<Float,Float> p = new CrateSpawnPosition<Float,Float>(704.f,160.f);
      	crateSpawnPositions.add(p);
      	p = new CrateSpawnPosition<Float,Float>(1408.f,160.f);
      	crateSpawnPositions.add(p);
    }
    /**
     * Reset player position and velocity.
  	 * @param defaultX The value for the X position
  	 * @param defaultY The value for the Y position
  	 * @param defaultDX The value for the Horizontal(X) velocity
  	 * @param defaultDY The value for the Vertical(Y) velocity
  	 */
  	private void resetSpritePositionAndVelocity(Sprite s, float defaultX, float defaultY, float defaultDX, float defaultDY)
  	{  
  		s.setX(defaultX);
  		s.setY(defaultY);
  		s.setVelocityX(defaultDX);
  		s.setVelocityY(defaultDY);
  	}
    /**
     * Simulate the effect that the grapple hook retracts back into the grapple gun.
     */
  	private void retractGrappleHook()
    {
  		Velocity v = null;
  		if(lookingRight)
			v = new Velocity(0.5f, grappleHook.getX(), grappleHook.getY(), player.getX()+player.getWidth(), player.getY()+20);
		else
			v = new Velocity(0.5f,  grappleHook.getX(), grappleHook.getY(),player.getX(), player.getY()+player.getHeight()/2);
  		
		grappleHook.setVelocityX((float)v.getdx());
		grappleHook.setVelocityY((float)v.getdy());
		grappleHookRetracting = true;
    }
  	/**
  	 * Return appropriate player state depending on the keys that have been pressed
     */
    private EPlayerState getPlayerStateBasedOnKeysPressed()
    {
    	if(rightKey && !crouchKey && !jumpKey)
    	{
    		lookingRight = true;
    		return EPlayerState.RUN_RIGHT;
    	}
    	
    	if(leftKey && !crouchKey && !jumpKey)
    	{
    		lookingRight = false;
    		return EPlayerState.RUN_LEFT;
    	}
    	
    	if(crouchKey)
    	{
    		if(rightKey) 
    			return EPlayerState.CROUCH_MOVE_RIGHT;
    		else if(leftKey) 
    			return EPlayerState.CROUCH_MOVE_LEFT;
    		else 
    			return EPlayerState.CROUCH;
    	}
    	
    	if(jumpKey && collisionBELOW)
    	{
    		if(!playerState.equals(EPlayerState.JUMP_RIGHT)
	    			&& !playerState.equals(EPlayerState.JUMP_LEFT)
	    			&& !playerState.equals(EPlayerState.JUMP))
    			posY=player.getY();
    		if(rightKey)
    			return EPlayerState.JUMP_RIGHT;
    		else if(leftKey)
    			return EPlayerState.JUMP_LEFT;
    		else
    			return EPlayerState.JUMP;

    	}
    	return EPlayerState.STANDING;
    }

  	
    /*
     *         KEY EVENTS
     */
    public void keyPressed(KeyEvent e) 
    { 
    	int key = e.getKeyCode();
    	
    	if (key == KeyEvent.VK_ESCAPE)
    		stop();

    	if( key == KeyEvent.VK_RIGHT) 
	    	rightKey = true;
    	
    	if(key == KeyEvent.VK_LEFT) 
    		leftKey=true;
    	
    	if (key == KeyEvent.VK_UP && !jumpKey)
    		jumpKey = true;
    	
    	if(key==KeyEvent.VK_DOWN)
	    	crouchKey = true;
    	
    	if (key == KeyEvent.VK_S)
    	{
    		Sound s = new Sound("sounds/caw.wav");
    		s.start();
    	}
    	
    	if(key==KeyEvent.VK_R)
    		resetSpritePositionAndVelocity(player,0,100,0,0);

    	playerState = getPlayerStateBasedOnKeysPressed();
    }
	public void keyReleased(KeyEvent e) 
	{
		int key = e.getKeyCode();
		switch (key)
		{
			case KeyEvent.VK_ESCAPE:
			{
				stop(); 
				break;
			}
			case KeyEvent.VK_UP:
			{
				jumpKey = false;
				break;
			}
			
			case KeyEvent.VK_RIGHT:
			{
				if(!crouchKey)
					playerState = EPlayerState.STANDING;
				else
					playerState = EPlayerState.CROUCH;
				rightKey = false;
				break;
			}
			
			case KeyEvent.VK_LEFT:
			{
				if(!crouchKey)
					playerState = EPlayerState.STANDING;
				else
					playerState = EPlayerState.CROUCH;
				leftKey = false;
				break;
			}
			
			case KeyEvent.VK_DOWN:
			{
				if(!collisionABOVE)
				{
					playerState = EPlayerState.STANDING;
					player.shiftY(-32);
					crouchKey = false;
				}
				break;
			}
			
			default:
				break;
		}
	}
    
	
	/*
	 * 			MOUSE EVENTS 
	 */
	public void mouseDragged(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) 
	{
		if(e.getX()<player.getX()+xOffset)
		{
			if(playerState.equals(EPlayerState.STANDING))
			{
				if(lookingRight)
				{
					player.setAnimation(getAppropriateAnimation());
					lookingRight=false;
				}
			}
		}
		else
		{
			if(playerState.equals(EPlayerState.STANDING))
			{
				if(!lookingRight)
				{
					player.setAnimation(getAppropriateAnimation());
					lookingRight=true;
				}
			}
		}
		if(!cursorChanged)
		{
			Toolkit tk = Toolkit.getDefaultToolkit();
			Cursor c = tk.createCustomCursor(loadImage("assets/images/CrossHair/crosshair.png"), new Point(0,0), "custom cursor");
			setCursor(c);
			cursorChanged=true;
		}
	}
	public void mouseWheelMoved(MouseWheelEvent e) 
	{
		//find the index of the currently selected gadget
		int count=0;
		for(String s:gadgets)
		{
			if(currentGadget.equals(s)) 
				break;
			count++;
		}
		//get the next gadget
		if(e.getWheelRotation()>0)
		{
			count++;
			if(count==gadgets.length)
				currentGadget = gadgets[0];
			else 
				currentGadget = gadgets[count];
		}
		//get the previous gadget
		if(e.getWheelRotation()<0)
		{
			count--;
			if(count==-1)
				currentGadget = gadgets[gadgets.length-1];
			else 
				currentGadget = gadgets[count];
		}
	}
	public void mousePressed(MouseEvent e) 
	{
		if(currentGadget.equals("Grapple Hook"))
		{
			if(!grappleHook.isVisible())
			{
				Velocity v;
				if(lookingRight)
				{
					grappleHook.setX(player.getX()+player.getWidth());
					grappleHook.setY(player.getY()+20);
				}
				else
				{
					grappleHook.setX(player.getX());
					grappleHook.setY(player.getY()+20);
				}
				v = new Velocity(0.5f, grappleHook.getX()+xOffset, grappleHook.getY()+yOffset, e.getX()+10, e.getY()+10);
				grappleHook.setVelocityX((float)v.getdx());
				grappleHook.setVelocityY((float)v.getdy());
				grappleHookRotation = v.getAngle();
				grappleHook.show();
			}
		}
		
		if(currentGadget.equals("Batarang"))
		{
			if(!batarang.isVisible())
			{
				Velocity v;
				if(lookingRight)
				{
					batarang.setX(player.getX()+player.getWidth());
					batarang.setY(player.getY()+26);
				}
				else
				{
					batarang.setX(player.getX());
					batarang.setY(player.getY()+26);	
				}
				v = new Velocity(.5f,batarang.getX()+xOffset,batarang.getY()+yOffset,e.getX()+10,e.getY()+10);
				batarang.setVelocityX((float)v.getdx());
				batarang.setVelocityY((float)v.getdy());
				batarang.show();
			}
		}
	}
	public void mouseClicked(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}

	
	
	/*
	 *		COLLISION DETECTION AND RECOVERY METHODS
	 */
	
    public boolean boundingBoxCollision(Sprite s1, Sprite s2)
    {
    	return ((s1.getX() + s1.getWidth()) >= s2.getX()) && (s1.getX() <= s2.getX()+ s2.getWidth()) &&
    			(s1.getY() + s1.getHeight()) >= s2.getY() && (s1.getY() <= s2.getY() + s2.getHeight());
    }
    public boolean boundingCircleCollision(Sprite s1, Sprite s2)
    {
    	float dx, dy, min;
    	dx = (s1.getX()-s2.getX());
    	dy = (s1.getY()-s2.getY());
    	min = (s1.getRadius()+s2.getRadius());
    	return ((dx*dx)+(dy*dy))<(min*min);
    }
    
    /**
     * Push Sprite UP by one pixel if sprite is stuck in a tile below it.
     * @param s Sprite to check and unstuck.
     * */
	private void recoverSpriteStuckInBottomTile(Sprite s) 
	{
		if(tmap.getTileChar(((int)s.getX()+s.getWidth()/2)/tmap.getTileWidth(), (int)(s.getY()+s.getHeight()-1)/tmap.getTileHeight())!='.')
			s.setY(s.getY()-1);
	}
    /**
     * Push Sprite LEFT by one pixel if sprite is stuck in a tile below it.
     * @param s Sprite to check and unstuck.
     * */
	private void recoverSpriteStuckInRightTile(Sprite s) 
	{
		if(tmap.getTileChar(((int)s.getX()+s.getWidth()-1)/tmap.getTileWidth(), (int)(s.getY()+s.getHeight())/tmap.getTileHeight())!='.')
			s.setX(s.getX()-1);
	}
    /**
     * Push Sprite RIGHT by one pixel if sprite is stuck in a tile below it.
     * @param s Sprite to check and unstuck.
     * */
	private void recoverSpriteStuckInLeftTile(Sprite s) 
	{
		if(tmap.getTileChar(((int)s.getX()-1)/tmap.getTileWidth(), (int)(s.getY()+s.getHeight())/tmap.getTileHeight())!='.')
			s.setX(s.getX()+1);
	}
	private boolean checkTopSideForCollision(Sprite s) 
	{
		boolean hit = false;
		for(int i=1; i<s.getWidth()-1; i++)
		{
			char tileCharTop = tmap.getTileChar(((int)s.getX()+i)/tmap.getTileWidth(), (int)(s.getY()-1)/tmap.getTileHeight());
			if(tileCharTop=='b' || tileCharTop == 'w' || tileCharTop == 'r' || tileCharTop == 'c')
				hit =true;
		}

		return hit;
	}
	private boolean checkLeftSideForCollision(Sprite s) 
	{
		boolean hit = false;
		for(int i=1; i<s.getHeight()-3; i++)
		{
			char tileCharLeft = tmap.getTileChar(((int)s.getX()-1)/tmap.getTileWidth(), ((int)s.getY()+i)/tmap.getTileHeight());
			if(tileCharLeft=='b' || tileCharLeft == 'w' || tileCharLeft == 'r' || tileCharLeft == 'c')
				hit =true;
		}

		return hit;
	}
	private boolean checkRightSideForCollision(Sprite s) 
	{
		boolean hit = false;
		for(int i=1; i<s.getHeight()-3; i++)
		{
			char tileCharRight = tmap.getTileChar(((int)s.getX()+s.getWidth()+1)/tmap.getTileWidth(), (int)(s.getY()+i)/tmap.getTileHeight());
			if(tileCharRight=='b' || tileCharRight == 'w' || tileCharRight == 'r' || tileCharRight == 'c')
				hit =true;
		}
		return hit;
	}
	private boolean checkBottomSideForCollision(Sprite s)
	{
		boolean hit = false;
		for(int i=1; i<s.getWidth()-1; i++)
		{
			char tileCharBottom = tmap.getTileChar(((int)s.getX()+i)/tmap.getTileWidth(), (int)(s.getY()+s.getHeight())/tmap.getTileHeight());
			if(tileCharBottom=='b' || tileCharBottom == 'w' || tileCharBottom == 'r' || tileCharBottom == 'c')
				hit =true;
		}
		return hit;
	}
	@SuppressWarnings("unused")
	private void log(String s)
	{
		System.out.print(s);
		//TODO DELETE ME AT THE END
	}
	
	
	/*
	 *     LOAD MAP,IMAGES,ANIMATIONS
	 */
	private void loadAnimations()
	{
		tmap.loadMap("assets/maps", "level1.txt");

        bgImage = loadImage("assets/images/city.png");
        
        standingFacingRight = new Animation();
        standingFacingRight.addFrame(loadImage("assets/images/BatmanStates/BatmanFacingRight.gif"),60);
        
        standingFacingLeft = new Animation();
        standingFacingLeft.addFrame(loadImage("assets/images/BatmanStates/BatmanFacingLeft.gif"), 60);
        
        movement_Right= new Animation();
        movement_Right.addFrame(loadImage("assets/images/BatmanStates/BatmanMoveRight.gif"), 60);
        
        movement_Left = new Animation();
        movement_Left.addFrame(loadImage("assets/images/BatmanStates/BatmanMoveLeft.gif"),60);
        
        jump_Right = new Animation();
        jump_Right.addFrame(loadImage("assets/images/BatmanStates/BatmanJumpRight.png"),60);
        
        jump_Left = new Animation();
        jump_Left.addFrame(loadImage("assets/images/BatmanStates/BatmanJumpLeft.png"),60);
        
        crateAnim = new Animation();
        crateAnim.addFrame(loadImage("assets/maps/crate.png"), 60);
        
        grapple = new Animation();
        grapple.addFrame(loadImage("assets/images/Projectiles/GrappleHook.png"), 60);
        
        grappleHookGun_Right = new Animation();
        grappleHookGun_Right.addFrame(loadImage("assets/images/BatmanWithGadgets/BatmanGrappleHookGunRight.gif"), 60);
        
        grappleHookGun_Left = new Animation();
        grappleHookGun_Left.addFrame(loadImage("assets/images/BatmanWithGadgets/BatmanGrappleHookGunLeft.gif"), 60);
        
        thugAnim = new Animation();//TODO CHANGE ANIMATION FOR THUG
        thugAnim.addFrame(loadImage("assets/images/BatmanWithGadgets/BatmanGrappleHookGunLeft.gif"), 60);
        
        thugProjectileAnim = new Animation();
        thugProjectileAnim.addFrame(loadImage("assets/images/Projectiles/thugProjectile.png"), 60);
        
        batarangAnim = new Animation();
        batarangAnim.addFrame(loadImage("assets/images/Projectiles/thugProjectile.png"), 60);
        
        transparent = new Animation();
        transparent.addFrame(loadImage("assets/images/BatmanStates/transparent.png"), 60);
        
        crouch = new Animation();
        crouch.addFrame(loadImage("assets/images/testCube.png"), 60);
        
        crouch_move_right = new Animation();
        crouch_move_right.addFrame(loadImage("assets/images/testCube.png"), 60);
        
        crouch_move_left = new Animation();
        crouch_move_left.addFrame(loadImage("assets/images/testCube.png"), 60);
	}
	private void loadSprites()
	{
		  	player = new Sprite(standingFacingRight);
	        player.setTag("player");
	        
	        grappleHook = new Sprite(grapple);
	        grappleHook.setTag("grappleHook");
	        grappleHook.hide();
	        
	        crate = new Sprite(crateAnim);
	        crate.setTag("crate");
	        
	        thug = new Sprite(thugAnim);
	        thug.setTag("thug");
	        
	        thugProjectile = new Sprite(thugProjectileAnim);
	        thugProjectile.setTag("thugProjectile");
	        
	        batarang = new Sprite(batarangAnim);
	        batarang.setTag("batarang");
	        
	        Animation ca = new Animation();
	        ca.addFrame(loadImage("images/cloud.png"), 1000); //TODO REPLACE IMAGE WITH BATS
	        
	        Sprite s;
	        for (int c=0; c<3; c++)
	        {
	        	s = new Sprite(ca);
	        	s.setX(screenWidth + (int)(Math.random()*200.0f));
	        	s.setY(30 + (int)(Math.random()*150.0f));
	        	s.setVelocityX(-0.02f);
	        	s.show();
	        	clouds.add(s);
	        }
	}

}