package com.github.hydos.ginger.engine.api.game;

public abstract class Game {
	
	public GameData data;
	
	public Game() {}
	
	public abstract void update();

	public abstract void exit();

}