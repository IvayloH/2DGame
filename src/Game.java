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
	final char[] tileMapChars = {'b','w','r','c','n','v','z'};

    private float jumpStartPos = .0f;  // keep track of jump start
    private int xOffset, yOffset; // made global as they are needed in the mouse press events
    
    // Game state flags
    private boolean cursorChanged = false;
    private boolean bossFight = false;
    
    //Pressed Key flags
    private boolean leftKey = false;
    private boolean rightKey = false;
    private boolean jumpKey = false;
    private boolean crouchKey = false;
    private boolean helpKey = false;
    private boolean nextLevel = false;
    
    // Game resources
    private Player player = null;
    private GrappleHook grappleHook = null;
    private Batarang batarang = null;
    private Boss boss = null;
    //private Sound levelMusic;
	private Sound jump = null;
	
	private LevelOne lvlOne;
    private Image bgImage = null;
    
    private ArrayList<Sprite> clouds = new ArrayList<Sprite>();
    private TileMap tmap = new TileMap();	// Our tile map, note that we load it in init()    

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
        loadAssets();
        loadSprites();
        
        addMouseListener(this);
        addMouseWheelListener(this);
        addMouseMotionListener(this);
        
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
        lvlOne = new LevelOne(player, boss, tmap, this);
    }

    /**
     * Draw the current state of the game
     */
    public void draw(Graphics2D g)
    {
    	calculateOffsets();
        setOffsetsAndDrawSprites(g);
        player.drawHUD(g);
        if(!player.isGameOver())
        {
	        grappleHook.drawGrappleHook(player, g);
	        lvlOne.draw	(g);
	        batarang.draw(g);
        }
        
        if(helpKey && !bossFight)
        	drawHELP(g);
        else if(!bossFight)
        {
        	g.setColor(Color.green);
        	g.drawString("Pres H to show/hide Controls", screenWidth-170, 50);
        }
        if(player.getX()+screenWidth/2+screenWidth/4>tmap.getPixelWidth() || bossFight)
        {
        	boss.draw(g);
        	bossFight=true;
        }
        if(player.isGameOver())
        {
        	g.setColor(Color.red);
        	g.drawString("GAME OVER", screenWidth/2, screenHeight/2);
        	g.drawString("Press Esc to Quit", screenWidth/2-15, screenHeight/2+15);
        	g.drawString("       or", screenWidth/2, screenHeight/2+30);
        	g.drawString("Press R to retry", screenWidth/2-15, screenHeight/2+45);
        }
        drawRain(g);
    }
    
    /**
     * Update any sprites and check for collisions
     * 
     * @param elapsed The elapsed time between this call and the previous call of elapsed
     */    
    public void update(long elapsed)
    {
    	if(bossFight)
    	{
    		boss.update(elapsed, player);
    		if(boss.isDead())
    			loadNextLevel();
    	}
    		
    	if(nextLevel)
    		loadNextLevel();

    	if(batarang.isVisible())
    			batarang.update(elapsed, player, boss, lvlOne);
    	
    	if (grappleHook.isVisible() && !player.isGameOver())
    			grappleHook.update(elapsed, player, lvlOne);
	
    	lvlOne.update(elapsed);

       	player.update(elapsed, grappleHook.isVisible(),jumpStartPos,tmap);
    }

    /*
     *         PRIVATE METHODS
     */
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
    		if(!player.isJumping())
    		{
	    		if(!player.isCrouching())
	    			player.shiftY(30);
	    		if(rightKey) 
	    			return Player.EPlayerState.CROUCH_MOVE_RIGHT;
	    		else if(leftKey) 
	    			return Player.EPlayerState.CROUCH_MOVE_LEFT;
	    		else 
	    			return Player.EPlayerState.CROUCH;
    		}
    	}
    	
    	if(jumpKey && checkBottomSideForCollision(player))
    	{
			jumpStartPos=player.getY();
    		if(rightKey)
    			return Player.EPlayerState.JUMP_RIGHT;
    		else if(leftKey)
    			return Player.EPlayerState.JUMP_LEFT;
    		else
    			return Player.EPlayerState.JUMP;
    	}
		return Player.EPlayerState.STANDING;
    }

    private void restartGame()
    {
    	lvlOne.restartLevel(); // start from level one
    	bossFight = false;
    }
    private void loadNextLevel()
    {
    	nextLevel = false;
    	player.reset();
    	tmap.loadMap("assets/maps", "level2.txt");
    }
    
    /*
     *         KEY EVENTS
     */
    public void keyPressed(KeyEvent e) 
    { 
    	int key = e.getKeyCode();
    	
    	if (key == KeyEvent.VK_ESCAPE)
    		stop();

    	if( key == KeyEvent.VK_D) 
	    	rightKey = true;
    	
    	if(key == KeyEvent.VK_A) 
    		leftKey=true;
    	
    	if (key == KeyEvent.VK_W && !jumpKey)
    	{
    		jumpKey = true;
    		jump = new Sound("assets/sounds/grunt_jump.wav");
    		jump.start();
    	}
    	
    	if(key==KeyEvent.VK_S)
    	{
	    	crouchKey = true;
    	}
    	if(key==KeyEvent.VK_R)
    	{
    		if(player.isGameOver())
    			restartGame();
    	}
    	
    	if(key==KeyEvent.VK_H)
    	{
    		if(!helpKey)
    			helpKey=true;
    		else
    			helpKey=false;
    	}
    	
    	//if player is already in a jump motion and do nothing
		if(!player.isJumping())
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
			case KeyEvent.VK_W:
			{
				jumpKey = false;
				break;
			}
			
			case KeyEvent.VK_D:
			{
				if(!crouchKey)
					player.setState(Player.EPlayerState.STANDING);
				else
					player.setState(Player.EPlayerState.CROUCH);
				rightKey = false;
				break;
			}
			
			case KeyEvent.VK_A:
			{
				if(!crouchKey)
					player.setState(Player.EPlayerState.STANDING);
				else
					player.setState(Player.EPlayerState.CROUCH);
			
				leftKey = false;
				break;
			}
			
			case KeyEvent.VK_S:
			{
				if(!checkTopSideForCollision(player))
				{
					player.setState(Player.EPlayerState.STANDING);
					player.shiftY(-player.getHeight());
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
		player.updateDirectionBasedOnMouseLocation(e.getX(), grappleHook.isVisible());
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
			if(!player.isGameOver())
				grappleHook.shoot(player, e.getX(), e.getY());
		}
		
		if(player.getCurrentGadget().equals("Batarang"))
		{
			if(!player.isGameOver())
				batarang.Throw(player, e.getX(), e.getY());
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
		char temp = tmap.getTileChar(((int)s.getX()+s.getWidth()/2)/tmap.getTileWidth(), (int)(s.getY()+s.getHeight()-1)/tmap.getTileHeight());
		for(char c : tileMapChars)
			if(c==temp)
				s.setY(s.getY()-1);
	}
    /**
     * Push Sprite LEFT by one pixel if sprite is stuck in a tile below it.
     * @param s Sprite to check and unstuck.
     * */
	public void recoverSpriteStuckInRightTile(Sprite s) 
	{
		char temp = tmap.getTileChar(((int)s.getX()+s.getWidth()-1)/tmap.getTileWidth(), (int)(s.getY()+s.getHeight())/tmap.getTileHeight());
		for(char c : tileMapChars)
			if(c==temp)
				s.setX(s.getX()-1);
	}
    /**
     * Push Sprite RIGHT by one pixel if sprite is stuck in a tile below it.
     * @param s Sprite to check and unstuck.
     * */
	public void recoverSpriteStuckInLeftTile(Sprite s) 
	{
		char temp = tmap.getTileChar(((int)s.getX()-1)/tmap.getTileWidth(), (int)(s.getY()+s.getHeight())/tmap.getTileHeight());
		for(char c : tileMapChars)
			if(c==temp)
				s.setX(s.getX()+1);
	}
	public boolean checkTopSideForCollision(Sprite s) 
	{
		boolean hit = false;
		for(int i=1; i<s.getWidth()-1; i++)
		{
			char tileCharTop = tmap.getTileChar(((int)s.getX()+i)/tmap.getTileWidth(), (int)(s.getY()-1)/tmap.getTileHeight());
			for(char c : tileMapChars)
				if(c==tileCharTop)
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
			for(char c : tileMapChars)
				if(c==tileCharLeft)
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
			for(char c : tileMapChars)
				if(c==tileCharRight)
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
			for(char c : tileMapChars)
				if(c==tileCharBottom)
					hit = true;
		}
		return hit;
	}

	
	
	/*
	 *     LOAD RESOURCES AND OFFSETS
	 */
	private void loadAssets()
	{
		tmap.loadMap("assets/maps", "level1.txt");
        bgImage = loadImage("assets/images/city.png");
        //levelMusic = new Sound("assets/sounds/level.wav"); TODO UNCOMMENT LATER ON
        //levelMusic.start();
	}
	private void loadSprites()
	{
	  	player = new Player(6,75.f,50.f, this);

        grappleHook = new GrappleHook(150,this);
        grappleHook.hide();
        
        batarang = new Batarang(this);
        
        boss = new Boss(this);
        
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
        
        for (Sprite s: clouds)
        {
        	s.setOffsets(xOffset,yOffset);
        	s.draw(g);
        }

        tmap.draw(g,xOffset,yOffset);
    }
	private void drawHELP(Graphics2D g)
    {
		g.setColor(Color.green);
    	g.drawString("Controls: Action   -  Key", screenWidth-250, 50);
    	g.drawString("Move - W/A/S/D", screenWidth-198, 65);
    	g.drawString("Use Gadget - Mouse1", screenWidth-198, 80);
    	g.drawString("Switch Gadget - Mouse Scroll", screenWidth-198, 95);
    }
	private void drawRain(Graphics2D g)
	{
        //draw rain
        for(int i=0; i<120; i++)
        {
        	g.setColor(Color.blue);
        	int xRange = (screenWidth-5)+1;
        	int yRange = (screenHeight-5)+1;
        	int x = (int)(Math.random()*xRange)+5;
        	int y = (int)(Math.random()*yRange)+5;
        	g.drawLine(x, y, x-3, y+3);
        }
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
        
        if(bossFight)
        {
        	xOffset = minOffsetX;
        	yOffset = minOffsetY;
        }
    }
    public int getXOffset(){return xOffset;}
    public int getYOffset() {return yOffset;}
}