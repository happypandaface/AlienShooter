package com.me.alienShooter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class MovementAction extends SpaceAction
{
	public static final int NO_ACTION = 0;
	public static final int MOVE_FORWARD = 1;
	public static final int MOVE_BACKWARD = 2;
	public static final int TURN_LEFT = 3;
	public static final int TURN_RIGHT = 4;
	public static final int FIRE_GUN = 5;
	private int type;
	private float magnitude;
	
	public MovementAction()
	{
		this(NO_ACTION);
	}
	
	public MovementAction(int t)
	{
		this(t, 1);
	}
	
	public MovementAction(int t, float mag)
	{
		type = t;
		magnitude = mag;
		setGoal(-1);
	}
	
	public void set(int t, float magn)
	{
		type = t;
		magnitude = magn;
		setGoal(-1);
	}
	
	private float currProgress;
	private float goal;
	public void setGoal(float amt)
	{
		currProgress = 0;
		goal = amt;
	}
	
	public boolean hasReachedGoal()
	{
		if (currProgress >= goal)
		{
			return true;
		}
		return false;
	}
	
	public boolean is(int t)
	{
		return type == t;
	}
	
	// this allows for actions that can cancel other actions (ex: being attacked stops you from running)
	public void process(Array<MovementAction> mActions)
	{
		
	}
	
	private Vector3 forwardVector = new Vector3(0, 0, -1);
	private Vector3 backwardVector = new Vector3(0, 0, 1);
	private Vector3 tempMoveVector = new Vector3();
	private Vector3 turningAxis = new Vector3(0, 1, 0);
	private Quaternion turnLeft = new Quaternion();//new Vector3(0, 1, 0), 45);
	private Quaternion turnRight = new Quaternion();//new Vector3(0, -1, 0), 45);
	private Vector2 gamePos = new Vector2();
	private Matrix4 moverTrans = new Matrix4();
	// do action (parameters will be updated as game progresses)
	public void doAction(ActionListener al, Moveable mover, GameTiles tiles, float delta)
	{
		if (mover.canTakeAction(this))
		{
			if (type != NO_ACTION)
			{
				if (type == FIRE_GUN)
				{
					al.fireGun();
				}else
				if (type == TURN_LEFT)
				{
					float amt = 45*magnitude*delta;
					if (goal != -1 && currProgress+amt > goal)
						amt = goal-currProgress;
					currProgress += amt;
					turnLeft.set(turningAxis, amt);
					mover.rotateDeg(amt);
					al.addRotation(amt);
				}else
				if (type == TURN_RIGHT)
				{
					float amt = 45*magnitude*delta;
					if (goal != -1 && currProgress+amt > goal)
						amt = goal-currProgress;
					currProgress += amt;
					turnRight.set(turningAxis, -amt);
					mover.rotateDeg(-amt);
					al.addRotation(-amt);
				}else
				{
					float amt = delta*magnitude;
					if (goal != -1 && currProgress+amt > goal)
						amt = goal-currProgress;
					if (type == MOVE_FORWARD)
					{
						tempMoveVector.set(forwardVector);
					}else
					if (type == MOVE_BACKWARD)
					{
						tempMoveVector.set(backwardVector);
					}
					tempMoveVector.scl(amt);
					currProgress += amt;
					//tempMoveVector.mul(mover.getTransform(moverTrans));
					//tempMoveVector.mul(new Matrix4().set(new Quaternion(new Vector3(0, 1, 0), mover.getRotation())));
					tempMoveVector.mul(new Matrix4().setTranslation(mover.getPosition(new Vector3())).mul(new Matrix4().set(new Quaternion(new Vector3(0, 1, 0), mover.getMoveRotation()))));
					//tempMoveVector.sub(cam.position);
					//cam.position.add(tempMoveVector);
					gamePos.set(tempMoveVector.x, tempMoveVector.z);
					GameTile[] currentTile = new GameTile[1];
					tiles.rectify(gamePos, currentTile);
					mover.setTile(currentTile[0]);
					tempMoveVector.x = gamePos.x;
					tempMoveVector.z = gamePos.y;
					mover.setPosition(tempMoveVector);
				}
			}
		}
	}
}
