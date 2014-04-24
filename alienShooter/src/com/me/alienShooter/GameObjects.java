package com.me.alienShooter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/*
 * WARNING! This class should be used cautiously as I believe overuse will lead to 
 * unreadability and instability of code
 */

public class GameObjects
{
	public List<Item> items = new ArrayList<Item>();
	public List<Trigger> triggers = new ArrayList<Trigger>();
	private List<Alien> aliens = new ArrayList<Alien>();
	private List<AlienSpawner> alienSpawners = new ArrayList<AlienSpawner>();
	public Player player;
	public GameMenu gMenu;
	private Vector2 tileDim = new Vector2();
	private ObjectCreator objCreat;
	
	public Vector2 getTileDim()
	{
		return tileDim;
	}
	
	public List<Alien> getAliens()
	{
		return aliens;
	}
	
	public List<AlienSpawner> getNests()
	{
		return alienSpawners;
	}
	
	public void setTileDim(Vector2 v)
	{
		tileDim.set(v);
	}
	
	public void setObjectCreator(ObjectCreator objCreat)
	{
		this.objCreat = objCreat;
	}
	
	public ObjectCreator getObjectCreator()
	{
		return objCreat;
	}
	
	public boolean hasObj(Object o)
	{
		if (o instanceof Item)
		{
			Iterator<Item> itemIter = items.iterator();
			while(itemIter.hasNext())
			{
				if (itemIter.next() == o)
					return true;
			}
		}else if (o instanceof Trigger)
		{
			Iterator<Trigger> trigIter = triggers.iterator();
			while(trigIter.hasNext())
			{
				if (trigIter.next() == o)
					return true;
			}
		}
		return false;
	}
	
	public void addItem(Item i)
	{
		if (!hasObj(i))
			items.add(i);
	}
	
	public void addTrigger(Trigger t)
	{
		if (!hasObj(t))
			triggers.add(t);
	}
}
