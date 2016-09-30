package com.kifui.micerun;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class MiceRunGame extends Game
{
	LevelManager levelManager;
	PlayScreen playScreen;
	//UIMainScreen mainScreen;
	StageMenu stageScreen;

	public void stageSelect()
	{
		//stageScreen.newGame();
		//setScreen(stageScreen);
	}
	
	public void NewGame(int levelIndex)
	{
		playScreen.newGame(levelIndex);
		setScreen(playScreen);
	}
	
	public void MainMenu()
	{
		//setScreen(mainScreen);
	}

	public void StageMenu()
	{
		setScreen(stageScreen);
	}
	
	
	@Override
	public void create()
	{
		levelManager = new LevelManager();
		playScreen = new PlayScreen(this);
		//mainScreen = new UIMainScreen(this);
		stageScreen = new StageMenu(this);
		
		// Load level data from JSON file
		levelManager.Load();
		
		// Initialize menus
		stageScreen.Init();

		// TEMP: start at stage menu
		StageMenu();
	}

	@Override
	public void dispose()
	{
		//mainScreen.dispose();
		playScreen.dispose();
		//stageScreen.dispose();
	}

	@Override
	public void render()
	{		
		super.render();
	}

	@Override
	public void resize(int width, int height)
	{
	}

	@Override
	public void pause()
	{
	}

	@Override
	public void resume()
	{
	}
}
