package com.me.alienShooter;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.Input.Keys;

public class GestureHandler implements InputProcessor
{
	// all the listeners (probs only be one)
	private Array<ActionListener> listeners;
	private MovementAction mAction = new MovementAction();// the move action
	private MovementAction tAction = new MovementAction();// the turn action
	
	public GestureHandler()
	{
		listeners = new Array<ActionListener>();
		// initialize all the reused vectors
		for (int i = 0; i < maxTouches; ++ i)
		{
			startDrag[i] = new Vector2();
			currDrag[i] = new Vector2();
			totalDrag[i] = new Vector2();
			minTouched[i] = new Vector2();
			maxTouched[i] = new Vector2();
		}
	}
	
	// there should be a removeListener method too
	public void addListener(ActionListener al)
	{
		listeners.add(al);
	}
	
	// this is used to relay the action to all the listeners
	public void doAction(MovementAction ma, float delta)
	{
		if (ma != null)
		{
			Iterator<ActionListener> alIter = listeners.iterator();
			while (alIter.hasNext())
			{
				ActionListener al = alIter.next();
				al.doAction(ma, delta);
			}
		}
	}
	
	private int maxTouches = 2;
	
	// the deadZone is a radius where no gesture is recorded
	private Vector2 deadZone = new Vector2();
	// the controlScale is a ring (beyond the deadZone) that alows the player some variance control 
	private Vector2 controlScale = new Vector2();
	// the this is the x diff you have to return to when you do a turn around gesture (x diff from start)
	// it's a bit bigger than the deadZone
	private float turnAround = 0;
	// this is the amt of time the user has been dragging. this is used to determine is the player made a quick gesture
	private float[] startDragCount = new float[maxTouches];
	// this is the amount of time since the release of a touch, used for multitouch gestures
	private float[] endDragCount = new float[maxTouches];
	// this is time that the player has after he start dragging to make a gesture
	private float delayTilFree = .5f;// this is for gestures (like open inventory, turn around, etc...)
	// this is set to true once when the player releases touch, used to make things synchronous
	private boolean[] touchReleased = new boolean[maxTouches];
	// this is true whenever the screen is touched (used to make things synchronous again)
	private boolean[] touchIsDown = new boolean[maxTouches];
	/* this is true when the player is in action such as turning around is prevents the free move/look 
	 * (bad coding, should be controlled with the "process" method of MoveAction
	 */
	private boolean inAction;
	
	// start drags of the touches
	private Vector2 startDrag[] = new Vector2[maxTouches];
	// other important vectors
	private Vector2 currDrag[] = new Vector2[maxTouches];
	private Vector2 totalDrag[] = new Vector2[maxTouches];
	private Vector2 minTouched[] = new Vector2[maxTouches];
	private Vector2 maxTouched[] = new Vector2[maxTouches];
	private GameMenu gameMenu;
	private boolean syncSwitchGameMenu;
	
	public void update(float delta)
	{
		// set up the deadZone and controlScale, should happen on resize
		deadZone.set(Gdx.graphics.getWidth()/20, Gdx.graphics.getHeight()/20);
		controlScale.set(Gdx.graphics.getWidth()/7, Gdx.graphics.getHeight()/7);
		turnAround = Gdx.graphics.getWidth()/10;
		
		for (int i = 0; i < touchIsDown.length; ++i)// increase clock
			if (touchIsDown[i])
				startDragCount[i] += delta;
			else
				endDragCount[i] += delta;
		
		if (syncSwitchGameMenu && gameMenu != null)
		{
			if (gameMenu.inMenu())
			{
				gameMenu.setInMenu(false);
			}else
			{
				gameMenu.setInMenu(true);
			}
			syncSwitchGameMenu = false;
		}
		
		boolean skipTap = gameMenu.comsumeLeave();
		
		
		if (inAction)
		{
			/* do the action in progress if there is one.
			 * step out of action when it's over
			 */
			for (int i = 0; i < touchReleased.length; ++i)// increase clock
				touchReleased[i] = false;
			if (tAction.hasReachedGoal())
			{
				inAction = false;
			}
		}else
		{
			/*
			 * the following code is for free look/move (and detecting gestures)
			 * free look/move wasn't planned and could (should?) be taken out and replaced with the simpler 45deg turn control
			 */
			// reset the move and turn actions
			tAction.set(MovementAction.NO_ACTION, 1);
			mAction.set(MovementAction.NO_ACTION, 1);
			
			// check for gestures
			for (int i = 0; i < maxTouches; ++i)
			{
				if (touchReleased[i] && startDragCount[i] < delayTilFree)
				{
					Gdx.app.log("touch", "handled");
					float dist = startDrag[i].dst(currDrag[i]);
					// this makes it so the handler doesn't immediately fire when leaving the menu
					if (!skipTap && dist < deadZone.len())
					{// if it's in the deadZone that means the player tapped the screen and wants to fire the gun
						doAction(new MovementAction(MovementAction.FIRE_GUN), 1);
					}else
					{// otherwise it's a gesture
						// maybs it's the first release of a multi-touch (user has to release both fingers for a multi-touch gesture)
						boolean multitouchStart = false;
						for (int c = 0; c < maxTouches; ++c)
						{
							if (i != c && touchIsDown[c])
								multitouchStart = true;
						}
						if (!multitouchStart)
						{
							// maybe this is the second release of a multi-touch
							boolean multitouch = false;
							int ot = 0;//other touch
							for (int c = 0; c < maxTouches; ++c)
							{
								if (i != c && endDragCount[c] < delayTilFree)// the other touch was released quick enough for
								{											 // this to be considered a multi-touch gesture
									ot = c;
									multitouch = true;
								}
							}
							if (multitouch)
							{
								Gdx.app.log("multitouch", "again");
								if (startDrag[i].cpy().sub(startDrag[ot]).len() < currDrag[i].cpy().sub(currDrag[ot]).len())
								{
									if (gameMenu != null)
										gameMenu.setInMenu(true);
								}else
								{
									if (gameMenu != null)
										gameMenu.setInMenu(false);
								}
							}else if (!gameMenu.inMenu())// stop reading other gestures when in menu
							{
								if (totalDrag[i].y < -deadZone.y)
								{// the following are down gestures
									if (Math.abs(totalDrag[i].x) < turnAround)
									{// this means the gesture ends below where it started
										if (currDrag[i].x - minTouched[i].x > deadZone.x)
										{// this checks to see if the player swiped down left then right and down which is a left turn around
											mAction.set(MovementAction.NO_ACTION, 1);
											tAction.set(MovementAction.TURN_LEFT, 10);
											tAction.setGoal(180);
											inAction = true;
										}else
										if (maxTouched[i].x - currDrag[i].x > deadZone.x)
										{// this checks to see if the player swiped down right then down left which is a right turn around
											mAction.set(MovementAction.NO_ACTION, 1);
											tAction.set(MovementAction.TURN_RIGHT, 10);
											tAction.setGoal(180);
											inAction = true;
										}
									}else
									{// this means the gesture ends either left or right of the origin
										if (totalDrag[i].x > deadZone.x)
										{// this is a down right drag, turn 90 degrees left
											mAction.set(MovementAction.NO_ACTION, 1);
											tAction.set(MovementAction.TURN_LEFT, 10);
											tAction.setGoal(90);
											inAction = true;
										}else
										if (totalDrag[i].x < -deadZone.x)
										{// this is a down right drag, turn 90 degrees right
											mAction.set(MovementAction.NO_ACTION, 1);
											tAction.set(MovementAction.TURN_RIGHT, 10);
											tAction.setGoal(90);
											inAction = true;
										}
									}
								}
							}
						}
					}
					// don't trigger a synchronous up cursor event again 
					touchReleased[i] = false;
				}
				
				// check if the player is making a multi-touch move
				boolean multitouch = false;
				for (int c = 0; c < maxTouches; ++c)
				{
					if (i != c && (touchIsDown[c] ||
							endDragCount[c] < delayTilFree))
						multitouch = true;
				}
				
				if (touchIsDown[i] && !multitouch)
				{// this is free look/move
					/* get the variability (if there is any) negative variabilities wont register
					 * as long as the totalDrag is tested again the deadZone after 
					 */
					float horizDragPercent = ((float)Math.abs(totalDrag[i].x)-deadZone.x)/(controlScale.x);
					float vertiDragPercent = ((float)Math.abs(totalDrag[i].y)-deadZone.y)/(controlScale.y);
					// set the max to be at the edge of the control ring
					if (horizDragPercent > 1)
						horizDragPercent = 1;
					if (vertiDragPercent > 1)
						vertiDragPercent = 1;
					/* the following are pretty self-explanatory
					 * tAction is the turning action and mAction is the move action
					 * maybe only one should be possible at a time?
					 */
					if (totalDrag[i].x > deadZone.x)
					{
						tAction.set(MovementAction.TURN_LEFT, horizDragPercent*3);
					}else if (totalDrag[i].x < -deadZone.x)
					{
						tAction.set(MovementAction.TURN_RIGHT, horizDragPercent*3);
					}
					if (totalDrag[i].y > deadZone.y)
					{
						mAction.set(MovementAction.MOVE_FORWARD, vertiDragPercent*14);
					}else if (totalDrag[i].y < -deadZone.y)
					{
						mAction.set(MovementAction.MOVE_BACKWARD, vertiDragPercent*6);
					}
				}
			}
		}
		
		// do the current move action and turn action (no effect if they're null)
		doAction(mAction, delta);
		doAction(tAction, delta);
	}
	
	@Override
	public boolean keyDown(int keycode) {
		// this is for testing the menu in desktop mode.
		if (gameMenu != null && Input.Keys.SPACE == keycode)
		{
			syncSwitchGameMenu = true;
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (pointer < maxTouches)
		{
			int i = pointer;
			// reset the timer
			startDragCount[i] = 0;
			touchIsDown[i] = true;
			touchReleased[i] = false;
			startDrag[i].set(screenX, screenY);
			// setup the min and max touched for gestures (such as turn around)
			minTouched[i].set(startDrag[i]);
			maxTouched[i].set(startDrag[i]);
			currDrag[i].set(screenX, screenY);
			totalDrag[i].set(startDrag[i].cpy().sub(currDrag[i]));
		}
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if (pointer < maxTouches)
		{
			int i = pointer;
			// tell the loop the touch was released and start count
			endDragCount[i] = 0;
			touchReleased[i] = true;
			touchIsDown[i] = false;
		}
		return false;
	}
	
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if (pointer < maxTouches)
		{
			int i = pointer;
			currDrag[i].set(screenX, screenY);
			// update the min and max touched
			if (screenX > maxTouched[i].x)
				maxTouched[i].x = screenX;
			if (screenY > maxTouched[i].y)
				maxTouched[i].y = screenY;
			if (screenX < minTouched[i].x)
				minTouched[i].x = screenX;
			if (screenY < minTouched[i].y)
				minTouched[i].y = screenY;
			// totalDrag is used to tell if the current drag is out of the deadZone and calc variability
			totalDrag[i].set(startDrag[i].cpy().sub(currDrag[i]));
		}
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}

	public void setMenu(GameMenu gameMenu) {
		this.gameMenu = gameMenu;
		
	}
	
}
