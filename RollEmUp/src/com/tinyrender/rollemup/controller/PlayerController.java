package com.tinyrender.rollemup.controller;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.tinyrender.rollemup.Controller;
import com.tinyrender.rollemup.GameObject;
import com.tinyrender.rollemup.Level;
import com.tinyrender.rollemup.box2d.PhysicsObject;
import com.tinyrender.rollemup.object.Player;

public class PlayerController implements Controller {
	Player player;
	Fixture fixture;
	
	Shape.Type shapeType;
	Vector2 vecPosOffset = new Vector2();
	
	public PlayerController(Player player) {
		this.player = player;
	}
	
	public void rollObject(GameObject other, World world) {
		Player.IS_GROWING = true;
		
		// Move object from level's list to player's
		player.level.objects.removeValue(other, true); // TODO: O(N) linear
		player.subObj.add(other);
		
		// Destroy object's joint then body
		if (other.joint != null)
			world.destroyJoint(other.joint);
		if (other.body != null)
			world.destroyBody(other.body);

		other.rolled = true;
		
		// Convert object position from box2d space to screen space
		other.pos.mul(Level.PTM_RATIO);
		
		other.orbitRadius = player.shape.getRadius() * Level.PTM_RATIO;
		
		//Gdx.app.log("otherRadius", Float.toString(other.orbitRadius));
	}
	
	@Override
	public void jump(GameObject object, float velocity) {
		object.body.applyLinearImpulse(0, velocity, object.pos.x, object.pos.y);
	}
	
	public void scaleCircle(PhysicsObject object, float scale, float offsetX, float offsetY) {
		fixture = object.body.getFixtureList().get(0);
		shapeType = fixture.getType();
		
		if (shapeType == Shape.Type.Circle) {
			CircleShape shape = (CircleShape) fixture.getShape();
			float radius = shape.getRadius();
			
			vecPosOffset.set(offsetX, offsetY);
			radius *= scale;
			shape.setPosition(vecPosOffset);
			shape.setRadius(radius);
		}
	}
	
	public void keyDown(int keyCode) {
		if (keyCode == Keys.SPACE) {
			if (player.state != Player.STATE_JUMPING && player.state != Player.STATE_FALLING)
				jump(player, 25.0f);
		}
	}
	
	public void keyUp(int keyCode) {
	}
	
	public void touchDown() {
		if (player.state != Player.STATE_JUMPING && player.state != Player.STATE_FALLING)
			jump(player, 25.0f);
	}
	
	public void touchUp() {
	}
}
