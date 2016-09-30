package com.kifui.micerun;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kifui.micerun.json.Cat;
import com.kifui.micerun.json.Dog;
import com.kifui.micerun.json.Level;
import com.kifui.micerun.json.Mice;

public class PlayScreen implements Screen, InputProcessor
{
	// Hud viewport is fixed size 
	private final int sceneHudWidth = 800;
	private final int sceneHudHeight = 480;
	
	// Map viewport size is calculated to fit map
	private int sceneWidth;
	private int sceneHeight;
	
	private final int bumpedSpeed = 220;
	
	private final int tileSize = 64;
	
	private SpriteBatch batch;
	private OrthographicCamera mapCamera;
	private OrthographicCamera hudCamera;
	private Viewport viewportMap;
	private Viewport viewportHud;
	private MiceRunGame game;
	
	//private ShapeRenderer debugDraw;
	
	// Clipping
	private Rectangle clipRect, scissorMazeRect;
	
	// Main textures
	private Texture t;
	
	// texture regions for the tiles
	private TextureRegion floorTR;
	
	private TextureRegion vertWallTR;
	private TextureRegion horzWallTR;

	private TextureRegion vertPathTR;
	private TextureRegion horzPathTR;
	private TextureRegion centerPathTR;
	
	// TEMP cat, dog  and mouse
	private TextureRegion catTR;
	private TextureRegion dogTR;
	private TextureRegion miceTR;
	
	// Maze items
	private TextureRegion cheeseTR;
	private TextureRegion catnipTR;
	
	// Locked row/col mark
	private TextureRegion lockedTR;
	
	// Frame
	private TextureRegion frameTR;
	private NinePatch frameNP;
	
	// HUD stuff
	private int miceCatchedCount;
	private int catCatchedCount;
	private int score;
	private String sScore;

	private float timer, initialTimer;
	private String sTimer;
	
	// Font
	private BitmapFont font;
	
	// Maze map object
	private Maze maze;
	
	// Maze base X,Y from viewport 0,0
	private int mazeBaseX;
	private int mazeBaseY;
	
	// Row/Col swipe permission
	private RowColPerm rowPerm[];
	private RowColPerm colPerm[];

	// Swiping row/column mode
	private boolean swiping;
	private float swipeScrollTime;
	private int swipeOffset;
	
	private ESwipeOrient eSwipeOrient;
	private int swipeIndex;
	private int swipeDirection;
	
	// Swipe counter for score
	private int swipeCount;
	
	private boolean touchDown;
	private Vector3 touchPt;
	
	// Move helper
	IntArray passageList;
	
	// Index of current level
	private int levelIndex;
	
	// Play mode
	// 1 : Ready
	// 2 : Play
	// 3 : Lose
	// 4 : Win
	private enum EMode
	{
		Ready,		// About to start
		Play,		// Playing (user input)
		Lose,		// Game Over, Player lost
		Win			// Gameover, Player win, show score
	};
	private EMode eMode;
	
	// Cat & mouse
	private MovingEntity cats[];
	private MovingEntity dogs[];
	private MovingEntity mice[];
	private int catCount;
	private int dogCount;
	private int miceCount;
	
	
	public PlayScreen(MiceRunGame game)
	{
		this.game = game;

		// Widget texture
		t = new Texture(Gdx.files.internal("data/main.png"));
		
		// floor and walls
		floorTR = new TextureRegion(t, 0, 0, tileSize, tileSize);
		
		// for clipping
		scissorMazeRect = new Rectangle();
		clipRect = new Rectangle();
		
		// walls
		horzWallTR = new TextureRegion(t,64,57,64,7);
		vertWallTR = new TextureRegion(t,128,0,7,64);

		// path markers
		horzPathTR = new TextureRegion(t,137,34,32,11);
		vertPathTR = new TextureRegion(t,137,0,11,32);
		centerPathTR = new TextureRegion(t,155,11,11,11);
		
		// Cat & Mice
		catTR = new TextureRegion(t,179,1,48,48);
		dogTR = new TextureRegion(t,178,52,48,48);
		miceTR = new TextureRegion(t,232,1,48,48);
		
		// Locked mark
		lockedTR = new TextureRegion(t,287,2,23,29);

		// frame nine-patch
		frameTR = new TextureRegion(t, 318, 2, 64, 64);
		frameNP = new NinePatch(frameTR, 24, 24, 24, 24);
		
		// item texture region
		cheeseTR = new TextureRegion(t, 385, 1, 32, 32);
		catnipTR = new TextureRegion(t, 422, 0, 24, 32);
		
		// Other initialization
		batch = new SpriteBatch();
		
		//debugDraw = new ShapeRenderer();
		
		mapCamera = new OrthographicCamera();
		mapCamera.setToOrtho(false, sceneWidth, sceneHeight);
		mapCamera.update();

		hudCamera = new OrthographicCamera();
		hudCamera.setToOrtho(false, sceneWidth, sceneHeight);
		hudCamera.update();
		
		font = new BitmapFont(Gdx.files.internal("data/default.fnt"), false);
		
		sTimer = new String();
		sScore = new String();
		
		maze = new Maze();

		touchPt = new Vector3();
		
		passageList = new IntArray();
		
		eMode = EMode.Ready;
	}
	
	public void newGame(int levelIndex)
	{
		int i;
		// Keep level index to restart a new game
		this.levelIndex = levelIndex;
		
		// Get level from manager
		Level level = game.levelManager.GetLevelData(levelIndex);
		
		// Initialize random level from data
		maze.NewMaze(level.width,  level.height, level.minWallsPerTile, level.maxWallsPerTile, level.seed);
		
		// Add items to the maze (Optional)
		if (level.item != null)
		{
			for(i=0; i<level.item.size; i++)
				maze.AddItem(level.item.get(i));
		}
		
		// Allocate row/column permissions
		rowPerm = new RowColPerm[level.height];
		for(i=0; i<level.height; i++)
			rowPerm[i] = new RowColPerm();
		colPerm = new RowColPerm[level.width];
		for(i=0; i<level.width; i++)
			colPerm[i] = new RowColPerm();

		// Calculate viewport size to fit maze
		sceneWidth = (level.width + 2) * tileSize;	// Required width
		sceneWidth *= (8f/7f);						// Add 1/8
		sceneHeight = (level.height + 2) * tileSize;
		

		// Set-up cameras & viewports
		viewportMap = new FitViewport(sceneWidth, sceneHeight, mapCamera);
		mapCamera.update();
		
		viewportHud = new FitViewport(sceneHudWidth, sceneHudHeight, hudCamera);
		hudCamera.update();
		
		// Calculate maze base X,Y corner values
		mazeBaseX = (sceneWidth - (maze.GetWidth()*tileSize)) / 2;
		mazeBaseY = (sceneHeight - (maze.GetHeight()*tileSize)) / 2;
		
		// Set the clip rectangle as the maze area
		clipRect.set(mazeBaseX, mazeBaseY, maze.GetWidth()*tileSize, maze.GetHeight()*tileSize);
		
		// score related
		swipeCount = 0;
		score = 0;
		miceCatchedCount = 0;
		catCatchedCount = 0;
		
		// Timer
		initialTimer = level.time;
		timer = initialTimer;
		
		// Input swiping related
		touchDown = false;
		swiping = false;

		// Cats
		catCount = level.cat.size;
		cats = new MovingEntity[catCount];
		for(i=0; i<catCount; i++)
			cats[i] = new MovingEntity();
		for(i=0; i<catCount; i++)
		{
			Cat c = level.cat.get(i);
			cats[i].Init(c.x, c.y, c.speed, c.runSpeed);
		}
		
		// Dogs (Optional)
		dogCount = 0;
		if (level.dog != null)
		{
			dogCount = level.dog.size;
			dogs = new MovingEntity[dogCount];
			for(i=0; i<dogCount; i++)
				dogs[i] = new MovingEntity();
			for(i=0; i<dogCount; i++)
			{
				Dog d = level.dog.get(i);
				dogs[i].Init(d.x, d.y, d.speed, d.runSpeed);
			}
		}
		
		// Mice
		miceCount = level.mice.size;
		mice = new MovingEntity[miceCount];
		for(i=0; i<miceCount; i++)
			mice[i] = new MovingEntity();
		for(i=0; i<miceCount; i++)
		{
			Mice m = level.mice.get(i);
			mice[i].Init(m.x, m.y, m.speed, m.runSpeed);
		}

		// If the level is set not to be predictable, Seed the random number 
		// generator from the time. If predictable, leave it as is since the
		// whole game play will be set from the level seed
		if (!level.predictable)
			MathUtils.random.setSeed(System.currentTimeMillis());
		
		// Level created. Ready to play
		eMode = EMode.Play;
	}
	
	@Override
	public void render(float delta)
	{
		// Updates done only when in playing mode
		if (eMode == EMode.Play)
		{
			// Update timer
			if (timer <= 0)
			{
				// GAME over, player lose
				timer = 0;
				eMode = EMode.Lose;
				CalculateFinalScore();
			}
			else
				timer -= delta;
			
			int mins = (int)timer/60;
			int secs = (int)timer % 60;
			int ms	 = (int)(((float)timer - ((int)timer)) * 10f);
			sTimer = String.format("Time: %02d:%02d:%01d", mins, secs, ms);
			
			// Update score
			sScore = String.format("Mice: %d", miceCatchedCount);
			
			// Update cat and mouse positions
			UpdateCatAndMice(delta);
			
			// Update swipe process
			if (swiping)
			{
				if (swipeScrollTime >= 1f/2f)
				{
					// Swiping done. Swipe actual maze
					// array tiles
					maze.OffsetTiles(eSwipeOrient, swipeIndex, swipeDirection);
					swiping = false;
					
					// Reduce all row/col cooldowns
					////UpdateRowColCooldowns();
					// Increment cooldown on the ones we just swiped
					////IncrementRowColCooldown(eSwipeOrient, swipeIndex, 1);
				}
				else
				{
					// Swiping
					swipeScrollTime += delta;
					swipeOffset = (int)((tileSize * swipeScrollTime) * 2f);
				}
			}

			// Update permissions before checking for swipe
			UpdateRowColPermissions();
			
			// Check for collision
			MiceCatched();
			CatCatched();
		}
		
		
		// Start Render
		Gdx.gl.glClearColor(0f, 0f, 0f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		//batch.setProjectionMatrix(hudCamera.combined);
		//batch.begin();
		//batch.end();
		
		// Render widgets
		batch.setProjectionMatrix(mapCamera.combined);
		batch.begin();
		RenderMaze(batch);
		RenderCatAndMice(batch);
		RenderRowColPermissions(batch);
		batch.end();

		// Render HUD
		batch.setProjectionMatrix(hudCamera.combined);
		batch.begin();
		//if (eMode == EMode.Play)
		{
			font.setColor(1f, 1f, 0f, 1f);
			font.draw(batch, sTimer, 10, sceneHudHeight-20);
			font.draw(batch, sScore, 10, sceneHudHeight-50);
		}

		if (eMode == EMode.Lose || eMode == EMode.Win)
		{
			// final score box
			int fw = 300;
			int fh = 200;
			if (eMode == EMode.Lose)
				frameNP.setColor(new Color(1f, .4f, .4f, .85f));
			else
				frameNP.setColor(new Color(.4f, 1f, .4f, .85f));
			frameNP.draw(batch,(sceneHudWidth-fw)*.5f, (sceneHudHeight-fh)*.5f, fw, fh);
			font.setColor(0f, .0f, .4f, 1f);
			
			if (eMode == EMode.Lose)
			{
				TextBounds tb = font.getBounds("GAME OVER");
				font.draw(batch, "GAME OVER", (sceneHudWidth-tb.width)*.5f, (sceneHudHeight*.5f)+20);
				tb = font.getBounds("You fail at catching mices...");
				font.draw(batch, "You fail at catching mices...", (sceneHudWidth-tb.width)*.5f, (sceneHudHeight*.5f)-20);
			}
			else
			{
				TextBounds tb = font.getBounds("FINAL SCORE");
				font.draw(batch, "FINAL SCORE", (sceneHudWidth-tb.width)*.5f, (sceneHudHeight*.5f)+20);
				
				tb = font.getBounds(sScore);
				font.draw(batch, sScore, (sceneHudWidth-tb.width)*.5f, (sceneHudHeight*.5f)-20);
			}
		}

		batch.end();
	}

	@Override
	public void resize(int width, int height)
	{
		viewportHud.update(width, height);
		viewportMap.update(width, height);
	}

	@Override
	public void show()
	{
		Gdx.input.setInputProcessor(this);
		Gdx.input.setCatchBackKey(true);
	}

	@Override
	public void hide()
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

	@Override
	public void dispose()
	{
	}
	
	/********************************************
	 *  Utility
	 *******************************************/
	
	private float COFF(float s1, float s2)
	{
		return (s2-s1) / 2;
	}
	
	private void GetPassageList(int x, int y, int exclusionDir)
	{
		passageList.clear();
		for(int i=0; i<4; i++)
		{
			if (maze.HavePassage(x, y, i))
			{
				if (exclusionDir != i)
					passageList.add(i);
			}
		}
	}
	
	private int GetReverseDirection(int direction)
	{
		switch(direction)
		{
		case 0: // left
			return 1;
		case 1: // right
			return 0;
		case 2: // up
			return 3;
		case 3: // down
			return 2;
		}
		return -1;
	}
	
	private void RenderMaze(SpriteBatch batch)
	{
		// Clip to maze area
		//ScissorStack.calculateScissors(mapCamera, 0, 0, sceneWidth, sceneHeight, batch.getTransformMatrix(), clipRect, scissorMazeRect);
		//ScissorStack.pushScissors(scissorMazeRect);
		
		// Render all tiles
		int tileBaseX, tileBaseY;
		int renderCount = 1;
		int tileBaseX2=0, tileBaseY2=0;
		for(int y=0; y<maze.GetHeight(); y++)
		{
			for(int x=0; x<maze.GetWidth(); x++)
			{
				// Is this tile swiping?
				boolean tileSwiped = IsTileSwiped(x, y);
				
				// base tile position
				tileBaseX = mazeBaseX + (x*tileSize);
				tileBaseY = mazeBaseY + (y*tileSize);
				
				// Tile offset
				renderCount = 1;
				if (tileSwiped)
				{
					if (eSwipeOrient == ESwipeOrient.SwipeHorz)
					{
						tileBaseX += (swipeOffset * swipeDirection);
						if (swipeDirection == 1 && x == maze.GetWidth()-1)
						{
							renderCount = 2;
							tileBaseX2 = mazeBaseX - tileSize + swipeOffset;
							tileBaseY2 = tileBaseY;
						}
						else if (swipeDirection == -1 && x == 0)
						{
							renderCount = 2;
							tileBaseX2 =  mazeBaseX + (maze.GetWidth()*tileSize) - swipeOffset;
							tileBaseY2 = tileBaseY;
						}
					}
					else
					{
						tileBaseY += (swipeOffset * swipeDirection);
						if (swipeDirection == 1 && y == maze.GetHeight()-1)
						{
							renderCount = 2;
							tileBaseX2 = tileBaseX;
							tileBaseY2 = mazeBaseY - tileSize + swipeOffset;
						}
						else if (swipeDirection == -1 && y == 0)
						{
							renderCount = 2;
							tileBaseX2 = tileBaseX;
							tileBaseY2 =  mazeBaseY + (maze.GetHeight()*tileSize) - swipeOffset;
						}
					}
				}
				
				// Render the  tile content. It will be rendered
				// 2 times if its a tile hat is swiped 
				// from end to start of row/column
				for(int i=0; i<renderCount; i++)
				{
					// For second tile instance render
					// Use the second tile position
					if (i == 1)
					{
						tileBaseX = tileBaseX2;
						tileBaseY = tileBaseY2;
					}
					
					// Tile floor
					batch.draw(floorTR,  tileBaseX, tileBaseY);
					
					// Draw walls and path markers
					if (maze.HaveTileWall(x, y, 0)) // Left wall
						batch.draw(vertWallTR,tileBaseX, tileBaseY);
					else
						batch.draw(horzPathTR,tileBaseX, tileBaseY + (tileSize-horzPathTR.getRegionHeight())/2);
					
					if (maze.HaveTileWall(x, y, 1)) // Right wall
						batch.draw(vertWallTR,tileBaseX+tileSize-vertWallTR.getRegionWidth(), tileBaseY);
					else
						batch.draw(horzPathTR,tileBaseX + tileSize/2, tileBaseY + (tileSize-horzPathTR.getRegionHeight())/2);
					
					if (maze.HaveTileWall(x, y, 2)) // Up wall
						batch.draw(horzWallTR,tileBaseX, tileBaseY+tileSize-horzWallTR.getRegionHeight());
					else
						batch.draw(vertPathTR,tileBaseX + (tileSize-vertPathTR.getRegionWidth())/2, tileBaseY + tileSize/2);
					
					if (maze.HaveTileWall(x, y, 3)) // Down wall
						batch.draw(horzWallTR,tileBaseX, tileBaseY);
					else
						batch.draw(vertPathTR,tileBaseX + (tileSize-vertPathTR.getRegionWidth())/2, tileBaseY);
					
					// Draw center path
					batch.draw(centerPathTR,tileBaseX + (tileSize-centerPathTR.getRegionWidth())/2, tileBaseY + (tileSize-centerPathTR.getRegionHeight())/2);
					
					// draw item
					switch(maze.GetTileItem(x, y))
					{
					case Cheese:
						batch.draw(cheeseTR,tileBaseX + (tileSize-cheeseTR.getRegionWidth())/2, tileBaseY + (tileSize-cheeseTR.getRegionHeight())/2);
						break;
					case Catnip:
						batch.draw(catnipTR,tileBaseX + (tileSize-catnipTR.getRegionWidth())/2, tileBaseY + (tileSize-catnipTR.getRegionHeight())/2);
						break;
					case None:
						// no item
						break;
					default:
						assert(false);
					}
				}
			}
		}
		batch.flush();
		
		// Disable clipping
		//ScissorStack.popScissors();
	}

	private void UpdateCatAndMice(float delta)
	{
		int i;
		// Cats
		// If the cat has reached a new tile, look for a mouse
		// and use the run speed if mouse can be seen
		// TODO: Fix UpdateCatSpeed();
		for(i=0; i<catCount; i++)
			UpdateMovingEntity(delta, cats[i]);
		// Dogs
		for(i=0; i<dogCount; i++)
			UpdateMovingEntity(delta, dogs[i]);
		// Mouses
		for(i=0; i<miceCount; i++)
			UpdateMovingEntity(delta, mice[i]);
	}

	private void UpdateCatSpeed()
	{
		for(int j=0; j<catCount; j++)
		{
			if (!cats[j].moving)
			{
				for(int i=0; i<miceCount; i++)
				{
					if (!mice[i].enabled)
						continue;
					
					if ((cats[j].tileY == mice[i].tileY && maze.SeeTileHorz(cats[j].tileX, mice[i].tileX, cats[j].tileY)) ||
						(cats[j].tileX == mice[i].tileX && maze.SeeTileVert(cats[j].tileY, mice[i].tileY, cats[j].tileX)))
					{
						// we saw a mouse
						cats[j].speed = cats[j].runSpeed;
	
						if (cats[j].tileY == mice[i].tileY)
						{
							if (cats[j].tileX <= mice[i].tileX)
							{
								cats[j].direction = 1;
								cats[j].tileDestX++;
							}
							else
							{
								cats[j].direction = 0;
								cats[j].tileDestX--;
							}
						}
						else
						{
							if (cats[j].tileY <= mice[i].tileY)
							{
								cats[j].direction = 2;
								cats[j].tileDestY++;
							}
							else
							{
								cats[j].direction = 3;
								cats[j].tileDestY--;
							}
						}
						cats[j].moveOffset = 0f;
						cats[j].moving = true;
						break;
					}
					else
						cats[j].speed = cats[j].normSpeed;
				}
			}
		}
	}
	
	private void UpdateMovingEntity(float delta, MovingEntity ent)
	{
		// Return if entity is disabled
		if (!ent.enabled)
			return;
		
		// Update mouvement
		if (!ent.moving)
		{
			// Reached a tile, scan for direction
			
			// Get reverse direction
			int reverseDir = GetReverseDirection(ent.direction);
			// Get the list of passage excluding the last direction (if any)
			GetPassageList(ent.tileX, ent.tileY, reverseDir);
			// If the passage list is empty, we are forced back to last direction
			// if the last direction is not block
			if (passageList.size == 0 && reverseDir!=-1)
			{
				if (maze.HavePassage(ent.tileX,  ent.tileY, reverseDir))
					passageList.add(reverseDir);
			}
			// If not empty anymore, random a direction and go
			if (passageList.size > 0)
			{
				ent.direction = passageList.random();
				ent.moveOffset = 0f;
				ent.moving = true;
				switch(ent.direction)
				{
				case 0: // left
					ent.tileDestX = ent.tileX - 1;
					ent.tileDestY = ent.tileY;
					break;
				case 1: // right
					ent.tileDestX = ent.tileX + 1;
					ent.tileDestY = ent.tileY;
					break;
				case 2: // up
					ent.tileDestY = ent.tileY + 1;
					ent.tileDestX = ent.tileX;
					break;
				case 3: // down
					ent.tileDestY = ent.tileY - 1;
					ent.tileDestX = ent.tileX;
					break;
				}
			}
		}
		else
		{
			// Currently moving between tiles
			if (!ent.swiped)
			{
				// 1. If the destination tile become part of a wipe, reverse the source/dest and double speed.
				if (IsTileSwiped(ent.tileDestX, ent.tileDestY))
				{
					// Swap X values
					ent.tileX = ent.tileX + ent.tileDestX;
					ent.tileDestX = ent.tileX - ent.tileDestX;
					ent.tileX = ent.tileX - ent.tileDestX;				
					// Swap Y values
					ent.tileY = ent.tileY + ent.tileDestY;
					ent.tileDestY = ent.tileY - ent.tileDestY;
					ent.tileY = ent.tileY - ent.tileDestY;
					// double speed
					ent.speed = bumpedSpeed;
					// Reverse direction
					ent.direction = GetReverseDirection(ent.direction);
					// reverse offset
					ent.moveOffset = tileSize - ent.moveOffset;
					//swipe flag On
					ent.swiped = true;
				}
				// 2. if source become swiped, double speed
				else if (IsTileSwiped(ent.tileX, ent.tileY))
				{
					// double speed
					ent.speed = bumpedSpeed;
					//swipe flag On
					ent.swiped = true;
				}
			}
			
			// Perform move
			ent.moveOffset += (ent.speed * delta);
			
			// Destination reached?
			if (ent.moveOffset > tileSize)
			{
				// Destination tile reached
				ent.moveOffset = 0f;
				ent.moving = false;
				ent.swiped = false;
				ent.tileX = ent.tileDestX;
				ent.tileY = ent.tileDestY;
				ent.speed = ent.normSpeed;
			}
		}
		
		// Update entity center X,Y position
		int x = mazeBaseX + (ent.tileX * tileSize);
		int y = mazeBaseY + (ent.tileY * tileSize);
		if (ent.moving)
		{
			switch(ent.direction)
			{
			case 0: // left
				x -= ent.moveOffset;
				break;
			case 1: // right
				x += ent.moveOffset;
				break;
			case 2: // up
				y += ent.moveOffset;
				break;
			case 3: // down
				y -= ent.moveOffset;
				break;
			}
		}
		ent.centerX = x+(tileSize/2);
		ent.centerY = y+(tileSize/2);
	}
	
	
	private void RenderCatAndMice(SpriteBatch batch)
	{
		int i;
		// cats
		for(i=0; i<catCount; i++)
			RenderMovingEntity(batch, cats[i], catTR);
		// dogs
		for(i=0; i<dogCount; i++)
			RenderMovingEntity(batch, dogs[i], dogTR);
		// Mouses
		for(i=0; i<miceCount; i++)
			RenderMovingEntity(batch, mice[i], miceTR);
	}
	
	private void RenderMovingEntity(SpriteBatch batch, MovingEntity ent, TextureRegion tr)
	{
		// Return if entity is disabled
		if (!ent.enabled)
			return;

		// Base tile X,Y position
		int x = mazeBaseX + (ent.tileX * tileSize);
		int y = mazeBaseY + (ent.tileY * tileSize);
		// if moving, add moving transition offset
		if (ent.moving)
		{
			switch(ent.direction)
			{
			case 0: // left
				x -= ent.moveOffset;
				break;
			case 1: // right
				x += ent.moveOffset;
				break;
			case 2: // up
				y += ent.moveOffset;
				break;
			case 3: // down
				y -= ent.moveOffset;
				break;
			}
		}
		
		/*
		debugDraw.begin(ShapeType.Line);
		debugDraw.circle(ent.centerX, ent.centerY, 20);
		debugDraw.end();
		*/
		
		// Draw entity
		batch.draw(tr, x+(tileSize-tr.getRegionWidth())/2 , y+(tileSize-tr.getRegionHeight())/2);
	}
	
	private void MiceCatched()
	{
		// Check  for collision between cat & mice
		for(int j=0; j<catCount; j++)
		{
			if (cats[j].enabled)
			{
				for(int i=0; i<miceCount; i++)
				{
					if (mice[i].enabled && Vector2.dst(cats[j].centerX, cats[j].centerY, mice[i].centerX, mice[i].centerY) < 40)
					{
						mice[i].enabled = false;
						miceCatchedCount++;
					}
				}
			}
		}
		// Is game over?
		if (miceCatchedCount == miceCount)
		{
			eMode = EMode.Win;
			CalculateFinalScore();
		}
	}
	
	private void CatCatched()
	{
		// Check  for collision between dogs & cats
		for(int j=0; j<dogCount; j++)
		{
			for(int i=0; i<catCount; i++)
			{
				if (cats[i].enabled && Vector2.dst(dogs[j].centerX, dogs[j].centerY, cats[i].centerX, cats[i].centerY) < 40)
				{
					cats[i].enabled = false;
					catCatchedCount++;
				}
			}
		}
		// Is game over?
		if (catCatchedCount == catCount)
		{
			// GAME over, player lose
			timer = 0;
			eMode = EMode.Lose;
			CalculateFinalScore();
		}
	}
	
	private void UpdateRowColPermissions()
	{
		int i;
		// 1. unlock permissions and decrease cooldowns
		for(i=0; i<maze.GetWidth(); i++)
			colPerm[i].locked = false;
		for(i=0; i<maze.GetHeight(); i++)
			rowPerm[i].locked = false;
		
		// 2. lock row/columns where cat and mouses are
		for(i=0; i<catCount; i++)
		{
			if (cats[i].enabled)
				UpdateEntityRowColPermissions(cats[i]);
		}

		for(i=0; i<dogCount; i++)
			UpdateEntityRowColPermissions(dogs[i]);
		
		for(i=0; i<miceCount; i++)
		{
			if (mice[i].enabled)
				UpdateEntityRowColPermissions(mice[i]);
		}
	}

	private void UpdateEntityRowColPermissions(MovingEntity ent)
	{
		// Lock only the tile where the entity is most on
		if (!ent.moving || ent.moveOffset < tileSize/2)
		{
			colPerm[ent.tileX].locked = true;
			rowPerm[ent.tileY].locked = true;
		}
		else
		{
			colPerm[ent.tileDestX].locked = true;
			rowPerm[ent.tileDestY].locked = true;
		}
	}	
	
	private void UpdateRowColCooldowns()
	{
		// This should be called only one time after a swipe is over
		for(int i=0; i<maze.GetWidth(); i++)
		{
			if (colPerm[i].coolDown > 0)
				colPerm[i].coolDown--;
		}
		for(int i=0; i<maze.GetHeight(); i++)
		{
			if (rowPerm[i].coolDown > 0)
				rowPerm[i].coolDown--;
		}
	}
	
	private void IncrementRowColCooldown(ESwipeOrient orient, int index, int increment)
	{
		if (eSwipeOrient == ESwipeOrient.SwipeHorz)
			rowPerm[index].coolDown += increment;
		else
			colPerm[index].coolDown += increment;
	}
	
	private void RenderRowColPermissions(SpriteBatch batch)
	{
		int x, y;
		// 1. Columns
		for(int i=0; i<maze.GetWidth(); i++)
		{
			// Top
			x = mazeBaseX + (i * tileSize);
			y = mazeBaseY + (maze.GetHeight() * tileSize);
			
			if (colPerm[i].locked)
				batch.draw(lockedTR, x+COFF(lockedTR.getRegionWidth(), tileSize), y);
			else
			{
				TextBounds tb = font.getBounds(String.valueOf(colPerm[i].coolDown));
				if (colPerm[i].coolDown == 0)
					font.setColor(.1f, .8f, .1f, 1f);
				else
					font.setColor(.8f, .1f, .1f, 1f);
				font.draw(batch, String.valueOf(colPerm[i].coolDown), x+COFF(tb.width, tileSize), y+COFF(tb.height, tileSize));
			}
			
			
			// Bottom
			y -= (maze.GetHeight() * tileSize);
			
			if (colPerm[i].locked)
				batch.draw(lockedTR, x+COFF(lockedTR.getRegionWidth(), tileSize), y-30);
			else
			{
				TextBounds tb = font.getBounds(String.valueOf(colPerm[i].coolDown));
				if (colPerm[i].coolDown == 0)
					font.setColor(.1f, .8f, .1f, 1f);
				else
					font.setColor(.8f, .1f, .1f, 1f);
				font.draw(batch, String.valueOf(colPerm[i].coolDown), x+COFF(tb.width, tileSize), y-COFF(tb.height, 30));
			}
		}		
		// 2. Rows
		for(int i=0; i<maze.GetHeight(); i++)
		{
			// Left
			x = mazeBaseX - 30;
			y = mazeBaseY + (i * tileSize);

			if (rowPerm[i].locked)
				batch.draw(lockedTR, x, y+COFF(lockedTR.getRegionHeight(), tileSize));
			else
			{
				TextBounds tb = font.getBounds(String.valueOf(rowPerm[i].coolDown));
				if (rowPerm[i].coolDown == 0)
					font.setColor(.1f, .8f, .1f, 1f);
				else
					font.setColor(.8f, .1f, .1f, 1f);
				font.draw(batch, String.valueOf(rowPerm[i].coolDown), x+COFF(tb.width, 30), y+tileSize-COFF(tb.height, tileSize));
			}
			
			// Right
			x = mazeBaseX + maze.GetWidth()*tileSize;
		
			if (rowPerm[i].locked)
				batch.draw(lockedTR, x, y+COFF(lockedTR.getRegionHeight(), tileSize));
			else
			{
				TextBounds tb = font.getBounds(String.valueOf(rowPerm[i].coolDown));
				if (rowPerm[i].coolDown == 0)
					font.setColor(.1f, .8f, .1f, 1f);
				else
					font.setColor(.8f, .1f, .1f, 1f);
				font.draw(batch, String.valueOf(rowPerm[i].coolDown), x+COFF(tb.width, 30), y+tileSize-COFF(tb.height, tileSize));
			}
		}
	}	
	
	private boolean IsTileSwiped(int x, int y)
	{
		// Return true if the  row/col where the tile is is being swiped
		if (swiping)
		{
			if ((eSwipeOrient == ESwipeOrient.SwipeHorz && swipeIndex == y) ||
				(eSwipeOrient == ESwipeOrient.SwipeVert && swipeIndex == x))
				return true;
		}
		return false;
	}

	
	private void CalculateFinalScore()
	{
		// Base score is the timer
		score = (int)(timer * 100f);

		// 2 seconds is removed from each swipe done
		score -= (swipeCount * 100);
		if (score < 0)
			score = 0;
		
		sScore = String.format("%d", (int)score);
	}
	
	
	/********************************************
	 *  Input processing
	 *******************************************/

	@Override
	public boolean keyDown(int keycode)
	{
		if(keycode == Keys.BACK || keycode == Keys.ESCAPE)
		{
			game.StageMenu();
		}
		return false;
	}


	@Override
	public boolean keyUp(int keycode)
	{
		return false;
	}


	@Override
	public boolean keyTyped(char character)
	{
		return false;
	}


	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button)
	{
		if (eMode == EMode.Play)
		{
			if (!swiping)
			{
				touchPt.set(screenX, screenY, 0);
				hudCamera.unproject(touchPt);
				
				// Make sure the touch is in the play box
				if (clipRect.contains(touchPt.x, touchPt.y))
					touchDown = true;
			}
		}
		
		return false;
	}


	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button)
	{
		//Vector3 touchPoint=new Vector3(screenX, screenY, 0);
		//hudCamera.unproject(touchPoint);
		if (eMode == EMode.Play)
			touchDown = false;
		else if (eMode == EMode.Lose || eMode == EMode.Win)
		{
			// Relaunch the game
			newGame(levelIndex);
		}
		
		return false;
	}


	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer)
	{
		if (touchDown)
		{
			Vector3 draggedPoint=new Vector3(screenX, screenY, 0);
			hudCamera.unproject(draggedPoint);

			// Get dragged length from touch down point and 
			// enable the drag once we got to a certain length
			float l = draggedPoint.dst2(touchPt);
			if (l > 600f)
			{
				// Get Direction, orientation and row/col index
				if (Math.abs(touchPt.x - draggedPoint.x) > Math.abs(touchPt.y - draggedPoint.y))
				{
					// Horizontal swipe
					eSwipeOrient = ESwipeOrient.SwipeHorz;
					swipeIndex = ((int)touchPt.y - mazeBaseY) / tileSize;
					swipeDirection = (((int)draggedPoint.x - touchPt.x) > 0) ? 1 : -1;
				}
				else
				{
					// Vertical swipe
					eSwipeOrient = ESwipeOrient.SwipeVert;
					swipeIndex = ((int)touchPt.x - mazeBaseX) / tileSize;
					swipeDirection = (((int)draggedPoint.y - touchPt.y) > 0) ? 1 : -1;
				}
				
				// Enable the swipe only if the row/col is not blocked and there is no cooldown
				if ((eSwipeOrient == ESwipeOrient.SwipeHorz && !rowPerm[swipeIndex].locked && rowPerm[swipeIndex].coolDown==0) ||
					(eSwipeOrient == ESwipeOrient.SwipeVert && !colPerm[swipeIndex].locked && colPerm[swipeIndex].coolDown==0))
				{
					swiping = true;
					swipeScrollTime = 0f;
					swipeOffset = 0;
					swipeCount++;
				}
				
				touchDown = false;
			}
		}
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY)
	{
		return false;
	}


	@Override
	public boolean scrolled(int amount)
	{
		return false;
	}

}
