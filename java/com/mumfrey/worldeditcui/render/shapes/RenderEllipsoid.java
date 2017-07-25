package com.mumfrey.worldeditcui.render.shapes;

import static com.mumfrey.liteloader.gl.GL.*;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.Tessellator;

import com.mumfrey.worldeditcui.render.RenderColour;
import com.mumfrey.worldeditcui.render.LineInfo;
import com.mumfrey.worldeditcui.render.points.PointCube;
import com.mumfrey.worldeditcui.util.Vector3;

/**
 * Draws an ellipsoid shape around a centre point.
 * 
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public class RenderEllipsoid extends RenderRegion
{
	protected PointCube centre;
	protected Vector3 radii;
	protected final static double twoPi = Math.PI * 2;
	protected double centreX;
	protected double centreY;
	protected double centreZ;
	
	public RenderEllipsoid(RenderColour colour, PointCube centre, Vector3 radii)
	{
		super(colour);
		this.centre = centre;
		this.radii = radii;
		this.centreX = centre.getPoint().getX() + 0.5;
		this.centreY = centre.getPoint().getY() + 0.5;
		this.centreZ = centre.getPoint().getZ() + 0.5;
	}
	
	@Override
	public void render(Vector3 cameraPos)
	{
		glPushMatrix();
		glTranslated(this.centreX - cameraPos.getX(), this.centreY - cameraPos.getY(), this.centreZ - cameraPos.getZ());
		
		for (LineInfo tempColour : this.colour.getColours())
		{
			tempColour.prepareRender();
			this.drawXZPlane(tempColour);
			this.drawYZPlane(tempColour);
			this.drawXYPlane(tempColour);
		}
		
		glPopMatrix();
	}
	
	protected void drawXZPlane(LineInfo colour)
	{
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer buf = tessellator.getBuffer();

		int yRad = (int)Math.floor(this.radii.getY());
		for (int yBlock = -yRad; yBlock < yRad; yBlock++)
		{
			buf.begin(GL_LINE_LOOP, VF_POSITION);
			colour.prepareColour();
			
			for (int i = 0; i <= 40; i++)
			{
				double tempTheta = i * twoPi / 40;
				double tempX = this.radii.getX() * Math.cos(tempTheta) * Math.cos(Math.asin(yBlock / this.radii.getY()));
				double tempZ = this.radii.getZ() * Math.sin(tempTheta) * Math.cos(Math.asin(yBlock / this.radii.getY()));
				
				buf.pos(tempX, yBlock, tempZ).endVertex();
			}
			tessellator.draw();
		}
		
		buf.begin(GL_LINE_LOOP, VF_POSITION);
		colour.prepareColour();
		
		for (int i = 0; i <= 40; i++)
		{
			double tempTheta = i * twoPi / 40;
			double tempX = this.radii.getX() * Math.cos(tempTheta);
			double tempZ = this.radii.getZ() * Math.sin(tempTheta);
			
			buf.pos(tempX, 0.0, tempZ).endVertex();
		}
		tessellator.draw();
	}
	
	protected void drawYZPlane(LineInfo colour)
	{
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer buf = tessellator.getBuffer();

		int xRad = (int)Math.floor(this.radii.getX());
		for (int xBlock = -xRad; xBlock < xRad; xBlock++)
		{
			buf.begin(GL_LINE_LOOP, VF_POSITION);
			colour.prepareColour();
			
			for (int i = 0; i <= 40; i++)
			{
				double tempTheta = i * twoPi / 40;
				double tempY = this.radii.getY() * Math.cos(tempTheta) * Math.sin(Math.acos(xBlock / this.radii.getX()));
				double tempZ = this.radii.getZ() * Math.sin(tempTheta) * Math.sin(Math.acos(xBlock / this.radii.getX()));
				
				buf.pos(xBlock, tempY, tempZ).endVertex();
			}
			tessellator.draw();
		}
		
		buf.begin(GL_LINE_LOOP, VF_POSITION);
		colour.prepareColour();
		
		for (int i = 0; i <= 40; i++)
		{
			double tempTheta = i * twoPi / 40;
			double tempY = this.radii.getY() * Math.cos(tempTheta);
			double tempZ = this.radii.getZ() * Math.sin(tempTheta);
			
			buf.pos(0.0, tempY, tempZ).endVertex();
		}
		tessellator.draw();
	}
	
	protected void drawXYPlane(LineInfo colour)
	{
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer buf = tessellator.getBuffer();

		int zRad = (int)Math.floor(this.radii.getZ());
		for (int zBlock = -zRad; zBlock < zRad; zBlock++)
		{
			buf.begin(GL_LINE_LOOP, VF_POSITION);
			colour.prepareColour();
			
			for (int i = 0; i <= 40; i++)
			{
				double tempTheta = i * twoPi / 40;
				double tempX = this.radii.getX() * Math.sin(tempTheta) * Math.sin(Math.acos(zBlock / this.radii.getZ()));
				double tempY = this.radii.getY() * Math.cos(tempTheta) * Math.sin(Math.acos(zBlock / this.radii.getZ()));
				
				buf.pos(tempX, tempY, zBlock).endVertex();
			}
			tessellator.draw();
		}
		
		buf.begin(GL_LINE_LOOP, VF_POSITION);
		colour.prepareColour();
		
		for (int i = 0; i <= 40; i++)
		{
			double tempTheta = i * twoPi / 40;
			double tempX = this.radii.getX() * Math.cos(tempTheta);
			double tempY = this.radii.getY() * Math.sin(tempTheta);
			
			buf.pos(tempX, tempY, 0.0).endVertex();
		}
		tessellator.draw();
	}
}
