package com.kifui.micerun.json;

import com.badlogic.gdx.utils.Array;

public class Level
{
	public int id;
	public boolean demo;
	public int seed;
	public boolean predictable;
	public int time;
	public int width;
	public int height;
	public int minWallsPerTile;
	public int maxWallsPerTile;

	public Array<Cat> cat;
	public Array<Dog> dog;
	public Array<Mice> mice;

	public Array<MazeItem> item;
}
