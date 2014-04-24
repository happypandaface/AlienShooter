package com.me.alienShooter;

import com.badlogic.gdx.math.Vector2;

public class GameTile
{
	public int x;
	public int y;
	private Vector2 pos = new Vector2();
	public static final int SOUTH_EXIT = 0;
	public static final int NORTH_EXIT = 1;
	public static final int EAST_EXIT = 2;
	public static final int WEST_EXIT = 3;
	public boolean[] exits = new boolean[4];
	public GameTile[] adjTiles = new GameTile[4];
	
	public GameTile(int xp, int yp)
	{
		x = xp;
		y = yp;
		getPos();// also sets the position
	}
	
	public Vector2 getPos()
	{
		return pos.set(x, y);
	}
	
	public Vector2 getPos(Vector2 v)
	{
		return v.set(x, y);
	}
	
	private Vector2 realWorldPos = new Vector2();
	public float calculateWorldDistance(Vector2 tileDimensions, Vector2 position, Vector2 returnDist)
	{
		realWorldPos.set(getPos()).scl(tileDimensions);
		returnDist.set(position).sub(realWorldPos);
		if (returnDist.x <= tileDimensions.x/2 && returnDist.x >= -tileDimensions.x/2)
			returnDist.x = 0;
		if (returnDist.y <= tileDimensions.y/2 && returnDist.y >= -tileDimensions.y/2)
			returnDist.y = 0;
		if (returnDist.x > tileDimensions.x/2)
			returnDist.x -= tileDimensions.x/2;
		if (returnDist.x < -tileDimensions.x/2)
			returnDist.x += tileDimensions.x/2;
		if (returnDist.y > tileDimensions.y/2)
			returnDist.y -= tileDimensions.y/2;
		if (returnDist.y < -tileDimensions.y/2)
			returnDist.y += tileDimensions.y/2;
		return returnDist.len();
		/*
		if (Math.abs(returnDist.y) > Math.abs(returnDist.x))
			return (float)Math.abs(returnDist.y);
		else
			return (float)Math.abs(returnDist.x);
			*/
	}
	
	public float calculateLogicalDistance(Vector2 pos2, Vector2 returnDist) {
		returnDist.set(pos2).sub(getPos());
		if (returnDist.x <= 1/2 && returnDist.x >= -1/2)
			returnDist.x = 0;
		if (returnDist.y <= 1/2 && returnDist.y >= -1/2)
			returnDist.y = 0;
		if (returnDist.x > 1/2)
			returnDist.x -= 1/2;
		if (returnDist.x < -1/2)
			returnDist.x += 1/2;
		if (returnDist.y > 1/2)
			returnDist.y -= 1/2;
		if (returnDist.y < -1/2)
			returnDist.y += 1/2;
		return returnDist.len();
	}
	
	public void setAdjTile(GameTile tile, int dir)
	{
		adjTiles[dir] = tile;
	}
	
	private Vector2 dispVec = new Vector2();
	public void rectify(Vector2 tileDimensions, Vector2 pos)
	{
		calculateWorldDistance(tileDimensions, pos, dispVec);
		pos.sub(dispVec);
		
	}
}
