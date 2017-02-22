import java.awt.Graphics2D;
import java.util.ArrayList;
import game2D.*;
public class Turret extends Sprite
{
	ArrayList<Pair<Float,Float>> turretSpawnPositions = new ArrayList<Pair<Float,Float>>();
	Game gct;
	Animation turretLeft;
	Animation turretRight;
	Animation turretFireLeft;
	Animation turretFireRight;
	
	public Turret(Animation turretLeft, Animation turretRight, Animation turretFireLeft, Animation turretFireRight, Game gct)
	{
		super(turretLeft);
		this.turretLeft = turretLeft;
		this.turretRight = turretRight;
		this.turretFireLeft = turretFireLeft;
		this.turretFireRight = turretFireRight;
		this.gct = gct;
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
	
	public void addTurretSpawnPosition(float x, float y)
	{
		Pair<Float,Float> turretPos = new Pair<Float,Float>(x,y);
		turretSpawnPositions.add(turretPos);
	}
}
