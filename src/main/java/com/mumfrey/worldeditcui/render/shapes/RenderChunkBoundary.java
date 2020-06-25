package com.mumfrey.worldeditcui.render.shapes;

import net.fabricmc.fabric.api.dimension.v1.EntityPlacer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.chunk.Chunk;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mumfrey.worldeditcui.render.LineStyle;
import com.mumfrey.worldeditcui.render.RenderStyle;
import com.mumfrey.worldeditcui.util.Vector3;

public class RenderChunkBoundary extends RenderRegion {
	private final MinecraftClient mc;
	private Render3DGrid grid;

	public RenderChunkBoundary(RenderStyle boundaryStyle, RenderStyle gridStyle, MinecraftClient minecraft) {
		super(boundaryStyle);

		this.mc = minecraft;

		this.grid = new Render3DGrid(gridStyle, Vector3.ZERO, Vector3.ZERO);
		this.grid.setSpacing(4.0);
	}

	@Override
	public void render(Vector3 cameraPos) {
		double yMax = this.mc.world != null ? this.mc.world.getHeight() : 256.0;
		double yMin = 0.0;

		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		
		Chunk ch = player.getEntityWorld().getChunk(player.getBlockPos());
		
		this.grid.setPosition(new Vector3(ch.getPos().getStartX(), yMin, ch.getPos().getStartZ()), new Vector3(ch.getPos().getEndX()+1, yMax, ch.getPos().getEndZ()+1));
		
		GlStateManager.pushMatrix();
		
		
		this.grid.render(Vector3.ZERO);
		
		
		//this.renderChunkBorder(yMin, yMax, ch.getPos().getEndX(), ch.getPos().getEndZ());

		
		if (this.mc.world != null) {
			//this.renderChunkBoundary(xChunk, zChunk, xBase, zBase);
		}
		// glPopMatrix();
		GlStateManager.popMatrix();
	}

	private void renderChunkBorder(double yMin, double yMax, double xBase, double zBase) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buf = tessellator.getBuffer();
		int spacing = 16;
		for (LineStyle line : this.style.getLines()) {
			if (line.prepare(this.style.getRenderType())) {
				buf.begin(0x1, VertexFormats.POSITION);
				line.applyColour();
				//for (int x = -16; x <= 32; x += spacing) {
				//3 mal ?
				for (int x = -16; x <= 32; x += spacing) {
					//3 mal ?
					for (int z = -16; z <= 32; z += spacing) {
						// buf.vertex(xBase + x, yMin, zBase - z);
						buf.vertex(xBase + x, yMin, zBase - z).next();

						// buf.vertex(xBase + x, yMax, zBase - z);
						buf.vertex(xBase + x, yMax, zBase - z).next();
						// buf.end(); ? needed
					}
				}
			/*	for (double y = yMin; y <= yMax; y += yMax) {
					buf.vertex(xBase, y, zBase).next();
					buf.vertex(xBase, y, zBase - 16).next();
					buf.vertex(xBase, y, zBase - 16).next();
					buf.vertex(xBase + 16, y, zBase - 16).next();
					buf.vertex(xBase + 16, y, zBase - 16).next();
					buf.vertex(xBase + 16, y, zBase).next();
					buf.vertex(xBase + 16, y, zBase).next();
					buf.vertex(xBase, y, zBase).next();
				}*/
				tessellator.draw();
			}
		}
	}

	private void renderChunkBoundary(int xChunk, int zChunk, double xBase, double zBase) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buf = tessellator.getBuffer();

		// Chunk chunk = this.mc.world.getChunkFromChunkCoords(xChunk, zChunk);
		Chunk chunk = this.mc.world.getChunk(xChunk, zChunk);

		for (LineStyle line : this.style.getLines()) {
			if (line.prepare(this.style.getRenderType())) {
				buf.begin(0x1, VertexFormats.POSITION);
				line.applyColour();

				int[][] lastHeight = { { -1, -1 }, { -1, -1 } };
				for (int i = 0, height = 0; i < 16; i++) {
					for (int j = 0; j < 2; j++) {
						for (int axis = 0; axis < 2; axis++) {
							// height = axis == 0 ? chunk.getHeightValue(j * 15, i) :
							// chunk.getHeightValue(i, j * 15);
							// hunk.getWorld().getHeight(p_getHeight_1_, p_getHeight_2_, p_getHeight_3_)

							height = axis == 0 ? chunk.getHeightmap(Type.WORLD_SURFACE).get(j * 15, i)
									: chunk.getHeightmap(Type.WORLD_SURFACE).get(i, j * 15);
							// height = axis == 0 ? chunk.getWorld().getHeight(Type.WORLD_SURFACE, j * 15,
							// i) :chunk.getWorld().getHeight(Type.WORLD_SURFACE,i, j * 15);
							double xPos = axis == 0 ? xBase + (j * 16) : xBase + i;
							double zPos = axis == 0 ? zBase - 16 + i : zBase - 16 + (j * 16);
							if (lastHeight[axis][j] > -1 && height != lastHeight[axis][j]) {
								buf.vertex(xPos, lastHeight[axis][j], zPos).next();
								buf.vertex(xPos, height, zPos).next();
							}
							buf.vertex(xPos, height, zPos).next();
							buf.vertex(xPos + axis, height, zPos + (1 - axis)).next();
							lastHeight[axis][j] = height;
						}
					}
				}

				tessellator.draw();
			}
		}
	}

}
