package com.tinyrender.rollemup.object;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.tinyrender.rollemup.Assets;
import com.tinyrender.rollemup.ExperienceChain;
import com.tinyrender.rollemup.GameObject;
import com.tinyrender.rollemup.Level;
import com.tinyrender.rollemup.box2d.BodyFactory;
import com.tinyrender.rollemup.box2d.PhysicsObject;
import com.tinyrender.rollemup.controller.PlayerController;

public class Player extends GameObject {
	public class GroundSensor implements QueryCallback {
		public Rectangle rect = new Rectangle();
		public ArrayList<Fixture> foundBodies = new ArrayList<Fixture>();
		
		@Override
		public boolean reportFixture(Fixture fixture) {
			foundBodies.add(fixture);
			return true;
		}
		
		public void update() {
			groundSensor.foundBodies.clear();
			
			groundSensor.rect.x = pos.x - groundSensor.rect.width/2.0f;
			groundSensor.rect.y = pos.y - shape.getRadius() - groundSensor.rect.height;
			
			world.QueryAABB(groundSensor, 
					groundSensor.rect.x, groundSensor.rect.y, 
					groundSensor.rect.width + groundSensor.rect.x,
					groundSensor.rect.height + groundSensor.rect.y);
		}
	}
	
	public final static float MAX_VELOCITY = 7.0f;
	public final static float MAX_JUMP = 12.0f;
	public final static int STATE_IDLE = 0;
	public final static int STATE_FALLING = 1;
	public final static int STATE_JUMPING = 2;
	public final static int STATE_ROLLING = 3;
	public final static int DIRECTION_NONE = 0;
	public final static int DIRECTION_LEFT = 1;
	public final static int DIRECTION_RIGHT = 2;
	
	public int state;
	public int direction;
	
	public float growthScale = 1.3f;
	public float forceYOffset;
	
	public Vector2 vel = new Vector2();
	
	public CircleShape shape;
	public GroundSensor groundSensor = new GroundSensor();
	public ExperienceChain xp = new ExperienceChain();
	public PlayerController controller = new PlayerController(this);
	public Array<GameObject> objectsToRoll = new Array<GameObject>();
	
	public Level worldLevel;
	GameObject rolledObj;
	
	public Player(Level worldLevel, World world) {
		super(world);
		this.worldLevel = worldLevel;
		children.ensureCapacity(80);
		rolledObj = new GameObject(world);
		
		this.level = 1;
		this.type = Type.PLAYER;
		
		objRep.setTexture(Assets.atlas.findRegion("player"));
		
		float radius = (objRep.halfWidth) * 0.7f / Level.PTM_RATIO;
		
		body = BodyFactory.createCircle(427.0f/Level.PTM_RATIO, 64.0f/Level.PTM_RATIO, radius,
										0.8f, 0.0f, 1.0f, false, BodyType.DynamicBody, world);
		body.setActive(true);
		
		pos = body.getPosition();
		shape = (CircleShape) body.getFixtureList().get(0).getShape();
		
		// Set collision attributes
		Filter filter = new Filter();
		filter.categoryBits = PhysicsObject.Category.PLAYER;
		filter.maskBits = PhysicsObject.Mask.COLLIDE_ALL;
		body.getFixtureList().get(0).setFilterData(filter);
		
		groundSensor.rect.width = 15.0f / Level.PTM_RATIO + radius;
		groundSensor.rect.height = 25.0f / Level.PTM_RATIO;
		
		forceYOffset = -(shape.getRadius() / 3.0f) * growthScale;
						
		contactResolver = new ContactResolver() {
			@Override
			public void enterContact(PhysicsObject collidesWith) {
				if (collidesWith.type == Type.ROLLABLE) {
					GameObject otherObject = (GameObject) collidesWith.body.getUserData();
					
					if (isRollable(otherObject))			
						objectsToRoll.add(otherObject);
				}
			}
			
			@Override public void leaveContact(PhysicsObject leftCollisionWith) { }
		};
		
		body.setUserData(this);
	}
	
	@Override
	public void update() {
		vel = body.getLinearVelocity();
		pos = body.getPosition();
		rot = body.getAngle() * MathUtils.radiansToDegrees;
		
		// Find the current moving direction
		if (vel.x <= -0.1f)
			direction = DIRECTION_LEFT;
		else if (vel.x >= 0.1f)
			direction = DIRECTION_RIGHT;
		else
			direction = DIRECTION_NONE;
		
		// Find the current player state between: idle, jumping, falling, rolling
		if (vel.y < 0.0f && !isGrounded())
			state = STATE_FALLING;
		else if (vel.y > 0.1f)
			state = STATE_JUMPING;
		else if ((direction == DIRECTION_LEFT || direction == DIRECTION_RIGHT) && isGrounded())
			state = STATE_ROLLING;
		else if (isGrounded())
			state = STATE_IDLE;
		
		if (xp.justLeveledUp())
			levelUp();
				
		// Desktop player controls
		if (Gdx.input.isKeyPressed(Keys.A))
			body.applyForceToCenter(-40.0f, forceYOffset);
		else if (Gdx.input.isKeyPressed(Keys.D))
			body.applyForceToCenter(40.0f, forceYOffset);

		// Stick objects
		for (int i = 0; i < objectsToRoll.size; i++) {
			rolledObj = objectsToRoll.pop();
			
			controller.rollObject(rolledObj);
			xp.addPoints(rolledObj.points);
		}
			
		// Keep X velocity within maximum
		if (Math.abs(vel.x) > MAX_VELOCITY) {			
			vel.x = Math.signum(vel.x) * MAX_VELOCITY;
			body.setLinearVelocity(vel.x, vel.y);
		}
		
		// Apply force when tilted, otherwise dampen down acceleration to stop
		if ((Gdx.input.getAccelerometerY() <= -0.35f && vel.x > -MAX_VELOCITY) ||
				Gdx.input.getAccelerometerY() >= 0.35f && vel.x < MAX_VELOCITY) {
			body.applyForceToCenter(Gdx.input.getAccelerometerY()*0.55f, forceYOffset);
		} else {
			body.setLinearVelocity(vel.x * 0.9f, vel.y);
		}
		
		// Apply small impulse at low speeds to regain momentum
		if (vel.x < MAX_VELOCITY/4.0f || vel.x > -MAX_VELOCITY/4.0f)
			body.applyLinearImpulse(Gdx.input.getAccelerometerY() * 0.1f, 0.0f, pos.x, pos.y);
		
		for (int i = 0; i < children.size; i++) {
			children.get(i).rot = this.rot;
			children.get(i).pos.set(this.pos.x, this.pos.y);
		}

		groundSensor.update();
	}
	
	public void levelUp() {
		xp.levelUp();
		controller.grow();		
	}
	
	public boolean isRollable(GameObject otherObj) {
		if (otherObj.type == Type.ROLLABLE && otherObj.children.size == 0)
			if (otherObj.level <= xp.getLevelTag())
				return true;
		return false;
	}
	
	public boolean isGrounded() {
		if (groundSensor.foundBodies.size() > 1)
			return true;
		return false;
	}
}