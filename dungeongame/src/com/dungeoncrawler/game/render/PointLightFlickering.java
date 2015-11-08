package com.dungeoncrawler.game.render;

import java.util.Random;

public class PointLightFlickering extends PointLight{

	float ticks = 0;
	float baseRange;
	Random generator;
	
	public PointLightFlickering(float x, float y, float z, float range, float r, float g, float b, float intensity) {
		super(x, y, z, range, r, g, b, intensity);
		baseRange = range;
		generator = new Random();
		ticks     = generator.nextFloat()*100.0f;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void update(){
		ticks += (0.2+generator.nextFloat()*0.8)*0.25;
		range = (float) (baseRange + Math.sin(ticks*0.10)*(baseRange*0.1)
								   + Math.sin(ticks*1.1 + 78)*(baseRange*0.1)
								   + Math.cos(ticks*0.25)*(baseRange*0.3)
								   + Math.sin(ticks*0.05 - 36)*(baseRange*0.1)	
								   + Math.cos(ticks*0.8 + 124)*(baseRange*0.1)
				);
	}

}
