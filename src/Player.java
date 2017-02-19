import java.awt.*;

import game2D.*;
public class Player extends Sprite
{
	private Animation standingLeft;
	private Animation standingRight;
	private Animation crouch;
	private Animation crouchMoveLeft;
	private Animation crouchMoveRight;
	private Animation runRight;
	private Animation runLeft;
	private Animation jumpRight;
	private Animation jumpLeft;
	private Animation grappleHookLeft;
	private Animation grappleHookRight;
	private Animation transparent64;
	private Animation transparent32;
    
	private boolean lookingRight = true;
	private boolean invincible = false;
	private boolean flashy = false;
	private boolean collisionABOVE = false;
	private boolean collisionBELOW = false;
	private boolean collisionRIGHT = false;
	private boolean collisionLEFT = false;
	private boolean gameOver = false;
	
	private final String[] gadgets = {"Batarang", "Grapple Hook"}; // holds all of batman's gadgets
	private String currentGadget = "Grapple Hook";

	private float startingX, startingY;
	private int lifeBars;
	private int amountOfDamageBeforeDeath;
	private final float RUNSPEED = .07f;
	private final float JUMPHEIGHT = 48;  // ??? tile.height()/2 + tile.height()/4
	private float invincibleTime = .0f;
	private float gravity = 0.01f;

	private Game gct;
	
    public enum EPlayerState
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
    EPlayerState playerState = EPlayerState.FALLING;
    
	public Player(int maxHP, float startingX, float startingY, Game gct)
	{
		super();
		this.amountOfDamageBeforeDeath = maxHP;
		lifeBars = amountOfDamageBeforeDeath;
		this.startingX = startingX;
		this.startingY = startingY;
		setX(startingX);
		setY(startingY);
		this.gct = gct;
		loadAnims();
	}
	
	public boolean isLookingRight() { return lookingRight; }
	public void setLookingRight(boolean b) { lookingRight = b; }
	public void setState(EPlayerState pState) { playerState = pState; }
	public EPlayerState getState() { return playerState; }
	public boolean isInvincible() { return invincible; }
	public int getLifeBars() { return lifeBars; }
	public void setMaxHealth(int amountOfDamageBeforeDeath) { this.amountOfDamageBeforeDeath = amountOfDamageBeforeDeath; }
	public boolean isGameOver() { return gameOver; }
	/**
	 * Returns currently equipped gadget.
	 * */
	public String getCurrentGadget() { return currentGadget; }
	
	public void update(long elapsed, boolean isGrappleHookVisible, float jumpStartPoint, TileMap tmap)
	{
		collisionABOVE = gct.checkTopSideForCollision(this);
		collisionBELOW = gct.checkBottomSideForCollision(this);
		collisionRIGHT = gct.checkRightSideForCollision(this);
		collisionLEFT = gct.checkLeftSideForCollision(this);
		if(invincible)
    	{
    		invincibleTime+=elapsed;
    		if(flashy)
    		{
    			this.setAnimation(getAppropriateAnimation(isGrappleHookVisible));
    			flashy=false;
    		}
    		else
    		{
    			if(isCrouching())
    				setAnimation(transparent32);
    			else
    				setAnimation(transparent64);
    			flashy=true;
    		}
    		if(invincibleTime>2000f)
    		{
    			invincible=false;
    			invincibleTime=0f;
    		}
    	}
    	
    	if(playerState.equals(EPlayerState.DEAD)) 
    	{
    		gameOver = true;
    	}
    	if(playerState.equals(EPlayerState.STANDING))
    	{
    		this.setVelocityX(.0f);
    		this.setVelocityY(.0f);
    	}
    	
    	if(isCrouching())
    	{
			if(playerState.equals(EPlayerState.CROUCH_MOVE_RIGHT) && !collisionRIGHT)
				this.setVelocityX(RUNSPEED/2);
			else if(playerState.equals(EPlayerState.CROUCH_MOVE_LEFT) && !collisionLEFT)
				this.setVelocityX(-RUNSPEED/2);
			else
				this.setVelocityX(.0f);
    	}
    	
    	if(isJumping())
    	{
    		if(!collisionABOVE)
    		{
    			this.setVelocityY(-gravity*elapsed);
    			if(playerState.equals(EPlayerState.JUMP_RIGHT) && !collisionRIGHT) this.setVelocityX(RUNSPEED);
    			else if(playerState.equals(EPlayerState.JUMP_LEFT) && !collisionLEFT) this.setVelocityX(-RUNSPEED);
    			else this.setVelocityX(.0f);
    			if(jumpStartPoint-this.getY()>JUMPHEIGHT)
    				playerState = EPlayerState.FALLING;
    		}
    		else
    			playerState = EPlayerState.FALLING;
    	}
    	
    	//change the playerState and animation after jump has ended
    	if(collisionBELOW && playerState.equals(EPlayerState.FALLING))
		{
			playerState = EPlayerState.STANDING;
			if(this.getVelocityX()>0)
				playerState = EPlayerState.RUN_RIGHT;
			else if(this.getVelocityX()<0)
				playerState = EPlayerState.RUN_LEFT;
			
			setAnimation(getAppropriateAnimation(isGrappleHookVisible));
		}
    	
    	if(playerState.equals(EPlayerState.RUN_RIGHT))
    	{
    		if(collisionRIGHT) 
    		{
    			this.setVelocityX(.0f);
    			gct.recoverSpriteStuckInRightTile(this);
    		}
    		else
    			this.setVelocityX(RUNSPEED);
    	}
    	
    	if(playerState.equals(EPlayerState.RUN_LEFT))
    	{
    		if(collisionLEFT)
    		{
    			this.setVelocityX(.0f);
    			gct.recoverSpriteStuckInLeftTile(this);
    		}
    		else
    			this.setVelocityX(-RUNSPEED);
    	}
    	
    	if(collisionBELOW && !isJumping())
    		this.setVelocityY(.0f);
    	else if(!isJumping())
    	{
    		this.setVelocityY(.05f);
    		this.setVelocityY(this.getVelocityY()+(gravity*elapsed)); // gravity adjustment
    		if(!isCrouching())
    		{
    			playerState = EPlayerState.FALLING;
    		}
    	}
    	if(!invincible)
    		setAnimation(getAppropriateAnimation(isGrappleHookVisible));
    	
        if(!gameOver)
        	update(elapsed);
        else
        	hide();
        
    	handleTileMapCollisions(elapsed,tmap);

	}
    private void handleTileMapCollisions(float elapsed, TileMap tmap)
    {
    	//Check if sprite has fallen off screen
        if (this.getY() + this.getHeight() > tmap.getPixelHeight())
        {
        	if(this.equals(this)) 
        	{
        		this.takeDamage(elapsed);
        		this.setState(Player.EPlayerState.DEAD);
        	}
        	else
        		this.hide();
        }
        
    	//Check Tile underneath the sprite for collision
        collisionBELOW = gct.checkBottomSideForCollision(this);
    	if(collisionBELOW)
    		gct.recoverSpriteStuckInBottomTile(this);
        
        //Check Tile to the RIGHT of the sprite for collision
        if(this.getState().equals(Player.EPlayerState.RUN_RIGHT) || this.getState().equals(Player.EPlayerState.CROUCH_MOVE_RIGHT))
        	collisionRIGHT = gct.checkRightSideForCollision(this);

        
        //Check Tile to the LEFT of the sprite for collision
        if(this.getState().equals(Player.EPlayerState.RUN_LEFT) || this.getState().equals(Player.EPlayerState.CROUCH_MOVE_LEFT))
        	collisionLEFT = gct.checkLeftSideForCollision(this);

        
        //Check Tile ABOVE the sprite for collision
        if(isJumping())
        {
        	collisionABOVE = gct.checkTopSideForCollision(this);
        	collisionRIGHT = gct.checkRightSideForCollision(this);
        	collisionLEFT = gct.checkLeftSideForCollision(this);
        }       
        
        if(this.getState().equals(Player.EPlayerState.FALLING))
        {
        	collisionRIGHT = gct.checkRightSideForCollision(this);
        	collisionLEFT = gct.checkLeftSideForCollision(this);
        }
    }
    
	/**
	 * Occurs when the player takes damage and reduces his health by 1.
	 */
	public void takeDamage(float elapsed)
	{
    	lifeBars--;
    	invincible=true;
    	if(lifeBars<1)
    		playerState = EPlayerState.DEAD;
	}
	/**
	 * Returns an animation depending on the State of the player.
	 */
	public Animation getAppropriateAnimation(boolean isGrappleHookVisible)
	{
    	if(!isGrappleHookVisible)
    	{
    		if(playerState.equals(EPlayerState.CROUCH))
    			return crouch;
    		if(playerState.equals(EPlayerState.CROUCH_MOVE_LEFT))
    			return crouchMoveLeft;
    		if(playerState.equals(EPlayerState.CROUCH_MOVE_RIGHT))
    			return crouchMoveRight;
	    	if(playerState.equals(EPlayerState.RUN_RIGHT))
	    		return runRight;
	    	if(playerState.equals(EPlayerState.RUN_LEFT))
	    		return runLeft;
	    	if(playerState.equals(EPlayerState.JUMP_RIGHT))
	    		return jumpRight;
	    	if(playerState.equals(EPlayerState.JUMP_LEFT))
	    		return jumpLeft;
	    	if(playerState.equals(EPlayerState.JUMP) || playerState.equals(EPlayerState.FALLING))
	    		if(lookingRight)
	    			return jumpRight;
	    		else 
	    			return jumpLeft;
			if(lookingRight)
				return standingRight;
			else 
				return standingLeft;
    	}
    	else
    	{
	    	if(lookingRight)
	    		return grappleHookRight;
			else 
				return grappleHookLeft;

    	}
	}
	/**
	 * Draws the HUD for the player's Life Bars/Gadgets
	 */
	public void drawHUD(Graphics2D g)
	{
        this.setOffsets(gct.getXOffset(), gct.getYOffset());
        this.draw(g);
        		
    	String msg = String.format("Equipped Gadget: %s", currentGadget); // TODO WILL BE REPLACED WITH AN IMAGE
        g.setColor(Color.red);
        g.drawString(msg, 20, 90);
        
        //Life Bars
        g.setColor(Color.black);
        int i=0, j=20;
        for(; i<getLifeBars(); i++,j+=8)
        {
        	g.fillRoundRect(j, 40, 5, 25, 6, 6);
        }
        for(i=0, j=20; i<amountOfDamageBeforeDeath; i++, j+=8)
        {
        	g.drawString("--", j, 40); //top line
        	g.drawString("--", j, 72); //bottom line
        }
        g.drawLine(j, 36, j, 68); // side line
	}
	/**
	 * Switches to the next or previous gadget depending on the number of mouse wheel scrolls.
	 */
	public void switchGadget(int wheelCount)
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
		if(wheelCount>0)
		{
			count++;
			if(count==gadgets.length)
				currentGadget = gadgets[0];
			else 
				currentGadget = gadgets[count];
		}
		//get the previous gadget
		if(wheelCount<0)
		{
			count--;
			if(count==-1)
				currentGadget = gadgets[gadgets.length-1];
			else 
				currentGadget = gadgets[count];
		}
	}
	public void updateDirectionBasedOnMouseLocation(int mouseX, boolean isGrappleHookVisible)
	{
		if(mouseX<this.getX()+gct.getXOffset())
		{
			if(this.getState().equals(Player.EPlayerState.STANDING))
			{
				if(this.isLookingRight())
				{
					this.setAnimation(this.getAppropriateAnimation(isGrappleHookVisible));
					this.setLookingRight(false);
				}
			}
		}
		else
		{
			if(this.getState().equals(Player.EPlayerState.STANDING))
			{
				if(!this.isLookingRight())
				{
					this.setAnimation(this.getAppropriateAnimation(isGrappleHookVisible));
					this.setLookingRight(true);
				}
			}
		}
	}
	public void reset()
	{
		lifeBars = amountOfDamageBeforeDeath;
		playerState = EPlayerState.FALLING;
		this.setX(startingX);
		this.setY(startingY);
		this.show();
		gameOver=false;
	}
	public boolean isJumping()
	{
		return (playerState.equals(EPlayerState.JUMP) 
				|| playerState.equals(EPlayerState.JUMP_RIGHT) 
				|| playerState.equals(EPlayerState.JUMP_LEFT));
	}
	public boolean isCrouching()
	{
		return (playerState.equals(EPlayerState.CROUCH) 
				|| playerState.equals(EPlayerState.CROUCH_MOVE_RIGHT) 
				||playerState.equals(EPlayerState.CROUCH_MOVE_LEFT));
	}
	
	private void loadAnims()
	{
		standingRight = new Animation();
    	standingRight.addFrame(gct.loadImage("assets/images/BatmanStates/BatmanFacingRight.gif"), 60);
        
    	setAnimation(standingRight);
    	
    	standingLeft = new Animation();
    	standingLeft.addFrame(gct.loadImage("assets/images/BatmanStates/BatmanFacingLeft.gif"), 60);
	        
	    runRight= new Animation();
	    runRight.addFrame(gct.loadImage("assets/images/BatmanStates/BatmanMoveRight.gif"), 60);
	        
        runLeft = new Animation();
        runLeft.addFrame(gct.loadImage("assets/images/BatmanStates/BatmanMoveLeft.gif"),60);
        
        jumpRight = new Animation();
        jumpRight.addFrame(gct.loadImage("assets/images/BatmanStates/BatmanJumpRight.png"),60);
        
        jumpLeft = new Animation();
        jumpLeft.addFrame(gct.loadImage("assets/images/BatmanStates/BatmanJumpLeft.png"),60);
        
        grappleHookRight = new Animation();
        grappleHookRight.addFrame(gct.loadImage("assets/images/BatmanWithGadgets/BatmanGrappleHookGunRight.gif"), 60);
        
        grappleHookLeft = new Animation();
        grappleHookLeft.addFrame(gct.loadImage("assets/images/BatmanWithGadgets/BatmanGrappleHookGunLeft.gif"), 60);
        
        transparent64 = new Animation();
        transparent64.addFrame(gct.loadImage("assets/images/BatmanStates/transparent64.png"), 60);
	    
        transparent32 = new Animation();
        transparent32.addFrame(gct.loadImage("assets/images/BatmanStates/transparent32.png"), 60);
        
        crouch = new Animation();
        crouch.addFrame(gct.loadImage("assets/images/testCube.png"), 60);

        crouchMoveRight = new Animation();
        crouchMoveRight.addFrame(gct.loadImage("assets/images/testCube.png"), 60);
        
        crouchMoveLeft = new Animation();
        crouchMoveLeft.addFrame(gct.loadImage("assets/images/testCube.png"), 60);
	}
}