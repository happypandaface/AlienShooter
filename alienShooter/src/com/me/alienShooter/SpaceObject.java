package com.me.alienShooter;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool.Poolable;

public class SpaceObject implements Poolable
{
	public Vector3 position = new Vector3();

	@Override
	public void reset() {
		position.set(Vector3.Zero);
	}
	
	
}
