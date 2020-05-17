package com.mumfrey.worldeditcui.render.shapes;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mumfrey.worldeditcui.render.LineStyle;
import com.mumfrey.worldeditcui.render.RenderStyle;
import com.mumfrey.worldeditcui.util.BoundingBox;
import com.mumfrey.worldeditcui.util.Observable;
import com.mumfrey.worldeditcui.util.Vector3;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;

/**
 * Draws the grid for a region between two corners in a cuboid region.
 * 
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public class Render3DGrid extends RenderRegion {
	private static final float CULL_RANGE = 128.0F;
	public static final double MIN_SPACING = 1.0;

	private Vector3 first, second;
	private double spacing = 1.0;

	public Render3DGrid(RenderStyle style, BoundingBox region) {
		this(style, region.getMin(), region.getMax());
		if (region.isDynamic()) {
			region.addObserver(this);
		}
	}

	public Render3DGrid(RenderStyle style, Vector3 first, Vector3 second) {
		super(style);
		this.first = first;
		this.second = second;
	}

	@Override
	public void notifyChanged(Observable<?> source) {
		this.setPosition((BoundingBox) source);
	}

	public void setPosition(BoundingBox region) {
		this.setPosition(region.getMin(), region.getMax());
	}

	public void setPosition(Vector3 first, Vector3 second) {
		this.first = first;
		this.second = second;
	}

	public Render3DGrid setSpacing(double spacing) {
		this.spacing = spacing;
		return this;
	}

	@Override
	public void render(Vector3 cameraPos) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buf = tessellator.getBuffer();
		
		 double x1 = this.first.getX() - cameraPos.getX(); 
		 double y1 = this.first.getY() - cameraPos.getY(); 
		 double z1 = this.first.getZ() - cameraPos.getZ(); 
		 double x2 = this.second.getX() - cameraPos.getX(); 
		 double y2 = this.second.getY() - cameraPos.getY(); 
		 double z2 = this.second.getZ() - cameraPos.getZ();
		
		if (this.spacing != 1.0) {
			// glDisableCulling();
			GlStateManager.disableCull();

			double[] vertices = { x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, // bottom
					x1, y2, z1, x2, y2, z1, x2, y2, z2, x1, y2, z2, // top
					x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, // west
					x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, // east
					x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1, // north
					x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2 // south
			};

			for (LineStyle line : this.style.getLines()) {
				if (line.prepare(this.style.getRenderType())) {
					buf.begin(0x7, VertexFormats.POSITION);
					line.applyColour(0.25F);
					for (int i = 0; i < vertices.length; i += 3) {
						buf.vertex(vertices[i], vertices[i + 1], vertices[i + 2]).next();
					}
					tessellator.draw();
				}
			}

			// glEnableCulling();
			GlStateManager.enableCull();
		}

		if (this.spacing < Render3DGrid.MIN_SPACING) {
			return;
		}

		double cullAt = Render3DGrid.CULL_RANGE * this.spacing;
		for (LineStyle line : this.style.getLines()) {
			if (!line.prepare(this.style.getRenderType())) {
				continue;
			}

			buf.begin(0x1, VertexFormats.POSITION);
			line.applyColour();

			for (double y = Math.max(y1, -cullAt); y <= y2 && y <= cullAt; y += this.spacing) {
				buf.vertex(x1, y, z2).next();
				buf.vertex(x2, y, z2).next();
				buf.vertex(x1, y, z1).next();
				buf.vertex(x2, y, z1).next();
				buf.vertex(x1, y, z1).next();
				buf.vertex(x1, y, z2).next();
				buf.vertex(x2, y, z1).next();
				buf.vertex(x2, y, z2).next();
			}

			for (double x = Math.max(x1, -cullAt); x <= x2 && x <= cullAt; x += this.spacing) {
				buf.vertex(x, y1, z1).next();
				buf.vertex(x, y2, z1).next();
				buf.vertex(x, y1, z2).next();
				buf.vertex(x, y2, z2).next();
				buf.vertex(x, y2, z1).next();
				buf.vertex(x, y2, z2).next();
				buf.vertex(x, y1, z1).next();
				buf.vertex(x, y1, z2).next();
			}

			for (double z = Math.max(z1, -cullAt); z <= z2 && z <= cullAt; z += this.spacing) {
				buf.vertex(x1, y1, z).next();
				buf.vertex(x2, y1, z).next();
				buf.vertex(x1, y2, z).next();
				buf.vertex(x2, y2, z).next();
				buf.vertex(x2, y1, z).next();
				buf.vertex(x2, y2, z).next();
				buf.vertex(x1, y1, z).next();
				buf.vertex(x1, y2, z).next();
			}

			tessellator.draw();
		}
	}
}
