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
    private SpriteExtension batarang = null;
    private Boss boss = null;
    //private Sound levelMusic;
	private Sound jump = null;
    private Image bgImage = null;
    
	private Level currLevel;
    private Collision collider;
	
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
        collider = new Collision(tmap);
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
        currLevel = new Level(player, boss, tmap, this);
    }

    /**
     * Draw the current state of the game
     */
    public void draw(Graphics2D g)
    {
    	calculateOffsets();
        setOffsetsAndDrawSprites(g);
        drawPlayerAndHUD(g);
        if(!player.isGameOver())
        {
	        drawGrappleHook(g);
	        drawProjectile(g, batarang);
        }
        drawLevel(g);
        
        if(helpKey && !bossFight)
        	drawHELP(g);
        else if(!bossFight)
        {
        	g.setColor(Color.green);
        	g.drawString("Pres H to show/hide Controls", screenWidth-170, 50);
        }
        if(player.getX()+screenWidth/2+screenWidth/4>tmap.getPixelWidth() || bossFight)
        {
        	drawBoss(g);
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
    		updateBoss(elapsed);
    		if(boss.isDead())
    			loadNextLevel();
    	}
    		
    	if(nextLevel)
    		loadNextLevel();

    	if(batarang.isVisible())
    		updateBatarang(elapsed);
    	
    	if (grappleHook.isVisible() && !player.isGameOver())
    		updateGrappleHook(elapsed);
	
    	updateLevel(elapsed);

       	player.update(elapsed, grappleHook.isVisible(),jumpStartPos,tmap);
    }


    /*
     * 			DRAW METHODS
     */
	/**
	 * Handles setting offsets for the boss and drawing 
	 * the boss HUD and projectile.
	 */
    private void drawBoss(Graphics2D g)
    {
    	boss.setOffsets(xOffset, yOffset);
    	boss.drawTransformed(g);
    	boss.show();
        g.setColor(Color.green);
        int i=0, j=getWidth()-95;
        for(; i<boss.getCurrentHP(); i++,j+=8)
        {
        	g.fillRoundRect(j,40, 5, 25, 6, 6);
        }
        for(i=0, j=getWidth()-95; i<boss.getMaxHP(); i++, j+=8)
        {
        	g.drawString("--", j, 40); //top line
        	g.drawString("--", j, 72); //bottom line
        }
        g.drawLine(j, 36, j, 68); // side line
        if(boss.getProjectile().isVisible())
        	drawProjectile(g, boss.getProjectile());
    }
  	/**
  	 * Draws the Grapple Hook and a line behind.
  	 */
    private void drawGrappleHook(Graphics2D g)
    {
    	 if(grappleHook.isVisible())
         {
    		 grappleHook.setRotation(grappleHook.getRotation());
    		 grappleHook.drawTransformed(g);
    		 grappleHook.setOffsets(xOffset, yOffset);
         	g.setColor(Color.black);
         	g.setStroke(new BasicStroke(3));
         	//TODO adjust the line so it follows the hook properly when rotated
         	if(player.isLookingRight())
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
	/**
	 * Draw each crate/thug by going through their ArrayLists accordingly.
	 * */
    private void drawLevel(Graphics2D g)
    {
    	//draw crates
		for(int i=0; i<currLevel.getCrateSpawnPositions().size(); i++)
        {
        	Crate c =  currLevel.getCrateSpawnPositions().get(i).getFirst();
    		c.drawTransformed(g);
    	    c.setOffsets(xOffset, yOffset);
        	Pair<Float,Float> crateLocation = currLevel.getCrateSpawnPositions().get(i).getSecond();
        	if(player.getX()+getWidth()>crateLocation.getFirst() && !c.isHit())
        		c.show();
        }
		//draw thugs
		for(int i=0; i<currLevel.getThugSpawnPositions().size(); i++)
		{
			Thug th = currLevel.getThugSpawnPositions().get(i).getFirst();
			th.drawTransformed(g);
			th.setOffsets(xOffset, yOffset);
			Pair<Float,Float> thugLocation = currLevel.getThugSpawnPositions().get(i).getSecond();
			if(player.getX()+getWidth()>thugLocation.getFirst() && !th.isKilled())
				th.show();
			if(th.getProjectile().isVisible())
				drawProjectile(g, th.getProjectile());
		}
		//draw turrets TODO finish this
		/*for(int i=0; i<currLevel.getTurretSpawnPositions().size(); i++)
		{
			Turret turret = currLevel.getTurretSpawnPositions().get(i).getFirst();
			turret.setOffsets(xOffset, yOffset);
			turret.drawTransformed(g);
		*/
    }
	/**
	 * Draws the HUD for the player's Life Bars/Gadgets
	 */
    private void drawPlayerAndHUD(Graphics2D g)
    {
        player.setOffsets(xOffset, yOffset);
        player.drawTransformed(g);
        		
    	String msg = "Equipped Gadget: "; 
    	if(player.getCurrentGadget().equals("Batarang"))
    		g.drawImage(loadImage("assets/images/BatmanGadgets/batarang.png"), 125, 75, null);
    	else if(player.getCurrentGadget().equals("Grapple Hok"))
    		g.drawImage(loadImage("assets/images/BatmanGadgets/grappleHookGun.png"), 125, 75, null);
    	
        g.setColor(Color.red);
        g.drawString(msg, 20, 90);
        
        //Life Bars
        g.setColor(Color.black);
        int i=0, j=20;
        for(; i<player.getLifeBars(); i++,j+=8)
        {
        	g.fillRoundRect(j, 40, 5, 25, 6, 6);
        }
        for(i=0, j=20; i<player.getMaxHP(); i++, j+=8)
        {
        	g.drawString("--", j, 40); //top line
        	g.drawString("--", j, 72); //bottom line
        }
        g.drawLine(j, 36, j, 68); // side line
	
    }
    private void drawProjectile(Graphics2D g, Sprite proj)
    {
		proj.setOffsets(xOffset, yOffset);
		proj.drawTransformed(g);
    }
   
    /*
     *       UPDATE METHODS
     */
    private void updateBatarang(long elapsed)
    {
    	for(int i=0; i<currLevel.getThugSpawnPositions().size(); i++)
		{
			Thug t = currLevel.getThugSpawnPositions().get(i).getFirst();
			if(collider.boundingBoxCollision(batarang,t))
				t.kill();
		}
		for(int i=0; i<currLevel.getCrateSpawnPositions().size(); i++)
		{
			Crate c = currLevel.getCrateSpawnPositions().get(i).getFirst();
			if(collider.boundingBoxCollision(batarang,c))
				batarang.hide();
		}
		
		if(collider.boundingBoxCollision(batarang,boss))
		{
			boss.takeDamage();
			batarang.hide();
		}
		if(collider.checkBottomSideForCollision(batarang) || collider.checkRightSideForCollision(batarang) || collider.checkLeftSideForCollision(batarang))
		{
			batarang.hide();
		}
		if(batarang.getX()>player.getX()+getWidth() || batarang.getX()<player.getX()-getWidth())
			batarang.hide();
		if(!player.isGameOver())
			batarang.update(elapsed);
    }
	/**
	 * Updates the boss and the projectile if visible. 
	 * Calls the shoot method in the end.
	 */
    private void updateBoss(long elapsed)
    {
    	if(boss.getCurrentHP()<1)
		{
			boss.hide();
			boss.kill();
		}
		else
		{
			if(!collider.checkBottomSideForCollision(boss))
			{
				boss.setVelocityY(.5f);
				boss.setVelocityY(boss.getVelocityY()+(0.01f*elapsed)); // gravity adjustment
			}
			else
			{
				if(boss.isInvulnerable())
				{
					if(boss.getInvulnerableTimer()>1000)
					{
						boss.setInvulnerable(false);
						boss.setInvulnerableTimer(0); //reset timer
					}
					else
					boss.setInvulnerableTimer(boss.getInvulnerableTimer()+elapsed);
					System.out.println(boss.getInvulnerableTimer()+elapsed);
				}
				boss.setVelocityY(.0f);
				if(player.getX()>boss.getX() && !boss.getProjectile().isVisible())
					boss.lookRight();
				else if(player.getX()<boss.getX() && !boss.getProjectile().isVisible())
					boss.lookLeft();
				collider.recoverSpriteStuckInBottomTile(boss);
				if(Math.random()>0.8)
					if(player.getX()+getWidth()>boss.getX() || boss.getX()<player.getX()-getWidth()) // check to see if player is close or not
	    				if(player.getY()+player.getHeight()-20>boss.getY() || player.getY()-player.getHeight()+20>boss.getY()) //player is near the same height
	    					boss.shoot(player);
			}
		}
		boss.update(elapsed);
		if(boss.getProjectile().isVisible())
			updateProjectile(elapsed, boss.getProjectile());
    }
    private void updateGrappleHook(long elapsed)
    {
    	for(int i=0; i<currLevel.getCrateSpawnPositions().size(); i++)
		{
			Crate crate = currLevel.getCrateSpawnPositions().get(i).getFirst();
	    	if(collider.boundingBoxCollision(grappleHook, crate) && !grappleHook.isGrappleHookRetracting())
	    	{
	    		grappleHook.retractGrappleHook(player);
	    		if(!collider.checkRightSideForCollision(crate))
	    		{//if next to a tile, then crate already fallen
	    			crate.setHit();
	    			crate.setHitX(crate.getX());
	    		}
	    	}
		}
				
    	//check for right/left collision depending on which way the hook is going
    	if(grappleHook.getVelocityX()>0)
    	{
    		if(collider.checkRightSideForCollision(grappleHook))
    			grappleHook.retractGrappleHook(player);
    	}
    	else if(collider.checkLeftSideForCollision(grappleHook))
    		grappleHook.retractGrappleHook(player);
    	
    	if((grappleHook.getX()>player.getX()+player.getWidth()+grappleHook.getHookLimit())
				|| (grappleHook.getX()<player.getX()-grappleHook.getHookLimit())
				|| (grappleHook.getY()>player.getY()+player.getHeight()/2+grappleHook.getHookLimit())
				|| (grappleHook.getY()<player.getY()-grappleHook.getHookLimit()))
    		grappleHook.retractGrappleHook(player);
    	
		if(grappleHook.isGrappleHookRetracting())
		{
			if(collider.boundingBoxCollision(player, grappleHook))
			{
				grappleHook.setVelocityX(.0f);
				grappleHook.setVelocityY(.0f);
				grappleHook.hide();
				grappleHook.setGrappleHookRetracting(false);
			}
		}
		grappleHook.update(elapsed);
    }
	/**
	 * Update the crates/boss/thugs and their projectiles if visible.
	 * */
    private void updateLevel(long elapsed)
    {
		int i=0;
		for(i=0; i<currLevel.getCrateSpawnPositions().size(); i++)
        {
			if(currLevel.getCrateSpawnPositions().get(i).getFirst().isHit())
				currLevel.getCrateSpawnPositions().get(i).getFirst().update(elapsed, player, tmap);
        }
		for(i=0; i<currLevel.getThugSpawnPositions().size(); i++)
		{
			Thug th = currLevel.getThugSpawnPositions().get(i).getFirst();
			if(th.isVisible())
				th.update(elapsed, player, tmap);
			if(th.getProjectile().isVisible())
	    	{
	    		if(collider.boundingBoxCollision(th.getProjectile(),player) && !player.isInvincible())
	    			player.takeDamage();
	    		if(th.getProjectile().isVisible())
	    			updateProjectile(elapsed, th.getProjectile());
	    	}
		}
		//boss bullet collision
		if(collider.boundingBoxCollision(boss.getProjectile(),player) && !player.isInvincible())
			player.takeDamage();
    }
    
    private void updateProjectile(long elapsed, SpriteExtension s)
    {
		collider = new Collision(tmap);
		//handle TileMap collisions
		if(s.isVisible())
        {
        	if(collider.checkLeftSideForCollision(s))
        		s.hide();
        	if(collider.checkRightSideForCollision(s))
        		s.hide();
        }
		s.update(elapsed);
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
	    		//if(!player.isCrouching())
	    			//player.shiftY(30);
	    		if(rightKey) 
	    			return Player.EPlayerState.CROUCH_MOVE_RIGHT;
	    		else if(leftKey) 
	    			return Player.EPlayerState.CROUCH_MOVE_LEFT;
	    		else 
	    			return Player.EPlayerState.CROUCH;
    		}
    	}
    	
    	if(jumpKey && collider.checkBottomSideForCollision(player))
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
    	currLevel.restartLevel(); // start from level one
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
    	if(key==KeyEvent.VK_1)
    	{
    		tmap.loadMap("assets/maps", "level1.txt");
    		currLevel = new Level(player, boss, tmap, this);
    	}
    	if(key==KeyEvent.VK_2)
    	{
    		tmap.loadMap("assets/maps", "level2.txt");
    		currLevel = new Level(player, boss, tmap, this);
    	}
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
				if(!collider.checkTopSideForCollision(player))
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
		//player.updateDirectionBasedOnMouseLocation(e.getX(), grappleHook.isVisible());
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
		  		if(!grappleHook.isVisible() && !player.isCrouching())
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
			if(!player.isGameOver())
			{
				//batarang.Throw(player, e.getX(), e.getY());
				if(!batarang.isVisible() && !player.isCrouching())
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
					v = new Velocity(.5f,batarang.getX()+xOffset,batarang.getY()+yOffset, e.getX()+10, e.getY()+10);
					batarang.setVelocityX((float)v.getdx());
					batarang.setVelocityY((float)v.getdy());
					batarang.playShootSound();
					batarang.setRotation(v.getAngle());
					batarang.show();
				}
			}
		}
	}
	public void mouseClicked(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}


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
	  	player = new Player(6,75.f,50.f, "player");
        grappleHook = new GrappleHook(150,"grappleHook");
        grappleHook.hide();
        
        batarang = new SpriteExtension("batarang");
        
        boss = new Boss("boss");
        
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