package com.mumfrey.worldeditcui.render.shapes;

import static com.mumfrey.liteloader.gl.GL.*;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.Tessellator;

import com.mumfrey.worldeditcui.render.RenderColour;
import com.mumfrey.worldeditcui.render.LineInfo;
import com.mumfrey.worldeditcui.render.points.PointCube;
import com.mumfrey.worldeditcui.util.Vector3;

/**
 * Draws the top and bottom circles around a cylindrical region
 * 
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public class RenderCylinderBox extends RenderRegion
{
	private double radX, radZ;
	private int minY, maxY;
	private double centreX, centreZ;
	
	public RenderCylinderBox(RenderColour colour, PointCube centre, double radX, double radZ, int minY, int maxY)
	{
		super(colour);
		this.radX = radX;
		this.radZ = radZ;
		this.minY = minY;
		this.maxY = maxY;
		this.centreX = centre.getPoint().getX() + 0.5;
		this.centreZ = centre.getPoint().getZ() + 0.5;
	}
	
	@Override
	public void render(Vector3 cameraPos)
	{
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer buf = tessellator.getBuffer();

		double xPos = this.centreX - cameraPos.getX();
		double zPos = this.centreZ - cameraPos.getZ();

		for (LineInfo tempColour : this.colour.getColours())
		{
			tempColour.prepareRender();
			
			double twoPi = Math.PI * 2;
			for (int yBlock : new int[] { this.minY, this.maxY + 1 })
			{
				buf.begin(GL_LINE_LOOP, VF_POSITION);
				tempColour.prepareColour();
				
				for (int i = 0; i <= 75; i++)
				{
					double tempTheta = i * twoPi / 75;
					double tempX = this.radX * Math.cos(tempTheta);
					double tempZ = this.radZ * Math.sin(tempTheta);
					
					buf.pos(xPos + tempX, yBlock - cameraPos.getY(), zPos + tempZ).endVertex();
				}
				tessellator.draw();
			}
		}
	}
}
