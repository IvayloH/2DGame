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
	
	

	int lifeBars;
	int amountOfDamageBeforeDeath;
	final float RUNSPEED = .07f;
	final float JUMPHEIGHT = 48;  // ??? tile.height()/2 + tile.height()/4
	float invincibleTime = .0f;
    float gravity = 0.01f;
	float jumpStartPoint = .0f;
	
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
    
	public Player(Animation standingRight)
	{
		super(standingRight);
		this.standingRight = standingRight;
	}
	
	public void setAnimations(Animation standingLeft, Animation runLeft, Animation runRight,
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
	
	public void setDetails(int amountOfDamageBeforeDeath)
	{
		this.amountOfDamageBeforeDeath = amountOfDamageBeforeDeath;
		lifeBars = amountOfDamageBeforeDeath;
	}
	
	public void update(float elapsed, boolean isGrappleHookVisible, Game gct, float jumpStartPoint)
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
    			setAppropriateAnimation(isGrappleHookVisible);
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
    	
    	if(!isGrappleHookVisible && !invincible)
    		setAppropriateAnimation(isGrappleHookVisible);
    	
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
			
			setAppropriateAnimation(isGrappleHookVisible);
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
	}
	
	public void takeDamage(long elapsed)
	{
    	lifeBars--;
    	invincible=true;
    	if(lifeBars<1)
    		playerState = EPlayerState.RESPAWN;
	}
	
	public void setAppropriateAnimation(boolean isGrappleHookVisible)
	{
	    {
	    	if(!isGrappleHookVisible)
	    	{
	    		if(playerState.equals(EPlayerState.CROUCH))
	    			this.setAnimation(crouch);
	    		if(playerState.equals(EPlayerState.CROUCH_MOVE_LEFT))
	    			this.setAnimation(crouchMoveLeft);
	    		if(playerState.equals(EPlayerState.CROUCH_MOVE_RIGHT))
	    			this.setAnimation(crouchMoveRight);
		    	if(playerState.equals(EPlayerState.RUN_RIGHT))
		    		this.setAnimation(runRight);
		    	if(playerState.equals(EPlayerState.RUN_LEFT))
		    		this.setAnimation(runLeft);
		    	if(playerState.equals(EPlayerState.JUMP_RIGHT))
		    		this.setAnimation(jumpRight);
		    	if(playerState.equals(EPlayerState.JUMP_LEFT))
		    		this.setAnimation(jumpLeft);
		    	if(playerState.equals(EPlayerState.JUMP) || playerState.equals(EPlayerState.FALLING))
		    		if(lookingRight)
		    			this.setAnimation(jumpRight);
		    		else 
		    			this.setAnimation(jumpLeft);
				if(lookingRight)
					this.setAnimation(standingRight);
				else 
					this.setAnimation(standingLeft);
	    	}
	    	else
	    	{
		    	if(lookingRight)
		    		this.setAnimation(grappleHookRight);
				else 
					this.setAnimation(grappleHookLeft);

	    	}
	    }
	}
	
	public EPlayerState getState()
	{
		return playerState;
	}
	
	public void setState(EPlayerState pState)
	{
		playerState = pState;
	}
	
}
