package com.mumfrey.worldeditcui.render.shapes;

import java.util.List;

import com.mumfrey.worldeditcui.render.RenderStyle;
import com.mumfrey.worldeditcui.render.LineStyle;
import com.mumfrey.worldeditcui.render.points.PointRectangle;
import com.mumfrey.worldeditcui.util.Vector2;
import com.mumfrey.worldeditcui.util.Vector3;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;

import static org.lwjgl.opengl.GL11.*;

/**
 * Draws the top and bottom rings of a polygon region
 * 
 * @author yetanotherx
 * @author lahwran
 * @author Adam Mummery-Smith
 */
public class Render2DBox extends RenderRegion {
	private List<PointRectangle> points;
	private int min, max;

	public Render2DBox(RenderStyle style, List<PointRectangle> points, int min, int max) {
		super(style);
		this.points = points;
		this.min = min;
		this.max = max;
	}

	@Override
	public void render(Vector3 cameraPos) {
		System.out.println("render Render2DBox");
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buf = tessellator.getBuffer();
		// double off = 0.03 - cameraPos.getY();
		double off = 0.03;
		for (LineStyle line : this.style.getLines()) {
			if (!line.prepare(this.style.getRenderType())) {
				continue;
			}

			// buf.begin(GL_LINES, VF_POSITION);
			buf.begin(GL_LINES, VertexFormats.POSITION);
			line.applyColour();

			for (PointRectangle point : this.points) {
				if (point != null) {
					Vector2 pos = point.getPoint();
					// double x = pos.getX() - cameraPos.getX();
					// double z = pos.getY() - cameraPos.getZ();

					double x = pos.getX();
					double z = pos.getY();
					buf.vertex(x + 0.5, this.min + off, z + 0.5).next();
					buf.vertex(x + 0.5, this.max + 1 + off, z + 0.5).next();
				}
			}
			tessellator.draw();
		}
	}
}
