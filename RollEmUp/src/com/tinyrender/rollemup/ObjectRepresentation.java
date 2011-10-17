package com.tinyrender.rollemup;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ObjectRepresentation extends Renderable {
	public float width;
	public float height;
	
	// Origin in upper left corner: X-right Y-down
	public TextureRegion texture;
	
	public void setTexture(TextureRegion texture) {
		this.texture = texture;
		width = texture.getRegionWidth();
		height = texture.getRegionHeight();
	}
	
	@Override
	public void draw() {
		// SpriteBatch.draw(textureRegion, x, y, width, height)
		Assets.batch.draw(texture, x, y, width, height);
	}
}