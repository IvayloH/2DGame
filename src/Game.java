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
	static final int screenWidth = 512;
	static final int screenHeight = 384;
	final char[] tileMapChars = {'b','w','r','c','n','v','z'};

    // Game state flags
    private boolean cursorChanged = false;
    private boolean bossFight = false;
    
    //Pressed Key flags
    private boolean leftKey = false;
    private boolean rightKey = false;
    private boolean jumpKey = false;
    private boolean crouchKey = false;
    private boolean helpKey = false;
    
    private boolean inMenu = false; // TODO set to true later
    private boolean difficultySelection = false;
    
    // Game resources
    private Player player = null;
    private GrappleHook grappleHook = null;
    private SpriteExtension batarang = null;
    private Boss boss = null;
	
    private Image bgImage = null;
    private Image mainMenu;
    private Image difficultyMenu;
    
	private Level currLevel;
    private Collision collider;
	
    private ArrayList<Sprite> bats = new ArrayList<Sprite>();
    private TileMap tmap = new TileMap();	// Our tile map, note that we load it in init()
    
    private int xOffset, yOffset;
    
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
        //System.out.println(tmap);
    }

    /**
     * You will probably want to put code to restart a game in
     * a separate method so that you can call it to restart
     * the game.
     */
    public void initialiseGame()
    {	
        currLevel = new Level(player, boss, tmap, "Level One");
        boss.setSpawn(1945f, 50f);
    }

    /**
     * Draw the current state of the game
     */
    public void draw(Graphics2D g)
    {
    	if(inMenu)
    	{
    		if(difficultySelection)
    			g.drawImage(difficultyMenu, 0, 0, null);
    		else
    		{
            	g.drawImage(mainMenu, 0, 0, null);
    		}
            return;
    	}
    	calculateOffsets();
        g.drawImage(bgImage,xOffset,yOffset,null);
        tmap.draw(g,xOffset,yOffset);
       
        for (Sprite s: bats)
        {
        	s.setOffsets(xOffset,yOffset); //FIXME spawn offscreen 
        	s.draw(g);
        }
    	
        drawPlayerAndHUD(g);
        if(!player.isKilled())
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
        	spawnBats();
        }
        if(player.isKilled())
        {
        	drawGameOverState(g);
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
    	if(inMenu) return;
    	if(bossFight)
    	{
    		updateBoss(elapsed);
    		if(boss.isKilled())
    			loadNextLevel();
    	}
    	
    	if(batarang.isVisible())
    		updateBatarang(elapsed);
    	
    	if (grappleHook.isVisible() && !player.isKilled())
    		updateGrappleHook(elapsed);
	
    	updateLevel(elapsed);
    	
        for (Sprite s: bats)
        	s.update(elapsed);
        
       	player.update(elapsed, grappleHook.isVisible(),player.getJumpStart(),tmap);
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
    		currLevel = new Level(player, boss, tmap, "Level One");
    	}
    	if(key==KeyEvent.VK_2)
    	{
    		tmap.loadMap("assets/maps", "level2.txt");
    		currLevel.clearLevel();
    		currLevel = new Level(player, boss, tmap, "Level Two");
    	}
    	if(key==KeyEvent.VK_4)
    	{
    		player.reset();
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
    	}
    	
    	if(key==KeyEvent.VK_S)
    	{
	    	crouchKey = true;
    	}
    	if(key==KeyEvent.VK_R)
    	{
    		if(player.isKilled())
    		{
    			currLevel.clearLevel();
    			restartLevel();
    		}
    	}
    	if(key==KeyEvent.VK_ENTER)
    	{
    		if(player.isKilled())
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
				if(player.isFacingRight())
				{
					player.setAnimation(player.getAppropriateAnimation(grappleHook.isVisible()));
					player.setFacingRight(false);
				}
			}
		}
		else
		{
			if(player.getState().equals(Player.EPlayerState.STANDING))
			{
				if(!player.isFacingRight())
				{
					player.setAnimation(player.getAppropriateAnimation(grappleHook.isVisible()));
					player.setFacingRight(true);
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
		if(inMenu && !difficultySelection)
		{
			if((e.getX()>180 && e.getX()<350) && (e.getY()>160 && e.getY()<200))
				difficultySelection = true;
			else if((e.getX()>220 && e.getX()<290) && (e.getY()>225 && e.getY()<260))
				System.exit(0);
		}
		else if(difficultySelection)
		{
			if((e.getX()>220 && e.getX()<290) && (e.getY()>90 && e.getY()<130))
				startGame(0);
			else if((e.getX()>190 && e.getX()<320) && (e.getY()>160 && e.getY()<200))
				startGame(1);
			else if((e.getX()>220 && e.getX()<300) && (e.getY()>225 && e.getY()<265))
				startGame(2);
		}
		else
		{
			if(player.getCurrentGadget().equals("Grapple Hook"))
			{
				if(!player.isKilled())
			  		if(!grappleHook.isVisible() && !player.isCrouching())
					{
						Velocity v;
						if(player.isFacingRight())
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
				if(!player.isKilled())
				{
					if(!batarang.isVisible() && !player.isCrouching())
					{
						Velocity v;
						if(player.isFacingRight())
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
	}
	public void mouseClicked(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}

    /**
     *         PRIVATE METHODS
     */
	
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
      				player.setFacingRight(true);
      				return Player.EPlayerState.RUN_RIGHT;
      			}
      			if(leftKey)
      			{
      				player.setFacingRight(false);
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
    		player.setJumpStart(player.getY());
    		player.playJumpSound();
    		if(rightKey)
    			return Player.EPlayerState.JUMP_RIGHT;
    		else if(leftKey)
    			return Player.EPlayerState.JUMP_LEFT;
    		else
    			return Player.EPlayerState.JUMP;
    	}
    	player.setJumpStart(0.f);
		return Player.EPlayerState.STANDING;
    }
    
    /**
     * Reset the current level from its start.
     * */
    private void restartLevel()
    {
    	currLevel.restartLevel(); // start from level one
    	bossFight = false;
    }
    
    /**
     * Load the first level of the game.
     * */
    private void restartGame()
    {
    	currLevel.restartGame();
    	bossFight = false;
    }
    
    private void loadNextLevel()
    {
    	player.reset();
    	bossFight=false;
    	tmap.loadMap("assets/maps", "level2.txt");
    	currLevel = new Level(player, boss, tmap, "Level Two");
    }
    
    private void spawnBats()
    {
    	Sprite s;
    	//hasnt been set up yet
    	if(bats.size()==0)
    	{
	    	Animation ca = new Animation();
	    	ca.addFrame(loadImage("assets/maps/Extras/bat.gif"), 1000);
	        for (int b=0; b<6; b++)
	        {
	        	s = new Sprite(ca);
	        	s.setX(screenWidth + (int)(Math.random()*200.0f));
	        	s.setY(30 + (int)(Math.random()*150.0f));
	        	s.setVelocityX(-0.6f);
	        	s.show();
	        	bats.add(s);
	        }
    	}
    	else
    	{
    		for (int b=0; b<6; b++)
    		{
    			s = bats.get(b);
    			s.setX(screenWidth + (int)(Math.random()*200.0f));
	        	s.setY(30 + (int)(Math.random()*150.0f));
    		}
    	}
    }
    
	/**
	 * Set the menu flags to false and call the setHpBasedOnDifficulty method
	 * for player and boss using the passed parameter.
	 */
    private void startGame(int diff)
    {
    	inMenu=false;
    	difficultySelection=false;
    	boss.setHpBasedOnDifficulty(diff);
    	player.setHpBasedOnDifficulty(diff);
    }
    
	/*
	 *    		 LOAD RESOURCES
	 */
	/**
	 * Load the tmap, backgrounds, menu images and level music.
	 */
    private void loadAssets()
	{
		tmap.loadMap("assets/maps", "level1.txt");
        bgImage = loadImage("assets/images/city.png");
        mainMenu = loadImage("assets/images/Menus/mainMenu.png");
        difficultyMenu = loadImage("assets/images/Menus/diffMenu.png");
        //levelMusic = new Sound("assets/sounds/level.wav"); TODO UNCOMMENT LATER ON
        //levelMusic.start();
	}
    
	/**
	 * Set up the player, grappleHook, batarang and boss sprites.
	 */
    private void loadSprites()
	{
	  	player = new Player(75.f,50.f, "player");
        grappleHook = new GrappleHook(150,"grappleHook");
        batarang = new SpriteExtension("batarang");
        boss = new Boss("boss");
        
        spawnBats();
	}
	
	
    /*
     *       UPDATE METHODS
     */
    private void updateBatarang(long elapsed)
    {
    	int i=0;
    	for(i=0; i<currLevel.getThugSpawnPositions().size(); i++)
		{
    		Enemy t = currLevel.getThugSpawnPositions().get(i).getFirst();
			if(collider.boundingBoxCollision(batarang,t))
				t.kill();
		}
    	for(i=0; i<currLevel.getTurretSpawnPositions().size(); i++)
		{
    		Enemy t = currLevel.getTurretSpawnPositions().get(i).getFirst();
			if(collider.boundingBoxCollision(batarang,t))
				t.kill();
		}
		for(i=0; i<currLevel.getCrateSpawnPositions().size(); i++)
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
		if(!player.isKilled())
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
			Enemy th = currLevel.getThugSpawnPositions().get(i).getFirst();
			if(th.isVisible())
			{
				//when its the first level, leave it easier
				if(!currLevel.getLevelName().equals("Level One"))
					th.update(elapsed, player, tmap, true);
				else 
					th.update(elapsed, player, tmap, false);
			}
			if(th.getProjectile().isVisible())
	    	{
	    		if(collider.boundingBoxCollision(th.getProjectile(),player) && !player.isInvincible())
	    			player.takeDamage();
	    		if(th.getProjectile().isVisible())
	    			updateProjectile(elapsed, th.getProjectile());
	    	}
		}
		for(i=0; i<currLevel.getTurretSpawnPositions().size(); i++)
		{
			Enemy turr = currLevel.getTurretSpawnPositions().get(i).getFirst();
			if(turr.isVisible())
			{
				//when its the first level, leave it easier
				if(!currLevel.getLevelName().equals("Level One"))
					turr.update(elapsed, player, tmap, true);
				else 
					turr.update(elapsed, player, tmap, false);
			}
			if(turr.getProjectile().isVisible())
	    	{
	    		if(collider.boundingBoxCollision(turr.getProjectile(),player) && !player.isInvincible())
	    			player.takeDamage();
	    		if(turr.getProjectile().isVisible())
	    			updateProjectile(elapsed, turr.getProjectile());
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
        	
        	//stop bullets from going through crates
    		for(int i=0; i<currLevel.getCrateSpawnPositions().size(); i++)
    		{
    			Crate c = currLevel.getCrateSpawnPositions().get(i).getFirst();
    			if(collider.boundingBoxCollision(batarang,c))
    				batarang.hide();
    		}
        }
		s.update(elapsed);
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
    		grappleHook.drawTransformed(g);
    		grappleHook.setOffsets(xOffset, yOffset);
    		g.setColor(Color.black);
    		g.setStroke(new BasicStroke(3));
    		//TODO adjust the line so it follows the hook properly when rotated - FUCK MY MISERABLE NON-MATHEMATICAL LIFE.
    		float yCent = (grappleHook.getY() + grappleHook.getHeight()/2);
    		float xCent = (grappleHook.getX() + grappleHook.getWidth()/2);
    		
    		float yDif = (grappleHook.getY() + grappleHook.getHeight()/2) - yCent;
    		float xDif = grappleHook.getX()- xCent;
    		
    		float newX = (float) (xDif*Math.cos(grappleHook.getRotation()) + xCent - yDif*Math.sin(grappleHook.getRotation()));
    		float newY = (float) (yDif*Math.cos(grappleHook.getRotation()) + yCent + xDif*Math.sin(grappleHook.getRotation()));
    				
    				
    				
    		if(player.isFacingRight())
    			g.drawLine(	(int)player.getX()+(int)player.getWidth()+xOffset,
    					(int)player.getY()+26+yOffset,
    					(int)newX+xOffset,
    					(int)newY+yOffset);
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
			Enemy th = currLevel.getThugSpawnPositions().get(i).getFirst();
			th.drawTransformed(g);
			th.setOffsets(xOffset, yOffset);
			Pair<Float,Float> thugLocation = currLevel.getThugSpawnPositions().get(i).getSecond();
			if(player.getX()+getWidth()>thugLocation.getFirst() && !th.isKilled())
				th.show();
			if(th.getProjectile().isVisible())
				drawProjectile(g, th.getProjectile());
		}
		for(int i=0; i<currLevel.getTurretSpawnPositions().size(); i++)
		{
			Enemy turret = currLevel.getTurretSpawnPositions().get(i).getFirst();
			turret.setOffsets(xOffset, yOffset);
			turret.drawTransformed(g);
			
			Pair<Float,Float> turretLocation = currLevel.getTurretSpawnPositions().get(i).getSecond();
			if(player.getX()+getWidth()>turretLocation.getFirst() && !turret.isKilled())
				turret.show();
			if(turret.getProjectile().isVisible())
				drawProjectile(g, turret.getProjectile());
		}
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
        for(; i<player.getCurrentHP(); i++,j+=8)
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
	
    private void drawHELP(Graphics2D g)
    {
		g.setColor(Color.green);
    	g.drawString("Controls: Action   -  Key", screenWidth-250, 50);
    	g.drawString("Move - W/A/S/D", screenWidth-198, 65);
    	g.drawString("Use Gadget - Mouse1", screenWidth-198, 80);
    	g.drawString("Switch Gadget - Mouse Scroll", screenWidth-198, 95);
    }
	
    private void drawGameOverState(Graphics2D g)
	{
    	g.setColor(Color.red);
    	g.drawString("GAME OVER", screenWidth/2, screenHeight/2);
    	g.drawString("Press Esc to Quit", screenWidth/2-15, screenHeight/2+15);
    	g.drawString("       or", screenWidth/2, screenHeight/2+30);
    	g.drawString("Press R to retry", screenWidth/2-15, screenHeight/2+45);
	}
	
    /**
	 * Draw inclined short lines to simulate rain.
	 * */
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
}