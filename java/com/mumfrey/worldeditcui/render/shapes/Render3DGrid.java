package com.mumfrey.worldeditcui.render.shapes;

import static com.mumfrey.liteloader.gl.GL.*;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.Tessellator;

import com.mumfrey.worldeditcui.render.LineInfo;
import com.mumfrey.worldeditcui.render.RenderColour;
import com.mumfrey.worldeditcui.util.BoundingBox;
import com.mumfrey.worldeditcui.util.Observable;
import com.mumfrey.worldeditcui.util.Vector3;

/**
 * Draws the grid for a region between
 * two corners in a cuboid region.
 * 
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public class Render3DGrid extends RenderRegion
{
	public static final double MIN_SPACING = 1.0;
	
	private Vector3 first, second;
	private double spacing = 1.0;
	
	public Render3DGrid(RenderColour colour, BoundingBox region)
	{
		this(colour, region.getMin(), region.getMax());
		if (region.isDynamic())
		{
			region.addObserver(this);
		}
	}
	
	public Render3DGrid(RenderColour colour, Vector3 first, Vector3 second)
	{
		super(colour);
		this.first = first;
		this.second = second;
	}
	
	@Override
	public void notifyChanged(Observable<?> source)
	{
		this.setPosition((BoundingBox)source);
	}

	public void setPosition(BoundingBox region)
	{
		this.setPosition(region.getMin(), region.getMax());
	}
	
	public void setPosition(Vector3 first, Vector3 second)
	{
		this.first = first;
		this.second = second;
	}
	
	public Render3DGrid setSpacing(double spacing)
	{
		this.spacing = spacing;
		return this;
	}
	
	@Override
	public void render(Vector3 cameraPos)
	{
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer buf = tessellator.getBuffer();
		double x1 = this.first.getX() - cameraPos.getX();
		double y1 = this.first.getY() - cameraPos.getY();
		double z1 = this.first.getZ() - cameraPos.getZ();
		double x2 = this.second.getX() - cameraPos.getX();
		double y2 = this.second.getY() - cameraPos.getY();
		double z2 = this.second.getZ() - cameraPos.getZ();
		
		if (this.spacing != 1.0)
		{
			glDisableCulling();
			
			double[] vertices = {
					x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, // bottom
					x1, y2, z1, x2, y2, z1, x2, y2, z2, x1, y2, z2, // top
					x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, // west
					x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, // east
					x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1, // north
					x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2  // south
			};
			
			for (LineInfo tempColour : this.colour.getColours())
			{
				buf.begin(GL_QUADS, VF_POSITION);
				tempColour.prepareRender();
				tempColour.prepareColour(0.25F);
				for (int i = 0; i < vertices.length; i += 3)
				{
					buf.pos(vertices[i], vertices[i + 1], vertices[i + 2]).endVertex();
				}
				tessellator.draw();
			}
			
			glEnableCulling();
		}
		
		if (this.spacing < Render3DGrid.MIN_SPACING)
		{
			return;
		}
		
		double cullAt = 128.0F;
		for (LineInfo tempColour : this.colour.getColours())
		{
			tempColour.prepareRender();
			
			buf.begin(GL_LINES, VF_POSITION);
			tempColour.prepareColour();
			
			for (double yoff = 0; yoff + y1 <= y2; yoff += this.spacing)
			{
				double y = y1 + yoff;
				buf.pos(x1, y, z2).endVertex();
				buf.pos(x2, y, z2).endVertex();
				buf.pos(x1, y, z1).endVertex();
				buf.pos(x2, y, z1).endVertex();
				buf.pos(x1, y, z1).endVertex();
				buf.pos(x1, y, z2).endVertex();
				buf.pos(x2, y, z1).endVertex();
				buf.pos(x2, y, z2).endVertex();
			}
			
			for (double xoff = 0; xoff + x1 <= x2; xoff += this.spacing)
			{
				double x = x1 + xoff;
//				boolean major = xoff % 10 == 0;
				if (x < -cullAt) continue;
				if (x > cullAt) break;
				buf.pos(x, y1, z1).endVertex();
				buf.pos(x, y2, z1).endVertex();
				buf.pos(x, y1, z2).endVertex();
				buf.pos(x, y2, z2).endVertex();
				buf.pos(x, y2, z1).endVertex();
				buf.pos(x, y2, z2).endVertex();
				buf.pos(x, y1, z1).endVertex();
				buf.pos(x, y1, z2).endVertex();
			}
			
			for (double zoff = 0; zoff + z1 <= z2; zoff += this.spacing)
			{
				double z = z1 + zoff;
//				boolean major = zoff % 10 == 0;
				if (z < -cullAt) continue;
				if (z > cullAt) break;
				buf.pos(x1, y1, z).endVertex();
				buf.pos(x2, y1, z).endVertex();
				buf.pos(x1, y2, z).endVertex();
				buf.pos(x2, y2, z).endVertex();
				buf.pos(x2, y1, z).endVertex();
				buf.pos(x2, y2, z).endVertex();
				buf.pos(x1, y1, z).endVertex();
				buf.pos(x1, y2, z).endVertex();
			}
			
			tessellator.draw();
		}
	}
}
