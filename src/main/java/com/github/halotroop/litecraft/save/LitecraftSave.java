package com.github.halotroop.litecraft.save;

import java.io.*;

import com.github.halotroop.litecraft.world.Chunk;

import tk.valoeghese.sod.BinaryData;

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
		this.file.mkdirs();
	}

	private final File file;

	public void saveChunk(Chunk chunk)
	{
		StringBuilder fileLocBuilder = new StringBuilder(this.file.getPath())
				.append('/').append(chunk.chunkX)
				.append('/').append(chunk.chunkZ);
		File chunkDir = new File(fileLocBuilder.toString());
		chunkDir.mkdirs();
		File chunkFile = new File(fileLocBuilder.append('/').append(chunk.chunkY).append(".sod").toString());

		try
		{
			chunkFile.createNewFile();

			BinaryData data = new BinaryData();
			chunk.write(data);
			data.write(chunkFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public Chunk readChunk(int chunkX, int chunkY, int chunkZ)
	{
		File chunkFile = new File(new StringBuilder(this.file.getPath())
				.append('/').append(chunkX)
				.append('/').append(chunkZ)
				.append('/').append(chunkY).append(".sod").toString());

		if (chunkFile.isFile())
		{
			BinaryData data = BinaryData.read(chunkFile);

			Chunk result = new Chunk(chunkX, chunkY, chunkZ);
			result.read(data);
			return result;
		}
		else return null;
	}

	private static final String SAVE_DIR = "./saves/";
}
