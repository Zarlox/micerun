package com.kifui.micerun;

public class MovingEntity
{
	// Source and destination tile
	public int tileX, tileY, tileDestX, tileDestY;
	
	// Center X,Y point from maze base x,y
	int centerX, centerY;
	
	// Offset between source and destination tile
	public float moveOffset;
	
	public float speed;			// current speed of the entity
	public float normSpeed;		// normal speed
	public float runSpeed;		// Running speed
	public boolean swiped;		// Entity pushed back/forward by a swipe of the destination/source
	
	public boolean moving;	// true when moving to a new tile in progress
	public int direction;	// 0-3 from top CLW
	
	public boolean enabled;	// Flag indicating if the entity is still in play
	
	MovingEntity()
	{
		Init(0, 0, 0f, 0f);
	}
	
	public void Init(int x, int y, float spd, float runSpd)
	{
		tileX = tileDestX = x;
		tileY = tileDestY = y;
		moveOffset = 0;
		
		speed = normSpeed = spd;
		runSpeed = runSpd;
		
		enabled = true;
		moving = false;
		swiped = false;
		direction = -1;
	}
}
