package com.kifui.micerun;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class StageMenu implements Screen, InputProcessor
{
	MiceRunGame game;
	
	private Texture t;
	
	private TextureRegion boxTR;
	private NinePatch boxNP;
	private TextureRegion boxMedalTR;
	private NinePatch boxMedalNP;
	
	private final int sceneWidth = 800;
	private final int sceneHeight = 480;
	private SpriteBatch batch;
	private OrthographicCamera camera;
	
	private int nbLevels;
	
	private Array<Rectangle> boxList;
	
	
	StageMenu(MiceRunGame game)
	{
		this.game = game;
		
		t = new Texture(Gdx.files.internal("data/menu.png"));

		boxTR = new TextureRegion(t, 0, 0, 45, 45);
		boxNP = new NinePatch(boxTR, 10, 10, 10, 10);
		boxMedalTR = new TextureRegion(t, 0, 49, 45, 17);
		boxMedalNP = new NinePatch(boxMedalTR, 5, 8, 5, 5);
		
		batch = new SpriteBatch();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, sceneWidth, sceneHeight);
		camera.update();
		
		boxList = new Array<Rectangle>();
	}
	
	public void Init()
	{
		// Call after levels are loaded
		nbLevels = game.levelManager.GetLevelCount();
		
		// Level box size
		int levelBoxWidth = 100;
		int levelBoxHeight = 120;
		
		int nbCols = 6;
		int col,row;
		for(int i=0; i<nbLevels; i++)
		{
			// position of this box 
			col = i % nbCols;
			row = i / nbCols;
			
			// Draw level button
			int spaceX = (sceneWidth - (nbCols * levelBoxWidth)) / (nbCols+1);
			int spaceY = 40;
			
			Rectangle r = new Rectangle(
					((col+1)*spaceX)+(col*levelBoxWidth),
					sceneHeight-(row*levelBoxHeight)-levelBoxHeight-(spaceY*(row+1)),
					levelBoxWidth,
					levelBoxHeight);
			
			boxList.add(r);
		}
		
	}
	
	/****************************/
	// Screen inherited functions
	/****************************/

	@Override
	public void render(float delta)
	{
		
		// Render
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		for(int i=0; i<boxList.size; i++)
		{
			Rectangle r = boxList.get(i);
			boxNP.setColor(new Color(.8f, 0f, .3f, 1f));
			boxNP.draw(batch, r.x, r.y, r.width, r.height);
		}

		batch.end();
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void show()
	{
		Gdx.input.setInputProcessor(this);
		Gdx.input.setCatchBackKey(true);
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
	
	
	/****************************/
	// InputProcessor inherited functions
	/****************************/
	
	@Override
	public boolean keyDown(int keycode)
	{
		if(keycode == Keys.BACK || keycode == Keys.ESCAPE)
		{
			// Exit application
			Gdx.app.exit();
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button)
	{
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button)
	{
		Vector3 v = new Vector3(screenX, screenY, 0);
		camera.unproject(v);
		for(int i=0; i<boxList.size; i++)
		{
			if(boxList.get(i).contains(v.x, v.y))
			{
				game.NewGame(i);
			}
		}
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
