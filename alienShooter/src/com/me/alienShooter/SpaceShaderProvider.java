package com.me.alienShooter;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;

public class SpaceShaderProvider implements ShaderProvider
{
	private Shader dirTexShader;
	private Shader myShader;
	private DefaultShaderProvider dirTexShader2;
	
	public SpaceShaderProvider()
	{
	}
	
	public void init()
	{
	}
	
	@Override
	public Shader getShader(Renderable renderable) {
		if (dirTexShader == null)
		{
			dirTexShader = new DefaultShader(renderable);
			dirTexShader.init();
		}
		if (myShader == null)
		{
			myShader = new DirectionalTexture();
			myShader.init();
		}
		return myShader;
	}

	@Override
	public void dispose() {
		if (dirTexShader != null)
			dirTexShader.dispose();
		if (myShader != null)
			myShader.dispose();
	}
	
}
