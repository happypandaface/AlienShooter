package com.me.alienShooter;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class Zone extends SpaceObject
{
	private ModelInstance zoneMI;
	private Texture zoneTex;
	
	public Zone(Array<Model> zoneModels, Array<Texture> zoneTextures)
	{
		zoneMI = new ModelInstance(zoneModels.get(0));
		zoneMI.materials.get(0).set(TextureAttribute.createDiffuse(zoneTextures.get(0)),
				new BlendingAttribute());
	}
	
	public void setPosition(Vector3 v)
	{
		position.set(v);
	}
	
	public void draw(ModelBatch mBatch, Environment environ, PerspectiveCamera cam)
	{
		zoneMI.transform = new Matrix4();
		//zoneMI.transform.setTranslation(position);
		//zoneMI.transform = new Matrix4().mul(new Matrix4().setToLookAt(position, cam.position, Vector3.Y));//new Matrix4();
		zoneMI.transform = cam.view.cpy().inv();
		zoneMI.transform.setTranslation(position);
		zoneMI.transform.mul(new Matrix4().set(new Quaternion(new Vector3(0, 1, 0), 180)));
		zoneMI.transform.mul(new Matrix4().set(new Quaternion(new Vector3(1, 0, 0), -90)));
		
		/*
		Vector3 distanceToCam = cam.position.cpy().sub(position);
		float rotToCam = (float)(Math.atan2(distanceToCam.x, distanceToCam.y)/Math.PI*180f);
		zoneMI.transform = new Matrix4();
		zoneMI.transform.setTranslation(position);
		zoneMI.transform.mul(new Matrix4().set(new Quaternion(new Vector3(0, 1, 0), 180-rotToCam)));
		zoneMI.transform.mul(new Matrix4().set(new Quaternion(new Vector3(1, 0, 0), -90)));
		*/
		
		//zoneMI.transform.mul(new Matrix4().setToLookAt(position, cam.position, Vector3.Y));
		//zoneMI.transform.setTranslation(position);
		//zoneMI.transform.rotate(new Quaternion(new Vector3(0, 0, 1), 90));
		
		mBatch.render(zoneMI, environ);
	}
}
