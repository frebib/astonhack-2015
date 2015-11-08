package com.dungeoncrawler.game.render;

import com.dungeoncrawler.generator.search.IHeuristic;

import utils3D.Camera;

public class PointLight{
	float x, y, z, range;
	float r, g, b, intensity;
	boolean enabled;
	
	public PointLight( float x, float y, float z, float range, float r, float g, float b, float intensity) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.range = range;
		this.r = r;
		this.g = g;
		this.b = b;
		this.intensity = intensity;
		this.enabled  = true;
	}
	
	public void update(){
		
	}
	
	public void setXYZ( float x, float y, float z ) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void setRange( float range ) {
		this.range = range;
	}
	
	public void setEnabled( boolean enable ) {
		this.enabled = enable;
	}
}
