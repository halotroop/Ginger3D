package com.github.hydos.ginger.engine.vulkan.shaders;

import static java.lang.ClassLoader.getSystemClassLoader;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.util.shaderc.Shaderc.*;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.file.*;

import org.lwjgl.system.NativeResource;

public class VKShaderUtils
{
	public static SPIRV compileShaderFile(String shaderFile, ShaderType shaderKind)
	{ return compileShaderAbsoluteFile(getSystemClassLoader().getResource(shaderFile).toExternalForm(), shaderKind); }

	public static SPIRV compileShaderAbsoluteFile(String shaderFile, ShaderType shaderKind)
	{
		try
		{
			String source = new String(Files.readAllBytes(Paths.get(new URI(shaderFile))));
			return compileShader(shaderFile, source, shaderKind);
		}
		catch (IOException | URISyntaxException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static SPIRV compileShader(String filename, String source, ShaderType shaderKind)
	{
		long compiler = shaderc_compiler_initialize();
		if (compiler == NULL)
		{ throw new RuntimeException("Failed to create shader compiler"); }
		long result = shaderc_compile_into_spv(compiler, source, shaderKind.kind, filename, "main", NULL);
		if (result == NULL)
		{ throw new RuntimeException("Failed to compile shader " + filename + " into SPIR-V"); }
		if (shaderc_result_get_compilation_status(result) != shaderc_compilation_status_success)
		{ throw new RuntimeException("Failed to compile shader " + filename + "into SPIR-V:\n " + shaderc_result_get_error_message(result)); }
		shaderc_compiler_release(compiler);
		return new SPIRV(result, shaderc_result_get_bytes(result));
	}

	public enum ShaderType
	{
		VERTEX_SHADER(shaderc_glsl_vertex_shader),
		GEOMETRY_SHADER(shaderc_glsl_geometry_shader),
		FRAGMENT_SHADER(shaderc_glsl_fragment_shader);

		private final int kind;

		ShaderType(int kind)
		{ this.kind = kind; }
	}

	public static final class SPIRV implements NativeResource
	{
		private final long handle;
		private ByteBuffer bytecode;

		public SPIRV(long handle, ByteBuffer bytecode)
		{
			this.handle = handle;
			this.bytecode = bytecode;
		}

		public ByteBuffer bytecode()
		{ return bytecode; }

		@Override
		public void free()
		{
			shaderc_result_release(handle);
			bytecode = null; // Help the GC
		}
	}
}
