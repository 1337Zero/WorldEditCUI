package com.mumfrey.worldeditcui.render.shapes;

import com.mumfrey.worldeditcui.render.RenderStyle;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mumfrey.worldeditcui.render.LineStyle;
import com.mumfrey.worldeditcui.render.points.PointCube;
import com.mumfrey.worldeditcui.util.Vector3;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;

/**
 * Draws an ellipsoid shape around a centre point.
 * 
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public class RenderEllipsoid extends RenderRegion {
	protected final static double TAU = Math.PI * 2.0;

	protected PointCube centre;
	protected Vector3 radii;

	protected double centreX, centreY, centreZ;

	public RenderEllipsoid(RenderStyle style, PointCube centre, Vector3 radii) {
		super(style);
		this.centre = centre;
		this.radii = radii;
		this.centreX = centre.getPoint().getX() + 0.5;
		this.centreY = centre.getPoint().getY() + 0.5;
		this.centreZ = centre.getPoint().getZ() + 0.5;
	}

	@Override
	public void render(Vector3 cameraPos) {
		System.out.println("render RenderEllipsoid");
		// glPushMatrix();
		GlStateManager.pushMatrix();
		// glTranslated(this.centreX - cameraPos.getX(), this.centreY -
		// cameraPos.getY(), this.centreZ - cameraPos.getZ());
		// GlStateManager.translated(this.centreX - cameraPos.getX(), this.centreY -
		// cameraPos.getY(), this.centreZ - cameraPos.getZ());
		GlStateManager.translated(this.centreX, this.centreY, this.centreZ);

		for (LineStyle line : this.style.getLines()) {
			if (line.prepare(this.style.getRenderType())) {
				this.drawXZPlane(line);
				this.drawYZPlane(line);
				this.drawXYPlane(line);
			}
		}

		// glPopMatrix();
		GlStateManager.popMatrix();
	}

	protected void drawXZPlane(LineStyle line) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buf = tessellator.getBuffer();

		int yRad = (int) Math.floor(this.radii.getY());
		for (int yBlock = -yRad; yBlock < yRad; yBlock++) {
			buf.begin(0x2, VertexFormats.POSITION);
			line.applyColour();

			for (int i = 0; i <= 40; i++) {
				double tempTheta = i * TAU / 40;
				double tempX = this.radii.getX() * Math.cos(tempTheta)
						* Math.cos(Math.asin(yBlock / this.radii.getY()));
				double tempZ = this.radii.getZ() * Math.sin(tempTheta)
						* Math.cos(Math.asin(yBlock / this.radii.getY()));

				buf.vertex(tempX, yBlock, tempZ).next();
			}
			tessellator.draw();
		}

		buf.begin(0x2, VertexFormats.POSITION);
		line.applyColour();

		for (int i = 0; i <= 40; i++) {
			double tempTheta = i * TAU / 40;
			double tempX = this.radii.getX() * Math.cos(tempTheta);
			double tempZ = this.radii.getZ() * Math.sin(tempTheta);

			buf.vertex(tempX, 0.0, tempZ).next();
		}
		tessellator.draw();
	}

	protected void drawYZPlane(LineStyle line) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buf = tessellator.getBuffer();

		int xRad = (int) Math.floor(this.radii.getX());
		for (int xBlock = -xRad; xBlock < xRad; xBlock++) {
			buf.begin(0x2, VertexFormats.POSITION);
			line.applyColour();

			for (int i = 0; i <= 40; i++) {
				double tempTheta = i * TAU / 40;
				double tempY = this.radii.getY() * Math.cos(tempTheta)
						* Math.sin(Math.acos(xBlock / this.radii.getX()));
				double tempZ = this.radii.getZ() * Math.sin(tempTheta)
						* Math.sin(Math.acos(xBlock / this.radii.getX()));

				buf.vertex(xBlock, tempY, tempZ).next();
			}
			tessellator.draw();
		}

		buf.begin(0x2, VertexFormats.POSITION);
		line.applyColour();

		for (int i = 0; i <= 40; i++) {
			double tempTheta = i * TAU / 40;
			double tempY = this.radii.getY() * Math.cos(tempTheta);
			double tempZ = this.radii.getZ() * Math.sin(tempTheta);

			buf.vertex(0.0, tempY, tempZ).next();
		}
		tessellator.draw();
	}

	protected void drawXYPlane(LineStyle line) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buf = tessellator.getBuffer();

		int zRad = (int) Math.floor(this.radii.getZ());
		for (int zBlock = -zRad; zBlock < zRad; zBlock++) {
			buf.begin(0x2, VertexFormats.POSITION);
			line.applyColour();

			for (int i = 0; i <= 40; i++) {
				double tempTheta = i * TAU / 40;
				double tempX = this.radii.getX() * Math.sin(tempTheta)
						* Math.sin(Math.acos(zBlock / this.radii.getZ()));
				double tempY = this.radii.getY() * Math.cos(tempTheta)
						* Math.sin(Math.acos(zBlock / this.radii.getZ()));

				buf.vertex(tempX, tempY, zBlock).next();
			}
			tessellator.draw();
		}

		buf.begin(0x2, VertexFormats.POSITION);
		line.applyColour();

		for (int i = 0; i <= 40; i++) {
			double tempTheta = i * TAU / 40;
			double tempX = this.radii.getX() * Math.cos(tempTheta);
			double tempY = this.radii.getY() * Math.sin(tempTheta);

			buf.vertex(tempX, tempY, 0.0).next();
		}
		tessellator.draw();
	}
}
