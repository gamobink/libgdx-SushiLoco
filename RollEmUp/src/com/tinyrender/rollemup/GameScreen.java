package com.tinyrender.rollemup;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;


public abstract class GameScreen extends InputAdapter implements Screen {
	public static InputMultiplexer inputMultiplexer;

	public GameScreen() {
		inputMultiplexer = new InputMultiplexer();
		Gdx.input.setInputProcessor(inputMultiplexer);
	}
}