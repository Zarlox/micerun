package com.kifui.micerun;

public class MazeTile
{
	public boolean leftWall;
	public boolean rightWall;
	public boolean upWall;
	public boolean downWall;
	
	public MazeItemType item;
	
	public void Set(MazeTile t)
	{
		leftWall = t.leftWall;
		rightWall = t.rightWall;
		upWall = t.upWall;
		downWall = t.downWall;
		item = t.item;
	}
	
	public void SwapTiles(MazeTile t)
	{
		MazeTile temp = new MazeTile();
		temp.Set(this);
		Set(t);
		t.Set(temp);
	}
}
