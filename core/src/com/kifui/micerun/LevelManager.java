package com.kifui.micerun;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;
import com.kifui.micerun.json.Level;
import com.kifui.micerun.json.Levels;

public class LevelManager
{
	private Json json;
	private Levels levels;
	
	LevelManager()
	{
		json = new Json();
		//json.setTypeName(null);
		//json.setUsePrototypes(false);
		//json.setIgnoreUnknownFields(true);
		//json.setOutputType(OutputType.json);		
	}
	
	public void Load()
	{
		levels = json.fromJson(Levels.class, 
                Gdx.files.internal("data/levels.json"));
		
	}
	
	public int GetLevelCount()
	{
		return levels.level.size;
	}
	
	public Level GetLevelData(int index)
	{
		return levels.level.get(index);
	}

}
