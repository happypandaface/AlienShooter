package com.me.alienShooter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;

public class ExitSign implements Objective
{
	public ModelInstance exitMI;
	public String levelName;
	public String entranceName;
	
	@Override
	public void draw(ModelBatch mBatch, Environment environment, float delta) {
		mBatch.render(exitMI, environment);
	}

	@Override
	public void trigger(float distance, Vector3 playerPos, ActionListener al) {
		Gdx.app.log("triggered", ""+exitMI.transform.getTranslation(new Vector3()));
		if (distance < 10)
		{
			al.loadLevel(levelName, entranceName);
			Gdx.app.log("range", "in range");
		}
	}
}
