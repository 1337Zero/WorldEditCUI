package com.mumfrey.worldeditcui.gui.controls;

import java.awt.Color;
import java.awt.Rectangle;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mumfrey.worldeditcui.LiteModWorldEditCUI;
import com.mumfrey.worldeditcui.config.CUIConfiguration;
import com.mumfrey.worldeditcui.render.RenderHelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;


/**
 * Colour picker flyout control, for use with the designable GUI properties
 * window
 *
 * @author Adam Mummery-Smith
 */
public class GuiColourPicker extends GuiControl{
	//public static final ResourceLocation COLOURPICKER_CHECKER = new ResourceLocation("wecui","textures/gui/checker.png");
	public static final Identifier COLOURPICKER_CHECKER = new Identifier("wecui","textures/gui/checker.png");
	//public static final ResourceLocation COLOURPICKER_PICKER = new ResourceLocation("wecui", "textures/gui/picker.png");
	public static final Identifier COLOURPICKER_PICKER = new Identifier("wecui", "textures/gui/picker.png");

	/**
	 * Indices into the hsb array
	 */
	private static final int H = 0, S = 1, B = 2;

	/**
	 * HSB values from Colour.RGBtoHSB, combined with opacity this is the
	 * authoritative version of the colour we are editing
	 */
	private float[] hsb;

	/**
	 * Original and altered RGB values
	 */
	private int rgb;

	/**
	 * Current opacity, stored as an offset byte in the usual position, eg. 0xFF <<
	 * 24
	 */
	private int opacity;

	/**
	 * Text boxes for manual entry
	 */
	
	private TextFieldWidget txtRed, txtGreen, txtBlue, txtAlpha;

	/**
	 * OK and cancel buttons
	 */
	private ButtonWidget btnOk, btnCancel;

	/**
	 * Flags to track whether dragging a slider
	 */
	private boolean draggingHS, draggingB, draggingA;

	/**
	 * Slider rects
	 */
	private Rectangle rectHSArea, rectBArea, rectAArea;

	/**
	 * Set when the user clicks ok or cancel
	 */
	private DialogResult result = DialogResult.None;

	private TextRenderer fontRenderer;

	public GuiColourPicker(MinecraftClient minecraft, int controlId, int xPos, int yPos, int initialColour,String displayText) {
		super(minecraft, controlId, xPos, yPos, 231, 173, displayText, (onpress)->{System.out.println("GuiColourPicker Pressed");});

		CUIConfiguration config = LiteModWorldEditCUI.instance.getController().getConfiguration();
		Color colour = new Color(initialColour);
		this.hsb = Color.RGBtoHSB(colour.getRed(), colour.getGreen(), colour.getBlue(), null);
		this.opacity = initialColour & 0xFF000000;
		if (this.opacity == 0x01000000)
			this.opacity = 0;

		this.fontRenderer = minecraft.textRenderer;
		//new TextFieldWidget(textrenderer,x,y,width,message)
		this.txtRed = new TextFieldWidget(this.fontRenderer, this.x + 188, this.y + 10, 32, 16, new LiteralText("red"));
		this.txtGreen = new TextFieldWidget(this.fontRenderer, this.x + 188, this.y + 30, 32, 16, new LiteralText("green"));
		this.txtBlue = new TextFieldWidget(this.fontRenderer, this.x + 188, this.y + 50, 32, 16, new LiteralText("blue"));
		this.txtAlpha = new TextFieldWidget(this.fontRenderer, this.x + 188, this.y + 70, 32, 16, new LiteralText("alpha"));
		
		this.txtRed.setMaxLength(3);
		this.txtGreen.setMaxLength(3);
		this.txtBlue.setMaxLength(3);
		this.txtAlpha.setMaxLength(3);

		this.rectHSArea = new Rectangle(this.x + 10, this.y + 10, 128, 128);
		this.rectBArea = new Rectangle(this.x + 143, this.y + 10, 15, 128);
		this.rectAArea = new Rectangle(this.x + 163, this.y + 10, 15, 128);

		//this.btnOk = new GuiControl(minecraft, 0, this.x + 9, this.y + 145, 55, 20, I18n.translate("gui.ok"),(onpress)-> {System.out.println("ok pressed");});
		this.btnOk = new ButtonWidget(this.x + 9, this.y + 145, 55, 20, new LiteralText(config.getMessage_gui_ok()),(onpress)-> {System.out.println("ok pressed");});
		//this.btnCancel = new GuiControl(minecraft, 1, this.x + 70, this.y + 145, 65, 20, I18n.translate("gui.cancel"),(onpress)-> {System.out.println("ok pressed");});
		this.btnCancel = new ButtonWidget(this.x + 70, this.y + 145, 65, 20, new LiteralText(config.getMessage_gui_cancel()),(onpress)-> {System.out.println("ok pressed");});
		
		
		
		this.updateColour();
	}

	public DialogResult getDialogResult() {
		return this.result;
	}

	public int getColour() {
		int opacity = this.opacity == 0 ? 0x01000000 : this.opacity;
		int rgb = opacity | (0xFFFFFF & Color.HSBtoRGB(this.hsb[H], this.hsb[S], this.hsb[B]));
		return rgb;
	}
	
	@Override
	public void renderButton(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
		//this.mouseDragged(minecraft, mouseX, mouseY);
		
		// Calculate coordinates for the selectors
		int hPos = this.x + 10 + (int) (128F * this.hsb[H]);
		int sPos = this.y + 10 + (128 - (int) (128F * this.hsb[S]));
		int bPos = this.y + 10 + (128 - (int) (128F * this.hsb[B]));
		int aPos = this.y + 10 + ((256 - ((this.opacity >> 24) & 0xFF)) / 2);

		// Calculate B colour
		int brightness = Color.HSBtoRGB(this.hsb[H], this.hsb[S], 1.0F) | 0xFF000000;

		// Draw backgrounds
		RenderHelper.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, 0xAA000000); // Background
		RenderHelper.drawRect(this.x + 9, this.y + 9, this.x + 139, this.y + 139, 0xFFA0A0A0); // HS background
		RenderHelper.drawRect(this.x + 142, this.y + 9, this.x + 159, this.y + 139, 0xFFA0A0A0); // B background
		RenderHelper.drawRect(this.x + 162, this.y + 9, this.x + 179, this.y + 139, 0xFFA0A0A0); // A background
		RenderHelper.drawRect(this.x + 187, this.y + 105, this.x + 221, this.y + 139, 0xFFA0A0A0); // Preview background

		// Draw colour picker
		this.mc.getTextureManager().bindTexture(GuiColourPicker.COLOURPICKER_PICKER);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.drawTexturedModalRect(this.x + 10, this.y + 10, this.x + 138, this.y + 138, 0, 0, 256, 256);
		this.drawCrossHair(hPos, sPos, 5, 1, 0xFF000000);

		// Draw brightness bar
		RenderHelper.drawGradientRect(this.x + 143, this.y + 10, this.x + 158, this.y + 138, brightness, 0xFF000000);
		this.drawRotText(matrix, this.fontRenderer, "Luminosity", this.x + 150, this.y + 74, 0xFF000000, false);
		RenderHelper.drawRect(this.x + 142, bPos - 1, this.x + 159, bPos + 1, 0xFFFFFFFF);

		// Draw opacity bar
		RenderHelper.drawGradientRect(this.x + 163, this.y + 10, this.x + 178, this.y + 138, 0xFFFFFFFF, 0xFF000000);
		this.drawRotText(matrix, this.fontRenderer, "Opacity", this.x + 170, this.y + 74, 0xFF000000, false);
		RenderHelper.drawRect(this.x + 162, aPos - 1, this.x + 179, aPos + 1, 0xFFFFFFFF);

		// Draw preview
		this.mc.getTextureManager().bindTexture(GuiColourPicker.COLOURPICKER_CHECKER);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		//glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.drawTexturedModalRect(this.x + 188, this.y + 106, this.x + 220, this.y + 138, 0, 0, 1024, 1024);
		RenderHelper.drawRect(this.x + 188, this.y + 106, this.x + 220, this.y + 138, this.rgb);

		
		// Draw text boxes
		this.txtRed.render(matrix,this.txtRed.x, this.txtRed.y, MinecraftClient.getInstance().getTickDelta());
		//this.txtRed.drawTextBox();
		this.txtGreen.render(matrix, this.txtGreen.x, this.txtGreen.y, MinecraftClient.getInstance().getTickDelta());
		//this.txtGreen.drawTextBox();
		this.txtBlue.render(matrix, this.txtBlue.x, this.txtBlue.y, MinecraftClient.getInstance().getTickDelta());
		//this.txtBlue.drawTextBox();
		this.txtAlpha.render(matrix, this.txtAlpha.x, this.txtAlpha.y, MinecraftClient.getInstance().getTickDelta());
		//this.txtAlpha.drawTextBox();
		
		this.btnOk.renderButton(matrix, btnOk.x, btnOk.y, MinecraftClient.getInstance().getTickDelta());		
		//this.btnOk.drawButton(minecraft, mouseX, mouseY, partialTicks);
		this.btnCancel.renderButton(matrix, btnCancel.x, btnCancel.y, MinecraftClient.getInstance().getTickDelta());	
		//this.btnCancel.drawButton(minecraft, mouseX, mouseY, partialTicks);
	}

	/*public void updateCursorCounter() {
		this.txtRed.
		this.txtRed.updateCursorCounter();
		this.txtGreen.updateCursorCounter();
		this.txtBlue.updateCursorCounter();
		this.txtAlpha.updateCursorCounter();
	}*/

	protected void updateColour() {
		this.rgb = this.opacity | (0xFFFFFF & Color.HSBtoRGB(this.hsb[H], this.hsb[S], this.hsb[B]));
		this.txtRed.setText(String.valueOf((this.rgb >> 16) & 0xFF));
		this.txtGreen.setText(String.valueOf((this.rgb >> 8) & 0xFF));
		this.txtBlue.setText(String.valueOf(this.rgb & 0xFF));
		this.txtAlpha.setText(String.valueOf((this.opacity >> 24) & 0xFF));
	}

	protected void updateColourFromTextEntry() {
		int currentRed = (this.rgb >> 16) & 0xFF;
		int currentGreen = (this.rgb >> 8) & 0xFF;
		int currentBlue = this.rgb & 0xFF;
		int currentOpacity = (this.opacity >> 24) & 0xFF;

		currentRed = (int) clamp(this.tryParseInt(this.txtRed.getText(), currentRed), 0, 255);
		currentGreen = (int) clamp(this.tryParseInt(this.txtGreen.getText(), currentGreen), 0, 255);
		currentBlue = (int) clamp(this.tryParseInt(this.txtBlue.getText(), currentBlue), 0, 255);
		currentOpacity = (int) clamp(this.tryParseInt(this.txtAlpha.getText(), currentOpacity), 0, 255);

		this.hsb = Color.RGBtoHSB(currentRed, currentGreen, currentBlue, null);
		this.opacity = (currentOpacity << 24) & 0xFF000000;
		this.updateColour();
	}

	protected int tryParseInt(String text, int defaultValue) {
		try {
			return Integer.parseInt(text);
		} catch (Exception ex) {
			return "".equals(text) ? 0 : defaultValue;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiButton#mouseDragged(net.minecraft.src.MinecraftClient,
	 * int, int)
	 */
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);

		if (this.draggingHS) {
			this.hsb[H] = clamp((float) (mouseX - this.x - 10), 0, 128) / 128F;
			this.hsb[S] = (128F - clamp((float) (mouseY - this.y - 10), 0, 128)) / 128F;
			this.updateColour();
			return true;
		}

		if (this.draggingB) {
			this.hsb[B] = (128F - clamp((float) (mouseY - this.y - 10), 0, 128)) / 128F;
			this.updateColour();
			return true;
		}

		if (this.draggingA) {
			this.opacity = (mouseY - this.y < 11) ? 0xFF000000
					: (((128 - (int) clamp((float) (mouseY - this.y - 10), 0, 128)) << 25) & 0xFF000000);
			this.updateColour();
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiButton#mousePressed(net.minecraft.src.MinecraftClient,
	 * int, int)
	 */
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
				
		if (super.mouseClicked(mouseX, mouseY, button)) {
			if (this.btnOk.mouseClicked(mouseX, mouseY, button))
				this.result = DialogResult.OK;

			if (this.btnCancel.mouseClicked(mouseX, mouseY, button))
				this.result = DialogResult.Cancel;

			if (this.rectHSArea.contains(mouseX, mouseY))
				this.draggingHS = true;

			if (this.rectBArea.contains(mouseX, mouseY))
				this.draggingB = true;

			if (this.rectAArea.contains(mouseX, mouseY))
				this.draggingA = true;

			this.txtRed.mouseClicked(mouseX, mouseY, button);
			this.txtGreen.mouseClicked(mouseX, mouseY, button);
			this.txtBlue.mouseClicked(mouseX, mouseY, button);
			this.txtAlpha.mouseClicked(mouseX, mouseY, button);

			return true;
		} else if (this.active) {
			this.result = DialogResult.Cancel;
		}
		return false;
	}
	
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.minecraft.src.GuiButton#mouseReleased(int, int)
	 */
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {		
		if (this.draggingHS) {
			this.hsb[H] = clamp((float) (mouseX - this.x - 10), 0, 128) / 128F;
			this.hsb[S] = (128F - clamp((float) (mouseY - this.y - 10), 0, 128)) / 128F;
			this.updateColour();
		}

		if (this.draggingB) {
			this.hsb[B] = (128F - clamp((float) (mouseY - this.y - 10), 0, 128)) / 128F;
			this.updateColour();
		}

		if (this.draggingA) {
			this.opacity = (mouseY - this.y < 11) ? 0xFF000000
					: (((128 - (int) clamp((float) (mouseY - this.y - 10), 0, 128)) << 25) & 0xFF000000);
			this.updateColour();
		}
		
		this.draggingHS = false;
		this.draggingB = false;
		this.draggingA = false;
		return super.mouseReleased(mouseX, mouseY, button);
	}

	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		this.txtRed.keyPressed(keyCode,scanCode,modifiers);
		this.txtGreen.keyPressed(keyCode,scanCode,modifiers);
		this.txtBlue.keyPressed(keyCode,scanCode,modifiers);
		this.txtAlpha.keyPressed(keyCode,scanCode,modifiers);
		this.updateColourFromTextEntry();
		
		this.txtRed.charTyped((char) keyCode, keyCode);
		this.txtGreen.charTyped((char) keyCode, keyCode);
		this.txtBlue.charTyped((char) keyCode, keyCode);
		this.txtAlpha.charTyped((char) keyCode, keyCode);
		this.updateColourFromTextEntry();
		
		//9 should be tab		
		if (keyCode == GLFW.GLFW_KEY_TAB) {
			if (this.txtRed.isFocused()) {
				this.txtRed.changeFocus(false);
				this.txtGreen.changeFocus(true);
				this.txtBlue.changeFocus(false);
				this.txtAlpha.changeFocus(false);
			} else if (this.txtGreen.isFocused()) {
				this.txtRed.changeFocus(false);
				this.txtGreen.changeFocus(false);
				this.txtBlue.changeFocus(true);
				this.txtAlpha.changeFocus(false);
			} else if (this.txtBlue.isFocused()) {
				this.txtRed.changeFocus(false);
				this.txtGreen.changeFocus(false);
				this.txtBlue.changeFocus(false);
				this.txtAlpha.changeFocus(true);
			} else {
				this.txtRed.changeFocus(true);
				this.txtGreen.changeFocus(false);
				this.txtBlue.changeFocus(false);
				this.txtAlpha.changeFocus(false);
			}
		}

		return true;
	}

	public static float clamp(float value, float min, float max) {
		return Math.min(Math.max(value, min), max);
	}

}