package com.me.alienShooter;


import java.util.Iterator;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.ArrayMap;

public class AlienShooter implements ApplicationListener, SceneHandler
{
	private ArrayMap<String, SpaceScene> scenes;
	private SpaceScene currentApp;
	private boolean loading;
	
	public AlienShooter()
	{
		scenes = new ArrayMap<String, SpaceScene>();
		scenes.put("game", new SpaceGame(this));
		scenes.put("main menu", new MainMenu(this));
		scenes.put("training", new TrainingMenu(this));
	}

	@Override
	public void create()
	{
		Iterator<SpaceScene> appIter = scenes.values();
		while (appIter.hasNext())
		{
			SpaceScene app = appIter.next();
			app.create();
		}
		reloadScene("main menu");
		switchScene("main menu");
	}

	@Override
	public void resize(int width, int height)
	{
		Iterator<SpaceScene> appIter = scenes.values();
		while (appIter.hasNext())
		{
			SpaceScene app = appIter.next();
			app.resize(width, height);
		}
	}

	@Override
	public void render()
	{
		if (!loading)
			currentApp.render();
	}

	@Override
	public void pause()
	{
		currentApp.pause();
	}

	@Override
	public void resume() {
		currentApp.resume();
	}

	@Override
	public void dispose()
	{
		Iterator<SpaceScene> appIter = scenes.values();
		while (appIter.hasNext())
		{
			SpaceScene app = appIter.next();
			app.dispose();
		}
	}

	@Override
	public void switchScene(String s)
	{
		//if (currentApp != null)
		//{
		//	currentApp.pause();
		//}
		Gdx.app.log("switched scene to", s);
		currentApp = scenes.get(s);
		Gdx.input.setInputProcessor(currentApp.getInputProcessor());
	}
	
	@Override
	public void reloadScene(String s)
	{
		loading = true;
		SpaceScene scene = scenes.get(s);
		scene.reset();
		scene.start();
		loading = false;
	}
}
