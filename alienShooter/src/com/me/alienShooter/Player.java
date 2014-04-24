package com.me.alienShooter;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class Player implements Moveable
{
	public PerspectiveCamera cam;
	private GameTile gTile;
	private Vector2 tilePos;
	private GameMenu gMenu;
	private Inventory inv;
	private float rotation;
	
	public Player()
	{
		inv = new Inventory();
	}
	
	public Inventory getInventory()
	{
		return inv;
	}

	@Override
	public void rotate(Quaternion q)
	{
		cam.rotate(q);
	}

	private Vector3 vec2ToVec3 = new Vector3();
	@Override
	public void setPosition(Vector2 v)
	{
		vec2ToVec3.set(v.x, 0, v.y);
		setPosition(vec2ToVec3);
	}
	
	public void setMenu(GameMenu menu)
	{
		gMenu = menu;
	}

	@Override
	public void setPosition(Vector3 v)
	{
		cam.position.set(v);
	}

	@Override
	public Matrix4 getTransform(Matrix4 trans)
	{
		trans.set(cam.view);
		trans.inv();
		return trans;
	}

	@Override
	public void setTile(GameTile tile) {
		gTile = tile;
	}

	@Override
	public GameTile getTile() {
		// TODO Auto-generated method stub
		return gTile;
	}

	@Override
	public Vector3 getPosition(Vector3 v) {
		return v.set(cam.position);
	}

	@Override
	public Vector2 getPosition(Vector2 v) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean canTakeAction(MovementAction action) {
		if (gMenu != null && gMenu.inMenu())
			return false;
		return true;
	}

	@Override
	public Vector2 getLogicalPos(Vector2 v, Vector2 tileDim) {
		return v.set(cam.position.x/tileDim.x, cam.position.z/tileDim.y);
	}

	@Override
	public float getRotation() {
		return rotation;
	}

	private Vector3 upVec = new Vector3(0, 1 ,0);
	private Quaternion turnQuat = new Quaternion();
	@Override
	public void rotateDeg(float amt)
	{
		rotation += amt;
		turnQuat.set(upVec, amt);
		cam.rotate(turnQuat);
	}

	@Override
	public float getMoveRotation() {
		return getRotation();
	}
}
