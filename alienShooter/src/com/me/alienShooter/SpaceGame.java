package com.me.alienShooter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
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
import com.badlogic.gdx.graphics.g3d.Shader;
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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Pixmap;

public class SpaceGame implements SpaceScene, InputProcessor, ActionListener, ObjectCreator
{
	Model gun;
	private final AssetManager assets = new AssetManager();
	ModelBatch mBatch;
	boolean loading = false;
	//ModelInstance gunInstance;
	public Array<ModelInstance> instances = new Array<ModelInstance>();
	public Array<ModelInstance> lazers = new Array<ModelInstance>();
	public Array<ModelInstance> shootEffects = new Array<ModelInstance>();
	public Array<Renderable> billboards = new Array<Renderable>();
	public Array<ModelInstance> bulletHoles = new Array<ModelInstance>();
	public Environment environment;
	public Array<ModelInstance> objectiveMis = new Array<ModelInstance>();
	public Array<Objective> objectives = new Array<Objective>();
	public ArrayMap<ModelInstance, Objective> objectiveMap = new ArrayMap<ModelInstance, Objective>();
	ShapeRenderer sRender;
	float gunCoolDown = 0;
	final float gunCoolDownMax = .2f;
	private ParticleEffect effect;
	private ParticleEffect aDeathEffect;
	private float effectScale;
	private SpriteBatch sBatch;
	private Array<LazerShot> lazerShots;
	private ParticleEffectPool effectPool;
	private ParticleEffectPool aDeathEffectPool;
	private AlienGameUtil AGU;
	private ArrayMap<Model, Array<Vector3[]>> environMeshArr = new ArrayMap<Model, Array<Vector3[]>>();
	private Array<Model> lazerModels;
	private Array<Texture> lazerTexures;
	private boolean fireGun;
	private Array<Sound> lazerSounds = new Array<Sound>();
	private Array<Sound> alienSounds = new Array<Sound>();
	private float currentCamRot = 0;
	private GestureHandler gHandler;
	private GameTiles tiles;
	private Model spaceTile;
	private Model spaceWall;
	private Model gunModel;
	private Player player;
	private SceneHandler sHandler;
	private InputMultiplexer im;
	private GameObjects gObjects;
	private Vector2 tileDimensions = new Vector2();
	private String currentLevel;
	private ArrayMap<String, Model> objectiveModels = new ArrayMap<String, Model>();
	private Array<Zone> zones;
	private SpaceShaderProvider shaderProvider;
	private Shader myShader;
	private StuffHolder stuffHolder;
	
	
	public SpaceGame(SceneHandler sh)
	{
		im = new InputMultiplexer();
		sHandler = sh;
	}
	
    private final Pool<LazerShot> lazerPool = new Pool<LazerShot>()
    {
		@Override
		protected LazerShot newObject() {
			return new LazerShot(lazerModels, lazerTexures, lazerSounds, effectPool);
		}
    };
	private Array<Model> alienModels;
	private Array<Texture> alienTextures;
    private final Pool<Alien> alienPool = new Pool<Alien>()
    {
		@Override
		protected Alien newObject() {
			return new Alien(alienModels, alienTextures, alienSounds, aDeathEffectPool);
		}
    };
    private final Pool<Vaepalian> vaepalianPool = new Pool<Vaepalian>()
    {
		@Override
		protected Vaepalian newObject() {
			return new Vaepalian(alienModels, alienTextures, alienSounds, aDeathEffectPool);
		}
    };
    private final Pool<Syringe> syringePool = new Pool<Syringe>()
    {
		@Override
		protected Syringe newObject() {
			return new Syringe(stuffHolder);
		}
    };
    private final Pool<Radio> radioPool = new Pool<Radio>()
    {
		@Override
		protected Radio newObject() {
			return new Radio(stuffHolder);
		}
    };
    private final Pool<AlienSpawner> spawnerPool = new Pool<AlienSpawner>()
    {
		@Override
		protected AlienSpawner newObject() {
			return new AlienSpawner(alienModels);
		}
    };
	private Array<Model> zoneModels;
	private Array<Texture> zoneTextures;
    private final Pool<Zone> zonePool = new Pool<Zone>()
    {
		@Override
		protected Zone newObject() {
			return new Zone(zoneModels, zoneTextures);
		}
    };
	
	//for mouse picking
	private Vector3 tempVector = new Vector3();
	private Vector3 intersection = new Vector3();
	
	@Override
	public void create()
	{
		shapeRenderer = new ShapeRenderer();// this is just for the loading screen
		
		assets.load("data/lazer.g3dj", Model.class);
		assets.load("data/lazerGun.g3dj", Model.class);
		assets.load("data/spawn.g3dj", Model.class);
		assets.load("data/alienSpawner.g3dj", Model.class);
		assets.load("data/spaceRoom.g3dj", Model.class);
		assets.load("data/spaceRoom2.g3dj", Model.class);
		assets.load("data/room3.g3dj", Model.class);
		assets.load("data/shipTile.g3dj", Model.class);
		assets.load("data/shipWall.g3dj", Model.class);
		assets.load("data/stupidPlane.g3dj", Model.class);
		assets.load("data/vapolian.g3dj", Model.class);
		assets.load("data/alienDeathP.p", ParticleEffect.class);
		assets.load("data/exp.p", ParticleEffect.class);
		assets.load("data/bulletHole.png", Texture.class);
		assets.load("data/puke.png", Texture.class);
		assets.load("data/chargingRed.png", Texture.class);
        assets.load("data/Powerup19.wav", Sound.class);
        assets.load("data/Laser_Shoot20.wav", Sound.class);
        assets.load("data/Explosion25.wav", Sound.class);
        assets.load("data/Explosion45.wav", Sound.class);
        assets.load("data/squish.mp3", Sound.class);
        assets.load("data/squish.wav", Sound.class);
        assets.load("data/exitSign.g3dj", Model.class);
        assets.load("data/syringe.g3dj", Model.class);
        assets.load("data/spaceRadio.g3dj", Model.class);
        assets.load("data/Gaseous.png", Texture.class);
        assets.load("data/syringeIcon.png", Texture.class);
        assets.load("data/pukeButtonSmall.png", Texture.class);
        assets.load("data/gunIconSmall.png", Texture.class);
        assets.load("data/radioIcon.png", Texture.class);
        //assets.load("data/fullShark.g3dj", Model.class);
        
        
		start();
	}
	
	@Override
	public void start()
	{
		Gdx.app.log("SpaceGame", "starting");
		stuffHolder = new StuffHolder();
		
		im.clear();
		gHandler = new GestureHandler();
		im.addProcessor(gHandler);
		gObjects = new GameObjects();
		gObjects.setObjectCreator(this);
		gObjects.gMenu = new GameMenu();
		im.addProcessor(gObjects.gMenu);
		gHandler.addListener(this);
		//Gdx.input.setInputProcessor(gHandler);
		sRender = new ShapeRenderer();
		
		AGU = new AlienGameUtil();
		
		lazerShots = new Array<LazerShot>();
		zones = new Array<Zone>();
		
		sBatch = new SpriteBatch();
		myShader = new DirectionalTexture();
		myShader.init();
		shaderProvider = new SpaceShaderProvider();
		shaderProvider.init();
		mBatch = new ModelBatch(/*shaderProvider*/);
		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.7f, 0.7f, 0.7f, 1f));
		environment.add(new DirectionalLight().set(1f, 1f, 1f, -1f, -0.8f, -0.2f));
		
		player = new Player();
		resetPlayer();
		
		gObjects.player = player;
		
		loading = true;
	}
	private void resetPlayer()
	{
		PerspectiveCamera cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		player.cam = cam;
		player.cam.position.set(1, 1, 1);
//		cam.rotate(30, 0, 1, 0);
		//cam.lookAt(0,0,0);
		player.cam.near = 0.1f;
		player.cam.far = 300f;
		player.cam.update();
	}
	
	private void doneLoading()
	{
		Gdx.app.log("SpaceGame", "done loading");
		alienModels = new Array<Model>();
		//alienModels.add(assets.get("data/fullShark.g3dj", Model.class));
		alienModels.add(assets.get("data/spawn.g3dj", Model.class));
		alienModels.add(assets.get("data/stupidPlane.g3dj", Model.class));
		alienModels.add(assets.get("data/vapolian.g3dj", Model.class));
		alienModels.add(assets.get("data/alienSpawner.g3dj", Model.class));
		alienTextures = new Array<Texture>();
		alienTextures.add(assets.get("data/puke.png", Texture.class));
		alienSounds = new Array<Sound>();
		alienSounds.add(assets.get("data/squish.wav", Sound.class));
		
		lazerModels = new Array<Model>();
		lazerModels.add(assets.get("data/stupidPlane.g3dj", Model.class));
		lazerModels.add(assets.get("data/lazer.g3dj", Model.class));
		lazerTexures = new Array<Texture>();
		lazerTexures.add(assets.get("data/bulletHole.png", Texture.class));
		lazerTexures.add(assets.get("data/chargingRed.png", Texture.class));
		lazerSounds = new Array<Sound>();
		lazerSounds.add(assets.get("data/Powerup19.wav", Sound.class));
		lazerSounds.add(assets.get("data/Laser_Shoot20.wav", Sound.class));
		lazerSounds.add(assets.get("data/Explosion45.wav", Sound.class));
		
		stuffHolder.putModel("syringe", assets.get("data/syringe.g3dj", Model.class));
		stuffHolder.putTex("syringeIcon", assets.get("data/syringeIcon.png", Texture.class));
		stuffHolder.putTex("menu", assets.get("data/pukeButtonSmall.png", Texture.class));
		stuffHolder.putTex("spaceRifle", assets.get("data/gunIconSmall.png", Texture.class));
		stuffHolder.putTex("radioIcon", assets.get("data/radioIcon.png", Texture.class));
		stuffHolder.putModel("radio", assets.get("data/spaceRadio.g3dj", Model.class));
		
		zoneModels = new Array<Model>();
		zoneModels.add(assets.get("data/stupidPlane.g3dj", Model.class));
		zoneTextures = new Array<Texture>();
		zoneTextures.add(assets.get("data/Gaseous.png", Texture.class));
		

		gunModel = assets.get("data/lazerGun.g3dj", Model.class);
		stuffHolder.putModel("spaceRifle", assets.get("data/lazerGun.g3dj", Model.class));
		
		player.getInventory().addItem(new SpaceGun(stuffHolder));
		gObjects.gMenu.setup(stuffHolder);
		gHandler.setMenu(gObjects.gMenu);// this needs to be done after the gameMenu is created
		player.setMenu(gObjects.gMenu);
		
		objectiveModels = new ArrayMap<String, Model>();
		objectiveModels.put("exit sign", assets.get("data/exitSign.g3dj", Model.class));
		
		effect = assets.get("data/exp.p", ParticleEffect.class);
		effectPool = new ParticleEffectPool(effect, 0, 10);
		
		aDeathEffect = assets.get("data/alienDeathP.p", ParticleEffect.class);
		aDeathEffectPool = new ParticleEffectPool(aDeathEffect, 0, 10);
		
		spaceTile = assets.get("data/shipTile.g3dj", Model.class);
		spaceWall = assets.get("data/shipWall.g3dj", Model.class);
		BoundingBox tileBox = new BoundingBox();
		spaceTile.calculateBoundingBox(tileBox);
		tileDimensions.set(tileBox.getDimensions().x, tileBox.getDimensions().z);
		gObjects.setTileDim(tileDimensions);

		loadLevel("entrance", "start");
		
		loading = false;
	}
	public void resetLevel()
	{
		gObjects.getAliens().clear();
		bulletHoles.clear();
		objectiveMis.clear();
		instances.clear();
		zones.clear();
		gObjects.items.clear();
		gObjects.getNests().clear();
	}
	public void loadLevel(String s, String entranceName)
	{
		// happens for all
		resetPlayer();
		resetLevel();
		
		currentCamRot = 0;
		
		// reset the gun
		//gunInstance = new ModelInstance(gunModel);
		//gunInstance.transform.setTranslation(new Vector3(0, -1.5f, -3));
		//gunInstance.transform.mul(new Matrix4().scl(.1f));
		// should be more here
		
		// specific to level
		if (s == "entrance")
		{
			// do tiles first
			tiles = new GameTiles();
			for (int i = 0; i < 10; ++i)
			{
				tiles.addTile(0, i);
			}
			tiles.addTile(1, 3);
			tiles.addTile(-1, 5);
			tiles.addTile(-2, 5);
			
			AGU.setTileOf(tileDimensions, player, tiles);
			levelGraphicsAndLogic(tiles);

			AlienSpawner as = newSpawner(0, 8, 0, "spawn");
			as.setMaxAliens(2);
			newSyringe(0, 3, 0, "red");
			newRadio(0, 2, 0, "red");
			
			environMeshArr.clear();
			AGU.getModelInstanceArrTriangles(instances, environMeshArr);
			AGU.getModelArrTriangles(objectiveModels, environMeshArr);
			
			//newAlien(0, 8, 0, "spawn");
			
			addExit(-2, 5, 90, "space prep", "start");
			
			// face the player correctly
			if (entranceName == "start")
			{
				player.setPosition(new Vector3(tileDimensions.x*0, 0, tileDimensions.y*0));
				new MovementAction(MovementAction.TURN_LEFT, 4).doAction(this, player, tiles, 1);
			}else
			if (entranceName == "from prep room")
			{
				player.setPosition(new Vector3(tileDimensions.x*-2, 0, tileDimensions.y*5));
				new MovementAction(MovementAction.TURN_RIGHT, 2).doAction(this, player, tiles, 1);
			}
		}else
		if (s == "space prep")
		{
			// do tiles first
			tiles = new GameTiles();
			for (int i = 0; i < 10; ++i)
			{
				tiles.addTile(0, i);
			}
			for (int i = 0; i < 10; ++i)
			{
				tiles.addTile(1, i);
			}
			tiles.addTile(0, -1);
			
			AGU.setTileOf(tileDimensions, player, tiles);
			levelGraphicsAndLogic(tiles);
			
			environMeshArr.clear();
			AGU.getModelInstanceArrTriangles(instances, environMeshArr);
			AGU.getModelArrTriangles(objectiveModels, environMeshArr);
			
			newAlien(1, 8, 0, "vaepalian");
			newAlien(0, 8, 0, "spawn");
			
			addExit(0, -1, 0, "entrance", "from prep room");
			
			// face the player correctly
			new MovementAction(MovementAction.TURN_LEFT, 4).doAction(this, player, tiles, 1);
		}else
		if (s == "test room")
		{
			// do tiles first
			tiles = new GameTiles();
			for (int i = 0; i < 3; ++i)
			{
				tiles.addTile(0, i);
				tiles.addTile(1, i);
				tiles.addTile(-1, i);
			}
			
			AGU.setTileOf(tileDimensions, player, tiles);
			levelGraphicsAndLogic(tiles);
			
			environMeshArr.clear();
			// get the level for making bullet holes
			AGU.getModelInstanceArrTriangles(instances, environMeshArr);
			// get the objects (door switches and exits and such) these dont get bullet holes
			AGU.getModelArrTriangles(objectiveModels, environMeshArr);
			
			//newAlien(1, 8, 0, "vaepalian");
			//newAlien(0, 8, 0, "spawn");
			
			//addExit(0, -1, 0, "entrance", "from prep room");
			
			addZone(0, 1);
			
			// face the player correctly
			new MovementAction(MovementAction.TURN_LEFT, 4).doAction(this, player, tiles, 1);
		}
		currentLevel = s;
	}
	
	public void levelGraphicsAndLogic(GameTiles gTiles)
	{
		// this function converts the logical GameTiles generated into real world instance and 
		// also sets up graphical and logical walls. The graphical walls serve to hittest
		// with lazers to see if a shot is valid.
		gTiles.setTileDimensions(tileDimensions.x, tileDimensions.y);
		Iterator<GameTile> tileIter = gTiles.iterator();
		while(tileIter.hasNext())
		{
			GameTile tile = tileIter.next();
			Vector3 tilePos = new Vector3(tileDimensions.x*tile.x, -5, tileDimensions.y*tile.y);
			ModelInstance floor = new ModelInstance(spaceTile);
			floor.transform.setTranslation(tilePos);
			instances.add(floor);
			GameTile northTile = gTiles.getTileAt(tile.x, tile.y+1);
			if (northTile == null)
			{
				ModelInstance northWall = new ModelInstance(spaceWall);
				northWall.transform.setTranslation(tilePos);
				northWall.transform.mul(new Matrix4().set(new Quaternion(new Vector3(0, 1, 0), 180)));
				instances.add(northWall);
			}else
			{
				tile.adjTiles[GameTile.NORTH_EXIT] = northTile;
				tile.exits[GameTile.NORTH_EXIT] = true;
			}
			GameTile southTile = gTiles.getTileAt(tile.x, tile.y-1);
			if (southTile == null)
			{
				ModelInstance southWall = new ModelInstance(spaceWall);
				southWall.transform.setTranslation(tilePos);
				southWall.transform.mul(new Matrix4().set(new Quaternion(new Vector3(0, 1, 0), 0)));
				instances.add(southWall);
			}else
			{
				tile.adjTiles[GameTile.SOUTH_EXIT] = southTile;
				tile.exits[GameTile.SOUTH_EXIT] = true;
			}
			GameTile eastTile = gTiles.getTileAt(tile.x+1, tile.y);
			if (eastTile == null)
			{
				ModelInstance eastWall = new ModelInstance(spaceWall);
				eastWall.transform.setTranslation(tilePos);
				eastWall.transform.mul(new Matrix4().set(new Quaternion(new Vector3(0, 1, 0), -90)));
				instances.add(eastWall);
			}else
			{
				tile.adjTiles[GameTile.EAST_EXIT] = eastTile;
				tile.exits[GameTile.EAST_EXIT] = true;
			}
			GameTile westTile = gTiles.getTileAt(tile.x-1, tile.y);
			if (westTile == null)
			{
				ModelInstance westWall = new ModelInstance(spaceWall);
				westWall.transform.setTranslation(tilePos);
				westWall.transform.mul(new Matrix4().set(new Quaternion(new Vector3(0, 1, 0), 90)));
				instances.add(westWall);
			}else
			{
				tile.adjTiles[GameTile.WEST_EXIT] = westTile;
				tile.exits[GameTile.WEST_EXIT] = true;
			}
		}
	}
	
	public void addExit(float x, float y, float rot, String levelName, String entranceName)
	{
		Vector3 tilePos = new Vector3(tileDimensions.x*x, -5, tileDimensions.y*y);
		ExitSign eSign = new ExitSign();
		eSign.levelName = levelName;
		eSign.entranceName = entranceName;
		eSign.exitMI = new ModelInstance(objectiveModels.get("exit sign"));
		eSign.exitMI.transform.setTranslation(tilePos);
		eSign.exitMI.transform.mul(new Matrix4().set(new Quaternion(new Vector3(0, 1, 0), rot)));
		objectives.add(eSign);
		objectiveMis.add(eSign.exitMI);
		objectiveMap.put(eSign.exitMI, eSign);
		//aliens.add(alien);
	}
	
	public void addZone(int x, int y)
	{
		Zone z = zonePool.obtain();
		z.setPosition(new Vector3(x*tileDimensions.x, 0, y*tileDimensions.y));
		zones.add(z);
	}
	
	private Alien makingAlien;
	public Alien newAlien(float x, float y, float rot, String type)
	{
		if (type == "spawn")
		{
			makingAlien = alienPool.obtain();
		}else if (type == "vaepalian")
		{
			makingAlien = vaepalianPool.obtain();
		}
		if (makingAlien == null)
			return null;
		makingAlien.alienMI.transform.set(new Quaternion().setEulerAngles(180, 0, 0));
		makingAlien.alienMI.transform.setTranslation(new Vector3(tileDimensions.x*x, -3, tileDimensions.y*y));
		makingAlien.alienMI.transform.mul(new Matrix4().set(new Quaternion(new Vector3(0, 1, 0), 180)));
		//alien.alienMI.transform.scl(2, 2, 2);
		AGU.setTileOf(tileDimensions, makingAlien, tiles);
		gObjects.getAliens().add(makingAlien);
		return makingAlien;
	}
	private AlienSpawner makingSpawner;
	public AlienSpawner newSpawner(float x, float y, float rot, String type)
	{
		makingSpawner = spawnerPool.obtain();
		makingSpawner.setPosition(new Vector3(tileDimensions.x*x, -3, tileDimensions.y*y));
		//alien.alienMI.transform.scl(2, 2, 2);
		AGU.setTileOf(tileDimensions, makingSpawner, tiles);
		gObjects.getNests().add(makingSpawner);
		return makingSpawner;
	}
	private Syringe makingSyringe;
	public Syringe newSyringe(float x, float y, float rot, String type)
	{
		makingSyringe = syringePool.obtain();
		Vector2 loc = new Vector2(x, y);
		makingSyringe.setPosition(loc);
		//alien.alienMI.transform.scl(2, 2, 2);
		AGU.setTileOf(tileDimensions, makingSyringe, tiles, loc);
		gObjects.items.add(makingSyringe);
		return makingSyringe;
	}
	private Radio makingRadio;
	public Radio newRadio(float x, float y, float rot, String type)
	{
		makingRadio = radioPool.obtain();
		Vector2 loc = new Vector2(x, y);
		makingRadio.setPosition(loc);
		//alien.alienMI.transform.scl(2, 2, 2);
		AGU.setTileOf(tileDimensions, makingRadio, tiles, loc);
		gObjects.items.add(makingRadio);
		return makingRadio;
	}
	
	private Vector2 lastShot = new Vector2();
	private ShapeRenderer shapeRenderer;
	@Override
	public void render()
	{
		if (loading && assets.update())
			doneLoading();
		if (loading)
		{
			Gdx.app.log("progress", ""+assets.getProgress());
			Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
			shapeRenderer.begin(ShapeType.Filled);
			shapeRenderer.setColor(1, 0, 0, 1);
			shapeRenderer.rect(0, 0, Gdx.graphics.getWidth()*assets.getProgress(), Gdx.graphics.getHeight());
			shapeRenderer.end();
			/*
			sBatch.setTransformMatrix(new Matrix4());
			sBatch.begin();
			Pixmap pm = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
			for (int i = 0; i < 16*16; ++ i)
				pm.drawPixel(i/16, i%16, 0xff0000);
			Texture t = new Texture(pm);
			sBatch.draw(t, 0, 0, Gdx.graphics.getWidth()*assets.getProgress(), Gdx.graphics.getHeight());
			sBatch.end();*/
		}else
		{
			
			float delta = Gdx.graphics.getDeltaTime();
			if (delta > .1f)
				delta = .1f;
			
			doFrameBuffers(sBatch, delta);
			
			// clear screen
			Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
			
			//Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
			
			doLogic(delta);
			// start rendering
			mBatch.begin(player.cam);
			Gdx.gl.glDisable(GL20.GL_BLEND);
			Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
			Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
			// these might be useful later
			//Gdx.gl20.glEnable(GL20.GL_BLEND);
			//Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			mBatch.render(instances, environment, myShader);
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
				ls.drawLazer(mBatch, environment, delta);
			}
			//mBatch.render(lazers, environment);
			Iterator<Alien> alienIter = gObjects.getAliens().iterator();
			while (alienIter.hasNext())
			{
				Alien alien = alienIter.next();
				alien.draw(mBatch, environment, delta);
			}
			Iterator<AlienSpawner> alienSpawnIter = gObjects.getNests().iterator();
			while (alienSpawnIter.hasNext())
			{
				AlienSpawner alienSpawner = alienSpawnIter.next();
				alienSpawner.draw(mBatch, environment, delta);
			}
			Iterator<Item> itemIter = gObjects.items.iterator();
			while (itemIter.hasNext())
			{
				Item currItem = itemIter.next();
				currItem.drawWorld(mBatch, environment, delta, tileDimensions);
			}
			Iterator<Objective> objectiveIter = objectives.iterator();
			while (objectiveIter.hasNext())
			{
				Objective obj = objectiveIter.next();
				obj.draw(mBatch, environment, delta);
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
			Iterator<Zone> zoneIter = zones.iterator();
			while (zoneIter.hasNext())
			{
				Zone zone = zoneIter.next();
				zone.draw(mBatch, environment, player.cam);
			}
			mBatch.end();
			sBatch.begin();
			
			Iterator<Alien> alienParticleIter = gObjects.getAliens().iterator();
			while (alienParticleIter.hasNext())
			{
				Alien a = alienParticleIter.next();
				a.doEffect(sBatch, mBatch, environment, AGU, player.cam, delta);
			}
			
			Iterator<LazerShot> lazerParticleIter = lazerShots.iterator();
			while (lazerParticleIter.hasNext())
			{
				LazerShot ls = lazerParticleIter.next();
				ls.doEffect(sBatch, AGU, player.cam, delta);
			}
			sBatch.setTransformMatrix(new Matrix4());
			if (gObjects.gMenu != null)
				gObjects.gMenu.draw(sBatch, gObjects, player, tileDimensions, delta);
			
			sBatch.end();
			Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
			mBatch.begin(player.cam);
			Iterator<LazerShot> lazerPowerIter = lazerShots.iterator();
			while (lazerPowerIter.hasNext())
			{
				LazerShot ls = lazerPowerIter.next();
				mBatch.render(ls.shootEffect);
			}
			Item itm = getPlayersCurrentItem();
			if (itm != null)
				itm.renderHand(mBatch, player, environment, currentCamRot);
			mBatch.end();
		}
	}
	
	private Item getPlayersCurrentItem()
	{
		if (player != null && player.getInventory() != null) 
		{
			Item itm = player.getInventory().getCurrentItem();
			if (itm != null)
				return itm;
		}
		return null;
	}

	private void doFrameBuffers(SpriteBatch sBatch, float delta)
	{
		for (int i = 0; i < gObjects.getAliens().size(); ++i)
		{
			Alien a = gObjects.getAliens().get(i);
			a.doFrameBuffers(sBatch, delta);
		}
	}
	
	private void doLogic(float delta)
	{
		gHandler.update(delta);
		for (int i = 0; i < gObjects.getNests().size(); ++i)
		{
			AlienSpawner as = gObjects.getNests().get(i);
			if (as.update(gObjects, delta))
			{
				gObjects.getNests().remove(i);
				spawnerPool.free(as);
				--i;
			}
		}
		for (int i = 0; i < lazerShots.size; ++i)
		{
			LazerShot ls = lazerShots.get(i);
			if (ls.update(bulletHoles, delta) || ls.checkAlienHit(gObjects))
			{
				lazerShots.removeIndex(i);
				lazerPool.free(ls);
				--i;
			}
		}
		for (int i = 0; i < gObjects.getAliens().size(); ++i)
		{
			Alien a = gObjects.getAliens().get(i);
			a.checkLevel(instances, environMeshArr, player.cam, bulletHoles, AGU, delta);
			if (a.update(tiles, gObjects, sHandler, delta))
			{
				gObjects.getAliens().remove(i);
				if (a instanceof Vaepalian)
					vaepalianPool.free((Vaepalian)a);
				else
					alienPool.free(a);
				--i;
			}
		}
		/*
		if (!loading && aliens.size == 0)
		{
			if (currentLevel != "test room")
				newAlien(0, 8, 0, "spawn");
		}*/
		
		// do gun logic
		if (fireGun)
		{
			//pickRay = cam.getPickRay(Gdx.input.getX(), -Gdx.input.getY());
			float x2 = (float)Gdx.input.getX()/(float)Gdx.graphics.getWidth()-.5f;
			float y2 = (float)Gdx.input.getY()/(float)Gdx.graphics.getHeight()-.5f;
			lastShot.set(x2, y2);
		}
		Item itm = getPlayersCurrentItem();
		if (itm != null)
			itm.positionItem(lastShot.x, lastShot.y, player, currentCamRot);
		if (gunCoolDown > 0)
			gunCoolDown -= delta;
		if (fireGun)
		{
			//Vector3 trans = gunInstance.transform.getTranslation(new Vector3());
			if (gunCoolDown <= 0)
			{
				if (tapOccured(instances, Gdx.input.getX(), Gdx.input.getY()))
					gunCoolDown = gunCoolDownMax;
			}
			fireGun = false;
		}
	}
	
	
	
	private Vector3[] closestIntersectionTriang = new Vector3[3];
	private Vector3 closestIntersection = new Vector3();
	private ModelInstance[] objectiveMI = new ModelInstance[1];// for returning
	private ModelInstance[] mi = new ModelInstance[1];// for returning
	private Ray pickRay;
	private boolean tapOccured(Array<ModelInstance> mis, int x, int y)
	{
		
		pickRay = player.cam.getPickRay(x, y);
		
		// go through the objectives first
		// the return "objectiveMI" will tell what objective was tagged.
		float itemDist = -1;
		Item selectedItem = null;
		Iterator<Item> itemIter = gObjects.items.iterator();
		while (itemIter.hasNext())
		{
			Item currItem = itemIter.next();
			float currItemDist = currItem.checkHit(AGU, pickRay, tileDimensions);
			if (currItemDist != -1 && (currItemDist < itemDist || itemDist == -1))
			{
				itemDist = currItemDist;
				selectedItem = currItem;
			}
		}
		if (itemDist == -1)
		{
			float objDist = AGU.getIntersectionTriangles(objectiveMis, environMeshArr, pickRay, closestIntersection, closestIntersectionTriang, objectiveMI);
			float wallDist = AGU.getIntersectionTriangles(mis, environMeshArr, pickRay, closestIntersection, closestIntersectionTriang, mi);
			pickRay = player.cam.getPickRay(x, y);
			if (wallDist != -1 && (objDist == -1 || wallDist < objDist))
			{
				Item itm = getPlayersCurrentItem();
				if (itm != null)
					itm.tapOccurred(AGU, lazerPool, lazerShots, currentCamRot, mi[0], player, closestIntersectionTriang, closestIntersection);
				
				return true;
			}else if (objDist != -1)
			{
				objectiveMap.get(objectiveMI[0]).trigger(objDist, player.getPosition(new Vector3()), this);
			}
		}
		return false;
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
		if (mBatch != null)
			mBatch.dispose();
		if (sBatch != null)
			sBatch.dispose();
		if (instances != null)
			instances.clear();
		if (gObjects.getAliens() != null)
			gObjects.getAliens().clear();
		currentCamRot = 0;
		//if (assets != null)
		//	assets.dispose();
	}

	@Override
	public void reset() {
		if (mBatch != null)
			mBatch.dispose();
		if (sBatch != null)
			sBatch.dispose();
		if (instances != null)
			instances.clear();
		if (gObjects.getAliens() != null)
			gObjects.getAliens().clear();
		
		// objectives
		if (objectives != null)
			objectives.clear();
		if (objectiveMap != null)
			objectiveMap.clear();
		if (objectiveMis != null)
			objectiveMis.clear();
		
		
		currentCamRot = 0;
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
		return false;
	}
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}
	
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}

	@Override
	public void addRotation(float f) {
		currentCamRot += f;
		
	}

	@Override
	public void doAction(MovementAction ma, float delta)
	{
		
		ma.doAction(this, player, tiles, delta);
		player.cam.update();
		
	}

	@Override
	public void fireGun()
	{
		fireGun = true;
	}

	@Override
	public InputProcessor getInputProcessor() {
		return im;
	}
}
