package com.me.alienShooter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;

public class Item extends BaseObject
{
	private boolean inWorld = true;
	protected float useRange;
	protected Texture iconTex;
	
	public Item()
	{
		super();
		useRange = 7.0f;
		hitSphere = new Vector3();
		gamePos = new Vector3();
	}
	
	public void setInWorld(boolean inWorld)
	{
		this.inWorld = inWorld;
	}
	
	public boolean isInWorld()
	{
		return this.inWorld;
	}
	
	private float rotation = 0;
	public void drawWorld(ModelBatch mBatch, Environment environment, float delta, Vector2 tileDim)
	{
		if (inWorld)
		{
			rotation += delta*100.0f;
			
			mi.transform.set(new Quaternion().setEulerAngles(180, 0, 0));
			mi.transform.setTranslation(new Vector3(tileDim.x*pos.x, -3, tileDim.y*pos.y));
			mi.transform.mul(new Matrix4().set(new Quaternion(new Vector3(0, 1, 0), rotation)));
			mi.transform.mul(new Matrix4().set(new Quaternion(new Vector3(1, 0, 0), 25.0f)));
			mi.transform.scale(0.3f, 0.3f, 0.3f);
			
			mBatch.render(mi, environment);
		}
	}
	private Vector3 hitSphere;
	private Vector3 gamePos;
	public float checkHit(AlienGameUtil AGU, Ray pickRay, Vector2 tileDim) {
		float rtnDist = -1;
		if (inWorld)
		{
			gamePos.set(tileDim.x*pos.x, -2, tileDim.y*pos.y);
			if (Intersector.intersectRaySphere(pickRay, gamePos, 1.0f, hitSphere))
			{
				float dst = hitSphere.sub(pickRay.origin).len();
				if (dst < useRange && (rtnDist == -1 || dst < rtnDist))
					rtnDist = dst;
			}
		}
		return rtnDist;
	}

	private Vector2 realWorldDist = new Vector2();
	private Vector3 realWorldPlayerPos = new Vector3();
	private Vector2 realWorldPlayerPos2 = new Vector2();
	public boolean closeEnoughForPickup(Player player, Vector2 tileDim)
	{
		player.getPosition(realWorldPlayerPos);
		realWorldPlayerPos2.set(realWorldPlayerPos.x, realWorldPlayerPos.z);
		realWorldDist.set(pos).scl(tileDim).sub(realWorldPlayerPos2);
		if (realWorldDist.len() < useRange)
			return true;
		return false;
	}

	public void drawIcon(SpriteBatch sBatch, float delta, int x, int y)
	{
		int w = (int)(Gdx.graphics.getWidth()*GameMenu.ITEM_ICON_WIDTH);
		int h = (int)(Gdx.graphics.getHeight()*GameMenu.ITEM_ICON_HEIGHT);
		sBatch.draw(iconTex, x-w/2, y-h/2, w, h);
		
	}
	
	private Vector3 itmEulDeg;
	public void renderHand(ModelBatch mBatch, Player player, Environment environment, float currentCamRot) {
		Vector3 trans = new Vector3(2, -1.2f, -3);
		trans.mul(player.getTransform(new Matrix4()));
		//itmEulDeg = new Vector3(-90+currentCamRot+120, 0, -5);
		mi.transform.set(new Matrix4());
		//mi.transform.mul(new Matrix4().set(new Quaternion().setEulerAngles(itmEulDeg.x, itmEulDeg.y, itmEulDeg.z)));
		mi.transform.mul(new Matrix4().set(new Quaternion(new Vector3(0, 1, 0), currentCamRot+105.0f)));
		mi.transform.mul(new Matrix4().set(new Quaternion(new Vector3(1, 0, 0), 25.0f)));
		mi.transform.scale(0.2f, 0.2f, 0.2f);
		mi.transform.setTranslation(trans);
		mBatch.render(mi, environment);
	}

	public void positionItem(float x, float y, Player player,
			float currentCamRot) {
		// TODO Auto-generated method stub
		
	}

	public boolean tapOccurred(AlienGameUtil AGU, Pool<LazerShot> lazerPool,
			Array<LazerShot> lazerShots, float currentCamRot,
			ModelInstance wallHit, Player player,
			Vector3[] closestIntersectionTriang, Vector3 closestIntersection) {
		// TODO Auto-generated method stub
		return false;
	}
}
