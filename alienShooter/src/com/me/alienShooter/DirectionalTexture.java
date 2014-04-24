package com.me.alienShooter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class DirectionalTexture implements Shader
{
	ShaderProgram program;
	Camera camera;
	RenderContext context;
	int u_projTrans;
	int u_worldTrans;
	int u_color;
	int s_texture;
	int normalMatrix;
	Texture useTex;

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init() {
		String vert = Gdx.files.internal("data/dirTex.vertex.glsl").readString();
		String frag = Gdx.files.internal("data/dirTex.fragment.glsl").readString();
		program = new ShaderProgram(vert, frag);
		if (!program.isCompiled())
			throw new GdxRuntimeException(program.getLog());
		u_projTrans = program.getUniformLocation("u_projTrans");
		u_worldTrans = program.getUniformLocation("u_worldTrans");
		u_color = program.getUniformLocation("u_color");
		s_texture = program.getUniformLocation("s_texture");
		normalMatrix = program.getUniformLocation("normalMatrix");
	}

	@Override
	public int compareTo(Shader other) {
		return 0;
	}

	@Override
	public boolean canRender(Renderable instance) {
		return true;
	}

	@Override
	public void begin(Camera camera, RenderContext context) {
		this.camera = camera;
		this.context = context;
		program.begin();
		program.setUniformMatrix(u_projTrans, camera.combined);
		//context.setDepthTest(GL20.GL_LEQUAL);
		//context.setCullFace(GL20.GL_BACK);
	}

	@Override
	public void render(Renderable renderable) {
		program.setUniformMatrix(u_worldTrans, renderable.worldTransform);
		program.setUniformf(u_color, MathUtils.random(), MathUtils.random(), MathUtils.random());
		
		try
		{
			Gdx.gl20.glActiveTexture(GL20.GL_TEXTURE0);
			((TextureAttribute)renderable.material.get(TextureAttribute.Diffuse)).textureDescription.texture.bind();
			program.setUniformi(s_texture, 0);
		}catch(NullPointerException e)
		{
			
		}
		
		program.setUniformf("sunLight.vColor", new Vector3(1f, 1f, 1f));
		program.setUniformf("sunLight.vDirection", new Vector3(0f, -1f, 0f));
		program.setUniformf("sunLight.fAmbientIntensity", .8f);
		program.setUniformMatrix(normalMatrix, renderable.worldTransform);
		
		renderable.mesh.render(program,
			renderable.primitiveType,
			renderable.meshPartOffset,
			renderable.meshPartSize);
	}

	@Override
	public void end() {
		program.end();
	}
	
}
