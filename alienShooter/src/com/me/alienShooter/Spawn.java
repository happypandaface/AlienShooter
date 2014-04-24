package com.me.alienShooter;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;

public class Spawn extends Alien
{
	public Spawn(Array<Model> alienModels, Array<Texture> alienTextures,
			Array<Sound> alienSounds, ParticleEffectPool effectPool)
	{
		super(alienModels, alienTextures,
			alienSounds, effectPool);
	}
}
