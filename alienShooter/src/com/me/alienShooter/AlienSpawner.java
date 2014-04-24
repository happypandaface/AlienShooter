package com.me.alienShooter;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool.Poolable;

public class AlienSpawner implements Poolable, Moveable, DeathListener, GetsHit
{
	private ModelInstance alienSpawnMI;
	protected GameTile gTile;
	private int maxAliens;
	private float spawnCooldown;
	private float spawnCooldownMax = 5;
	private List<Alien> aliens = new ArrayList<Alien>();
	private Vector3 position = new Vector3();
	private float maxHealth = 5;
	private float currHealth = maxHealth;
	
	public AlienSpawner(Array<Model> alienModels)
	{
		alienSpawnMI = new ModelInstance(alienModels.get(3));
	}
	
	public void setMaxAliens(int num)
	{
		this.maxAliens = num;
	}

	@Override
	public void reset()
	{
		aliens.clear();
		spawnCooldownMax = 5;
		currHealth = maxHealth;
	}
	
	private Vector2 spawnPos;
	public boolean update(GameObjects gObjects, float delta)
	{
		if (spawnCooldown > 0)
			spawnCooldown -= delta;
		else
		{
			if (aliens.size() < maxAliens)
			{
				spawnCooldown = spawnCooldownMax;
				spawnPos = this.getTile().getPos();
				aliens.add((Alien)gObjects.getObjectCreator().newAlien(spawnPos.x, spawnPos.y, (float)(Math.random()*180), "spawn").addDeathListener(this));
			}
		}
		if (currHealth <= 0)
			return true;
		return false;
	}
	
	
	@Override
	public void rotate(Quaternion q)
	{
		
	}
	
	@Override
	public void setPosition(Vector2 v)
	{
		
	}
	
	@Override
	public void setPosition(Vector3 v)
	{
		position.set(v);
	}
	
	@Override
	public Matrix4 getTransform(Matrix4 trans)
	{
		setupGraphicsMatrix();
		trans.set(alienSpawnMI.transform);
		return trans;
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
		return v.set(position);
	}
	
	public void draw(ModelBatch mBatch, Environment environment, float delta)
	{
		setupGraphicsMatrix();
		mBatch.render(alienSpawnMI, environment);
	}

	private void setupGraphicsMatrix() {
		alienSpawnMI.transform = new Matrix4();
		alienSpawnMI.transform.setTranslation(position);
		float scl = currHealth/maxHealth*0.2f+0.8f;
		alienSpawnMI.transform.scale(scl, scl, scl);
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

	@Override
	public Vector2 getLogicalPos(Vector2 v, Vector2 tileDim) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getRotation() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void rotateDeg(float amt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void died(Dies d)
	{
		aliens.remove(d);
	}

	@Override
	public boolean getHit(HitsThings ht)
	{
		currHealth -= 1;
		if (currHealth == 0)
			return true;
		return false;
	}

	@Override
	public float getMoveRotation() {
		// TODO Auto-generated method stub
		return 0;
	}
}
