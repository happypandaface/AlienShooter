package com.me.alienShooter;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.InputProcessor;

public interface SpaceScene extends ApplicationListener
{
	public InputProcessor getInputProcessor();
	public void reset();
	public void start();
}
