import java.awt.Graphics2D;
import game2D.*;
public class Turret extends Sprite
{
	private Game gct;
	private Animation turretLeft;
	private Animation turretRight;
	private Animation turretFireLeft;
	private Animation turretFireRight;
	
	public Turret(Game gct)
	{
		super();
		this.gct = gct;
		loadAssets();
	}
	
	public void draw(Graphics2D g)
	{
		setOffsets(gct.getXOffset(), gct.getYOffset());
		drawTransformed(g);
	}
	
	public void update(float elapsed)
	{
		if(gct.checkBottomSideForCollision(this))
		{
			setVelocityY(.0f);
		}
	}
	/**
	 * Load the necessary assets for the sprite to work.
	 * Also sets default animation.
	 * */
	private void loadAssets()
	{
		 turretLeft = new Animation();
	     turretLeft.addFrame(gct.loadImage("assets/images/Enemies/Turret/turret_idle_l.gif"), 60);
	     setAnimation(turretLeft);
	     
	     turretRight = new Animation();
	     turretRight.addFrame(gct.loadImage("assets/images/Enemies/Turret/turret_idle_r.gif"), 	60);
	     
	     turretFireLeft = new Animation();
	     turretFireLeft.addFrame(gct.loadImage("assets/images/Enemies/Turret/turret_sh_l.gif"), 60);
	     
	     turretFireRight = new Animation();
	     turretFireRight.addFrame(gct.loadImage("assets/images/Enemies/Turret/turret_sh_r.gif"), 60);
	}
}
