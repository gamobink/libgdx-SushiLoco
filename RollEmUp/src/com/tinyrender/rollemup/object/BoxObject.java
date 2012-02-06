package com.tinyrender.rollemup.object;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.World;
import com.tinyrender.rollemup.GameObject;
import com.tinyrender.rollemup.Level;
import com.tinyrender.rollemup.box2d.BodyFactory;

public class BoxObject extends GameObject {
	public BoxObject(World world) {
		super(world);
	}
	
	public BoxObject(TextureRegion texture, float x, float y, float density, int level,
			int points, boolean isSensor, boolean doUpdate, Type type, World world) {
		super(world);
		
		this.points = points;
		this.level = level;
		this.type = type;
		
		objRep.setTexture(texture);

		float hx = objRep.width / 2.0f / Level.PTM_RATIO;
		float hy = objRep.height / 2.0f / Level.PTM_RATIO;
		
		body = BodyFactory.createBox(x, y, hx, hy, density, isSensor, BodyType.DynamicBody, world);
		body.setUserData(this);
	}
}
