package com.me.alienShooter;

public interface ActionListener
{
	public void addRotation(float f);
	public void fireGun();
	public void doAction(MovementAction ma, float delta);
	public void loadLevel(String s, String entranceName);
}