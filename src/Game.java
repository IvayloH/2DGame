import java.awt.*;
import java.awt.event.KeyEvent;
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

public class Game extends GameCore 
{
	static final float RUNSPEED = .07f;
	//
	//  Should images be 31x31 so they fall between tiles? Might Solve Collision Glitch
	//
	//
	
	
	// Useful game constants
	static int screenWidth = 512;   //512
	static int screenHeight = 384;  //384

    float 	lift = 0.005f;
    float	gravity = 0.01f;
    
    // Game state flags
    boolean collisionRIGHT = false;
    boolean collisionLEFT = false;
    boolean collisionABOVE = false;
    boolean collisionBELOW = false;
    
    //Pressed Key flags
    boolean leftKey = false;
    boolean rightKey = false;
    
    enum ESpriteState
    {
    	RUN_LEFT,
    	RUN_RIGHT,
    	JUMP,
    	JUMP_LEFT,
    	JUMP_RIGHT,
    	STANDING,
    	FALLING,
    	DEAD,
    	IDLE
    }
    
    
   //Sprite State
    ESpriteState spriteState = ESpriteState.FALLING;
    
    // Game resources
    Animation landing;
    
    Sprite	player = null;
    
    ArrayList<Sprite> clouds = new ArrayList<Sprite>();

    TileMap tmap = new TileMap();	// Our tile map, note that we load it in init()
    
    long lives;         			// number of lives left


    /**
	 * The obligatory main method that creates
     * an instance of our class and starts it running
     * 
     * @param args	The list of parameters this program might use (ignored)
     */
    public static void main(String[] args) {

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
        
        landing = new Animation();
        landing.loadAnimationFromSheet("assets/images/batman1.png", 1, 1, 60);
        
        // Initialise the player with an animation
        player = new Sprite(landing);
        
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
    	// Be careful about the order in which you draw objects - you
    	// should draw the background first, then work your way 'forward'

    	// First work out how much we need to shift the view 
    	// in order to see where the player is.
        int xo = 0;
        int yo = 0;
        
        //xo = 0-(int)player.getX();
        
        
        // If relative, adjust the offset so that
        // it is relative to the player

        // ...?
        
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
        
        // Show score and status information
        String msg = String.format("Lives: %d", lives);
        g.setColor(Color.darkGray);
        g.drawString(msg, getWidth() - 80, 50);
    }

    /**
     * Update any sprites and check for collisions
     * 
     * @param elapsed The elapsed time between this call and the previous call of elapsed
     */    
    public void update(long elapsed)
    {
    	if(spriteState.equals(ESpriteState.DEAD)) 
    	{
    		lives--;
    		if(lives<1) 
    			stop(); // stop game if player loses all lives
    		else
    		{
    			resetPlayerPositionAndVelocity(0,100,0,0);
    		}
    	}
    		
    	if(spriteState.equals(ESpriteState.STANDING))
    	{
    		player.setVelocityX(.0f);
    		player.setVelocityY(.0f);
    	}
    	
    	if(spriteState.equals(ESpriteState.JUMP))
    	{
    		player.setVelocityY(-.04f);
    	}
    	if(spriteState.equals(ESpriteState.JUMP_LEFT))
    	{
    		//TODO rotate sprite
    		player.setVelocityY(-.04f);
    		player.setVelocityX(-RUNSPEED);
    	}
    	if(spriteState.equals(ESpriteState.JUMP_RIGHT))
    	{
    		player.setVelocityY(-.04f);
    		player.setVelocityX(RUNSPEED);
    	}
    	
    	if(spriteState.equals(ESpriteState.RUN_RIGHT))
    	{
    		if(collisionRIGHT) 
    			player.setVelocityX(.0f);
    		else
    			player.setVelocityX(RUNSPEED);
    	}
    	
    	if(spriteState.equals(ESpriteState.RUN_LEFT))
    	{
    		if(collisionLEFT) 
    			player.setVelocityX(.0f);
    		else
    			player.setVelocityX(-RUNSPEED);
    	}
    	
    	if(collisionBELOW && !spriteState.equals(ESpriteState.JUMP))
    		player.setVelocityY(.0f);
    	else if(!spriteState.equals(ESpriteState.JUMP)) 
    	{
    		player.setVelocityY(.05f);
    		player.setVelocityY(player.getVelocityY()+(gravity*elapsed)); // Make adjustments to the speed of the sprite due to gravity
    	}
    	
       	//player.setAnimationSpeed(1.0f);
                
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
    	// This method should check actual tile map collisions. For
    	// now it just checks if the player has gone off the bottom
    	// of the tile map.
    	
        if (player.getY() + player.getHeight() > tmap.getPixelHeight())
        {
        	// Put the player back on the map
        	player.setY(tmap.getPixelHeight() - player.getHeight());
        	
        	//TODO uncomment so player dies once they fall off the map
        	//spriteState = ESpriteState.DEAD;
        }
        
        //Restricts player from going off the top of the screen
        if(player.getY()<0)
        {
        	player.setY(0);
        }
        
       //Getting the sprite's location on the tile map
        int tileLocationX = (int)(player.getX()/tmap.getTileWidth());
        int tileLocationY = (int)(player.getY()/tmap.getTileHeight());
        
        char tileCharBelow = tmap.getTileChar(tileLocationX, tileLocationY+player.getHeight()/32);
        char tileCharRight = tmap.getTileChar(tileLocationX+player.getWidth()/32, tileLocationY);
        char tileCharLeft = tmap.getTileChar(tileLocationX, tileLocationY);
        
      //Check Tile underneath the player for collision
        if(tileCharBelow == 'p' || tileCharBelow=='t' || tileCharBelow=='b')
        {
        	collisionBELOW = true;
        }
        else
        {
        	char possibleSecondTile = tmap.getTileChar(tileLocationX+player.getWidth()/32, tileLocationY+player.getHeight()/32);
        	//check if player is in 2 tiles
        	if(possibleSecondTile == 'p' || possibleSecondTile == 't' || possibleSecondTile == 'b')
        	{
        		collisionBELOW = true;
        	}
        	else
        	{
        		collisionBELOW = false; //player not standing on a second tile 
        		//spriteState = ESpriteState.FALLING;
        	}
        }
        
        //Check Tile to the RIGHT of the player for collision
        if(tileCharRight == 'p' || tileCharRight == 't' || tileCharRight=='b')
        	collisionRIGHT = true;
        else
        	collisionRIGHT=false;
        
      //Check Tile to the LEFT of the player for collision
        if(tileCharLeft == 'p' || tileCharLeft == 't' || tileCharLeft=='b')
        	collisionLEFT = true;
        else
        	collisionLEFT = false;
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
    	  
    	if (key==KeyEvent.VK_DOWN) 
    	{
    		player.setVelocityX(.0f);
    		player.setVelocityY(.0f);
    	}
    	if( key == KeyEvent.VK_RIGHT) 
    	{
    		spriteState = ESpriteState.RUN_RIGHT;
    		rightKey = true;
    	}
    	
    	if(key == KeyEvent.VK_LEFT) 
    	{	
    		spriteState = ESpriteState.RUN_LEFT;
    		leftKey=true;
    	}
    	
    	if (key == KeyEvent.VK_UP && collisionBELOW)
    	{
    		if(rightKey)
    			spriteState = ESpriteState.JUMP_RIGHT;
    		else if(leftKey)
    			spriteState = ESpriteState.JUMP_LEFT;
    		else 
    			spriteState = ESpriteState.JUMP;
    	}
    	
    	if (key == KeyEvent.VK_S)
    	{
    		// Example of playing a sound as a thread
    		Sound s = new Sound("sounds/caw.wav");
    		s.start();
    	}
    }

    public boolean boundingBoxCollision(Sprite s1, Sprite s2)
    {
    	return false;   	
    }


	public void keyReleased(KeyEvent e) { 

		int key = e.getKeyCode();

		// Switch statement instead of lots of ifs...
		// Need to use break to prevent fall through.
		switch (key)
		{
			case KeyEvent.VK_ESCAPE:
			{
				stop(); 
				break;
			}
			case KeyEvent.VK_UP:
			{
				spriteState = ESpriteState.FALLING;
				break;
			}
			
			case KeyEvent.VK_RIGHT:
			{
				spriteState = ESpriteState.STANDING;
				rightKey = false;
				break;
			}
			
			case KeyEvent.VK_LEFT:
			{
				spriteState = ESpriteState.STANDING;
				leftKey = false;
				break;
			}
			
			default:
				break;
		}
	}
	
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
