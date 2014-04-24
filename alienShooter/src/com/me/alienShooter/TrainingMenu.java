package com.me.alienShooter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;

public class TrainingMenu implements SpaceScene {

	private Stage stage;
	private AssetManager assets;
	private boolean loading;
	private SceneHandler sHandler;
	private InputMultiplexer im;
	
	public TrainingMenu(SceneHandler sh)
	{
		im = new InputMultiplexer();
		sHandler = sh;
	}
	
	@Override
	public void create()
	{
		
		assets = new AssetManager();
		
		assets.load("data/pukeButtonSmall.png", Texture.class);
		assets.load("data/alienFont_freedom.fnt", BitmapFont.class);
		assets.load("data/alienFont_small_freedom.fnt", BitmapFont.class);
		assets.load("data/kensington_gothic.fnt", BitmapFont.class);
		
		start();
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() {
		stage = new Stage();
		im.addProcessor(stage);
		// TODO Auto-generated method stub
		loading = true;
		
	}
	
	public void doneLoading()
	{
		TextureRegion pukeButton = new TextureRegion(assets.get("data/pukeButtonSmall.png", Texture.class));
		BitmapFont buttonFont = assets.get("data/kensington_gothic.fnt", BitmapFont.class);
		NinePatchDrawable upPatch = new NinePatchDrawable(new NinePatch(pukeButton, 16, 16, 16, 16));

		TextButtonStyle style = new TextButtonStyle();
		style.up = upPatch;
		style.down = upPatch;
		style.font = buttonFont;
		
		Table table = new Table();
		table.setFillParent(true);
		stage.addActor(table);

		TextButton button2 = new TextButton("Shooting!", style);
		table.add(button2);
		table.row();

		TextButton button3 = new TextButton("Engineering!", style);
		table.add(button3);
		table.row();

		TextButton hazButton = new TextButton("Hazards!", style);
		table.add(hazButton);
		table.row();
		
		TextButton button1 = new TextButton("Back", style);
		button1.addListener(new ClickListener()
		{
			@Override
			public void clicked(InputEvent event, float x, float y)
			{
				sHandler.reloadScene("main menu");
				sHandler.switchScene("main menu");
			}
		});
		table.add(button1);
		table.row();
		
		loading = false;
	}

	@Override
	public void resize(int width, int height)
	{
		
	}

	@Override
	public void render()
	{
		if (loading && assets.update())
		{
			doneLoading();
		}

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		stage.act(Gdx.graphics.getDeltaTime());
		stage.draw();
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public InputProcessor getInputProcessor() {
		return im;
	}

}
