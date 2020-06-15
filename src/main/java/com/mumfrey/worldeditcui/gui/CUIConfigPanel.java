package com.mumfrey.worldeditcui.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.util.NarratorManager;

import com.mumfrey.worldeditcui.LiteModWorldEditCUI;
import com.mumfrey.worldeditcui.config.CUIConfiguration;
import com.mumfrey.worldeditcui.gui.controls.GuiChooseKeyButton;
import com.mumfrey.worldeditcui.gui.controls.GuiColourButton;
import com.mumfrey.worldeditcui.gui.controls.GuiControl;
import com.mumfrey.worldeditcui.render.ConfiguredColour;


/**
 * @author Adam Mummery-Smith
 */
public class CUIConfigPanel extends Screen{

	private static final int COLOUR_OPTION_BASE_ID = 100;	
	private static final int CONTROL_SPACING = 24;
	private static final int CONTROL_TOP = 120;
	private static final int CONTROLS_PADDING = 10;
	private static final int EXTRA_CONTROLS_SPACING = 16;
	private static final int EXTRA_CONTROLS_HEIGHT = CUIConfigPanel.EXTRA_CONTROLS_SPACING * 2;

	private MinecraftClient mc;
	private List<AbstractPressableButtonWidget> controlList = new ArrayList<AbstractPressableButtonWidget>();
	//private List<GuiColourButton> colourButtonList = new ArrayList<GuiColourButton>();
	//private AbstractPressableButtonWidget activeControl;
	private CheckboxWidget chkPromiscuous, chkAlwaysOnTop, chkClearAll;
	private int colourButtonsBottom;
	
	public CUIConfigPanel() {
		super(NarratorManager.EMPTY);
		this.mc = MinecraftClient.getInstance();
	}

	public String getPanelTitle() {
		return LiteModWorldEditCUI.instance.getController().getConfiguration().getMessage_wecui_options_title();
	}

	public int getContentHeight() {
		return this.colourButtonsBottom + CUIConfigPanel.EXTRA_CONTROLS_HEIGHT + CUIConfigPanel.CONTROLS_PADDING;
	}

	protected void init() {
		super.init();
		CUIConfiguration config = LiteModWorldEditCUI.instance.getController().getConfiguration();
		this.controlList.clear();
		int nextId = 0;
		for (ConfiguredColour colour : ConfiguredColour.values()) {
			GuiColourButton guicb = new GuiColourButton(this.mc, nextId, 24,CUIConfigPanel.CONTROL_TOP + nextId * CUIConfigPanel.CONTROL_SPACING, 40, 20, colour);
			this.controlList.add(guicb);

			this.controlList.add(new ButtonWidget(234,CUIConfigPanel.CONTROL_TOP + nextId * CUIConfigPanel.CONTROL_SPACING, 60, 20, "Reset",
					(onpress)->{
						colour.setColour(colour.getDefault());
						guicb.updateColour(colour);
					}));
			nextId++;
			//break;
		}	
		
		this.colourButtonsBottom = CUIConfigPanel.CONTROL_TOP + nextId * CUIConfigPanel.CONTROL_SPACING + CUIConfigPanel.EXTRA_CONTROLS_SPACING;
		this.chkPromiscuous = new CheckboxWidget( 24, 26, 150, 20, config.getMessage_gui_options_compat_spammy(), config.isPromiscuous());
		this.addButton(this.chkPromiscuous);
		
		this.chkAlwaysOnTop = new CheckboxWidget( 24, 80, 150, 20, config.getMessage_gui_options_compat_ontop(),config.isAlwaysOnTop());
		this.addButton(this.chkAlwaysOnTop);
		this.chkClearAll = new CheckboxWidget(24,this.colourButtonsBottom + CUIConfigPanel.EXTRA_CONTROLS_SPACING,150, 20, config.getMessage_gui_options_extra_clearall(), config.isClearAllOnKey());
		this.addButton(this.chkClearAll);

		controlList.add(new GuiChooseKeyButton(24, 24, this.colourButtonsBottom + CUIConfigPanel.EXTRA_CONTROLS_SPACING+50, 150, 20, "Hotkey: Chunk", config.getKey_chunk(), "".split(";"),"chunk"));
		controlList.add(new GuiChooseKeyButton(24, 200, this.colourButtonsBottom + CUIConfigPanel.EXTRA_CONTROLS_SPACING+50, 150, 20, "Hotkey: Clear", config.getKey_clear(), "".split(";"),"clear"));
		controlList.add(new GuiChooseKeyButton(24, 24, this.colourButtonsBottom + CUIConfigPanel.EXTRA_CONTROLS_SPACING+80, 150, 20, "Hotkey: Lshift", config.getKey_lshift(), "".split(";"),"lshift"));
		controlList.add(new GuiChooseKeyButton(24, 200, this.colourButtonsBottom + CUIConfigPanel.EXTRA_CONTROLS_SPACING+80, 150, 20, "Hotkey: Rshift", config.getKey_rshift(), "".split(";"),"rshift"));
		controlList.add(new GuiChooseKeyButton(24, 24, this.colourButtonsBottom + CUIConfigPanel.EXTRA_CONTROLS_SPACING+110, 150, 20, "Hotkey: Toggle", config.getKey_toggle(), "".split(";"),"toggle"));
		
		
		
		for (AbstractPressableButtonWidget control : this.controlList) {
			this.addButton(control);
		}
		
				
		
		//add save button
		this.addButton(new ButtonWidget(150, this.colourButtonsBottom + CUIConfigPanel.EXTRA_CONTROLS_SPACING+170, 40,20, "save", (e)-> {			
			for (AbstractPressableButtonWidget control : this.controlList) {
				if(control instanceof GuiColourButton) {
					GuiColourButton colourButton = (GuiColourButton)control;
					colourButton.save();
				}				
			}			
			
			config.setPromiscuous(this.chkPromiscuous.isChecked());
			config.setAlwaysOnTop(this.chkAlwaysOnTop.isChecked());
			config.setClearAllOnKey(this.chkClearAll.isChecked());
			
			config.save();
			MinecraftClient.getInstance().openScreen(null);
		}));
		//add cancel button
		this.addButton(new ButtonWidget(200, this.colourButtonsBottom + CUIConfigPanel.EXTRA_CONTROLS_SPACING+170, 40,20, "cancel", (e)-> {
			MinecraftClient.getInstance().openScreen(null);
		}));
		
		GuiControl.setScreenSizeAndScale(MinecraftClient.getInstance().getWindow().getWidth(), this.getContentHeight(), (int)mc.getWindow().getScaleFactor());
	}
	
	@Override
	public void render(int mouseX, int mouseY, float partialTicks) {
		//this.renderBackground(200);
		this.renderDirtBackground(255);
		this.drawString(this.mc.textRenderer, LiteModWorldEditCUI.instance.getController().getConfiguration().getMessage_gui_options_compat_title(), 10,CUIConfigPanel.CONTROLS_PADDING+(int)offset, 0xFFFFFF55);
		this.drawString(this.mc.textRenderer, LiteModWorldEditCUI.instance.getController().getConfiguration().getMessage_gui_options_colours_title(), 10, 64+(int)offset, 0xFFFFFF55);
		this.drawString(this.mc.textRenderer, LiteModWorldEditCUI.instance.getController().getConfiguration().getMessage_gui_options_extra_title(), 10, this.colourButtonsBottom+(int)offset,0xFFFFFF55);
		this.drawString(this.mc.textRenderer, LiteModWorldEditCUI.instance.getController().getConfiguration().getMessage_gui_options_extra_key_select(), 10,  this.colourButtonsBottom + CUIConfigPanel.EXTRA_CONTROLS_SPACING+30+(int)offset,0xFFFFFF55);
		this.drawString(this.mc.textRenderer, LiteModWorldEditCUI.instance.getController().getConfiguration().getMessage_gui_options_save_reminder(), 100,  this.colourButtonsBottom + CUIConfigPanel.EXTRA_CONTROLS_SPACING+150+(int)offset,0xFFFFFF55);
		
		for(AbstractButtonWidget button : this.buttons) {
			int tempButtonY = button.y;
			button.y += offset;
			button.render(mouseX, mouseY, partialTicks);
			button.y = tempButtonY;
		}
		
		//close all picker
		
		for(AbstractButtonWidget button : this.buttons) {
			if(button instanceof GuiColourButton) {
				//int tempButtonY = button.y;
				//button.y -= offset;
				((GuiColourButton)button).drawPicker(mc, mouseX, mouseY, partialTicks);
				//button.y = tempButtonY;
			}
		}
		
		//super.render(mouseX, mouseY, partialTicks);
	}
	
	double offset;
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		boolean noscroll = false;
		for(AbstractButtonWidget abutton : this.buttons) {
			if(abutton instanceof GuiColourButton) {
				if(((GuiColourButton)abutton).isPickerShown()) {
					noscroll = true;
					//break;
				}
			}
			abutton.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		}
		if(!noscroll) {
			offset += (mouseY - startDrag);
		}
		
		this.startDrag = mouseY;		
		return super.mouseDragged(mouseX, mouseY + offset, button, deltaX, deltaY);		
	}
	
	
	@Override
	public boolean mouseScrolled(double d, double e, double amount) {
		boolean noscroll = false;
		for(AbstractButtonWidget abutton : this.buttons) {
			if(abutton instanceof GuiColourButton) {
				if(((GuiColourButton)abutton).isPickerShown()) {
					noscroll = true;
					//break;
				}
			}
			abutton.mouseScrolled(d,e,amount);
		}
		if(!noscroll) {
			offset += (amount*10);
		}
		
		//this.startDrag = mouseY;		
		//return super.mouseDragged(mouseX, mouseY + offset, button, deltaX, deltaY);		
		return super.mouseScrolled(d, e, amount);
	}
	
	
	private void actionPerformed(AbstractPressableButtonWidget control) {
		if (control instanceof CheckboxWidget) {
			CheckboxWidget chk = (CheckboxWidget) control;
			chk.onPress();
		}		
	}
	
	double startDrag = 0;
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {	
		startDrag = mouseY;				
		boolean makeActive = true;
		for (AbstractPressableButtonWidget control : this.controlList) {
			int tempButtonY = control.y;
			control.y += offset;		
			if (control.mouseClicked(mouseX, mouseY,button)) {				
				if (makeActive) {
					makeActive = false;
					this.actionPerformed(control);
					control.y = tempButtonY;
					return true;
				}
			}else if(control instanceof GuiColourButton) {
				GuiColourButton guiC = (GuiColourButton) control;
				guiC.closePicker(true);
			}
			control.y = tempButtonY;
		}
		return super.mouseClicked(mouseX, mouseY-offset, button);
		//return false;
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {		
		for (AbstractButtonWidget buttonw: this.buttons) {
			buttonw.mouseReleased(mouseX, mouseY, button);	
		}		
		return super.mouseReleased(mouseX, mouseY-offset, button);
	}
	
	
	
	@Override
	public boolean keyPressed(int keyChar, int keyCode,int modifiers) {
		if (keyChar == GLFW.GLFW_KEY_ESCAPE) {
			this.onClose();
			return false;
		}

		for (AbstractButtonWidget button: this.buttons) {
			if(button instanceof GuiColourButton) {
				((GuiColourButton)button).keyPressed(keyChar, keyCode,modifiers);
			}else if(button instanceof GuiChooseKeyButton) {
				((GuiChooseKeyButton)button).keyPressed(keyChar, keyCode,modifiers);
			}
		}
		return true;
	}
	

	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		for (AbstractButtonWidget button: this.buttons) {
			if(button instanceof GuiColourButton) {
				((GuiColourButton)button).mouseMoved(mouseX, mouseY);
			}			
		}	
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		
		for (AbstractButtonWidget button: this.buttons) {
			if(button instanceof GuiColourButton) {
				((GuiColourButton)button).isMouseOver(mouseX, mouseY);
			}			
		}		
		return false;
	}
}