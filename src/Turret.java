import game2D.*;

public class Turret extends SpriteExtension
{
	private boolean killed = false;
	
	public Turret(String tag)
	{
		super();
		this.tag = tag;
		loadAssets();
	}
	
	public SpriteExtension getProjectile() { return projectile; }
	public boolean isKilled() { return killed; }
	public void kill() { killed = true; }
	public void reset() { killed = false; }
	
	public void update(float elapsed, TileMap tmap)
	{
		collider = new Collision(tmap);
		if(collider.checkBottomSideForCollision(this))
		{
			setVelocityY(.0f);
		}
	}
}
