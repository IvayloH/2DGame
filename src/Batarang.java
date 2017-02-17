import java.awt.Graphics2D;

import game2D.*;

public class Batarang extends Sprite
{
	Game gct;
	public Batarang(Animation anim, Game gct)
	{
		super(anim);
		this.gct = gct;
	}
	
	public void draw(Graphics2D g)
	{
		setOffsets(gct.getXOffset(), gct.getYOffset());
		drawTransformed(g);
	}
	
	public void update(Thug enemy, Sprite player )
	{
		if(gct.boundingBoxCollision(this,enemy))
			enemy.kill();
		if(gct.checkBottomSideForCollision(this) || gct.checkRightSideForCollision(this) || gct.checkLeftSideForCollision(this))
		{
			this.hide();
		}
		if(this.getX()>player.getX()+gct.getWidth() || this.getX()<player.getX()-gct.getWidth())
			this.hide();
	}
}
