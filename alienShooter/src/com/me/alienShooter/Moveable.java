package com.me.alienShooter;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public interface Moveable
{
	public void rotate(Quaternion q);
	public void rotateDeg(float amt);
	public void setPosition(Vector2 v);
	public void setPosition(Vector3 v);
	public Vector3 getPosition(Vector3 v);
	public Vector2 getPosition(Vector2 v);
	public float getRotation();
	public float getMoveRotation();
	public Matrix4 getTransform(Matrix4 trans);
	public void setTile(GameTile gTile);
	public Vector2 getLogicalPos(Vector2 v, Vector2 tileDim);
	public GameTile getTile();
	public boolean canTakeAction(MovementAction action);
}
