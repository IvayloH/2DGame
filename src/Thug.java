import game2D.*;
public class Thug extends Sprite
{
	Game gct;
	EnemyProjectile enemyProjectile;
	Animation animLeft;
	Animation animRight;
	Animation shootLeft;
	Animation shootRight;
	
	public Thug(Animation animLeft, Animation animRight, Animation shootLeft, Animation shootRight, EnemyProjectile enemyProjectile, Game gct)
	{
		super(animLeft);
		this.animLeft = animLeft;
		this.animRight = animRight;
		this.gct = gct;
		this.shootLeft = shootLeft;
		this.shootRight = shootRight;
		this.enemyProjectile = enemyProjectile;
	}
	
	public void update(float elapsed, float gravity, Sprite player)
	{
		if(!gct.checkBottomSideForCollision(this))
		{
			this.setVelocityY(.5f);
    		this.setVelocityY(this.getVelocityY()+(gravity*elapsed)); // gravity adjustment
		}
		else
		{
			if(player.getX()>this.getX() && !enemyProjectile.isVisible())
				this.setAnimation(animRight);
			else if(player.getX()<this.getX() && !enemyProjectile.isVisible())
				this.setAnimation(animLeft);
			this.setVelocityY(.0f);
			gct.recoverSpriteStuckInBottomTile(this);
    		if(Math.random()>0.3)
    			if(player.getX()+gct.getWidth()<this.getX() || this.getX()>player.getX()-gct.getHeight()) // check to see if player is close or not
    				if(player.getY()+player.getHeight()-10>this.getY() || player.getY()-player.getHeight()+10<this.getY()) //player is near the same height
    					Shoot(player);
		}
	}
	
	public void Shoot(Sprite player)
	{
    	if(!enemyProjectile.isVisible() || enemyProjectile.getX()+gct.getWidth()<this.getX() || enemyProjectile.getX()-gct.getWidth()>this.getX())
    	{
    		enemyProjectile.setScale(0.8f);
			Velocity v;
			if(player.getX()>this.getX())
			{
				this.setAnimation(shootRight);
				enemyProjectile.setX(this.getX()+this.getWidth());
				enemyProjectile.setY(this.getY()+15);
				v = new Velocity(0.7f,enemyProjectile.getX()+gct.getXOffset(),enemyProjectile.getY()+gct.getYOffset(),this.getX()+gct.getXOffset()+50,this.getY()+26+gct.getYOffset());
				enemyProjectile.setRotation(180);
			}
			else
			{
				this.setAnimation(shootLeft);
				enemyProjectile.setX(this.getX());
				enemyProjectile.setY(this.getY()+15);
				v = new Velocity(0.7f,enemyProjectile.getX()+gct.getXOffset(),enemyProjectile.getY()+gct.getYOffset(),this.getX()+gct.getXOffset()-50,this.getY()+26+gct.getYOffset());
				enemyProjectile.setRotation(0);
			}

			enemyProjectile.setVelocityX((float)v.getdx());
			enemyProjectile.show();
    	}
	}
}
