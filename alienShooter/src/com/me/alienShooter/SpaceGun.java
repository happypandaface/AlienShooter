package com.me.alienShooter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

public class SpaceGun extends Item
{
	private Vector3 normalVector = new Vector3();
	private Quaternion norQuat = new Quaternion();
	private Matrix4 norMatrix = new Matrix4();
	private Matrix4 localEndGun = new Matrix4().setTranslation(new Vector3(-8, 0, 0));
	private ModelInstance gunInstance;
	
	public SpaceGun(StuffHolder mh)
	{
		gunInstance = new ModelInstance(mh.getModel("spaceRifle"));
		mi = gunInstance;
		iconTex = mh.getTex("spaceRifle");
	}
	
	@Override
	public boolean tapOccurred(AlienGameUtil AGU, Pool<LazerShot> lazerPool,
			Array<LazerShot> lazerShots, float currentCamRot,
			ModelInstance wallHit, Player player,
			Vector3[] closestIntersectionTriang, Vector3 closestIntersection)
	{
		LazerShot ls = lazerPool.obtain();
		ls.start();
		lazerShots.add(ls);
		
		AGU.setAtEndOfGun(ls.shootEffect, gunInstance);
		
		AGU.calcNormal(closestIntersectionTriang, true, normalVector, norQuat);
		// the intersection point needs to be flipped as well
		AGU.reorientIntersection(closestIntersection);
		// this pushes the bullet out a bit from the wall
		AGU.setForBillBoard(closestIntersection, normalVector);
		
		//ls.shootEffect.transform.mul(new Matrix4().setToLookAt(ls.shootEffect.transform.getTranslation(new Vector3()), cam.position, new Vector3(0, 1, 0)));
		ls.shootEffect.transform.mul(new Matrix4().set(new Quaternion(new Vector3(0, 0, 1), -currentCamRot)));//currentCamRot)));
		
		// order matters
		ls.bulletHole.transform = new Matrix4();
		ls.bulletHole.transform.mul(wallHit.transform);// apply the object's transform (the intersection coords are in local space)
		ls.bulletHole.transform.mul(new Matrix4().setToTranslation(closestIntersection.cpy()));// apply the local cords of the intersection
		ls.bulletHole.transform.mul(norMatrix.set(norQuat.nor()));// make the bullet hole face away from the triangle (wall)
		Vector3 screenCoords;
		Vector3 worldCoords = new Vector3();
		ls.bulletHole.transform.getTranslation(worldCoords);
		screenCoords = worldCoords.cpy();
		player.cam.project(screenCoords);
		//Gdx.app.log("screenCoords", "x: "+screenCoords.x+"y: "+screenCoords.y+"z: "+screenCoords.z);
		ls.destination = worldCoords.cpy();
		ls.effectSize = player.cam.position.dst(worldCoords);
		ls.effectSize = (50.0f-ls.effectSize)/50.0f*0.4f;
		
		if (ls.effectSize > 0)
		{
			ls.particleEffect.reset();
			ls.particleEffect.start();
			
			//ls.particleEffect.setPosition(screenCoords.x/ls.effectSize, screenCoords.y/ls.effectSize);
			//ls.particleEffect.reset();
			//ls.particleEffect.start();
		}
		//gunInstance.transform.mul(new Matrix4().setToLookAt(worldCoords.sub(gunInstance.transform.getTranslation(new Vector3())).nor(), new Vector3(0, -1, 0)));
		//lazer.transform.mul(new Matrix4().setToLookAt(lazer.transform.getTranslation(new Vector3()).sub(worldCoords).nor(), new Vector3(0, 1, 0)));
		//gunInstance.transform.mul(new Matrix4().set(new Quaternion().setEulerAngles(-90, 0, 0)));
		
		ls.lazerMI.materials.get(0).set(new ColorAttribute(1, 1.0f, 0.0f, 0.0f, 1.0f));
		ls.lazerMI.transform = new Matrix4();//.set(local);
		ls.lazerMI.transform.mul(gunInstance.transform);
		ls.lazerMI.transform.mul(localEndGun);
		return true;
		
		//AGU.setAtEndOfGun(ls.lazerMI, gunInstance);
		//ls.lazerMI.transform.setTranslation(gunInstance.transform.cpy().mul(local).getTranslation(new Vector3()));
		//ls.lazerMI.transform.scl(.1f,.1f,.1f);
		//ls.lazerMI.transform.mul(new Matrix4().setToLookAt(ls.lazerMI.transform.getTranslation(new Vector3()), worldCoords, new Vector3(0, -1, 0)));
		//ls.lazerMI.transform.mul(new Matrix4().set(new Quaternion().setEulerAngles(-90, 0, 0)));
		//lazer.transform.mul(new Matrix4().setToLookAt(lazer.transform.getTranslation(new Vector3()).sub(worldCoords).nor(), new Vector3(0, 1, 0)));
		//lazer.transform.mul(new Matrix4().set(new Quaternion().setEulerAngles(90, 90, -90)));
	}

	@Override
	public void renderHand(ModelBatch mBatch, Player player, Environment environment, float currentCamRot) {
		if (gunInstance != null)
			mBatch.render(gunInstance, environment);
	}

	private Vector3 gunEulDeg;
	@Override 
	public void positionItem(float x, float y, Player player, float currentCamRot) {
		if (gunInstance != null)
		{
			Vector3 trans = new Vector3(0, -1.5f, -3);
			trans.mul(player.getTransform(new Matrix4()));
			gunInstance.transform.set(new Matrix4());
			gunEulDeg = new Vector3(-x*90-90+currentCamRot, 0, y*90/2-5);
			//gunInstance.transform.mul(cam.invProjectionView);
			gunInstance.transform.mul(new Matrix4().set(new Quaternion().setEulerAngles(gunEulDeg.x, gunEulDeg.y, gunEulDeg.z)));
			gunInstance.transform.mul(new Matrix4().scl(.1f));
			gunInstance.transform.setTranslation(trans);
		}
	}
}
