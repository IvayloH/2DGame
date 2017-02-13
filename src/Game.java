import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.awt.Cursor;
import java.awt.Toolkit;

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
    
    int xOffset, yOffset;
    
    // Game state flags
    boolean collisionRIGHT = false;
    boolean collisionLEFT = false;
    boolean collisionABOVE = false;
    boolean collisionBELOW = false;
    boolean crateHit = false;
    boolean grappleHookRetracting = false;
    boolean cursorChanged = false;
    
    //Pressed Key flags
    boolean leftKey = false;
    boolean rightKey = false;
    boolean spaceKey = false;
    
    // Batman Direction
    boolean lookingRight = true;

    enum EPlayerState
    {
    	RUN_LEFT,
    	RUN_RIGHT,
    	JUMP,
    	JUMP_LEFT,
    	JUMP_RIGHT,
    	STANDING,
    	FALLING,
    	TAKING_DAMAGE,
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
    
    Sprite	player = null;
    Sprite grappleHook = null;
    Sprite thug = null;
    Sprite crate = null;
    
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
        Sprite s;	// Temporary reference to a sprite

        tmap.loadMap("assets/maps", "level1.txt");

        bgImage = loadImage("assets/images/city.png");
        
        standingFacingRight = new Animation();
        standingFacingRight.addFrame(loadImage("assets/images/BatmanFacingRight.gif"),60);
        
        standingFacingLeft = new Animation();
        standingFacingLeft.addFrame(loadImage("assets/images/BatmanFacingLeft.gif"), 60);
        
        movement_Right= new Animation();
        movement_Right.addFrame(loadImage("assets/images/BatmanMoveRight.gif"), 60);
        
        movement_Left = new Animation();
        movement_Left.addFrame(loadImage("assets/images/BatmanMoveLeft.gif"),60);
        
        jump_Right = new Animation();
        jump_Right.addFrame(loadImage("assets/images/BatmanJumpRight.png"),60);
        
        jump_Left = new Animation();
        jump_Left.addFrame(loadImage("assets/images/BatmanJumpLeft.png"),60);
        
        crateAnim = new Animation();
        crateAnim.addFrame(loadImage("assets/maps/crate.png"), 60);
        
        grapple = new Animation();
        grapple.addFrame(loadImage("assets/images/grapple.png"), 60);
        
        grappleHookGun_Right = new Animation();
        grappleHookGun_Right.addFrame(loadImage("assets/images/BatmanGrappleHookGunRight.gif"), 60);
        
        grappleHookGun_Left = new Animation();
        grappleHookGun_Left.addFrame(loadImage("assets/images/BatmanGrappleHookGunLeft.gif"), 60);
        
        // Initialise the player with an animation
        player = new Sprite(standingFacingRight);
        player.setTag("player");
        
        //Initialise the grapple hook with an animation
        grappleHook = new Sprite(grapple);
        grappleHook.setTag("grappleHook");
        grappleHook.hide();
        
        //Initialise the crate with an animation
        crate = new Sprite(crateAnim);
        crate.setTag("crate");
        
        // Load a single cloud animation
        Animation ca = new Animation();
        ca.addFrame(loadImage("images/cloud.png"), 1000); //TODO REPLACE IMAGE WITH BATS
        
        // Create 3 clouds at random positions off the screen
        // to the right
        for (int c=0; c<3; c++)
        {
        	s = new Sprite(ca);
        	s.setX(screenWidth + (int)(Math.random()*200.0f));
        	s.setY(30 + (int)(Math.random()*150.0f));
        	s.setVelocityX(-0.02f);
        	s.show();
        	clouds.add(s);
        }
        addMouseListener(this);
        addMouseWheelListener(this);
        addMouseMotionListener(this);
        
        //TODO generate Crate Spawn positions and add them to the list
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
    	resetPlayerPositionAndVelocity(50,100,0,0);
        player.show();
    }

    /**
     * Draw the current state of the game
     */
    public void draw(Graphics2D g)
    {
    	xOffset = screenWidth/2-(int)player.getX(); //FIXME
    	//yOffset=screenHeight/2-(int)player.getY();
    	yOffset = 0;
        int minOffsetX= screenWidth-tmap.getPixelWidth();
        int maxOffsetX = 0;
        
        if(xOffset>maxOffsetX)
        	xOffset = maxOffsetX;
        else if(xOffset<minOffsetX)
        	xOffset=minOffsetX;
                 
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
        
        // Apply offsets to tile map and draw  it
        tmap.draw(g,xOffset,yOffset);
        
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
        
        //draw hook and line behind it
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

        // draw crate 
        crate.drawTransformed(g);
        crate.setOffsets(xOffset, yOffset);
        if(currCrate<crateSpawnPositions.size() && !crateHit)
        {
	        CrateSpawnPosition<Float, Float> p = crateSpawnPositions.get(currCrate);
	        if(player.getX()+screenWidth>p.getX())
	        {
	        	crate.setX(p.getX());
	            crate.setY(p.getY());	            
	            crate.show();
	        }
	        else if(!crateHit)
	        	crate.hide();
        }
    }

    /**
     * Update any sprites and check for collisions
     * 
     * @param elapsed The elapsed time between this call and the previous call of elapsed
     */    
    public void update(long elapsed)
    {
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
    	
    	if (grappleHook.isVisible())
    	{
    		grappleHook.update(elapsed);
    		if((grappleHook.getX()>player.getX()+player.getWidth()+HOOKLIMIT)
    				|| (grappleHook.getX()<player.getX()-HOOKLIMIT)
    				|| (grappleHook.getY()>player.getY()+player.getHeight()/2+HOOKLIMIT)
    				|| (grappleHook.getY()<player.getY()-HOOKLIMIT))
    			retractGrappleHook();
    		
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
    	}
    	
    	if(playerState.equals(EPlayerState.TAKING_DAMAGE)) 
    	{
    		lifeBars--;
    		if(lifeBars<1) 
    		{
    			stop(); // stop game if player loses all lives
    			//TODO add an end game state
    			//playerState = EPlayerState.DEAD;
    		}
    		else
    		{
    			resetPlayerPositionAndVelocity(0,100,0,0);
    			playerState = EPlayerState.FALLING;
    		}
    	}
    		
    	if(playerState.equals(EPlayerState.STANDING))
    	{
    		player.setVelocityX(.0f);
    		player.setVelocityY(.0f);
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
			if(lookingRight && !rightKey)
				player.setAnimation(standingFacingRight);
			else if(!lookingRight && !leftKey)
				player.setAnimation(standingFacingLeft);
			playerState = EPlayerState.STANDING;
			
			if(rightKey)
			{
				player.setAnimation(movement_Right);
				playerState = EPlayerState.RUN_RIGHT;
			}
			else if(leftKey)
			{
				player.setAnimation(movement_Left);
				playerState = EPlayerState.RUN_LEFT;
			}
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
    	//Check if sprite has fallen under screen
        if (s.getY() + s.getHeight() > tmap.getPixelHeight())
        {
        	if(s.getTag().equals("player")) 
        		playerState = EPlayerState.TAKING_DAMAGE;
        	else
        		s.hide();
        }
        
    	//Check Tile underneath the sprite for collision
        collisionBELOW = checkBottomSideForCollision(s);
    	if(collisionBELOW)
    		recoverSpriteStuckInBottomTile(s);
        
        //Check Tile to the RIGHT of the sprite for collision
        if(playerState.equals(EPlayerState.RUN_RIGHT))
        	collisionRIGHT = checkRightSideForCollision(s);

        
        //Check Tile to the LEFT of the sprite for collision
        if(playerState.equals(EPlayerState.RUN_LEFT))
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
        	else
        		if(checkLeftSideForCollision(grappleHook))
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
        }
    }

    
  	/**
  	 * Create and add all the (x,y) locations to spawn a crate there
       */
    private void initialiseCrateSpawnPoints()
    {
      	CrateSpawnPosition<Float,Float> p = new CrateSpawnPosition<Float,Float>(288.f,256.f);
      	crateSpawnPositions.add(p);
      	p = new CrateSpawnPosition<Float,Float>(896.f,256.f);
      	crateSpawnPositions.add(p);
    }
    /**
     * Reset player position and velocity.
  	 * @param defaultX The value for the X position
  	 * @param defaultY The value for the Y position
  	 * @param defaultDX The value for the Horizontal(X) velocity
  	 * @param defaultDY The value for the Vertical(Y) velocity
  	 */
  	private void resetPlayerPositionAndVelocity(float defaultX, float defaultY, float defaultDX, float defaultDY)
  	{  
  		player.setX(defaultX);
  		player.setY(defaultY);
  		player.setVelocityX(defaultDX);
  		player.setVelocityY(defaultDY);
  	}
    /**
     * Simulate the effect that the grapple hook retracts back into the grapple gun.
     */
  	private void retractGrappleHook()
    {
  		Velocity v = null;
  		if(lookingRight)
		{
			v = new Velocity(0.5f, grappleHook.getX(), grappleHook.getY(), player.getX()+player.getWidth(), player.getY()+20);
			grappleHook.setVelocityX((float)v.getdx());
			grappleHook.setVelocityY((float)v.getdy());
		}
		else
		{
			v = new Velocity(0.5f,  grappleHook.getX(), grappleHook.getY(),player.getX(), player.getY()+player.getHeight()/2);
			grappleHook.setVelocityX((float)v.getdx());
			grappleHook.setVelocityY((float)v.getdy());
		}
		grappleHookRetracting = true;
    }
    
    /*
     *         KEY EVENTS
     */
    public void keyPressed(KeyEvent e) 
    { 
    	//TODO Flag the keys pressed and decide on the action at the end
    	int key = e.getKeyCode();
    	
    	if (key == KeyEvent.VK_ESCAPE)
    	{
    		stop();
    	}
    	if(key==KeyEvent.VK_DOWN)
    	{
    		grappleHook.setVelocityX(0);
    		grappleHook.setVelocityY(0);
    	}
    	if( key == KeyEvent.VK_RIGHT) 
    	{
    		playerState = EPlayerState.RUN_RIGHT;
    		rightKey = true;
    		lookingRight=true;
    		if(!grappleHookRetracting)// so player does not move right while grapple gun should be out
    			player.setAnimation(movement_Right);
    	}
    	
    	if(key == KeyEvent.VK_LEFT) 
    	{	
    		playerState = EPlayerState.RUN_LEFT;
    		leftKey=true;
    		lookingRight=false;
    		if(!grappleHookRetracting) // so player does not move left while grapple gun should be out
    			player.setAnimation(movement_Left);
    	}
    	
    	if (key == KeyEvent.VK_UP && collisionBELOW && !spaceKey)
    	{
    		posY=player.getY();
    		if(rightKey)
    			playerState = EPlayerState.JUMP_RIGHT;
    		else if(leftKey)
    			playerState = EPlayerState.JUMP_LEFT;
    		else
    			playerState = EPlayerState.JUMP;
    		
    		if(!grappleHookRetracting)
    		{
    			if(lookingRight)
    				player.setAnimation(jump_Right);
				else
					player.setAnimation(jump_Left);
    		}
    		spaceKey = true;
    		
    	}
    	
    	if (key == KeyEvent.VK_S)
    	{
    		// Example of playing a sound as a thread
    		Sound s = new Sound("sounds/caw.wav");
    		s.start();
    	}
    	
    	if(key==KeyEvent.VK_R)
    	{
    		resetPlayerPositionAndVelocity(0,100,0,0);
    		lifeBars--;
    	}
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
				//TODO modify so player cannot spam jump button 
				playerState = EPlayerState.FALLING;
				spaceKey = false;
				if(lookingRight && !rightKey)
					player.setAnimation(standingFacingRight);
				else if(!lookingRight && !leftKey)
					player.setAnimation(standingFacingLeft);

				if(rightKey)
				{
					player.setAnimation(movement_Right);
					playerState = EPlayerState.RUN_RIGHT;
				}
				else if(leftKey)
				{
					player.setAnimation(movement_Left);
					playerState = EPlayerState.RUN_LEFT;
				}
				break;
			}
			
			case KeyEvent.VK_RIGHT:
			{
				playerState = EPlayerState.STANDING;
				rightKey = false;
				player.setAnimation(standingFacingRight);
				break;
			}
			
			case KeyEvent.VK_LEFT:
			{
				playerState = EPlayerState.STANDING;
				leftKey = false;
				player.setAnimation(standingFacingLeft);
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
					if(!grappleHookRetracting)
						player.setAnimation(standingFacingLeft);
					else
						player.setAnimation(grappleHookGun_Left);
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
					if(!grappleHookRetracting)
						player.setAnimation(standingFacingRight);
					else
						player.setAnimation(grappleHookGun_Right);
					lookingRight=true;
				}
			}
		}
		if(!cursorChanged)
		{
			Toolkit tk = Toolkit.getDefaultToolkit();
			Cursor c = tk.createCustomCursor(loadImage("assets/images/crosshairs.png"), new Point(0,0), "custom cursor");
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
			if(!grappleHook.isVisible())
			{
				Velocity v;
				if(lookingRight)
				{
					grappleHook.setX(player.getX()+player.getWidth());
					grappleHook.setY(player.getY()+20);
					
					v = new Velocity(0.5f, player.getX() + player.getWidth()+xOffset, player.getY() + 20 +yOffset, e.getX()+10, e.getY()+10);
					grappleHook.setVelocityX((float)v.getdx());
					grappleHook.setVelocityY((float)v.getdy());
					
					player.setAnimation(grappleHookGun_Right);
				}
				else
				{
					grappleHook.setX(player.getX());
					grappleHook.setY(player.getY()+26);
					
					v = new Velocity(0.5f, player.getX()+xOffset, player.getY() +26+yOffset, e.getX()+10, e.getY()+10);
					grappleHook.setVelocityX((float)v.getdx());
					grappleHook.setVelocityY((float)v.getdy());
					
					player.setAnimation(grappleHookGun_Left);
				}
				grappleHookRotation = v.getAngle();
				grappleHook.show();
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
		if(tmap.getTileChar(((int)s.getX()+s.getWidth()/2)/tmap.getTileWidth(), (int)(s.getY()+s.getHeight()-2)/tmap.getTileHeight())!='.')
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
		for(int i=1; i<s.getHeight()-1; i++)
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
		for(int i=1; i<s.getHeight()-1; i++)
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
			char tileCharBottom = tmap.getTileChar(((int)s.getX()+i)/tmap.getTileWidth(), (int)(s.getY()+s.getHeight()+1)/tmap.getTileHeight());
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
}