import game2D.*;

public class Player extends SpriteExtension
{
	private int[] difficultyScale = {9,6,3};
	//Animations
	private Animation crouch_Right;
	private Animation crouch_Left;
	private Animation crouchMoveLeft;
	private Animation crouchMoveRight;
	private Animation jumpRight;
	private Animation jumpLeft;
	private Animation transparent64;
	private Animation transparent32;
	//Sounds
	private Sound damaged = null;
	private Sound jump = null;
	//Flags
	private boolean invincible = false;
	private boolean flashy = false;
	private boolean collisionABOVE = false;
	private boolean collisionBELOW = false;
	private boolean collisionRIGHT = false;
	private boolean collisionLEFT = false;
	//Gadgets
	private final String[] gadgets = {"Batarang", "Grapple Hook"}; // holds all of batman's gadgets
	private String currentGadget = "Grapple Hook";
	//useful variables
	private float startingX, startingY;
	private final float RUNSPEED = .07f;
	private final float JUMPHEIGHT = 48;
	private float invincibleTime = .0f;

	
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
    
	public Player(float startingX, float startingY, String tag)
	{
		super();
		this.startingX = startingX;
		this.startingY = startingY;
		setX(startingX);
		setY(startingY);
		maxHP = 8; //default
		lifeBars = maxHP;
		this.tag = tag;
		loadAssets();
		loadAdditionalAssets();
	}
	public void setHpBasedOnDifficulty(int difficulty)
	{ 
		maxHP = difficultyScale[difficulty];
		lifeBars = maxHP;
	}
	
	public boolean isFacingRight() { return walkingRight; }
	public void setFacingRight(boolean b) { walkingRight = b; }
	public void setState(EPlayerState pState) { playerState = pState; }
	public EPlayerState getState() { return playerState; }
	public boolean isInvincible() { return invincible; }
	
	/**
	 * Returns currently equipped gadget.
	 * */
	
	public String getCurrentGadget() { return currentGadget; }
	
	/**
	 * Loads the sound file and calls the start() method.
	 * */
	public void playJumpSound() 
	{
		jump = new Sound("assets/sounds/grunt_jump.wav");
		jump.start();
	}
	
	/**
	 * Get results from the checks for collisions and other changes in player state.
	 * Update player based on those results.
	 * */
	public void update(long elapsed, boolean isGrappleHookVisible, float jumpStartPoint, TileMap tmap)
	{
		collider = new Collision(tmap);
		collisionABOVE = collider.checkTopSideForCollision(this);
		collisionBELOW = collider.checkBottomSideForCollision(this);
		collisionRIGHT = collider.checkRightSideForCollision(this);
		collisionLEFT = collider.checkLeftSideForCollision(this);
		
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
    		killed = true;
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
    			collider.recoverSpriteStuckInRightTile(this);
    		}
    		else
    			this.setVelocityX(RUNSPEED);
    	}
    	
    	if(playerState.equals(EPlayerState.RUN_LEFT))
    	{
    		if(collisionLEFT)
    		{
    			this.setVelocityX(.0f);
    			collider.recoverSpriteStuckInLeftTile(this);
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
    	}
    	if(!invincible)
    		setAnimation(getAppropriateAnimation(isGrappleHookVisible));
    	
        if(!killed)
        	update(elapsed);
        else
        	hide();
        
    	handleTileMapCollisions(tmap);

	}
	
	/**
	 * Occurs when the player takes damage and reduces his health by 1.
	 */
	public void takeDamage()
	{
    	lifeBars--;
    	invincible=true;
    	if(lifeBars<1)
    		playerState = EPlayerState.DEAD;
    	else
    	{
    		damaged = new Sound("assets/sounds/grunt_damaged.wav");
    		damaged.start();
    	}
	}
	
	/**
	 * Returns an animation depending on the State of the player.
	 */
	public Animation getAppropriateAnimation(boolean isGrappleHookVisible)
	{
    	if(!isGrappleHookVisible)
    	{
    		if(playerState.equals(EPlayerState.CROUCH))
    		{
    			if(walkingRight)
    				return crouch_Right;
    			return crouch_Left;
    		}
    		if(playerState.equals(EPlayerState.CROUCH_MOVE_LEFT))
    			return crouchMoveLeft;
    		if(playerState.equals(EPlayerState.CROUCH_MOVE_RIGHT))
    			return crouchMoveRight;
	    	if(playerState.equals(EPlayerState.RUN_RIGHT))
	    		return moveRight;
	    	if(playerState.equals(EPlayerState.RUN_LEFT))
	    		return moveLeft;
	    	if(playerState.equals(EPlayerState.JUMP_RIGHT))
	    		return jumpRight;
	    	if(playerState.equals(EPlayerState.JUMP_LEFT))
	    		return jumpLeft;
	    	if(playerState.equals(EPlayerState.JUMP) || playerState.equals(EPlayerState.FALLING))
	    		if(walkingRight)
	    			return jumpRight;
	    		else 
	    			return jumpLeft;
			if(walkingRight)
				return standRight;
			else 
				return standLeft;
    	}
    	else
    	{
	    	if(walkingRight)
	    		return fireRight;
			else 
				return fireLeft;

    	}
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
	
	/**
	 * Sets the animation for the player based on the location of the mouse on the screen.
	 * */
	public void updateDirectionBasedOnMouseLocation(int mouseX, boolean isGrappleHookVisible)
	{
		if(mouseX<this.getX())
		{
			if(this.getState().equals(Player.EPlayerState.STANDING))
			{
				if(this.isFacingRight())
				{
					this.setAnimation(this.getAppropriateAnimation(isGrappleHookVisible));
					this.setFacingRight(false);
				}
			}
		}
		else
		{
			if(this.getState().equals(Player.EPlayerState.STANDING))
			{
				if(!this.isFacingRight())
				{
					this.setAnimation(this.getAppropriateAnimation(isGrappleHookVisible));
					this.setFacingRight(true);
				}
			}
		}
	}
	
	/**
	 * Reset Player position, health and game state.
	 * */
	public void reset()
	{
		lifeBars = maxHP;
		playerState = EPlayerState.FALLING;
		this.setX(startingX);
		this.setY(startingY);
		this.show();
		killed=false; //TODO move gameOver state out of player and into level or game
	}
	
	/**
	 * Returns true if player is in any of the jumping states.
	 * */
	public boolean isJumping()
	{
		return (playerState.equals(EPlayerState.JUMP) 
				|| playerState.equals(EPlayerState.JUMP_RIGHT) 
				|| playerState.equals(EPlayerState.JUMP_LEFT));
	}
	
	/**
	 * Returns true if player is in any of the crouching states.
	 * */
	public boolean isCrouching()
	{
		return (playerState.equals(EPlayerState.CROUCH) 
				|| playerState.equals(EPlayerState.CROUCH_MOVE_RIGHT) 
				||playerState.equals(EPlayerState.CROUCH_MOVE_LEFT));
	}
	
	/**
	 *  Check for tile map collisions using the collision methods from the Game Class.
	 *  Store the results in the collision flags.
	 *  Or make appropriate changes to the player state.
	 * */
	private void handleTileMapCollisions(TileMap tmap)
    {
    	//Check if sprite has fallen off screen
        if (this.getY() + this.getHeight() > tmap.getPixelHeight())
        {
        	this.takeDamage();
        	this.setState(Player.EPlayerState.DEAD);
        }
        
    	//Check Tile underneath the sprite for collision
        collisionBELOW = collider.checkBottomSideForCollision(this);
    	if(collisionBELOW)
    		collider.recoverSpriteStuckInBottomTile(this);
        
        //Check Tile to the RIGHT of the sprite for collision
        if(this.getState().equals(Player.EPlayerState.RUN_RIGHT) || this.getState().equals(Player.EPlayerState.CROUCH_MOVE_RIGHT))
        	collisionRIGHT = collider.checkRightSideForCollision(this);

        
        //Check Tile to the LEFT of the sprite for collision
        if(this.getState().equals(Player.EPlayerState.RUN_LEFT) || this.getState().equals(Player.EPlayerState.CROUCH_MOVE_LEFT))
        	collisionLEFT = collider.checkLeftSideForCollision(this);

        
        //Check Tile ABOVE the sprite for collision
        if(isJumping())
        {
        	collisionABOVE = collider.checkTopSideForCollision(this);
        	collisionRIGHT = collider.checkRightSideForCollision(this);
        	collisionLEFT = collider.checkLeftSideForCollision(this);
        }       
        
        if(this.getState().equals(Player.EPlayerState.FALLING))
        {
        	collisionRIGHT = collider.checkRightSideForCollision(this);
        	collisionLEFT = collider.checkLeftSideForCollision(this);
        }
    }
	
	/**
	 * Load the necessary assets for the sprite to work.
	 * Also sets default animation.
	 * */
	private void loadAdditionalAssets()
	{
        jumpRight = new Animation();
        jumpRight.addFrame(loadImage("assets/images/BatmanStates/BatmanJumpRight.png"),60);
        
        jumpLeft = new Animation();
        jumpLeft.addFrame(loadImage("assets/images/BatmanStates/BatmanJumpLeft.png"),60);

        transparent64 = new Animation();
        transparent64.addFrame(loadImage("assets/images/BatmanStates/transparent64.png"), 60);
	    
        transparent32 = new Animation();
        transparent32.addFrame(loadImage("assets/images/BatmanStates/transparent32.png"), 60);
        
        crouch_Right = new Animation();
        crouch_Right.addFrame(loadImage("assets/images/BatmanStates/crouch_R.gif"), 60);
        
        crouch_Left = new Animation();
        crouch_Left.addFrame(loadImage("assets/images/BatmanStates/crouch_L.gif"), 60);

        crouchMoveRight = new Animation();
        crouchMoveRight.addFrame(loadImage("assets/images/BatmanStates/crouch_move_R.gif"), 60);
        
        crouchMoveLeft = new Animation();
        crouchMoveLeft.addFrame(loadImage("assets/images/BatmanStates/crouch_move_L.gif"), 60);
	}
}