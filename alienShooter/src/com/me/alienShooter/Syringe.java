package com.me.alienShooter;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;

public class Syringe extends Item
{
	public Syringe (StuffHolder mh)
	{
		super();
		this.mi = new ModelInstance(mh.getModel("syringe"));
		this.iconTex = mh.getTex("syringeIcon");
	}
}