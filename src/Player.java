import java.awt.*;

import game2D.*;
public class Player extends Sprite
{
	Animation standingLeft;
	Animation standingRight;
    Animation crouch;
    Animation crouchMoveLeft;
    Animation crouchMoveRight;
    Animation runRight;
    Animation runLeft;
    Animation jumpRight;
    Animation jumpLeft;
    Animation grappleHookLeft;
    Animation grappleHookRight;
    Animation transparent;
    
    boolean lookingRight = true;
	boolean invincible = false;
	boolean flashy = false;
	boolean collisionABOVE = false;
	boolean collisionBELOW = false;
	boolean collisionRIGHT = false;
	boolean collisionLEFT = false;
	
	
	final String[] gadgets = {"Batarang", "Grapple Hook"}; // holds all of batman's gadgets
	String currentGadget = "Grapple Hook";
	
	int lifeBars;
	int amountOfDamageBeforeDeath;
	final float RUNSPEED = .07f;
	final float JUMPHEIGHT = 48;  // ??? tile.height()/2 + tile.height()/4
	float invincibleTime = .0f;
    float gravity = 0.01f;
	float jumpStartPoint = .0f;

	
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
    
	public Player(Animation standingRight, int maxHP)
	{
		super(standingRight);
		this.standingRight = standingRight;
		this.amountOfDamageBeforeDeath = maxHP;
		lifeBars = amountOfDamageBeforeDeath;
	}
	
	public boolean isLookingRight() { return lookingRight; }
	public void setLookingRight(boolean b) { lookingRight = b; }
	public void setState(EPlayerState pState) { playerState = pState; }
	public EPlayerState getState() { return playerState; }
	public boolean isInvincible() { return invincible; }
	public int getLifeBars() { return lifeBars; }
	public void setMaxHealth(int amountOfDamageBeforeDeath) { this.amountOfDamageBeforeDeath = amountOfDamageBeforeDeath; }
	/**
	 * Returns currently equipped gadget.
	 * */
	public String getCurrentGadget() { return currentGadget; }
	
	public void loadAdditionalAnimations(Animation standingLeft, Animation runLeft, Animation runRight,
			Animation jumpLeft, Animation jumpRight, Animation crouch, Animation crouchLeft, Animation crouchRight,
			Animation grappleHookLeft, Animation grappleHookRight, Animation transparent)
	{
		this.standingLeft = standingLeft;
		this.runLeft = runLeft;
		this.runRight = runRight;
		this.jumpLeft = jumpLeft;
		this.jumpRight = jumpRight;
		this.crouch = crouch;
		this.crouchMoveLeft = crouchLeft;
		this.crouchMoveRight = crouchRight;
		this.grappleHookLeft = grappleHookLeft;
		this.grappleHookRight = grappleHookRight;
		this.transparent = transparent;
	}	
	public void update(float elapsed, boolean isGrappleHookVisible, float jumpStartPoint, TileMap tmap, Game gct)
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
    			this.setAnimation(transparent);
    			flashy=true;
    		}
    		if(invincibleTime>2000f)
    		{
    			invincible=false;
    			invincibleTime=0f;
    		}
    	}
    	
    	if(playerState.equals(EPlayerState.RESPAWN)) 
    	{
    		if(lifeBars<1) 
    		{
    			gct.resetSpritePositionAndVelocity(this,75,50,0,0);
    			playerState = EPlayerState.FALLING;
    			lifeBars = amountOfDamageBeforeDeath;
    			//stop(); // stop game if player loses all lives
    			//TODO add an end game state
    			//playerState = EPlayerState.DEAD;
    		}
    	}
    	if(playerState.equals(EPlayerState.STANDING))
    	{
    		this.setVelocityX(.0f);
    		this.setVelocityY(.0f);
    	}
    	
    	if(playerState.equals(EPlayerState.CROUCH) || playerState.equals(EPlayerState.CROUCH_MOVE_LEFT) || playerState.equals(EPlayerState.CROUCH_MOVE_RIGHT))
    	{
			if(playerState.equals(EPlayerState.CROUCH_MOVE_RIGHT) && !collisionRIGHT)
				this.setVelocityX(RUNSPEED/2);
			else if(playerState.equals(EPlayerState.CROUCH_MOVE_LEFT) && !collisionLEFT)
				this.setVelocityX(-RUNSPEED/2);
			else
				this.setVelocityX(.0f);
    	}
    	
    	if(playerState.equals(EPlayerState.JUMP) || playerState.equals(EPlayerState.JUMP_RIGHT) || playerState.equals(EPlayerState.JUMP_LEFT))
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
    	
    	if(collisionBELOW && !playerState.equals(EPlayerState.JUMP) && !playerState.equals(EPlayerState.JUMP_LEFT)&& !playerState.equals(EPlayerState.JUMP_RIGHT))
    		this.setVelocityY(.0f);
    	else if(!playerState.equals(EPlayerState.JUMP)&& !playerState.equals(EPlayerState.JUMP_LEFT) && !playerState.equals(EPlayerState.JUMP_RIGHT)) 
    	{
    		this.setVelocityY(.05f);
    		this.setVelocityY(this.getVelocityY()+(gravity*elapsed)); // gravity adjustment
    	}
    	if(!invincible)
    		setAnimation(getAppropriateAnimation(isGrappleHookVisible));
    	
    	handleTileMapCollisions(elapsed,tmap,gct);
	}
    private void handleTileMapCollisions(float elapsed, TileMap tmap, Game gct)
    {
    	//Check if sprite has fallen off screen
        if (this.getY() + this.getHeight() > tmap.getPixelHeight())
        {
        	if(this.equals(this)) 
        	{
        		this.takeDamage(elapsed);
        		this.setState(Player.EPlayerState.RESPAWN);
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
        if(this.getState().equals(Player.EPlayerState.JUMP) || this.getState().equals(Player.EPlayerState.JUMP_RIGHT) || this.getState().equals(Player.EPlayerState.JUMP_LEFT))
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
    		playerState = EPlayerState.RESPAWN;
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
    	String msg = String.format("Equipped Gadget: %s", currentGadget); // TODO WILL BE REPLACED WITH AN IMAGE
        g.setColor(Color.red);
        g.drawString(msg, 20, 90);
        
        //Life Bars
        msg="------------";
        g.setColor(Color.black);
        g.drawString(msg, 20, 40);
        for(int i=0,j=20; i<getLifeBars(); i++,j+=8)
        {
        	g.fillRoundRect(j, 40, 5, 25, 6, 6);
        }
        g.drawLine(68, 36, 68, 68);
        g.drawString(msg, 20, 72);
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
}
