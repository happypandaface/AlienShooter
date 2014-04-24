package com.me.alienShooter;

import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

public class GameMenu implements InputProcessor
{
	private boolean active;
	private Texture menuTex;
	NinePatch menuNine;
	public static final float ITEM_ICON_WIDTH = 0.2f;
	public static final float ITEM_ICON_HEIGHT = 0.25f;
	public static final float INV_WINDOW_WIDTH = 0.7f;
	public static final float INV_WINDOW_HEIGHT = 0.65f;
	public static final float INV_WINDOW_MARGIN_X = (1-INV_WINDOW_WIDTH)/2;
	public static final float INV_WINDOW_MARGIN_Y = (1-INV_WINDOW_HEIGHT)/2;
	
	private boolean beingDragged = false;
	private Vector2 startDrag = new Vector2();
	private Vector2 currDrag = new Vector2();
	private boolean dragUpSync;
	private float timeDragged;
	private float tapAllowance = 0.5f;
	private float moveAllowance = 10;
	private boolean leaving;
	private boolean disregardingFirstEndTap;
	
	public GameMenu()
	{
		
	}
	
	public void setup(StuffHolder stuffHolder)
	{
		TextureRegion menuTex = new TextureRegion(stuffHolder.getTex("menu"));
		menuNine = new NinePatch(menuTex, 16, 16, 16, 16);
	}

	public void draw(SpriteBatch sBatch, GameObjects gObjects, Player player, Vector2 tileDimensions, float delta)
	{
		int h = Gdx.graphics.getHeight();
		int w = Gdx.graphics.getWidth();
		if (beingDragged)
			timeDragged += delta;
		if (disregardingFirstEndTap)
		{
			disregardingFirstEndTap = false;
			dragUpSync = false;
		}
		if (inMenu())
		{// if we're in the menu draw the background and the player's items
			menuNine.draw(sBatch, w*INV_WINDOW_MARGIN_X, h*INV_WINDOW_MARGIN_Y, w*INV_WINDOW_WIDTH, h*INV_WINDOW_HEIGHT);
			Iterator<Item> playerItemsIter = player.getInventory().getItems().iterator();
			int i = 0;
			while (playerItemsIter.hasNext())
			{
				Item itm = playerItemsIter.next();
				boolean skipThisOne = false;
				if (dragUpSync && checkStartDragInv(i) && checkEndDragOutOfInv())
				{// this means we just released an item outside of 
					gObjects.addItem(itm);
					itm.setInWorld(true);
					itm.setPosition(player.getTile().getPos());
					dragUpSync = false;
					skipThisOne = true;
				}else if (dragUpSync && didntDrag() && timeDragged < tapAllowance && checkStartDragInv(i))
				{
					gObjects.player.getInventory().setCurrentItem(itm);
					this.setInMenu(false);
					this.leavingMenu();
				}
				if (!skipThisOne)
				{
					if (beingDragged && checkStartDragInv(i))
						itm.drawIcon(sBatch, delta, (int)currDrag.x, (int)currDrag.y);
					else
						itm.drawIcon(sBatch, delta, (int)(w*INV_WINDOW_MARGIN_X+w*ITEM_ICON_WIDTH/2+w*ITEM_ICON_WIDTH*i), (int)(h-h*INV_WINDOW_MARGIN_Y-h*ITEM_ICON_HEIGHT/2));
				}
				++i;
			}
			player.getInventory().processInconsistencies();// this will remove items that are actually in world
		}
		
		int itemsNear = 0;
		Iterator<Item> itemIter2 = gObjects.items.iterator();
		while (itemIter2.hasNext())
		{
			Item currItem = itemIter2.next();
			if (currItem.isInWorld())
			{
				// iterate through the items and see if any of them are close enough to pick up
				if (currItem.closeEnoughForPickup(player, tileDimensions))
				{
					// if they are, put them on the screen
					sBatch.setTransformMatrix(new Matrix4());
					boolean skipThisOne = false;
					if (dragUpSync && checkStartDrag(itemsNear))
					{// this is true when the user just released their finger
						// if we moved the item into the menu, remove it from the game,
						// and add it to the inventory
						if (checkStartDrag(itemsNear))
						{
							if (!checkEndDragOutOfInv())
							{
								currItem.setInWorld(false);
								player.getInventory().addItem(currItem);
								dragUpSync = false;	// this makes sure no other items accidentally are picked up
													// in this loop
								skipThisOne = true;
							}
						}
					}
					if (!skipThisOne)
					{
						if (inMenu() && beingDragged && checkStartDrag(itemsNear))
						{
							// if you're dragging them, draw them being dragged.
							currItem.drawIcon(sBatch, delta, (int)currDrag.x, (int)currDrag.y);
						}else
						{
							currItem.drawIcon(sBatch, delta, (int)(w*ITEM_ICON_WIDTH/2+w*ITEM_ICON_WIDTH*itemsNear), (int)(h*ITEM_ICON_HEIGHT/2));
						}
					}
					itemsNear++;// move other items over
				}
			}
		}
		dragUpSync = false;// this makes the drag up only happen once
	}
	
	public void leavingMenu()
	{
		this.leaving = true;
	}
	
	public boolean comsumeLeave()
	{
		boolean leaving = this.leaving;
		this.leaving = false;
		return leaving;
	}
	
	public void disregardFirstEndTap()
	{
		this.disregardingFirstEndTap = true;
	}

	private boolean didntDrag()
	{
		return startDrag.cpy().sub(currDrag).len() < moveAllowance;
	}
	
	private boolean checkEndDragOutOfInv() {
		int w = Gdx.graphics.getWidth();
		int h = Gdx.graphics.getHeight();
		int x = (int) (currDrag.x - (w*INV_WINDOW_MARGIN_X));
		int y = (int) (currDrag.y - (h*INV_WINDOW_MARGIN_Y));
		Gdx.app.log("end drag", ""+x+", " + y);
		if (x > 0 &&
			y > 0 &&
			x < w*INV_WINDOW_WIDTH &&
			y < h*INV_WINDOW_HEIGHT)
			return false;
		return true;
	}

	private boolean checkStartDragInv(int i)
	{
		int w = Gdx.graphics.getWidth();
		int h = Gdx.graphics.getHeight();
		int x = (int) (startDrag.x - (w*INV_WINDOW_MARGIN_X+w*ITEM_ICON_WIDTH/2+w*ITEM_ICON_WIDTH*i));
		int y = (int) (startDrag.y - (h-h*INV_WINDOW_MARGIN_Y-h*ITEM_ICON_HEIGHT/2));
		if (x > -w*ITEM_ICON_WIDTH/2 &&
			y > -h*ITEM_ICON_HEIGHT/2 &&
			x < w*ITEM_ICON_WIDTH/2 &&
			y < h*ITEM_ICON_HEIGHT/2)
			return true;
		return false;
	}
	
	private boolean checkStartDrag(int itemsNear)
	{
		int h = Gdx.graphics.getHeight();
		int w = Gdx.graphics.getWidth();
		if (h*ITEM_ICON_HEIGHT > startDrag.y &&
			w*ITEM_ICON_WIDTH*itemsNear < startDrag.x &&
			w*ITEM_ICON_WIDTH*(itemsNear+1) > startDrag.x)
			return true;
		return false;
	}

	public boolean inMenu()
	{
		return active;
	}
	
	public void setInMenu(boolean a)
	{
		disregardFirstEndTap();
		active = a;
	}

	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (beingDragged == true)
		{
			beingDragged = false;// this prevents multi touch stuff
			startDrag.set(0, 0);
			currDrag.set(0, 0);
			timeDragged = tapAllowance+1;
		}else{
			beingDragged = true;
			timeDragged = 0;
			startDrag.set(screenX, Gdx.graphics.getHeight()-screenY);
			currDrag.set(screenX, Gdx.graphics.getHeight()-screenY);
		}
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		beingDragged = false;
		dragUpSync = true;
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		currDrag.set(screenX, Gdx.graphics.getHeight()-screenY);
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		currDrag.set(screenX, Gdx.graphics.getHeight()-screenY);
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}
}
