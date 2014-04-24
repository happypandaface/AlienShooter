package com.me.alienShooter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.utils.Array;

public class Inventory
{
	private List<Item> items;
	private Item currentItem;
	
	public Inventory()
	{
		items = new ArrayList<Item>();
	}
	
	public Item getCurrentItem()
	{
		if (currentItem == null && items.size() > 0)
			currentItem = items.get(0);
		return currentItem;
	}
	
	public void setCurrentItem(Item i)
	{
		if (hasItem(i))
			currentItem = i;
	}
	
	public boolean hasItem(Item i)
	{
		Iterator<Item> itemIter = items.iterator();
		while(itemIter.hasNext())
		{
			if (itemIter.next() == i)
				return true;
		}
		return false;
	}
	
	public List<Item> getItems()
	{
		return items;
	}
	
	public void addItem(Item i)
	{
		i.setInWorld(false);
		if (!hasItem(i))
			items.add(i);
		if (currentItem == null)
			currentItem = i;
	}
	
	public void processInconsistencies() {
		for(int i = 0; i < items.size(); ++i)
		{
			Item itm = items.get(i);
			if (itm.isInWorld())
			{
				if (itm == currentItem)
					currentItem = null;
				items.remove(i);
				--i;
			}
		}
	}
}
