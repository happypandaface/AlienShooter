package com.me.alienShooter;

import java.util.ArrayList;
import java.util.List;

public class Trigger
{
	public static final int TILE_TRIGGER = 1;
	
	private int type;
	private List<SpaceAction> actions;
	
	public Trigger()
	{
		actions = new ArrayList<SpaceAction>();
	}
	
	public void addAction(SpaceAction sa)
	{
		actions.add(sa);
	}
	
	public boolean checkConditions()
	{
		return false;
	}
	
	public void setType(int t)
	{
		type = t;
	}
	
	public boolean checkType(int t)
	{
		return type == t;
	}
}
