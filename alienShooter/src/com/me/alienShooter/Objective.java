package com.me.alienShooter;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;

public interface Objective
{
	public void draw(ModelBatch mBatch, Environment environment, float delta);
	public void trigger(float distance, Vector3 playerPos, ActionListener al);
}
