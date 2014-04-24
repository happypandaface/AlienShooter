package com.me.alienShooter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class AlienGameUtil extends ExtendedGameUtil
{
	
	private Matrix4 localEndGun = new Matrix4().setTranslation(new Vector3(-8, 0, 0));
	private Quaternion endOfGunRot = new Quaternion().setEulerAngles(0, 90, 0);
	private Vector3 endOfGunCurrPos = new Vector3();
	public void setAtEndOfGun(ModelInstance mi, ModelInstance gun)
	{
		mi.transform.set(endOfGunRot);// should be adjusted for camera
		mi.transform.setTranslation(gun.transform.cpy().mul(localEndGun).getTranslation(endOfGunCurrPos));
	}
	public static float norAngleDeg(float rotation)
	{
		float rot = rotation;
		while (rot > 180)
			rot -= 360;
		while (rot < -180)
			rot += 360;
		return rot;
	}
	
	private Vector3 projectedCoords = new Vector3();
	private Vector3 effectDistToCam = new Vector3();
	public void doParticleEffect(Vector3 coords3d, ParticleEffect particleEffect, float size, SpriteBatch sBatch, PerspectiveCamera cam, float delta)
	{
		particleEffect.update(delta);
		
		if (cam.frustum.pointInFrustum(coords3d))
		{
			projectedCoords.set(coords3d);
			cam.project(projectedCoords);
			effectDistToCam.set(coords3d);
			effectDistToCam.sub(cam.position);
			float distance = effectDistToCam.len();
			distance = 1/distance;
			
			sBatch.setTransformMatrix(new Matrix4().setTranslation(projectedCoords.x, projectedCoords.y, 0).scl(distance*size, distance*size, distance*size));
			particleEffect.draw(sBatch);
		}
	}
	
	private GameTile[] tempTile = new GameTile[1];
	private Matrix4 tempMat = new Matrix4();
	private Vector3 tempVec = new Vector3();
	public void setTileOf(Vector2 tileDimensions, Moveable m, GameTiles gTiles)
	{
		m.getTransform(tempMat);
		tempMat.getTranslation(tempVec);
		m.setTile(gTiles.getTileAtWorld(tileDimensions, tempVec.x, tempVec.z));
	}
	public void setTileOf(Vector2 tileDimensions, Moveable m, GameTiles gTiles, Vector2 tile)
	{
		Gdx.app.log("syringe tile", ""+tile.toString());
		m.setTile(gTiles.getTileAtLogical(tile.x, tile.y));
	}
}
