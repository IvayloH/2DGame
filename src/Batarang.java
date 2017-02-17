import game2D.*;

public class Batarang extends Sprite
{
	public Batarang(Animation anim)
	{
		super(anim);
	}
	
	public void update(Sprite enemy, Sprite player, Game gct)
	{
		if(gct.boundingBoxCollision(this,enemy))
			enemy.hide();
		if(gct.checkBottomSideForCollision(this) || gct.checkRightSideForCollision(this) || gct.checkLeftSideForCollision(this))
		{
			this.hide();
		}
		if(this.getX()>player.getX()+gct.getWidth() || this.getX()<player.getX()-gct.getWidth())
			this.hide();
	}
}
