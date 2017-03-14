import game2D.*;

public class Collision
{
	final char[] tileMapChars = {'b','w','r','c','n','v','z'};
	TileMap tmap;
	
	public Collision(TileMap tmap)
	{ 
		this.tmap =tmap;
	}
	
    public boolean boundingBoxCollision(Sprite s1, Sprite s2)
    {
    	return ((s1.getX() + s1.getWidth()) >= s2.getX()) && (s1.getX() <= s2.getX()+ s2.getWidth()) &&
    			(s1.getY() + s1.getHeight()) >= s2.getY() && (s1.getY() <= s2.getY() + s2.getHeight());
    }
    
    public boolean boundingCircleCollision(Sprite s1, Sprite s2)
    {
    	float dx, dy, min;
    	dx = (s1.getX()-s2.getX());
    	dy = (s1.getY()-s2.getY());
    	min = (s1.getRadius()+s2.getRadius());
    	return ((dx*dx)+(dy*dy))<(min*min);
    }
    
    /**
     * Push Sprite UP by one pixel if sprite is stuck in a tile below it.
     * @param s Sprite to check and unstuck.
     * */
	public void recoverSpriteStuckInBottomTile(Sprite s) 
	{
		char temp = tmap.getTileChar(((int)s.getX()+s.getWidth()/2)/tmap.getTileWidth(), (int)(s.getY()+s.getHeight()-1)/tmap.getTileHeight());
		for(char c : tileMapChars)
			if(c==temp)
				s.setY(s.getY()-1);
	}
	
    /**
     * Push Sprite LEFT by one pixel if sprite is stuck in a tile below it.
     * @param s Sprite to check and unstuck.
     * */
	public void recoverSpriteStuckInRightTile(Sprite s) 
	{
		char temp = tmap.getTileChar(((int)s.getX()+s.getWidth()-1)/tmap.getTileWidth(), (int)(s.getY()+s.getHeight())/tmap.getTileHeight());
		for(char c : tileMapChars)
			if(c==temp)
				s.setX(s.getX()-1);
	}
	
    /**
     * Push Sprite RIGHT by one pixel if sprite is stuck in a tile below it.
     * @param s Sprite to check and unstuck.
     * */
	public void recoverSpriteStuckInLeftTile(Sprite s) 
	{
		char temp = tmap.getTileChar(((int)s.getX()-1)/tmap.getTileWidth(), (int)(s.getY()+s.getHeight())/tmap.getTileHeight());
		for(char c : tileMapChars)
			if(c==temp)
				s.setX(s.getX()+1);
	}
	
    public boolean checkTopSideForCollision(Sprite s) 
	{
		boolean hit = false;
		for(int i=1; i<s.getWidth()-1; i++)
		{
			char tileCharTop = tmap.getTileChar(((int)s.getX()+i)/tmap.getTileWidth(), (int)(s.getY()-1)/tmap.getTileHeight());
			for(char c : tileMapChars)
				if(c==tileCharTop)
					hit =true;
		}
		return hit;
	}
    
	public boolean checkLeftSideForCollision(Sprite s) 
	{
		boolean hit = false;
		for(int i=1; i<s.getHeight()-3; i++)
		{
			char tileCharLeft = tmap.getTileChar(((int)s.getX()-1)/tmap.getTileWidth(), ((int)s.getY()+i)/tmap.getTileHeight());
			for(char c : tileMapChars)
				if(c==tileCharLeft)
					hit =true;
		}
		return hit;
	}
	
	public boolean checkRightSideForCollision(Sprite s) 
	{
		boolean hit = false;
		for(int i=1; i<s.getHeight()-3; i++)
		{
			char tileCharRight = tmap.getTileChar(((int)s.getX()+s.getWidth()+1)/tmap.getTileWidth(), (int)(s.getY()+i)/tmap.getTileHeight());
			for(char c : tileMapChars)
				if(c==tileCharRight)
					hit =true;
		}
		return hit;
	}
	
	public boolean checkBottomSideForCollision(Sprite s)
	{
		boolean hit = false;
		for(int i=1; i<s.getWidth()-1; i++)
		{
			char tileCharBottom = tmap.getTileChar(((int)s.getX()+i)/tmap.getTileWidth(), (int)(s.getY()+s.getHeight())/tmap.getTileHeight());
			for(char c : tileMapChars)
				if(c==tileCharBottom)
					hit = true;
		}
		return hit;
	}
}
