import java.awt.Graphics2D;

import game2D.*;

public class Batarang extends Sprite
{
	private Game gct;
	private Animation batarangAnim;
	private Sound throwing;
	
	public Batarang(Game gct)
	{
		super();
		this.gct = gct;
		loadAssets();
	}
	public void update(long elapsed, Player player, Boss boss, Level lvl)
	{
		for(int i=0; i<lvl.getThugSpawnPositions().size(); i++)
		{
			Thug t = lvl.getThugSpawnPositions().get(i).getFirst();
			if(gct.boundingBoxCollision(this,t))
				t.kill();
		}
		for(int i=0; i<lvl.getCrateSpawnPositions().size(); i++)
		{
			Crate c = lvl.getCrateSpawnPositions().get(i).getFirst();
			if(gct.boundingBoxCollision(this,c))
				this.hide();
		}
		
		if(gct.boundingBoxCollision(this,boss))
		{
			boss.takeDamage();
			this.hide();
		}
		if(gct.checkBottomSideForCollision(this) || gct.checkRightSideForCollision(this) || gct.checkLeftSideForCollision(this))
		{
			this.hide();
		}
		if(this.getX()>player.getX()+gct.getWidth() || this.getX()<player.getX()-gct.getWidth())
			this.hide();
		if(!player.isGameOver())
			update(elapsed);
	}
	public void draw(Graphics2D g)
	{
		setOffsets(gct.getXOffset(), gct.getYOffset());
		drawTransformed(g);
	}
	public void Throw(Player player, float mouseX, float mouseY)
	{
		if(!this.isVisible() && !player.isCrouching())
		{
			Velocity v;
			if(player.isLookingRight())
			{
				this.setX(player.getX()+player.getWidth());
				this.setY(player.getY()+26);
			}
			else
			{
				this.setX(player.getX());
				this.setY(player.getY()+26);	
			}
			v = new Velocity(.5f,this.getX()+gct.getXOffset(),this.getY()+gct.getYOffset(), mouseX+10, mouseY+10);
			this.setVelocityX((float)v.getdx());
			this.setVelocityY((float)v.getdy());
			throwing = new Sound("assets/sounds/grunt_throw.wav");
			throwing.start();
			this.setRotation(v.getAngle());
			this.show();
		}
	}
	/**
	 * Load the necessary assets for the sprite to work.
	 * Also sets default animation.
	 * */
	private void loadAssets()
	{
        batarangAnim = new Animation();
        batarangAnim.addFrame(gct.loadImage("assets/images/BatmanGadgets/batarang.gif"), 60);
        setAnimation(batarangAnim);
	}
}