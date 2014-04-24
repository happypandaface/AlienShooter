package com.me.alienShooter;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;

public class StuffHolder
{
	private ArrayMap<String, Model> models;
	private ArrayMap<String, Texture> textures;
	
	public StuffHolder()
	{
		models = new ArrayMap<String, Model>();
		textures = new ArrayMap<String, Texture>();
	}
	
	public void putModel(String s, Model m)
	{
		models.put(s, m);
	}
	public Model getModel(String s)
	{
		return models.get(s);
	}
	
	public void putTex(String s, Texture t)
	{
		textures.put(s, t);
	}
	public Texture getTex(String s)
	{
		return textures.get(s);
	}
}
