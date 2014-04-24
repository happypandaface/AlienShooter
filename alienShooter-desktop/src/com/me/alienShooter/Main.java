package com.me.alienShooter;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class Main {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "alienShooter";
		cfg.useGL20 = true;
//		cfg.width = 800;
//		cfg.height = 600;
		cfg.width = 480;
		cfg.height = 320;
//		cfg.fullscreen = true;
		
		new LwjglApplication(new AlienShooter(), cfg);
	}
}
