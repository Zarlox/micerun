package com.kifui.micerun;

import com.badlogic.gdx.math.MathUtils;
import com.kifui.micerun.json.MazeItem;

public class Maze
{
	private MazeTile tiles[][];
	private int mazeWidth, mazeHeight;
	
	private int minWalls, maxWalls;
	
	Maze()
	{
		
	}
	
	public void NewMaze(int w, int h, int minW, int maxW, int seed)
	{
		mazeWidth = w;
		mazeHeight = h;
		
		minWalls = minW;
		maxWalls = maxW;
		
		MathUtils.random.setSeed(seed);
		
		// Allocate tiles
		tiles = new MazeTile[w][h];
		for(int y=0; y<h; y++)
		{
			for(int x=0; x<w; x++)
			{
				tiles[x][y] = new MazeTile();
				
				// Randomize walls on tile
				RandomizeTile(tiles[x][y]);
			}
		}
	}
	
	public void AddItem(MazeItem item)
	{
		// Add item in maze array from JSON structure
		tiles[item.x][item.y].item = GetItemType(item.type);
	}
	
	public MazeItemType GetTileItem(int x, int y)
	{
		if (tiles[x][y].item == null)
			return MazeItemType.None;
		else
			return tiles[x][y].item;
	}
	
	private MazeItemType GetItemType(int intType)
	{
		// JSON integer type to enum
		switch(intType)
		{
		case 1:
			return MazeItemType.Cheese;
		case 2:
			return MazeItemType.Catnip;
		default:
			assert(false);
			return MazeItemType.None;
		}
	}
	
	private void RandomizeTile(MazeTile tile)
	{
		// Random number of wall 2-4
		int nbWalls = MathUtils.random(minWalls, maxWalls);
		for(int i=0; i<nbWalls; i++)
		{
			switch(MathUtils.random(0, 3))
			{
			case 0:
				tile.leftWall = true;
				break;
			case 1:
				tile.rightWall = true;
				break;
			case 2:
				tile.upWall = true;
				break;
			case 3:
				tile.downWall = true;
				break;
			}
		}
	}
	
	public int GetWidth()
	{
		return mazeWidth;
	}

	public int GetHeight()
	{
		return mazeHeight;
	}
	
	public boolean HaveTileWall(int x, int y, int wall)
	{
		// Check for a wall on this tile
		// Wall (0:left 1:right 2:up 3:down)
		switch(wall)
		{
		case 0:
			return tiles[x][y].leftWall;
		case 1:
			return tiles[x][y].rightWall;
		case 2:
			return tiles[x][y].upWall;
		case 3:
			return tiles[x][y].downWall;
		}
		assert(false);
		return false;
	}
	
	public boolean SeeTileHorz(int x1, int x2, int y)
	{
		if (x1 == x2)
			return true;
		// reorder so x1 is lower
		if (x2 < x1)
		{
			int tx = x1;
			x1 = x2;
			x2 = tx;
		}
		
		for(int i=x1; i<x2; i++)
		{
			if (!HavePassage(i, y, 1))
				return false;
		}
		return true;
	}

	public boolean SeeTileVert(int y1, int y2, int x)
	{
		if (y1 == y2)
			return true;
		// reorder so y1 is lower
		if (y2 < y1)
		{
			int ty = y1;
			y1 = y2;
			y2 = ty;
		}
		
		for(int i=y1; i<y2; i++)
		{
			if (!HavePassage(x, i, 2))
				return false;
		}
		return true;
	}
	
	public boolean HavePassage(int x, int y, int wall)
	{
		// Check for passage from this tile in a specified direction
		// Wall (0:left 1:right 2:up 3:down)
		switch(wall)
		{
		case 0:
			return !(x == 0 || tiles[x][y].leftWall || tiles[x-1][y].rightWall);
		case 1:
			return !(x == mazeWidth-1 || tiles[x][y].rightWall || tiles[x+1][y].leftWall);
		case 2:
			return !(y == mazeHeight-1 || tiles[x][y].upWall || tiles[x][y+1].downWall);
		case 3:
			return !(y == 0 || tiles[x][y].downWall || tiles[x][y-1].upWall);
		}
		assert(false);
		return false;
	}

	public void OffsetTiles(ESwipeOrient eSwipeOrient, int swipeIndex, int swipeDir)
	{
		if (eSwipeOrient == ESwipeOrient.SwipeHorz)
		{
			if (swipeDir == -1)
			{
				for(int i=0; i<mazeWidth-1; i++)
					tiles[i][swipeIndex].SwapTiles(tiles[i+1][swipeIndex]);
			}
			else
			{
				for(int i=mazeWidth-1; i>0; i--)
					tiles[i][swipeIndex].SwapTiles(tiles[i-1][swipeIndex]);
			}
		}
		else
		{
			if (swipeDir == -1)
			{
				for(int i=0; i<mazeHeight-1; i++)
					tiles[swipeIndex][i].SwapTiles(tiles[swipeIndex][i+1]);
			}
			else
			{
				for(int i=mazeHeight-1; i>0; i--)
					tiles[swipeIndex][i].SwapTiles(tiles[swipeIndex][i-1]);
			}
		}
	}

}
