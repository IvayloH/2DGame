import java.util.ArrayList;
import game2D.*;
public class Turret extends Sprite
{
	ArrayList<SpawnPosition<Float,Float>> turretSpawnPositions = new ArrayList<SpawnPosition<Float,Float>>();
	
	Animation turretLeft;
	Animation turretRight;
	Animation turretFireLeft;
	Animation turretFireRight;
	
	public Turret(Animation turretLeft, Animation turretRight, Animation turretFireLeft, Animation turretFireRight)
	{
		super(turretLeft);
		this.turretLeft = turretLeft;
		this.turretRight = turretRight;
		this.turretFireLeft = turretFireLeft;
		this.turretFireRight = turretFireRight;
	}
}
