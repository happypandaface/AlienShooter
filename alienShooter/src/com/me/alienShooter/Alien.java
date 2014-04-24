package com.me.alienShooter;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.Pool.Poolable;

class AttackAnimation extends SpaceAction
{
	private Alien alien;
	private Moveable target;
	private float direction;
	private float windup_time = 1.5f;
	private float jump_time = 1.5f;
	private static final int WINDUP = 1;
	private static final int JUMPING = 2;
	private int cur_state;
	private float cur_time;
	private boolean attacking = false;
	private MovementAction mAction = new MovementAction();
	private float jumpSpeed = 25.0f;
	private float hitRange = 0.2f;
	
	public AttackAnimation()
	{
		
	}
	private void calcDirection(GameObjects gObjs)
	{
		if (alien != null && target != null)
			direction = MoveableUtil.getLogDiff(alien, target, gObjs.getTileDim(), new Vector2()).angle();
		
	}
	public void setAlien(Alien a, GameObjects gObjs)
	{
		alien = a;
		calcDirection(gObjs);
	}
	public void setTarget(Moveable m, GameObjects gObjs)
	{
		target = m;
		calcDirection(gObjs);
	}
	public void startAttack()
	{
		cur_time = 0;
		cur_state = WINDUP;
		mAction.set(MovementAction.MOVE_FORWARD, jumpSpeed);
		attacking = true;
	}
	public boolean isAttacking()
	{
		return attacking;
	}
	// returns true if the attack hit
	public boolean doAction(float delta, GameTiles tiles, GameObjects gObjs)
	{
		cur_time += delta;
		switch(cur_state)
		{
			case WINDUP:
				if (cur_time < windup_time)
				{
					alien.rotateTo(direction);
				}else
				{
					cur_time = 0;
					cur_state = JUMPING;
				}
				break;
			case JUMPING: 
				if (cur_time < jump_time)
				{
					float trigAlong = (float)(Math.sin(cur_time*Math.PI/jump_time));
					alien.setHeight((float)(trigAlong*2)-3);
					alien.setHRot((float)(trigAlong*30.0f));
					alien.rotateTo(direction);
					Vector2 diff = MoveableUtil.getLogDiff(alien, gObjs.player, gObjs.getTileDim(), new Vector2());
					if (diff.len() < hitRange)
						return true;
					mAction.doAction(alien, alien, tiles, delta);
				}else
				{
					attacking = false;
				}
				break;
		}
		return false;
	}
}

public class Alien implements Poolable, Moveable, ActionListener, Dies
{
	protected AnimationController Acontroller;
	protected AnimationController Acontroller2;
	public ModelInstance alienMI;
	public ModelInstance particleBoard;
	private Vector3 pos = new Vector3();
	protected BoundingBox boundingBox;
	protected boolean hit = false;
	protected boolean justHit = false;
	protected boolean dieNext = false;
	protected ParticleEffect dieEffect;
	protected float effectSize = .5f;
	protected Model planeModel;
	protected Texture gutsTex;
	protected Sound squishSound;
	protected GameTile gTile;
	private float rotation = 0;
	protected float turnSpeed = 15;
	protected float slowingTurnSpeedMul = 3;
	private float currHeight = -3;
	private float currHorizRot = 0;
	private List<DeathListener> deathListeners = new ArrayList<DeathListener>();
	private AttackAnimation atkAct;
	private float attackRange = 1.5f;
	protected float baseRot = 0;
	protected Vector3 horizAngles = new Vector3(1, 0, 0);
	
	public Alien()
	{
		reset();
	}

	private Vector3 upVec = new Vector3(0, 1 ,0);
	private Quaternion turnQuat = new Quaternion();
	private Matrix4 turnMat = new Matrix4();
	private Quaternion horizQuat = new Quaternion(); 
	private Matrix4 horizMat = new Matrix4();
	public void calcMI()
	{
		alienMI.transform.set(new Matrix4());
		alienMI.transform.setTranslation(pos);
		turnQuat.set(upVec, rotation+baseRot);
		turnMat.set(turnQuat);
		alienMI.transform.mul(turnMat);
		horizQuat.set(horizAngles, currHorizRot);
		alienMI.transform.mul(horizMat.set(horizQuat));
	}
	
	public Alien(Array<Model> alienModels, Array<Texture> alienTextures, Array<Sound> alienSounds, ParticleEffectPool effectPool)
	{
		squishSound = alienSounds.get(0);
		planeModel = alienModels.get(1);
		gutsTex = alienTextures.get(0);
		dieEffect = effectPool.obtain();
		alienMI = new ModelInstance(alienModels.get(0));
		Acontroller = new AnimationController(alienMI);
		//for (int i = 0; i < alienMI.animations.size; i++)
		//	Gdx.app.log("anim", alienMI.animations.get(i).id);
		Acontroller.setAnimation(alienMI.animations.get(0).id, -1);
		//Acontroller2.setAnimation(alienMI.animations.get(6).id, -1);
		boundingBox = new BoundingBox();
		alienMI.calculateBoundingBox(boundingBox);
		
		particleBoard = new ModelInstance(planeModel);
	}
	
	public void setHeight(float h)
	{
		currHeight = h;
	}
	public void setHRot(float h)
	{
		currHorizRot = h;
	}

	@Override
	public void reset() {
		currHeight = -3;
		currHorizRot = 0;
		atkAct = new AttackAnimation();
		hit = false;
		justHit = false;
		dieNext = false;
		effectSize = .5f;
		rotation = 0;
		deathListeners.clear();
	}
	
	private Matrix4 destMat = new Matrix4();
	private MovementAction mAction = new MovementAction();
	private MovementAction tAction = new MovementAction();
	private Vector2 tempDistPlayer = new Vector2();
	public boolean update(GameTiles tiles, GameObjects gObjs, SceneHandler sh, float delta)
	{
		GameTile playTile = gObjs.player.getTile();
		GameTile thisTile = getTile();
		playTile.getPos(tempDistPlayer).sub(thisTile.getPos());
		//Gdx.app.log("dist to play", ""+tempDistPlayer);
		
		if (!hit)
		{
			Vector3 playerDist = new Vector3();
			gObjs.player.getPosition(playerDist);
			playerDist.sub(getPosition(new Vector3()));
			playerDist.y = 0;
			if (playerDist.len() < tiles.getTileDimensions().len())
			{
				//sh.reloadScene("main menu");
				//sh.switchScene("main menu");
			}
		}
		
		//alienMI.transform.rotate(new Quaternion(0, 0, 1, delta)); //cool hallucinate effect
		if (hit == true)
		{
			if (checkIfDead())
			{
				alertDeath();
				return true;
			}
		}else
		{
			if (atkAct != null && atkAct.isAttacking())
			{
				if (atkAct.doAction(delta, tiles, gObjs))
					getHit(null);
			}else
			{
				// TODO:
				// roam function
				Vector2 diff = MoveableUtil.getLogDiff(this, gObjs.player, gObjs.getTileDim(), new Vector2());
				if (diff.len() < attackRange && getDiffRot(diff.angle()) < 10.0f)
				{
					attack(gObjs.player, delta, tiles, gObjs);
				}else
					follow(gObjs.player, delta, tiles, gObjs);
			}
		}
		return false;
	}
	
	private float getDiffRot(float deg) {
		return Math.abs(AlienGameUtil.norAngleDeg(getRotation()-(deg)));
	}

	private void attack(Moveable m, float delta, GameTiles tiles, GameObjects gObjs)
	{
		if (atkAct == null)
			atkAct = new AttackAnimation();
		atkAct.setAlien(this, gObjs);
		atkAct.setTarget(m, gObjs);
		atkAct.startAttack();
	}
	
	private void follow(Moveable m, float delta, GameTiles tiles, GameObjects gObjs)
	{
		float angle = getRotation()-m.getLogicalPos(new Vector2(), gObjs.getTileDim()).sub(getLogicalPos(new Vector2(), gObjs.getTileDim())).angle()+180;
		angle = AlienGameUtil.norAngleDeg(angle);
		Acontroller.update(delta);
		float absAngle = Math.abs(angle/45*slowingTurnSpeedMul);
		if (angle > 0)
			tAction.set(MovementAction.TURN_LEFT, absAngle < turnSpeed? absAngle : turnSpeed);
		else
			tAction.set(MovementAction.TURN_RIGHT, absAngle < turnSpeed? absAngle : turnSpeed);
		tAction.doAction(this, this, tiles, delta);
		mAction.set(MovementAction.MOVE_FORWARD, 5);
		mAction.doAction(this, this, tiles, delta);
	}
	
	private void alertDeath()
	{
		for (DeathListener dl : deathListeners)
			dl.died(this);
	}

	protected boolean checkIfDead()
	{
		if (dieNext || dieEffect.isComplete())
			return true;
		return false;
	}

	private Vector3[] closestIntersectionTriang = new Vector3[3];
	private Vector3 closestIntersection = new Vector3();
	private ModelInstance[] mi = new ModelInstance[1];// for returning
	private Vector3 normalVector = new Vector3();
	private Quaternion norQuat = new Quaternion();
	private Matrix4 norMatrix = new Matrix4();
	private Ray pickRay = new Ray(new Vector3(), new Vector3());
	private Vector3 currPosTest = new Vector3();
	private Vector3 currDir = new Vector3(0, 0, 1);
	private Vector3 currDirTest = new Vector3(0, -1, 0);
	private Vector3 deathCoords = new Vector3();
	public void checkLevel(Array<ModelInstance> mis, ArrayMap<Model, Array<Vector3[]>> environMeshArr, PerspectiveCamera cam, Array<ModelInstance> billboards, AlienGameUtil AGU, float delta)
	{
		if (hit == true)
		{
			if (justHit == true)
			{
				squishSound.play();
				//alienMI.transform.getTranslation(currPosTest);
				//currDirTest.set(currDir);
				//alienMI.transform.mul(currDirTest);

				//Gdx.app.log("vec1", ""+alienMI.transform.getTranslation(new Vector3()));
				pickRay.set(currPosTest, currDirTest);
				pickRay.mul(alienMI.transform);
				if (AGU.getIntersectionTriangles(mis, environMeshArr, pickRay, closestIntersection, closestIntersectionTriang, mi) != -1)
				{

					AGU.calcNormal(closestIntersectionTriang, true, normalVector, norQuat);
					AGU.reorientIntersection(closestIntersection);
					AGU.setForBillBoard(closestIntersection, normalVector);
					
					ModelInstance alienGuts = new ModelInstance(planeModel);
					alienGuts.materials.get(0).set(TextureAttribute.createDiffuse(gutsTex),
							new BlendingAttribute());
					
					alienGuts.transform = new Matrix4();
					alienGuts.transform.mul(mi[0].transform);// apply the object's transform (the intersection coords are in local space)
					alienGuts.transform.mul(new Matrix4().setToTranslation(closestIntersection.cpy()));// apply the local cords of the intersection
					alienGuts.transform.mul(norMatrix.set(norQuat.nor()));// make the bullet hole face away from the triangle (wall)
					alienGuts.transform.scale(4,4,4);
					billboards.add(alienGuts);// add the bullet hole
				}
				justHit = false;
				alienMI.transform.getTranslation(deathCoords);
				dieEffect.reset();
				dieEffect.start();
//				Model bulletHoleModel = assets.get("data/stupidPlane.g3dj", Model.class);
//				ModelInstance bulletHole = new ModelInstance(bulletHoleModel);
//				bulletHole.materials.get(0).set(TextureAttribute.createDiffuse(new Texture(Gdx.files.internal("data/bulletHole.png"))),
//						new BlendingAttribute());
//				billboards.add(bulletHole);
			}
		}
	}
	
	public boolean getHit(LazerShot ls)
	{
		if (hit == false)
		{
			hit = true;
			justHit = true;
			return true;
		}
		return false;
	}
	
	public void draw(ModelBatch mBatch, Environment environment, float delta)
	{
		if (!hit)
		{
			calcMI();
			mBatch.render(alienMI, environment);
		}
	}
	
	private FrameBuffer fbo;
	private TextureRegion fboRegion;
	public void doFrameBuffers(SpriteBatch sBatch, float delta)
	{
		/*
		fbo = new FrameBuffer(Format.RGBA8888, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight(), false);
		
		fboRegion = new TextureRegion(fbo.getColorBufferTexture(), 0, 0,
				Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());
		
		fbo.begin();
		// we need to first clear our FBO with transparent black
		Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 
		// start our batch
		sBatch.begin();
		//batch.setBlendFunction(-1, -1);
		// end (flush) our batch
		dieEffect.draw(sBatch);
		
		sBatch.end();
 
		// unbind the FBO
		fbo.end();
 
		// now let's reset blending to the default...
		sBatch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		//Gdx.app.log("part", ""+particleBoard);
		particleBoard.materials.get(0).set(TextureAttribute.createDiffuse(fboRegion.getTexture()),
				new BlendingAttribute());
		*/
	}
	
	private Vector3 screenCoords = new Vector3();
	// this function isn't used
	public void doEffect(SpriteBatch sBatch, PerspectiveCamera cam, float delta)
	{
		if (hit)
		{
			screenCoords.set(deathCoords);
			cam.project(screenCoords);
			//dieEffect.setPosition(screenCoords.x/effectSize, screenCoords.y/effectSize);
			dieEffect.update(delta);
			sBatch.setTransformMatrix(new Matrix4().setTranslation(screenCoords.x, screenCoords.y, 0).scl(effectSize, effectSize, effectSize));
			dieEffect.draw(sBatch);
		}
	}
	
	public void doEffect(SpriteBatch sBatch, ModelBatch mBatch, Environment environment, AlienGameUtil AGU, PerspectiveCamera cam, float delta)
	{
		if (hit)
		{
			/*
			dieEffect.update(delta);
			particleBoard.transform = new Matrix4().setTranslation(deathCoords.cpy().add(new Vector3(0, 1, 0)));
			particleBoard.transform.scl(5, 5, 5);
			//particleBoard.transform.mul(new Matrix4().set(new Quaternion(new Vector3(0, 1, 0), 90)));
			mBatch.render(particleBoard, environment);
			*/
			AGU.doParticleEffect(deathCoords, dieEffect, 9, sBatch, cam, delta);
		}
	}
	
	@Override
	public void rotate(Quaternion q)
	{
		Vector3 check = new Vector3();
		float r = q.getAxisAngle(check);
		Gdx.app.log("new rot", ""+rotation);
		rotation -= q.getAxisAngle(new Vector3());
		rotation = AlienGameUtil.norAngleDeg(rotation);
		alienMI.transform.mul(new Matrix4().set(q));
	}
	
	private Vector3 tempSetVec = new Vector3();
	@Override
	public void setPosition(Vector2 v)
	{
		tempSetVec.set(v.x, currHeight, v.y);
		setPosition(tempSetVec);
		//alienMI.transform.setTranslation(tempSetVec);
	}
	@Override
	public void setPosition(Vector3 v)
	{
		tempSetVec.set(v);
		tempSetVec.y = currHeight;
		pos.set(tempSetVec);
		//alienMI.transform.setTranslation(tempSetVec);
	}
	
	@Override
	public Matrix4 getTransform(Matrix4 trans)
	{
		trans.set(alienMI.transform);
		return trans;
	}

	@Override
	public void addRotation(float f)
	{
	}

	@Override
	public void fireGun()
	{
	}

	@Override
	public void doAction(MovementAction ma, float delta)
	{
	}

	@Override
	public void setTile(GameTile tile) {
		gTile = tile;
	}

	@Override
	public GameTile getTile() {
		// TODO Auto-generated method stub
		return gTile;
	}

	@Override
	public Vector3 getPosition(Vector3 v) {
		return alienMI.transform.getTranslation(v);
	}

	@Override
	public void loadLevel(String s, String en) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Vector2 getPosition(Vector2 v) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean canTakeAction(MovementAction action) {
		return true;
	}
	
	private Vector3 rtnLogPos = new Vector3();
	@Override
	public Vector2 getLogicalPos(Vector2 v, Vector2 tileDim) {
		alienMI.transform.getTranslation(rtnLogPos);
		return v.set(rtnLogPos.x/tileDim.x, rtnLogPos.z/tileDim.y);
	}
	
	private Quaternion rotQuat = new Quaternion();
	@Override
	public float getRotation() {
		//alienMI.transform.getRotation(rotQuat).getAngleAround(0, 1, 0);
		rotation = AlienGameUtil.norAngleDeg(rotation);
		float rot = AlienGameUtil.norAngleDeg(180-(rotation+90));
		return rot;
	}
	
	@Override
	public void rotateDeg(float amt) {
		rotation += amt;
	}
	
	//@Override
	public void rotateTo(float amt) {
		float diff = this.getRotation()-amt;
		rotateDeg(diff);
	}

	@Override
	public Dies addDeathListener(DeathListener dl) {
		deathListeners.add(dl);
		return this;
	}

	@Override
	public float getMoveRotation() {
		return 180-getRotation()-90;
	}
}
