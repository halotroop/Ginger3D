package com.github.halotroop.litecraft.save;

import java.io.File;

public final class LitecraftSave
{
	public LitecraftSave(String name, boolean mustCreateNew)
	{
		StringBuilder sb = new StringBuilder(SAVE_DIR).append(name);
		File saveFile = new File(sb.toString());
		
		if (mustCreateNew)
		{
			while (saveFile.exists())
			{
				sb.append('_');
				saveFile = new File(sb.toString());
			}
		}

		this.file = saveFile;
	}
	
	private final File file;

	private static final String SAVE_DIR = "./saves/";
}
