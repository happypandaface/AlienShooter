package com.me.alienShooter;

import java.util.Iterator;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.MeshBuilder;
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.graphics.Mesh;

public class AlienShooter implements ApplicationListener, InputProcessor
{
	Model gun;
	AssetManager assets;
	ModelBatch mBatch;
	boolean loading = false;
	public PerspectiveCamera cam;
	ModelInstance gunInstance;
	public Array<ModelInstance> instances = new Array<ModelInstance>();
	public Array<ModelInstance> shootEffects = new Array<ModelInstance>();
	public Array<Renderable> billboards = new Array<Renderable>();
	public Array<ModelInstance> bulletHoles = new Array<ModelInstance>();
	public Environment environment;
	ShapeRenderer sRender;
	Array<Alien> aliens;
	float gunCoolDown = 0;
	final float gunCoolDownMax = 2;
	Vector3 gunEulDeg = new Vector3();
	
	//for mouse picking
	private Vector3 tempVector = new Vector3();
	private Vector3 intersection = new Vector3();
	
	@Override
	public void create()
	{
		Gdx.input.setInputProcessor(this);
		sRender = new ShapeRenderer();
		
		aliens = new Array<Alien>();
		
		mBatch = new ModelBatch();
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.7f, 0.7f, 0.7f, 1f));
		environment.add(new DirectionalLight().set(1f, 1f, 1f, -1f, -0.8f, -0.2f));
		
		cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		//cam.position.set(0, 0, 0);
		//cam.rotate(45, 0, 1, 0);
		//cam.lookAt(0,0,0);
		cam.near = 0.1f;
		cam.far = 300f;
		cam.update();
		
		assets = new AssetManager();
		assets.load("data/lazer.g3dj", Model.class);
		assets.load("data/spawn.g3dj", Model.class);
		assets.load("data/spaceRoom.g3dj", Model.class);
		assets.load("data/stupidPlane.g3dj", Model.class);
		
		loading = true;
	}
	
	private void doneLoading()
	{
		Model ship = assets.get("data/lazer.g3dj", Model.class);
		gunInstance = new ModelInstance(ship);
		gunInstance.transform.setTranslation(new Vector3(0, -1.5f, -3));
		gunInstance.transform.mul(new Matrix4().scl(.1f));
		
		Model spaceRoom = assets.get("data/spaceRoom.g3dj", Model.class);
		ModelInstance roomInst = new ModelInstance(spaceRoom);
		roomInst.transform.setTranslation(new Vector3(0, -5, -15));
		//roomInst.transform.scale(.1f, .1f, .1f);
		instances.add(roomInst);
		
		ModelInstance mi = roomInst;
		Iterator<Mesh> meshIter = mi.model.meshes.iterator();
		while (meshIter.hasNext())
		{
			Mesh mesh = meshIter.next();
			float[] verts = new float[mesh.getNumVertices()*8];
			mesh.getVertices(verts);
			for (int i = 0; i < mesh.getNumVertices()*8; ++i)
			{
				Gdx.app.log("vert", ""+verts[i]);
			}
			short[] indices = new short[mesh.getNumIndices()];
			mesh.getIndices(indices);
			for (int i = 0; i < mesh.getNumIndices(); ++i)
			{
				Gdx.app.log("indi", ""+indices[i]);
			}
		}
		Iterator<Node> nodeIter = mi.nodes.iterator();
		while (nodeIter.hasNext())
		{
			Node n = nodeIter.next();
			Gdx.app.log("node", n.translation.toString());
			Iterator<NodePart> nodePartIter = n.parts.iterator();
			while (nodePartIter.hasNext())
			{
				NodePart np = nodePartIter.next();
				//Gdx.app.log("mesh part verts:", ""+np.meshPart.numVertices);
				MeshPart meshPart = np.meshPart;
				Mesh mesh = meshPart.mesh;
				int floatsInAVertex = mesh.getVertexSize()/4;//sizeof(float);
				float[] verts = new float[mesh.getNumVertices()*floatsInAVertex];
				mesh.getVertices(verts);
				//Gdx.app.log("offset", ""+meshPart.indexOffset);
				short[] indices = new short[mesh.getNumIndices()];//-meshPart.indexOffset];
				mesh.getIndices(indices);//, -meshPart.indexOffset);
				Gdx.app.log("meshPartStart:", "go");
				for (int i = 0 ; i < indices.length ; i++)
				{
					if (i >= meshPart.indexOffset && i < meshPart.indexOffset+meshPart.numVertices)
						Gdx.app.log("meshPartVert:", ""+indices[i]);
				}
				//int indNum = 0;
				//float[] triangs = new float[mesh.getNumIndices()*3];
			}
		}

		
		Model alienModel = assets.get("data/spawn.g3dj", Model.class);
		ModelInstance alienInst = new ModelInstance(alienModel);
		alienInst.transform.set(new Quaternion().setEulerAngles(180, 0, 0));
		alienInst.transform.setTranslation(new Vector3(0, -4, -30));
		AnimationController Acontroller;
		Acontroller = new AnimationController(alienInst);
		for (int i = 0; i < alienInst.animations.size; i++)
			Gdx.app.log("anim", alienInst.animations.get(i).id);
		Acontroller.setAnimation(alienInst.animations.get(0).id, -1);
		Alien alien = new Alien();
		alien.Acontroller = Acontroller;
		alien.model = alienInst;
		aliens.add(alien);
		
		loading = false;
	}
	
	public static Vector3 multQuadByVec(Quaternion quat, Vector3 vec){
	    float num = quat.x * 2f;
	    float num2 = quat.y * 2f;
	    float num3 = quat.z * 2f;
	    float num4 = quat.x * num;
	    float num5 = quat.y * num2;
	    float num6 = quat.z * num3;
	    float num7 = quat.x * num2;
	    float num8 = quat.x * num3;
	    float num9 = quat.y * num3;
	    float num10 = quat.w * num;
	    float num11 = quat.w * num2;
	    float num12 = quat.w * num3;
	    Vector3 result = new Vector3();
	    result.x = (1f - (num5 + num6)) * vec.x + (num7 - num12) * vec.y + (num8 + num11) * vec.z;
	    result.y = (num7 + num12) * vec.x + (1f - (num4 + num6)) * vec.y + (num9 - num10) * vec.z;
	    result.z = (num8 - num11) * vec.x + (num9 + num10) * vec.y + (1f - (num4 + num5)) * vec.z;
	    return result;
	}

	@Override
	public void render() {
		if (loading && assets.update())
			doneLoading();
		
		float delta = Gdx.graphics.getDeltaTime();
		if (delta > .1f)
			delta = .1f;
		
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		/*
		sRender.begin(ShapeType.Filled);
		sRender.setColor(1, 1, 1, 1);
		sRender.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		sRender.end();
		*/
		

		Ray pickRay = null;
		if (gunCoolDown > 0)
			gunCoolDown -= delta;
		if (Gdx.input.isTouched())
		{
			pickRay = cam.getPickRay(Gdx.input.getX(), -Gdx.input.getY());
			float x2 = (float)Gdx.input.getX()/(float)Gdx.graphics.getWidth()-.5f;
			float y2 = (float)Gdx.input.getY()/(float)Gdx.graphics.getHeight()-.5f;
			Vector3 trans = gunInstance.transform.getTranslation(new Vector3());
			gunInstance.transform.set(new Matrix4());
			gunEulDeg = new Vector3(-x2*90-90, 0, y2*90/2-5);
			gunInstance.transform.mul(new Matrix4().set(new Quaternion().setEulerAngles(gunEulDeg.x, gunEulDeg.y, gunEulDeg.z)));
			gunInstance.transform.mul(new Matrix4().scl(.1f));
			gunInstance.transform.setTranslation(trans);
			if (gunCoolDown <= 0)
			{
				Model powerUpModel = assets.get("data/stupidPlane.g3dj", Model.class);
				ModelInstance powerUp = new ModelInstance(powerUpModel);
				powerUp.transform.set(new Quaternion().setEulerAngles(0, 90, 0));
				Matrix4 local = new Matrix4().setTranslation(new Vector3(-10, 0, 0));
				powerUp.transform.setTranslation(gunInstance.transform.cpy().mul(local).getTranslation(new Vector3()));
				Vector3 dir = powerUp.transform.getTranslation(new Vector3());
				powerUp.materials.get(0).set(TextureAttribute.createDiffuse(new Texture(Gdx.files.internal("data/chargingRed.png"))),
						new BlendingAttribute());
				shootEffects.add(powerUp);
				gunCoolDown = gunCoolDownMax;
			}
		}
		mBatch.begin(cam);
		Gdx.gl20.glEnable(GL20.GL_BLEND);
	    Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		mBatch.render(instances, environment);
		Iterator<ModelInstance> environIter = instances.iterator();
		while (environIter.hasNext())
		{
			ModelInstance mi = environIter.next();
			Iterator<Node> nodeIter = mi.nodes.iterator();
			while (nodeIter.hasNext())
			{
				Node n = nodeIter.next();
				pickRay = cam.getPickRay(Gdx.input.getX(), Gdx.input.getY());
				//n.calculateWorldTransform();
				n.calculateLocalTransform();
				pickRay.mul(mi.transform.cpy().inv());
				pickRay.mul(n.calculateWorldTransform().cpy().inv());
				Iterator<NodePart> nodePartIter = n.parts.iterator();
				while (nodePartIter.hasNext())
				{
					NodePart np = nodePartIter.next();
					MeshPart meshPart = np.meshPart;
					Mesh mesh = meshPart.mesh;
					int floatsInAVertex = mesh.getVertexSize()/4;//sizeof(float);
					float[] verts = new float[mesh.getNumVertices()*floatsInAVertex];
					mesh.getVertices(verts);
					//Gdx.app.log("offset", ""+meshPart.indexOffset);
					short[] indicesFull = new short[mesh.getNumIndices()];//-meshPart.indexOffset];
					mesh.getIndices(indicesFull);//, -meshPart.indexOffset);
					short[] indices = new short[meshPart.numVertices];
					int currIndex = 0;
					for (int i = 0 ; i < indicesFull.length ; ++i)
					{
						if (i >= meshPart.indexOffset && i < meshPart.indexOffset+meshPart.numVertices)
						{
							indices[currIndex] = indicesFull[i];
							++currIndex;
						}
					}
					int indNum = 0;
					float[] triangs = new float[meshPart.numVertices*3];
					while (indNum < meshPart.numVertices)
					{
						triangs[indNum*3] = verts[indices[indNum]*floatsInAVertex];
						triangs[indNum*3+1] = verts[indices[indNum]*floatsInAVertex+1];
						triangs[indNum*3+2] = verts[indices[indNum]*floatsInAVertex+2];
						indNum++;
					}
					// normals may not be used
					float[] normals = new float[meshPart.numVertices*3];
					while (indNum < meshPart.numVertices)
					{
						normals[indNum*3] = verts[indices[indNum]*floatsInAVertex+3];
						normals[indNum*3+1] = verts[indices[indNum]*floatsInAVertex+4];
						normals[indNum*3+2] = verts[indices[indNum]*floatsInAVertex+5];
						indNum++;
					}
					//Gdx.app.log("meshVert", ""+mesh.getNumVertices());
					//Gdx.app.log("triags", ""+meshPart.numVertices);
					for (int i = 0; i < meshPart.numVertices/3; ++i)
					{
						Vector3[] triVectors = new Vector3[3];
						for (int c = 0; c < 3; ++c)
						{
							triVectors[c] = new Vector3(
									triangs[i*9+c*3],
									triangs[i*9+c*3+1],
									triangs[i*9+c*3+2]);
						}
						// normals may not be used
						Vector3[] triNormals = new Vector3[3];
						for (int c = 0; c < 3; ++c)
						{
							triNormals[c] = new Vector3(
									triangs[i*9+c*3],
									triangs[i*9+c*3+1],
									triangs[i*9+c*3+2]);
						}
						pickRay = cam.getPickRay(Gdx.input.getX(), Gdx.input.getY());
						//n.calculateWorldTransform();
						n.calculateLocalTransform();
						//pickRay.mul(n.calculateWorldTransform().cpy().inv().mul(mi.transform.cpy().inv()));
						pickRay.mul(mi.transform.cpy().inv());
						pickRay.mul(n.calculateWorldTransform().cpy().inv());
						if (Gdx.input.isTouched() && pickRay != null && 
								Intersector.intersectRayTriangle(
										pickRay, 
										triVectors[0], triVectors[1], triVectors[2], intersection))
						{
							Gdx.app.log("hit", "walls"+intersection.x+"y"+intersection.y+"z"+intersection.z);
							for (int c = 0; c < 3; ++c)
							{
								Gdx.app.log("hit", "normals"+triNormals[c].x+"y"+triNormals[c].y+"z"+triNormals[c].z);
							}
							Vector3 U = triNormals[1].cpy().sub(triNormals[0]);
							Vector3 V = triNormals[2].cpy().sub(triNormals[0]);
							Vector3 N = U.crs(V);
							//Gdx.app.log("normal", "x"+N.x+"y"+N.y+"z"+N.z);
							
							Model powerUpModel = assets.get("data/spawn.g3dj", Model.class);
							ModelInstance powerUp = new ModelInstance(powerUpModel);
							//powerUp.transform.mul(new Matrix4().set(n.calculateWorldTransform().cpy().inv().getRotation(new Quaternion())));
							//powerUp.transform.mul(new Matrix4().set(mi.transform.cpy().inv().getRotation(new Quaternion())));
							//powerUp.transform.mul(new Matrix4().setToTranslation(intersection.cpy().scl(13, 13, 13)));
							//powerUp.transform.set(new Quaternion().setEulerAngles(0, 90, 0));
							//Matrix4 local = new Matrix4().setTranslation(intersection);
							float temp = intersection.y;
							intersection.y = intersection.z;
							intersection.z = -temp;
							//powerUp.transform.mul(new Matrix4().set(new Quaternion().setEulerAngles(90, 90, 90)));
							//powerUp.transform.translate(intersection);
							powerUp.transform.mul(new Matrix4().setToTranslation(intersection.cpy()));
							//powerUp.transform.mul(n.calculateWorldTransform().cpy().inv());
							powerUp.transform.mul(mi.transform);
							//powerUp.transform.mul(n.calculateWorldTransform().cpy());
							//powerUp.transform.mul(mi.transform.cpy());
							//powerUp.transform.mul(new Matrix4().setToTranslation(intersection.cpy().scl(13, 13, 13)));
							//powerUp.transform.mul(mi.transform);
							//powerUp.transform.mul(n.calculateWorldTransform());
							//pickRay.mul(mi.transform.cpy().mul(n.calculateWorldTransform()));
							//powerUp.transform.mul(new Matrix4().setTranslation(n.calculateWorldTransform().getTranslation(new Vector3())).inv());
							//powerUp.transform.mul(new Matrix4().setTranslation(mi.transform.getTranslation(new Vector3())).inv());
							//powerUp.transform.mul(n.calculateWorldTransform().cpy().inv());
							//powerUp.transform.set(n.calculateWorldTransform().cpy());
							//powerUp.transform.setTranslation(intersection);
							
							//powerUp.transform.mul(n.calculateWorldTransform().cpy().inv());
							//powerUp.transform.mul(mi.transform.cpy().inv());
							//powerUp.transform.mul(new Matrix4().set(new Quaternion().setEulerAngles(0, 90, 0)));
							//powerUp.transform.setTranslation(0, 0, -5);
							//powerUp.materials.get(0).set(TextureAttribute.createDiffuse(new Texture(Gdx.files.internal("data/chargingRed.png"))),
							//		new BlendingAttribute());
							bulletHoles.add(powerUp);
						}
					}
					/*
					if (Gdx.input.isTouched() && pickRay != null && Intersector.intersectRayTriangles(pickRay, triangs, intersection))
					{
						
						Gdx.app.log("hit", "walls"+intersection.x+"y"+intersection.y+"z"+intersection.z);
					}*/
				}
			}
		}/* no node or identity node transformation picking
		while (environIter.hasNext())
		{
			ModelInstance mi = environIter.next();
			pickRay = cam.getPickRay(Gdx.input.getX(), Gdx.input.getY());
			pickRay.mul(mi.transform);
			Iterator<Mesh> meshIter = mi.model.meshes.iterator();
			while (meshIter.hasNext())
			{
				Mesh mesh = meshIter.next();
				int floatsInAVertex = mesh.getVertexSize()/4;//sizeof(float);
				float[] verts = new float[mesh.getNumVertices()*floatsInAVertex];
				mesh.getVertices(verts);
				short[] indices = new short[mesh.getNumIndices()];
				mesh.getIndices(indices);
				int indNum = 0;
				float[] triangs = new float[mesh.getNumIndices()*3];
				while (indNum < mesh.getNumIndices())
				{
					triangs[indNum*3] = verts[indices[indNum]*floatsInAVertex];
					triangs[indNum*3+1] = verts[indices[indNum]*floatsInAVertex+1];
					triangs[indNum*3+2] = verts[indices[indNum]*floatsInAVertex+2];
					indNum++;
				}
				//Gdx.app.log("meshVert", ""+mesh.getNumVertices());
				if (Gdx.input.isTouched() && pickRay != null && Intersector.intersectRayTriangles(pickRay, triangs, intersection))
				{
					
					Gdx.app.log("hit", "walls"+intersection.x+"y"+intersection.y+"z"+intersection.z);
				}
			}
		}*/
		Iterator<Alien> alienIter = aliens.iterator();
		while (alienIter.hasNext())
		{
			Alien alien = alienIter.next();
			Matrix4 destMat = alien.model.transform.cpy().setTranslation(0, -5, 0);
			alien.model.transform.lerp(destMat, delta/10);
			//if (destMat.getTranslation(new Vector3()).len() < 1)
			//Gdx.app.log("hit", ""+alien.model.transform.getTranslation(new Vector3()).dst2(destMat.getTranslation(new Vector3())));
			mBatch.render(alien.model, environment);
			alien.Acontroller.update(delta);
			if (pickRay != null)
			{
				alien.model.transform.getTranslation(tempVector);
				pickRay = cam.getPickRay(Gdx.input.getX(), Gdx.input.getY());
				pickRay.mul(alien.model.transform.cpy().inv());
				
				BoundingBox bb = new BoundingBox();
				alien.model.calculateBoundingBox(bb);//shouldn't be every frame
				if (Gdx.input.isTouched() && pickRay != null && Intersector.intersectRayBounds(pickRay, bb, intersection))
				{
					Gdx.app.log("hit", "ray");
				}
				/*
				*/
			}
		}
		Iterator<Renderable> rendIter = billboards.iterator();
		while (rendIter.hasNext())
		{
			Renderable rend = rendIter.next();
			mBatch.render(rend);
		}
		Iterator<ModelInstance> bhIter = bulletHoles.iterator();
		while (bhIter.hasNext())
		{
			ModelInstance rend = bhIter.next();
			mBatch.render(rend);
		}
		Iterator<ModelInstance> shootIter = shootEffects.iterator();
		while (shootIter.hasNext())
		{
			ModelInstance rend = shootIter.next();
			float s = 1-(delta)*(2);
			//rend.transform.scale(s, s, s);
			mBatch.render(rend);
		}
		//mBatch.flush();
		//Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
		if (gunInstance != null)
			mBatch.render(gunInstance, environment);
		mBatch.end();
	}
	
	private void aimGun(int x, int y)
	{
		/*
		float x2 = (float)x/Gdx.graphics.getWidth()-.5f;
		float y2 = (float)y/Gdx.graphics.getHeight()-.5f;
		//gunInstance.transform = new Matrix4().rotate(new Quaternion().setEulerAngles(x2*90, 0, y2*90));
		Vector3 trans = gunInstance.transform.getTranslation(new Vector3());
		gunInstance.transform.set(new Quaternion().setEulerAngles(-x2*90-90, 0, y2*90/2-5));
		gunInstance.transform.setTranslation(trans);
		Ray pickRay = null;
		pickRay = cam.getPickRay(x, y, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		
		gunInstance.transform.getTranslation(tempVector);
		if (pickRay != null && Intersector.intersectRaySphere(pickRay, tempVector, 1, intersection))
		{
			Gdx.app.log("hit", "ray");
		}
		*/
	}
	
	@Override
	public void pause()
	{
		
	}

	@Override
	public void resize(int width, int height) {
		
	}

	@Override
	public void resume() {
		
	}

	@Override
	public void dispose() {
		mBatch.dispose();
		instances.clear();
		assets.dispose();
	}

	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		aimGun(screenX, screenY);
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		aimGun(screenX, screenY);
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}
}
