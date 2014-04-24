package com.me.alienShooter;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;

public class FlyGuy extends Alien
{
	
	public FlyGuy(Array<Model> alienModels, Array<Texture> alienTextures,
			Array<Sound> alienSounds, ParticleEffectPool effectPool, StuffHolder stuffHolder)
	{
		super(alienModels, alienTextures,
			alienSounds, effectPool);
		alienMI = new ModelInstance(stuffHolder.getModel("flyGuy"));
		Acontroller = new AnimationController(alienMI);
		Acontroller.setAnimation(alienMI.animations.get(0).id, -1);
		boundingBox = new BoundingBox();
		alienMI.calculateBoundingBox(boundingBox);
		baseRot = 90;
		horizAngles = new Vector3(0, 0, 1);
	}
}
