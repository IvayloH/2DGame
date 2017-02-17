import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
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

    float jumpStartPos = .0f;  // keep track of jump start
    int xOffset, yOffset; // made global as they are needed in the mouse press events
    
    // Game state flags
    boolean cursorChanged = false;

    //Pressed Key flags
    boolean leftKey = false;
    boolean rightKey = false;
    boolean jumpKey = false;
    boolean crouchKey = false;
    boolean helpKey = false;
    
    // Game resources
    Animation standingFacingLeft = null;
    Animation standingFacingRight = null;
    Animation movement_Right = null;
    Animation movement_Left = null;
    Animation jump_Right = null;
    Animation jump_Left = null;
    Animation crateAnim = null;
    Animation grapple = null;
    Animation grappleHookGun_Right = null;
    Animation grappleHookGun_Left = null;
    Animation thugLeftAnim = null;
    Animation thugRightAnim = null;
    Animation thugFireLeftAnim = null;
    Animation thugFireRightAnim = null;
    Animation thugProjectileAnim = null;
    Animation batarangAnim = null;
    Animation transparent = null;
    Animation crouch = null;
    Animation crouch_move_left=null;
    Animation crouch_move_right=null;
    
    
    Player player = null;
    GrappleHook grappleHook = null;
    Crate crate = null;
    Batarang batarang = null;
    Thug thug_one = null;
    Sprite enemyProjectile = null;
    
    Image bgImage = null;
    
    ArrayList<Sprite> clouds = new ArrayList<Sprite>();
    TileMap tmap = new TileMap();	// Our tile map, note that we load it in init()    

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
        
        crate.initialiseCrateSpawnPoints();
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
    	resetSpritePositionAndVelocity(player,75,50,0,0);
        player.show();
        
        resetSpritePositionAndVelocity(thug_one,450,50,0,0);
        thug_one.show();
    }

    /**
     * Draw the current state of the game
     */
    public void draw(Graphics2D g)
    {
    	calculateOffsets();
        setOffsetsAndDrawSprites(g);
        player.drawHUD(g);
        grappleHook.drawGrappleHook(player,this,g);
        crate.drawCrate(player, this, g);
        
        if(helpKey)
        	drawHELP(g);
        else
        	g.drawString("Pres H to show/hide Controls", screenWidth-170, 50);
    }

    /**
     * Update any sprites and check for collisions
     * 
     * @param elapsed The elapsed time between this call and the previous call of elapsed
     */    
    public void update(long elapsed)
    {
    	if(thug_one.isVisible())
    	{
    		thug_one.update(elapsed, player);
    		thug_one.update(elapsed);
    	}
    	
    	if(enemyProjectile.isVisible())
    	{
    		if(boundingBoxCollision(enemyProjectile,player) && !player.isInvincible())
    			player.takeDamage(elapsed);
    		enemyProjectile.update(elapsed);
    	}
    	
    	if(batarang.isVisible())
    	{
    		batarang.update(thug_one, player, this);
    		batarang.update(elapsed);
    	}
    	
    	if(crate.isHit())
    		crate.update(elapsed, tmap, this);

    	if (grappleHook.isVisible())
    	{
    		grappleHook.update(player, crate, this);
    		grappleHook.update(elapsed);
    	}
    	
       	for (Sprite s: clouds)
       		s.update(elapsed);
       	
       	player.update(elapsed, grappleHook.isVisible(),jumpStartPos,tmap,this);
        player.update(elapsed);

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
        if(enemyProjectile.isVisible())
        {
        	if(checkLeftSideForCollision(enemyProjectile))
        		enemyProjectile.hide();
        	if(checkRightSideForCollision(enemyProjectile))
        		enemyProjectile.hide();
        }
    }

    /**
     * Reset player position and velocity.
  	 * @param defaultX The value for the X position
  	 * @param defaultY The value for the Y position
  	 * @param defaultDX The value for the Horizontal(X) velocity
  	 * @param defaultDY The value for the Vertical(Y) velocity
  	 */
  	public void resetSpritePositionAndVelocity(Sprite s, float defaultX, float defaultY, float defaultDX, float defaultDY)
  	{  
  		s.setX(defaultX);
  		s.setY(defaultY);
  		s.setVelocityX(defaultDX);
  		s.setVelocityY(defaultDY);
  	}
  	/**
  	 * Return appropriate player state depending on the keys that have been pressed
     */
    private Player.EPlayerState getPlayerStateBasedOnKeysPressed()
    {
    	if((rightKey || leftKey) && !crouchKey && !jumpKey)
    	{
      		if(!player.getState().equals(Player.EPlayerState.JUMP_RIGHT) && !player.getState().equals(Player.EPlayerState.JUMP_LEFT))
      		{
      			if(rightKey)
      			{
      				player.setLookingRight(true);
      				return Player.EPlayerState.RUN_RIGHT;
      			}
      			if(leftKey)
      			{
      				player.setLookingRight(false);
      				return Player.EPlayerState.RUN_LEFT;
      			}
      		}
    	}
    	
    	if(crouchKey)
    	{
    		if(rightKey) 
    			return Player.EPlayerState.CROUCH_MOVE_RIGHT;
    		else if(leftKey) 
    			return Player.EPlayerState.CROUCH_MOVE_LEFT;
    		else 
    			return Player.EPlayerState.CROUCH;
    	}
    	
    	if(jumpKey && checkBottomSideForCollision(player))
    	{
    		{
    			jumpStartPos=player.getY();
	    		if(rightKey)
	    			return Player.EPlayerState.JUMP_RIGHT;
	    		else if(leftKey)
	    			return Player.EPlayerState.JUMP_LEFT;
	    		else
	    			return Player.EPlayerState.JUMP;
    		}
    	}
		return Player.EPlayerState.STANDING;
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

    	if(key==KeyEvent.VK_R)
    		resetSpritePositionAndVelocity(player,50,100,0,0);
    	
    	if(key==KeyEvent.VK_H)
    	{
    		if(!helpKey)
    			helpKey=true;
    		else
    			helpKey=false;
    	}
    	//if player is already in a jump motion and do nothing
		if(!player.getState().equals(Player.EPlayerState.JUMP_RIGHT) 
				&& !player.getState().equals(Player.EPlayerState.JUMP_LEFT) 
				&& !player.getState().equals(Player.EPlayerState.JUMP))
			
				player.setState(getPlayerStateBasedOnKeysPressed());
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
					player.setState(Player.EPlayerState.STANDING);
				else
					player.setState(Player.EPlayerState.CROUCH);
				rightKey = false;
				break;
			}
			
			case KeyEvent.VK_LEFT:
			{
				if(!crouchKey)
					player.setState(Player.EPlayerState.STANDING);
				else
					player.setState(Player.EPlayerState.CROUCH);
			
				leftKey = false;
				break;
			}
			
			case KeyEvent.VK_DOWN:
			{
				if(!checkTopSideForCollision(player))
				{
					player.setState(Player.EPlayerState.STANDING);
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
			if(player.getState().equals(Player.EPlayerState.STANDING))
			{
				if(player.isLookingRight())
				{
					player.setAnimation(player.getAppropriateAnimation(grappleHook.isVisible()));
					player.setLookingRight(false);
				}
			}
		}
		else
		{
			if(player.getState().equals(Player.EPlayerState.STANDING))
			{
				if(!player.isLookingRight())
				{
					player.setAnimation(player.getAppropriateAnimation(grappleHook.isVisible()));
					player.setLookingRight(true);
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
		player.switchGadget(e.getWheelRotation());
	}
	public void mousePressed(MouseEvent e) 
	{
		if(player.getCurrentGadget().equals("Grapple Hook"))
		{
			if(!grappleHook.isVisible() && !player.getState().equals(Player.EPlayerState.CROUCH))
			{
				Velocity v;
				if(player.isLookingRight())
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
				grappleHook.setRotation(v.getAngle());
				grappleHook.show();
			}
		}
		
		if(player.getCurrentGadget().equals("Batarang"))
		{
			if(!batarang.isVisible())
			{
				Velocity v;
				if(player.isLookingRight())
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
	public void recoverSpriteStuckInBottomTile(Sprite s) 
	{
		if(tmap.getTileChar(((int)s.getX()+s.getWidth()/2)/tmap.getTileWidth(), (int)(s.getY()+s.getHeight()-1)/tmap.getTileHeight())!='.')
			s.setY(s.getY()-1);
	}
    /**
     * Push Sprite LEFT by one pixel if sprite is stuck in a tile below it.
     * @param s Sprite to check and unstuck.
     * */
	public void recoverSpriteStuckInRightTile(Sprite s) 
	{
		if(tmap.getTileChar(((int)s.getX()+s.getWidth()-1)/tmap.getTileWidth(), (int)(s.getY()+s.getHeight())/tmap.getTileHeight())!='.')
			s.setX(s.getX()-1);
	}
    /**
     * Push Sprite RIGHT by one pixel if sprite is stuck in a tile below it.
     * @param s Sprite to check and unstuck.
     * */
	public void recoverSpriteStuckInLeftTile(Sprite s) 
	{
		if(tmap.getTileChar(((int)s.getX()-1)/tmap.getTileWidth(), (int)(s.getY()+s.getHeight())/tmap.getTileHeight())!='.')
			s.setX(s.getX()+1);
	}
	public boolean checkTopSideForCollision(Sprite s) 
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
	public boolean checkLeftSideForCollision(Sprite s) 
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
	public boolean checkRightSideForCollision(Sprite s) 
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
	public boolean checkBottomSideForCollision(Sprite s)
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
	 *     LOAD MAP,IMAGES,ANIMATIONS,DRAW
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
        
        thugLeftAnim = new Animation();
        thugLeftAnim.addFrame(loadImage("assets/images/Enemies/Thug/thug_sl.gif"), 60);
        
        thugRightAnim = new Animation();
        thugRightAnim.addFrame(loadImage("assets/images/Enemies/Thug/thug_sr.gif"), 60);
        
        thugProjectileAnim = new Animation();
        thugProjectileAnim.addFrame(loadImage("assets/images/Projectiles/thugProjectile.png"), 60);
        
        thugFireRightAnim = new Animation();
        thugFireRightAnim.addFrame(loadImage("assets/images/Enemies/Thug/thug_fire_right.gif"), 60);

        thugFireLeftAnim = new Animation();
        thugFireLeftAnim.addFrame(loadImage("assets/images/Enemies/Thug/thug_fire_left.gif"), 60);
	}
	private void loadSprites()
	{
		  	player = new Player(standingFacingRight, 6);
	        player.loadAdditionalAnimations(standingFacingLeft, movement_Left, movement_Right, jump_Left, jump_Right, 
	        					 crouch_move_right, crouch, crouch_move_right, grappleHookGun_Left, grappleHookGun_Right, transparent);
	       
	        grappleHook = new GrappleHook(grapple,150);
	        grappleHook.hide();
	        
	        //crate = new Sprite(crateAnim);
	        crate = new Crate(crateAnim);
	        batarang = new Batarang(batarangAnim);
	        
	        enemyProjectile = new Sprite(thugProjectileAnim);
	        
	        thug_one = new Thug(thugLeftAnim, thugRightAnim, thugFireLeftAnim, thugFireRightAnim, enemyProjectile, this);
	        
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
    private void setOffsetsAndDrawSprites(Graphics2D g)
    {
        g.drawImage(bgImage,0,0,null);

        player.setOffsets(xOffset, yOffset);
        player.draw(g);
        
        thug_one.setOffsets(xOffset, yOffset);
        thug_one.draw(g);
        
        enemyProjectile.setOffsets(xOffset, yOffset);
        enemyProjectile.drawTransformed(g);
        
        batarang.setOffsets(xOffset, yOffset);
        batarang.draw(g);
        
        for (Sprite s: clouds)
        {
        	s.setOffsets(xOffset,yOffset);
        	s.draw(g);
        }

        tmap.draw(g,xOffset,yOffset);
    }
	private void drawHELP(Graphics2D g)
    {
    	g.drawString("Controls: Action   -  Key", screenWidth-250, 50);
    	g.drawString("Move - Arrows", screenWidth-198, 65);
    	g.drawString("Use Gadget - Mouse1", screenWidth-198, 80);
    	g.drawString("Switch Gadget - Mouse Scroll", screenWidth-198, 95);
    }
	private void calculateOffsets()
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
    }
    public int getXOffset(){return xOffset;}
    public int getYOffset() {return yOffset;}
}