package com.mumfrey.worldeditcui.render;

import com.mumfrey.worldeditcui.render.RenderStyle.RenderType;

import net.minecraft.client.renderer.GlStateManager;

/**
 * Stores data about a line that can be rendered
 * 
 * @author lahwran
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public class LineStyle
{
	public final float lineWidth, red, green, blue, alpha;
	public final RenderType renderType;
	
	public LineStyle(RenderType renderType, float lineWidth, float red, float green, float blue)
	{
		this(renderType, lineWidth, red, green, blue, 1.0f);
	}
	
	public LineStyle(RenderType renderType, float lineWidth, float red, float green, float blue, float alpha)
	{
		this.lineWidth = lineWidth;
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
		this.renderType = renderType;
	}
	
	/**
	 * Sets the lineWidth and depthFunction based on this style
	 */
	public boolean prepare(RenderType renderType)
	{
		if (this.renderType.matches(renderType))
		{
			 GlStateManager.lineWidth(this.lineWidth);
			//glLineWidth(this.lineWidth);
			//glDepthFunc(this.renderType.depthFunc);
			GlStateManager.depthFunc(this.renderType.depthFunc);
			
			return true;
		}
		
		return false;
	}
	
	public void applyColour()
	{
		GlStateManager.color4f(this.red, this.green, this.blue, this.alpha);
		//glColor4f(this.red, this.green, this.blue, this.alpha);
	}
	
	public void applyColour(float tint)
	{
		GlStateManager.color4f(this.red, this.green, this.blue, this.alpha * tint);
		//glColor4f(this.red, this.green, this.blue, this.alpha * tint);
	}
}
