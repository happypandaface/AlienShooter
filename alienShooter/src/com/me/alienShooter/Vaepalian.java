package com.me.alienShooter;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;

public class Vaepalian extends Alien
{
	private ArrayMap<String, Model> modelMap;

	public Vaepalian(Array<Model> alienModels, Array<Texture> alienTextures,
			Array<Sound> alienSounds, ParticleEffectPool effectPool)
	{
		squishSound = alienSounds.get(0);
		planeModel = alienModels.get(1);
		gutsTex = alienTextures.get(0);
		dieEffect = effectPool.obtain();
		alienMI = new ModelInstance(alienModels.get(2));
		Acontroller = new AnimationController(alienMI);
		//for (int i = 0; i < alienMI.animations.size; i++)
		//	Gdx.app.log("anim", alienMI.animations.get(i).id);
		Acontroller.setAnimation(alienMI.animations.get(0).id, -1);
		//Acontroller2.setAnimation(alienMI.animations.get(6).id, -1);
		boundingBox = new BoundingBox();
		alienMI.calculateBoundingBox(boundingBox);
		
		particleBoard = new ModelInstance(planeModel);
		
		//super(alienModels, alienTextures, alienSounds, effectPool);
	}
}
