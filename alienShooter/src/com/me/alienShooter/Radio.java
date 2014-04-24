package com.me.alienShooter;

import com.badlogic.gdx.graphics.g3d.ModelInstance;

public class Radio extends Item 
{
	public Radio (StuffHolder mh)
	{
		super();
		this.mi = new ModelInstance(mh.getModel("radio"));
		this.iconTex = mh.getTex("radioIcon");
	}
}
