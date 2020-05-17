package com.mumfrey.worldeditcui.gui.controls;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mumfrey.worldeditcui.render.ConfiguredColour;
import com.mumfrey.worldeditcui.render.RenderHelper;

import net.minecraft.client.MinecraftClient;

/**
 * Colour picker button control, spawns a style picker when clicked
 * 
 * @author Adam Mummery-Smith
 */
public class GuiColourButton extends GuiControl {
	/**
	 * Picker active colour
	 */
	private int colour = 0xFF000000;

	private ConfiguredColour lineColour;

	private GuiColourPicker picker;

	private boolean pickerClicked = false;

	public GuiColourButton(MinecraftClient minecraft, int id, int xPosition, int yPosition, int controlWidth,int controlHeight, ConfiguredColour lineColour) {
		super(minecraft, id, xPosition, yPosition, controlWidth, controlHeight, lineColour.getDisplayName(),(onpress) ->{System.out.println("GuiColourButton pressed");});
		
		
		this.lineColour = lineColour;
		this.updateColour(lineColour);
	}

	/**
	 * @param lineColour
	 */
	public void updateColour(ConfiguredColour lineColour) {
		if (lineColour == this.lineColour) {
			this.colour = lineColour.getColourIntARGB();
		}
	}

	public int getColour() {
		return this.colour;
	}

	public void save() {
		this.lineColour.setColourIntRGBA(this.colour);
	}

	
	
	@Override
	public void renderButton(int mouseX, int mouseY, float partialTicks) {
		MinecraftClient minecraft = MinecraftClient.getInstance();
		if (this.visible) {
			boolean mouseOver = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width&& mouseY < this.y + this.height;
			int borderColour = mouseOver || this.picker != null ? 0xFFFFFFFF : 0xFFA0A0A0;			
			RenderHelper.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, borderColour);
			int v = Math.min(Math.max((int) (((float) this.height / (float) this.width) * 1024F), 256), 1024);
			minecraft.getTextureManager().bindTexture(GuiColourPicker.COLOURPICKER_CHECKER);			
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.drawTexturedModalRect(this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1, 0, 0,1024, v);
			RenderHelper.drawRect(this.x + 1, this.y + 1, this.x + this.width - 1, this.y + this.height - 1, this.colour);
			if (this.getMessage() != null && this.getMessage().length() > 0) {
				this.drawString(minecraft.textRenderer, this.getMessage(), this.x + this.width + 8,this.y + (this.height - 8) / 2, 0xFFFFFFFF);
			}
		}
	}

	public void drawPicker(MinecraftClient minecraft, int mouseX, int mouseY, float partialTicks) {
		if (this.visible && this.picker != null) {
			this.picker.render(mouseX, mouseY, partialTicks);
			//this.picker.drawButton(minecraft, mouseX, mouseY, partialTicks);

			if (this.picker.getDialogResult() == DialogResult.OK) {
				this.closePicker(true);
			} else if (this.picker.getDialogResult() == DialogResult.Cancel) {
				this.closePicker(false);
			}
		}
	}

	/**
	 * 
	 */
	public void closePicker(boolean getColour) {
		if (getColour)
			this.colour = this.picker.getColour();
		this.picker = null;
		this.pickerClicked = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiButton#mouseReleased(int, int)
	 */
	@Override
	public boolean mouseReleased(double mouseX, double mouseY,int button) {
		if (this.pickerClicked && this.picker != null) {
			this.pickerClicked = false;
			return this.picker.mouseReleased(mouseX, mouseY,button);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.minecraft.src.GuiButton#mousePressed(net.minecraft.src.MinecraftClient,
	 * int, int)
	 */
	@Override
	public boolean mouseClicked(double mouseX, double mouseY,int button) {
		boolean pressed = super.mouseClicked(mouseX, mouseY, button);
		if (this.picker == null) {
			if (pressed) {
				int xPos = Math.min(this.x + this.width, GuiControl.lastScreenWidth - 233);
				//int yPos = Math.min(this.y, GuiControl.lastScreenHeight - 175);

				this.picker = new GuiColourPicker(MinecraftClient.getInstance(), 1, xPos, 50, this.colour, "Choose colour");
				this.pickerClicked = false;
			}

			return pressed;
		}
		this.pickerClicked = this.picker.mouseClicked(mouseX, mouseY,button);

		if (pressed && !this.pickerClicked) {
			this.closePicker(true);
		}

		return this.pickerClicked;
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if(this.picker != null) {
			this.picker.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		}		
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}
	
	@Override
	public boolean keyPressed(int keyChar, int keyCode,int modifiers) {
		//return (this.picker != null) ? this.picker.textBoxKeyTyped(keyChar, keyCode) : false;
		return (this.picker != null) ? this.picker.keyPressed(keyChar, keyCode, modifiers): false;
	}
	public boolean isPickerShown() {
		return this.picker != null;
	}
}