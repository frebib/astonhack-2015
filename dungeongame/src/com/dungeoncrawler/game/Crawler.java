package com.dungeoncrawler.game;

import com.dungeoncrawler.game.render.DungeonScene;

import Application3D.Application3D;

public class Crawler {
	public static Application3D app3D;
	public static void main(String[] args) {
		
		
		
		// TODO Auto-generated method stub
		app3D = Application3D.getApp();
		app3D.start();
		
		DungeonScene ds = new DungeonScene();
		app3D.registerRenderInstance(ds);
		
		System.out.println("test");
		
	}
}
