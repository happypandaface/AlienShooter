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
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
	public Array<ModelInstance> lazers = new Array<ModelInstance>();
	public Array<ModelInstance> shootEffects = new Array<ModelInstance>();
	public Array<Renderable> billboards = new Array<Renderable>();
	public Array<ModelInstance> bulletHoles = new Array<ModelInstance>();
	public Environment environment;
	ShapeRenderer sRender;
	Array<Alien> aliens;
	float gunCoolDown = 0;
	final float gunCoolDownMax = .2f;
	Vector3 gunEulDeg = new Vector3();
	Ray pickRay;
	private ParticleEffect effect;
	private float effectScale;
	private SpriteBatch sBatch;
	private Array<LazerShot> lazerShots;
	private ParticleEffectPool effectPool;
	
	//for mouse picking
	private Vector3 tempVector = new Vector3();
	private Vector3 intersection = new Vector3();
	
	@Override
	public void create()
	{
		Gdx.input.setInputProcessor(this);
		sRender = new ShapeRenderer();
		
		aliens = new Array<Alien>();
		lazerShots = new Array<LazerShot>();
		
		sBatch = new SpriteBatch();
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
		assets.load("data/lazerGun.g3dj", Model.class);
		assets.load("data/spawn.g3dj", Model.class);
		assets.load("data/spaceRoom.g3dj", Model.class);
		assets.load("data/spaceRoom2.g3dj", Model.class);
		assets.load("data/stupidPlane.g3dj", Model.class);
		assets.load("data/exp.p", ParticleEffect.class);
		
		loading = true;
	}
	
	private void doneLoading()
	{
		Model ship = assets.get("data/lazerGun.g3dj", Model.class);
		gunInstance = new ModelInstance(ship);
		gunInstance.transform.setTranslation(new Vector3(0, -1.5f, -3));
		gunInstance.transform.mul(new Matrix4().scl(.1f));
		
		effect = assets.get("data/exp.p", ParticleEffect.class);
		effect.start();
		effectPool = new ParticleEffectPool(effect, 0, 10);
		
		Model spaceRoom = assets.get("data/spaceRoom.g3dj", Model.class);
		ModelInstance roomInst = new ModelInstance(spaceRoom);
		roomInst.transform.setTranslation(new Vector3(0, -5, -15));
		//roomInst.transform.scale(.1f, .1f, .1f);
		instances.add(roomInst);
		
		ModelInstance mi = roomInst;
		/*
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
		}*/
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

	@Override
	public void render() {
		if (loading && assets.update())
			doneLoading();
		
		float delta = Gdx.graphics.getDeltaTime();
		if (delta > .1f)
			delta = .1f;
		
		// clear screen
		Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		for (int i = 0; i < lazerShots.size; ++i)
		{
			if (lazerShots.get(i).update(delta))
			{
				lazerShots.removeIndex(i);
				--i;
			}
		}
		
		// do gun logic
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
				gunCoolDown = gunCoolDownMax;
				doBulletHole(instances, Gdx.input.getX(), Gdx.input.getY());
			}
		}
		// start rendering
		mBatch.begin(cam);
		// these might be useful later
		//Gdx.gl20.glEnable(GL20.GL_BLEND);
		//Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		mBatch.render(instances, environment);
		/*
		Iterator<ModelInstance> lazerIter = lazers.iterator();
		while (lazerIter.hasNext())
		{
			ModelInstance l = lazerIter.next();
			l.transform.mul(new Matrix4().setToTranslation(new Vector3(delta*-200.0f, 0, 0)));
		}*/
		Iterator<LazerShot> lazerShotIter = lazerShots.iterator();
		while (lazerShotIter.hasNext())
		{
			LazerShot ls = lazerShotIter.next();
			mBatch.render(ls.lazerMI, environment);
		}
		//mBatch.render(lazers, environment);
		Iterator<Alien> alienIter = aliens.iterator();
		while (alienIter.hasNext())
		{
			Alien alien = alienIter.next();
			Matrix4 destMat = alien.model.transform.cpy().setTranslation(0, -5, 0);
			alien.model.transform.lerp(destMat, delta/10);
			mBatch.render(alien.model, environment);
			alien.Acontroller.update(delta);
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
		mBatch.end();
		Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
		sBatch.begin();
		
		Iterator<LazerShot> lazerParticleIter = lazerShots.iterator();
		while (lazerParticleIter.hasNext())
		{
			LazerShot ls = lazerParticleIter.next();
			ls.doEffect(sBatch, delta);
		}
		/*
		if (effect != null)
		{
			effect.update(delta);
			sBatch.setTransformMatrix(new Matrix4().scl(effectScale, effectScale, effectScale));
			effect.draw(sBatch);
		}*/
		sBatch.end();
		mBatch.begin(cam);
		Iterator<LazerShot> lazerPowerIter = lazerShots.iterator();
		while (lazerPowerIter.hasNext())
		{
			LazerShot ls = lazerPowerIter.next();
			mBatch.render(ls.shootEffect);
		}
		if (gunInstance != null)
			mBatch.render(gunInstance, environment);
		mBatch.end();
	}
	
	private boolean doBulletHole(Array<ModelInstance> mis, int x, int y)
	{
		LazerShot ls = new LazerShot();
		lazerShots.add(ls);
		
		Matrix4 local = new Matrix4().setTranslation(new Vector3(-8, 0, 0));
		
		Model powerUpModel = assets.get("data/stupidPlane.g3dj", Model.class);
		ModelInstance powerUp = new ModelInstance(powerUpModel);
		powerUp.transform.set(new Quaternion().setEulerAngles(0, 90, 0));
		powerUp.transform.setTranslation(gunInstance.transform.cpy().mul(local).getTranslation(new Vector3()));
		powerUp.materials.get(0).set(TextureAttribute.createDiffuse(new Texture(Gdx.files.internal("data/chargingRed.png"))),
				new BlendingAttribute());
		//shootEffects.add(powerUp);
		ls.shootEffect = powerUp;
		
		
		Model lazerModel = assets.get("data/lazer.g3dj", Model.class);
		ModelInstance lazer = new ModelInstance(lazerModel);
		//lazer.transform.set(new Quaternion().setEulerAngles(0, 90, 0));
		//Matrix4 local = new Matrix4().setTranslation(new Vector3(-10, 0, 0));
		//powerUp.materials.get(0).set(TextureAttribute.createDiffuse(new Texture(Gdx.files.internal("data/chargingRed.png"))),
		//		new BlendingAttribute());
		ls.lazerMI = lazer;
		//lazers.add(lazer);
		
		Iterator<Alien> alienIter = aliens.iterator();
		while (alienIter.hasNext())
		{
			Alien alien = alienIter.next();
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
		Vector3[] closestIntersectionTriang = new Vector3[3];
		Vector3 closestIntersection = new Vector3();
		ModelInstance[] mi = new ModelInstance[1];// for returning
		if (ExtendedGameUtil.getIntersectionTriangles(mis, cam, x, y, closestIntersection, closestIntersectionTriang, mi) != -1)
		{
			
			// get the normal vector from the triangle hit
			Vector3 U = closestIntersectionTriang[1].cpy().sub(closestIntersectionTriang[0]);
			Vector3 V = closestIntersectionTriang[2].cpy().sub(closestIntersectionTriang[0]);
			Vector3 N = U.crs(V);
			N.nor();
			
			// it's probably inverted (depends on exporter)
			float tempNormal = N.y;
			N.y = N.z;
			N.z = -tempNormal;
			
			// make the bullethole obj
			Model bulletHoleModel = assets.get("data/stupidPlane.g3dj", Model.class);
			ModelInstance bulletHole = new ModelInstance(bulletHoleModel);
			bulletHole.materials.get(0).set(TextureAttribute.createDiffuse(new Texture(Gdx.files.internal("data/bulletHole.png"))),
					new BlendingAttribute());
			ls.bulletHole = bulletHole;
			bulletHole.materials.get(0).get(BlendingAttribute.class, BlendingAttribute.Type).opacity = 0.0f;
			
			// find quaternion from normal
			Quaternion q = new Quaternion();
			Vector3 v2 = new Vector3(0, 1, 0).nor();
			Vector3 v1 = N;// add .cpy() to this if it causes problems
			q.setFromCross(v2, v1);
			
			// the intersection point needs to be flipped as well
			float temp = closestIntersection.y;
			closestIntersection.y = closestIntersection.z;
			closestIntersection.z = -temp;
			
			// this pushes the bullet out a bit from the wall
			closestIntersection.add(N.scl(.1f, .1f, .1f));
			// order matters
			bulletHole.transform.mul(mi[0].transform);// apply the object's transform (the intersection coords are in local space)
			bulletHole.transform.mul(new Matrix4().setToTranslation(closestIntersection.cpy()));// apply the local cords of the intersection
			bulletHole.transform.mul(new Matrix4().set(q.nor()));// make the bullet hole face away from the triangle (wall)
			bulletHoles.add(bulletHole);// add the bullet hole
			Vector3 screenCoords;
			Vector3 worldCoords = new Vector3();
			bulletHole.transform.getTranslation(worldCoords);
			screenCoords = worldCoords.cpy();
			cam.project(screenCoords);
			//Gdx.app.log("screenCoords", "x: "+screenCoords.x+"y: "+screenCoords.y+"z: "+screenCoords.z);
			ls.destination = worldCoords.cpy();
			ls.effectSize = cam.position.dst(worldCoords);
			ls.effectSize = (50.0f-effectScale)/50.0f*0.2f;
			if (ls.effectSize > 0)
			{
				ls.particleEffect = effectPool.obtain();  //assets.get("data/exp.p", ParticleEffect.class);
				
				ls.particleEffect.setPosition(screenCoords.x/ls.effectSize, screenCoords.y/ls.effectSize);
				//ls.particleEffect.reset();
				//ls.particleEffect.start();
			}
			//gunInstance.transform.mul(new Matrix4().setToLookAt(worldCoords.sub(gunInstance.transform.getTranslation(new Vector3())).nor(), new Vector3(0, -1, 0)));
			//lazer.transform.mul(new Matrix4().setToLookAt(lazer.transform.getTranslation(new Vector3()).sub(worldCoords).nor(), new Vector3(0, 1, 0)));
			//gunInstance.transform.mul(new Matrix4().set(new Quaternion().setEulerAngles(-90, 0, 0)));
			
			lazer.materials.get(0).set(new ColorAttribute(1, 1.0f, 0.0f, 0.0f, 1.0f));
			lazer.transform.setTranslation(gunInstance.transform.cpy().mul(local).getTranslation(new Vector3()));
			lazer.transform.scl(.1f,.1f,.1f);
			lazer.transform.mul(new Matrix4().setToLookAt(lazer.transform.getTranslation(new Vector3()), worldCoords, new Vector3(0, -1, 0)));
			lazer.transform.mul(new Matrix4().set(new Quaternion().setEulerAngles(-90, 0, 0)));
			//lazer.transform.mul(new Matrix4().setToLookAt(lazer.transform.getTranslation(new Vector3()).sub(worldCoords).nor(), new Vector3(0, 1, 0)));
			//lazer.transform.mul(new Matrix4().set(new Quaternion().setEulerAngles(90, 90, -90)));
			return true;
		}
		return false;
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
