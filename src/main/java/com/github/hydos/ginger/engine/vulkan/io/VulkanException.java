package com.github.hydos.ginger.engine.vulkan.io;

public class VulkanException extends RuntimeException
{

	public VulkanException(String string)
	{
		super(string);
	}

	/**
	 * the exception type thrown when a vulkan error is thrown
	 */
	private static final long serialVersionUID = -6985060773180054456L;
}
