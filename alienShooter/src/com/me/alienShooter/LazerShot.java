package com.me.alienShooter;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool.Poolable;

public class LazerShot implements Poolable, HitsThings
{
	public ModelInstance lazerMI;
	public ModelInstance shootEffect;
	public ModelInstance bulletHole;
	public ParticleEffect particleEffect;
	public Vector3 destination;	
	public float effectSize;
	
	private Model planeModel;
	private Texture bulletHoleTex;
	
	private boolean turnedOffShootEffect = false;
	private boolean startedFireEffect = false;
	private float powerScale = 1;
	
	private Sound lazerPower;
	private Sound lazerFire;
	private Sound lazerExplode;
	
	public LazerShot(Array<Model> lazerModels, Array<Texture> lazerTexures, Array<Sound> lazerSounds, ParticleEffectPool effectPool)
	{
		planeModel = lazerModels.get(0);
		bulletHoleTex = lazerTexures.get(0);
		
		lazerPower = lazerSounds.get(0);
		lazerFire = lazerSounds.get(1);
		lazerExplode = lazerSounds.get(2);

		shootEffect = new ModelInstance(planeModel);
		shootEffect.materials.get(0).set(TextureAttribute.createDiffuse(lazerTexures.get(1)),
				new BlendingAttribute());
		
		Model lazerModel = lazerModels.get(1);
		lazerMI = new ModelInstance(lazerModel);
		
		particleEffect = effectPool.obtain();  //assets.get("data/exp.p", ParticleEffect.class);
		effectSize = 1;
	}
	
	public void start()
	{
		lazerPower.play();
		
		bulletHole = new ModelInstance(planeModel);
		bulletHole.materials.get(0).set(TextureAttribute.createDiffuse(bulletHoleTex),
				new BlendingAttribute());
		
		bulletHole.materials.get(0).get(BlendingAttribute.class, BlendingAttribute.Type).opacity = 0.0f;
		shootEffect.materials.get(0).get(BlendingAttribute.class, BlendingAttribute.Type).opacity = 1.0f;
	}
	
	public void reset()
	{
		turnedOffShootEffect = false;
		startedFireEffect = false;
		powerScale = 1;
	}
	
	private Vector3 deathCoords = new Vector3();
	public boolean update(Array<ModelInstance> bulletHoles, float delta)
	{
		//lazerMI.transform.mul(new Matrix4().setToTranslation(new Vector3(delta*-200.0f, 0, 0)));
		if (destination != null)
		{
			Matrix4 destMat = lazerMI.transform.cpy().setTranslation(destination);
			Vector3 currPos = new Vector3();
			lazerMI.transform.getTranslation(currPos);
			Vector3 direction = destination.cpy().sub(currPos).nor();
			float restDistance = currPos.dst(destination);
			float frameDist = delta*80.0f;
			if (restDistance < frameDist)
			{
				if (!startedFireEffect)
				{
					lazerExplode.play();
					// make the bullet hole visible
					bulletHole.transform.getTranslation(deathCoords);
					bulletHole.materials.get(0).get(BlendingAttribute.class, BlendingAttribute.Type).opacity = 1.0f;
					startedFireEffect = true;
					bulletHoles.add(bulletHole);// add the bullet hole
					//particleEffect.reset();
					//particleEffect.start();
				}else
				if (particleEffect.isComplete())
					return true;
			}else
			{
				//lazerMI.transform.lerp(destMat, delta*3);
				if (powerScale > .1)
				{
					float s = 1.0f-delta/powerScale*5f;
					powerScale *= s;
					shootEffect.transform.scale(s, s, s);
				}else
				{
					if (!turnedOffShootEffect)
					{
						lazerFire.play();
						turnedOffShootEffect = true;
						shootEffect.materials.get(0).get(BlendingAttribute.class, BlendingAttribute.Type).opacity = 0f;
					}
					lazerMI.transform.setTranslation(currPos.add(direction.scl(frameDist)));
				}
			}
		}else
			return true;
		return false;
	}
	
	private Vector3 nestPos = new Vector3();
	private Vector3 thisPos = new Vector3();
	public boolean checkAlienHit(GameObjects gObjs)
	{
		if (!startedFireEffect)// make sure it doesn't kill aliens after it's already hit a wall (might be cool to allow actually)
		{
			Iterator<Alien> alienIter = gObjs.getAliens().iterator();
			while (alienIter.hasNext())
			{
				Alien alien = alienIter.next();
				float dist = lazerMI.transform.getTranslation(new Vector3()).cpy().dst(alien.alienMI.transform.getTranslation(new Vector3()));
				if (dist < 4)
				{
					if (alien.getHit(this))
						return true;
				}
			}
			for (AlienSpawner as : gObjs.getNests())
			{
				float dist = as.getPosition(nestPos).sub(getPosition(thisPos)).len();
				if (dist < 4)
				{
					as.getHit(this);
					return true;
				}
			}
		}
		return false;
	}
	
	public Vector3 getPosition(Vector3 thisPos2) {
		// TODO Auto-generated method stub
		return lazerMI.transform.getTranslation(thisPos2);
	}

	public void drawLazer(ModelBatch mBatch, Environment environment, float delta)
	{
		if (!startedFireEffect && turnedOffShootEffect)
			mBatch.render(lazerMI, environment);
	}
	
	public void doEffect(SpriteBatch sBatch, AlienGameUtil AGU, PerspectiveCamera cam, float delta)
	{
		if (startedFireEffect && particleEffect != null)
		{
			AGU.doParticleEffect(deathCoords, particleEffect, 3, sBatch, cam, delta*3.0f);
		}
	}
}
