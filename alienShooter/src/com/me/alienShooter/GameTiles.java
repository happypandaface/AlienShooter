package com.me.alienShooter;

import java.util.Iterator;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class GameTiles
{
	// the tiles should always be iterated through with a for loop because you can't nest iterators
	public Array<GameTile> tiles;
	private Vector2 tileDimensions = new Vector2();
	
	public GameTiles()
	{
		tiles = new Array<GameTile>();
	}
	
	public void addTile(int x, int y)
	{
		tiles.add(new GameTile(x, y));
	}
	
	public void setTileDimensions(float w, float h)
	{
		tileDimensions.set(w, h);
	}
	
	public Vector2 getTileDimensions()
	{
		return tileDimensions;
	}
	
	public Iterator<GameTile> iterator()
	{
		return tiles.iterator();
	}
	
	public GameTile getTileAt(int x, int y)
	{
		for(int i = 0; i < tiles.size; ++i)
		{
			GameTile tile = tiles.get(i);
			if (tile.x == x && tile.y == y)
			{
				return tile;
			}
		}
		return null;
	}

	private Vector2 tempWorldVec = new Vector2();
	public GameTile getTileAtWorld(Vector2 tileDimensions, float x, float y)
	{
		Vector2 pos = new Vector2(x, y);
		for(int i = 0; i < tiles.size; ++i)
		{
			GameTile tile = tiles.get(i);
			float dist = tile.calculateWorldDistance(tileDimensions, pos, tempWorldVec);
			if (dist == 0)
				return tile;
		}
		return null;
	}

	private Vector2 tempLogicalVec = new Vector2();
	public GameTile getTileAtLogical(float x, float y)
	{
		Vector2 pos = new Vector2(x, y);
		for(int i = 0; i < tiles.size; ++i)
		{
			GameTile tile = tiles.get(i);
			float dist = tile.calculateLogicalDistance(pos, tempLogicalVec);
			if (dist == 0)
				return tile;
		}
		return null;
	}
	
	private Vector2 smallestCorrection = new Vector2();
	private Vector2 tempCorrectionVec = new Vector2();
	private Vector2 currTileVec = new Vector2();
	private float allowance = 0f;// this is so you can get to other tiles
	public void rectify(Vector2 v, GameTile[] gt)
	{
		boolean correctionSet = false;
		float smallestDistance = 0;
		GameTile closestTile = new GameTile(0, 0);
		for(int i = 0; i < tiles.size; ++i)
		{
			GameTile tile = tiles.get(i);
			currTileVec.set(tile.x*tileDimensions.x, tile.y*tileDimensions.y);
			//tempCorrectionVec.set(v).sub(currTileVec);// should be calculated on the tile
			float currSmallestDist = tile.calculateWorldDistance(tileDimensions, v, tempCorrectionVec);
			boolean newSmallestDist = false;
			if (correctionSet == false)
			{// only occurs on the first one
				correctionSet = true;
				newSmallestDist = true;
			}else
			{
				if (currSmallestDist < smallestDistance)
					newSmallestDist = true;
			}
			if (newSmallestDist)
			{
				closestTile = tile;
				smallestCorrection.set(tempCorrectionVec);
				smallestDistance = currSmallestDist;
			}
		}
		if (correctionSet)// only doesn't occur if there's no tiles
		{
			// also should be calculated on tile
			gt[0] = closestTile;
			closestTile.rectify(tileDimensions, v);
		}
	}
}
