package com.mumfrey.worldeditcui;

import java.util.Iterator;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mumfrey.worldeditcui.render.RenderHelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

/**
 * 
 * @author 1337Zero
 * 
 * This marks a Block in a specific position
 * draw needs to be called from a render-friendly state
 *
 */
public class BlockMark  {

	private int x,y,z;
	private MinecraftClient minecraft;
	private float r,g,b,alpha;
	
	public BlockMark(int x,int y , int z,MinecraftClient minecraft,float r,float g,float b,float alpha) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.minecraft = minecraft;
		this.r =r;
		this.g = g;
		this.b = b;
		this.alpha = alpha;
	}
	
	public void draw(float partialTicks) {		
		RenderHelper.setUpRenderer(partialTicks,MinecraftClient.getInstance().player);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		
	    BlockPos blockpos = new BlockPos(x,y-2,z);
	    Box axis = new Box(blockpos);
	    buffer.begin(3, VertexFormats.POSITION_COLOR);
	    
	    buffer.vertex(axis.minX, axis.minY, axis.minZ).color(r, g, b, alpha).next();
		buffer.vertex(axis.maxX, axis.minY, axis.minZ).color(r, g, b, alpha).next();
	    tessellator.draw();
	
	    buffer.begin(3, VertexFormats.POSITION_COLOR);
		buffer.vertex(axis.minX, axis.minY, axis.minZ).color(r, g, b, alpha).next();
		buffer.vertex(axis.minX, axis.minY, axis.maxZ).color(r, g, b, alpha).next();	
	    tessellator.draw();
	    
	    buffer.begin(3, VertexFormats.POSITION_COLOR);
		buffer.vertex(axis.maxX, axis.minY, axis.maxZ).color(r, g, b, alpha).next();
		buffer.vertex(axis.maxX, axis.minY, axis.minZ).color(r, g, b, alpha).next();	
	    tessellator.draw();
	    
	    buffer.begin(3, VertexFormats.POSITION_COLOR);
		buffer.vertex(axis.maxX, axis.minY, axis.maxZ).color(r, g, b, alpha).next();
		buffer.vertex(axis.minX, axis.minY, axis.maxZ).color(r, g, b, alpha).next();	
	    tessellator.draw();
	    	    
	    buffer.begin(3, VertexFormats.POSITION_COLOR);
		buffer.vertex(axis.minX, axis.maxY, axis.minZ).color(r, g, b, alpha).next();
		buffer.vertex(axis.maxX, axis.maxY, axis.minZ).color(r, g, b, alpha).next();	
	    tessellator.draw();
	    
	    buffer.begin(3, VertexFormats.POSITION_COLOR);
		buffer.vertex(axis.minX, axis.maxY, axis.minZ).color(r, g, b, alpha).next();
		buffer.vertex(axis.minX, axis.maxY, axis.maxZ).color(r, g, b, alpha).next();	
	    tessellator.draw();
	    
	    buffer.begin(3, VertexFormats.POSITION_COLOR);
		buffer.vertex(axis.maxX, axis.maxY, axis.maxZ).color(r, g, b, alpha).next();
		buffer.vertex(axis.maxX, axis.maxY, axis.minZ).color(r, g, b, alpha).next();	
	    tessellator.draw();
	    
	    buffer.begin(3, VertexFormats.POSITION_COLOR);
		buffer.vertex(axis.maxX, axis.maxY, axis.maxZ).color(r, g, b, alpha).next();
		buffer.vertex(axis.minX, axis.maxY, axis.maxZ).color(r, g, b, alpha).next();	
	    tessellator.draw();
	    	    
	    buffer.begin(3, VertexFormats.POSITION_COLOR);
		buffer.vertex(axis.minX, axis.maxY, axis.minZ).color(r, g, b, alpha).next();
		buffer.vertex(axis.minX, axis.minY, axis.minZ).color(r, g, b, alpha).next();
	    tessellator.draw();
	    
	    buffer.begin(3, VertexFormats.POSITION_COLOR);
		buffer.vertex(axis.maxX, axis.maxY, axis.maxZ).color(r, g, b, alpha).next();
		buffer.vertex(axis.maxX, axis.minY, axis.maxZ).color(r, g, b, alpha).next();
	    tessellator.draw();	
	    
	    buffer.begin(3, VertexFormats.POSITION_COLOR);
		buffer.vertex(axis.maxX, axis.maxY, axis.minZ).color(r, g, b, alpha).next();
		buffer.vertex(axis.maxX, axis.minY, axis.minZ).color(r, g, b, alpha).next();
	    tessellator.draw();	
	    
	    buffer.begin(3, VertexFormats.POSITION_COLOR);
		buffer.vertex(axis.minX, axis.maxY, axis.maxZ).color(r, g, b, alpha).next();
		buffer.vertex(axis.minX, axis.minY, axis.maxZ).color(r, g, b, alpha).next();
	    tessellator.draw();	
	    
	    RenderHelper.normalizeRenderer();		
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}

}
