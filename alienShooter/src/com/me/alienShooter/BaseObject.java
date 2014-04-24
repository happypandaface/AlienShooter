package com.me.alienShooter;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool.Poolable;

public class BaseObject implements Poolable, Moveable 
{
	public ModelInstance mi;
	protected GameTile gTile;
	protected Vector2 pos;
	
	public BaseObject()
	{
		pos = new Vector2();
	}

	@Override
	public void rotate(Quaternion q) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPosition(Vector2 v) {
		pos.set(v.x, v.y);
	}
	
	@Override
	public void setPosition(Vector3 v) {
		mi.transform.setTranslation(v);
	}

	@Override
	public Vector3 getPosition(Vector3 v) {
		return mi.transform.getTranslation(v);
	}

	@Override
	public Matrix4 getTransform(Matrix4 trans) {
		trans.set(mi.transform);
		return trans;
	}

	@Override
	public void setTile(GameTile gTile) {
		this.gTile = gTile;
	}

	@Override
	public GameTile getTile()
	{

		return gTile;
	}

	@Override
	public void reset()
	{
		
	}

	@Override
	public Vector2 getPosition(Vector2 v) {
		return v.set(pos);
	}

	@Override
	public boolean canTakeAction(MovementAction action) {
		return true;
	}

	@Override
	public Vector2 getLogicalPos(Vector2 v, Vector2 tileDim) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getRotation() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void rotateDeg(float amt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public float getMoveRotation() {
		// TODO Auto-generated method stub
		return 0;
	}
}
