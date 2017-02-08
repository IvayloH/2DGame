import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
@SuppressWarnings("serial")

public class Game extends GameCore implements MouseListener
{
	// Useful game constants
	static final int screenWidth = 512;   //512
	static final int screenHeight = 384;  //384
	static final float RUNSPEED = .07f;
	static final float JUMPHEIGHT = 48;  // ??? tile.height()/2 + tile.height()/4
	
    float lift = 0.005f;
    float gravity = 0.01f;
    float posY = .0f;  // keep track of jump start
    
    // Game state flags
    boolean collisionRIGHT = false;
    boolean collisionLEFT = false;
    boolean collisionABOVE = false;
    boolean collisionBELOW = false;
    
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
    
    Sprite	player = null;
    Sprite grappleHook = null;
    
    
    ArrayList<Sprite> clouds = new ArrayList<Sprite>();
    TileMap tmap = new TileMap();	// Our tile map, note that we load it in init()    
    int lives = 0;         			// number of lives left


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

        // Load the tile map and print it out so we can check it is valid
        tmap.loadMap("assets/maps", "map.txt");

        // Create a set of background sprites that we can 
        // rearrange to give the illusion of motion
        
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
        
        // Initialise the player with an animation
        player = new Sprite(standingFacingRight);
        
        player.setTag("player");
        
        //Initialise the grapple hook with an animation
        grapple = new Animation();
        grapple.addFrame(loadImage("assets/images/grapple.png"), 100);
        grappleHook = new Sprite(grapple);
        grappleHook.setTag("grappleHook");
        grappleHook.hide();
        
        // Load a single cloud animation
        Animation ca = new Animation();
        ca.addFrame(loadImage("images/cloud.png"), 1000);
        
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
    	lives = 3;
    	resetPlayerPositionAndVelocity(0,100,0,0);
        player.show();
    }
    
    /**
     * Draw the current state of the game
     */
    public void draw(Graphics2D g)
    {
    	// First work out how much we need to shift the view 
    	// in order to see where the player is.
        int xo = 0;
        int yo = 0;
        
        
        g.setColor(Color.blue);
        g.fillRect(0, 0, getWidth(), getHeight());
        
        // Apply offsets to sprites then draw them
        for (Sprite s: clouds)
        {
        	s.setOffsets(xo,yo);
        	s.draw(g);
        }

        // Apply offsets to player and draw 
        player.setOffsets(-xo, yo);
        player.draw(g);
                
        // Apply offsets to tile map and draw  it
        tmap.draw(g,xo,yo);
        
        grappleHook.draw(g);
        // Show score and status information
        String msg = String.format("Lives: %d", lives);
        g.setColor(Color.red);
        g.drawString(msg, getWidth() - 80, 50);
        
        addMouseListener(this);
    }

    /**
     * Update any sprites and check for collisions
     * 
     * @param elapsed The elapsed time between this call and the previous call of elapsed
     */    
    public void update(long elapsed)
    {
    	if (grappleHook.isVisible())
    		grappleHook.update(elapsed);	
 
    	if(playerState.equals(EPlayerState.DEAD)) 
    	{
    		lives--;
    		if(lives<1) 
    		{
    			stop(); // stop game if player loses all lives
    			//TODO add an end game state
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
    	
    	if(playerState.equals(EPlayerState.RUN_RIGHT))
    	{
    		if(collisionRIGHT) 
    			player.setVelocityX(.0f);
    		else
    			player.setVelocityX(RUNSPEED);
    	}
    	
    	if(playerState.equals(EPlayerState.RUN_LEFT))
    	{
    		if(collisionLEFT) 
    			player.setVelocityX(.0f);
    		else
    			player.setVelocityX(-RUNSPEED);
    	}
    	
    	if(collisionBELOW && !playerState.equals(EPlayerState.JUMP) && !playerState.equals(EPlayerState.JUMP_LEFT)&& !playerState.equals(EPlayerState.JUMP_RIGHT))
    		player.setVelocityY(.0f);
    	else if(!playerState.equals(EPlayerState.JUMP)&& !playerState.equals(EPlayerState.JUMP_LEFT) && !playerState.equals(EPlayerState.JUMP_RIGHT)) 
    	{
    		player.setVelocityY(.05f);
    		player.setVelocityY(player.getVelocityY()+(gravity*elapsed)); // Make adjustments to the speed of the sprite due to gravity
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
     	//flag indicating whether the sprite has hit a tile 
    	boolean hit = false;
        if (s.getY() + s.getHeight() > tmap.getPixelHeight())
        {
        	if(s.getTag().equals("player")) 
        		playerState = EPlayerState.DEAD;
        	else
        		s.hide();
        }
        
        //Check Tile underneath the player for collision
        if(true)
        {
        	for(int i=1; i<player.getWidth()-1; i++)
        	{
        		char tileCharBottom = tmap.getTileChar(((int)s.getX()+i)/tmap.getTileWidth(), (int)(s.getY()+player.getHeight()+1)/tmap.getTileHeight());
	    		if(tileCharBottom=='p' || tileCharBottom == 't' || tileCharBottom == 'b')
	    			hit =true;
        	}
        	
        	if(hit)
        	{
        		collisionBELOW = true;
        	}
        	else 
        		collisionBELOW=false;
        }
        
        //Check Tile to the RIGHT of the player for collision
        if(playerState.equals(EPlayerState.RUN_RIGHT))
        {
        	hit = false;
        	for(int i=1; i<player.getHeight()-1; i++)
        	{
        		char tileCharRight = tmap.getTileChar(((int)s.getX()+s.getWidth()+1)/tmap.getTileWidth(), (int)(s.getY()+i)/tmap.getTileHeight());
	    		if(tileCharRight=='p' || tileCharRight == 't' || tileCharRight == 'b')
	    			hit =true;
        	}
        	System.out.println("1:"+playerState.toString()+ "  "+hit);
        	if(hit)
        		collisionRIGHT = true;
        	else 
        		collisionRIGHT=false;
        }
        
        //Check Tile to the LEFT of the player for collision
        if(playerState.equals(EPlayerState.RUN_LEFT))
        {
        	hit = false;
        	for(int i=1; i<player.getHeight()-1; i++)
        	{
        		char tileCharLeft = tmap.getTileChar(((int)s.getX()-1)/tmap.getTileWidth(), ((int)s.getY()+i)/tmap.getTileHeight());
	    		if(tileCharLeft=='p' || tileCharLeft == 't' || tileCharLeft == 'b')
	    			hit =true;
        	}
        	System.out.println("2:"+playerState.toString()+ "  "+hit);
        	if(hit)
        		collisionLEFT = true;
        	else 
        		collisionLEFT=false;
        }
        
        //Check Tile ABOVE the player for collision
        if(playerState.equals(EPlayerState.JUMP) || playerState.equals(EPlayerState.JUMP_RIGHT) || playerState.equals(EPlayerState.JUMP_LEFT))
        {
        	//flag indicating whether the sprite has hit a tile 
        	hit = false;
        	for(int i=1; i<player.getWidth()-1; i++)
        	{
        		char tileCharTop = tmap.getTileChar(((int)s.getX()+i)/tmap.getTileWidth(), (int)(s.getY()-1)/tmap.getTileHeight());
	    		if(tileCharTop=='p' || tileCharTop == 't' || tileCharTop == 'b')
	    			hit =true;
        	}
        	System.out.println("2.1:"+playerState.toString()+ "  "+hit);
        	if(hit)
        		collisionABOVE = true;
        	else 
        		collisionABOVE=false;
        	
        	if(playerState.equals(EPlayerState.JUMP_RIGHT))
        	{
        		boolean hit2 = false;
	        	for(int i=1; i<player.getHeight()-1; i++)
	        	{
	        		char tileCharRight = tmap.getTileChar(((int)s.getX()+s.getWidth()+1)/tmap.getTileWidth(), (int)(s.getY()+i)/tmap.getTileHeight());
		    		if(tileCharRight=='p' || tileCharRight == 't' || tileCharRight == 'b')
		    			hit2 =true;
	        	}
	        	System.out.println("2.2:"+playerState.toString()+ "  "+hit2);
	        	if(hit2)
	        		collisionRIGHT = true;
	        	else 
	        		collisionRIGHT=false;
        	}
        	
        	if(playerState.equals(EPlayerState.JUMP_LEFT))
        	{
        		hit = false;
            	for(int i=1; i<player.getHeight()-1; i++)
            	{
            		char tileCharLeft = tmap.getTileChar(((int)s.getX()-1)/tmap.getTileWidth(), ((int)s.getY()+i)/tmap.getTileHeight());
    	    		if(tileCharLeft=='p' || tileCharLeft == 't' || tileCharLeft == 'b')
    	    			hit =true;
            	}
            	System.out.println("2:"+playerState.toString()+ "  "+hit);
            	if(hit)
            		collisionLEFT = true;
            	else 
            		collisionLEFT=false;
        	}
        }
       /*
        *	TODO add collision detection if player+height goes into another tile(a.k.a. player is stuck in a tile underneath so needs to be moved up a bit
        */
        
        /*
         *     TODO GRAPPLE HOOK COLLISIONS
         */
        
        if(grappleHook.isVisible())
        {
            //Getting the grappleHook current location on the tile map
            char hookLocationTileMap = tmap.getTileChar((int)(grappleHook.getX()/tmap.getTileWidth()), (int)(grappleHook.getY()/tmap.getTileHeight())); 
            if(hookLocationTileMap=='p' || hookLocationTileMap=='t' || hookLocationTileMap=='b')
            	grappleHook.hide();
            //TODO hide hook after it goes off display; update collision detection
        }
        
    }    
    
    /**
     * Override of the keyPressed event defined in GameCore to catch our
     * own events
     * 
     *  @param e The event that has been generated
     */
    public void keyPressed(KeyEvent e) 
    { 
    	//TODO Flag the keys pressed and decide on the action at the end
    	int key = e.getKeyCode();
    	
    	if (key == KeyEvent.VK_ESCAPE) 
    		stop();
    	
    	if( key == KeyEvent.VK_RIGHT) 
    	{
    		playerState = EPlayerState.RUN_RIGHT;
    		rightKey = true;
    		lookingRight=true;
    		player.setAnimation(movement_Right);
    	}
    	
    	if(key == KeyEvent.VK_LEFT) 
    	{	
    		playerState = EPlayerState.RUN_LEFT;
    		leftKey=true;
    		lookingRight=false;
    		player.setAnimation(movement_Left);
    	}
    	
    	if (key == KeyEvent.VK_UP && collisionBELOW)
    	{
    		posY=player.getY();
    		if(rightKey)
    			playerState = EPlayerState.JUMP_RIGHT;
    		else if(leftKey)
    			playerState = EPlayerState.JUMP_LEFT;
    		else
    			playerState = EPlayerState.JUMP;
    		
    		if(lookingRight)
				player.setAnimation(jump_Right);
			else
				player.setAnimation(jump_Left);
    		
    		
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
    	}
    	e.consume();
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
	
    public boolean boundingBoxCollision(Sprite s1, Sprite s2)
    {
    	//TODO grappleHook and Box
    	return false;   	
    }

	
	public void mousePressed(MouseEvent e) 
	{
		if(!grappleHook.isVisible())
		{
			grappleHook.setX(player.getX()+player.getWidth());
			grappleHook.setY(player.getY()+player.getHeight()/2);
			
			Velocity v = new Velocity(0.5f, player.getX() + 90, player.getY() + 15, e.getX(), e.getY());
			grappleHook.setVelocityX((float)v.getdx());
			grappleHook.setVelocityY((float)v.getdy());
			
			grappleHook.show();
		}
	}
	public void mouseClicked(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}
	
	
	/**
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
}