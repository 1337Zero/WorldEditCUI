package com.mumfrey.worldeditcui.gui.controls;

import java.awt.event.KeyEvent;

import com.mumfrey.worldeditcui.LiteModWorldEditCUI;
import com.mumfrey.worldeditcui.config.CUIConfiguration;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.LiteralText;



public class GuiChooseKeyButton extends ButtonWidget{
	private int buttonkey = 0;
	private int xstart = 0;
	private int ystart = 0;
	private int width = 0;
	private int height = 0;
	private String txt = "";
	private String buttonname = "";
	private boolean waiting = false;
	private String configName = null;
	
	public GuiChooseKeyButton(int id, int x, int y, int width, int height,String label, int buttonkey,String[] overlayText,String configName){
		//super(id, x, y, width, height, label, overlayText);		
		super(x,y,width,height,Text.method_30163(label),(onpress)->{});
		this.buttonkey = buttonkey;
		this.xstart = x;
		this.ystart = y;
		this.width = width;
		this.height = height;
		this.txt = label;
		this.buttonname = label;
		this.configName = configName;
		setLabel(label + ": " + makeBetterReadable(buttonkey));
	}
	private String makeBetterReadable(int key) {
		String name = KeyEvent.getKeyText(buttonkey);		
		if(name.length() > 10) {
			return "" + key;
		}
		return name;
	}

	
	@Override
	public void render(MatrixStack matrices,int x, int y, float partialTicks) {
		
		super.render(matrices,x, y, partialTicks);		
		if(waiting){
			this.setMessage(new LiteralText(buttonname + " waiting..."));
		}else{
			this.setMessage(new LiteralText(buttonname + ": " + makeBetterReadable(buttonkey)));
		}
	}
	@Override
	public boolean mouseClicked(double x, double y, int p_mouseClicked_5_) {
		if(super.mouseClicked(x, y, p_mouseClicked_5_)) {
			 waiting = true;
			 this.setMessage(new LiteralText("waiting..."));
			 onPress.onPress(this);
			 return true;
		}	 
		return false;
	}
	/**
	 * Set the text of the Button
	 * @param txt ,the new Label of the Button
	 */
	public void setLabel(String txt){
		this.txt = txt;
	}
	/**
	 * Get the text of the Button
	 * @return String ,the Label of the Button
	 */
	public String getLabel(){
		return txt;
	}
	/**
	 * Get the key of the Button as Integer value
	 * @return Integer
	 */
	public int getButtonkey() {
		return buttonkey;
	}
	/**
	 * Sets the key of the Button
	 * @param buttonkey ,the key for that Button
	 */
	public void setButtonkey(int buttonkey) {
		waiting = false;
		this.buttonkey = buttonkey;
	}	
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {		
		if(!waiting)return false;
		this.waiting = false;
		this.buttonkey = keyCode;
		this.setMessage(new LiteralText(buttonname + ": " + makeBetterReadable(buttonkey)));		
		CUIConfiguration config = LiteModWorldEditCUI.instance.getController().getConfiguration();
				
		if(configName.equalsIgnoreCase("chunk")) {
			config.setKey_chunk(buttonkey);
		}else if(configName.equalsIgnoreCase("clear")) {
			config.setKey_clear(buttonkey);
		}else if(configName.equalsIgnoreCase("control")) {
			config.setKey_control(buttonkey);
		}else if(configName.equalsIgnoreCase("lshift")) {
			config.setKey_lshift(buttonkey);
		}else if(configName.equalsIgnoreCase("rshift")) {
			config.setKey_rshift(buttonkey);
		}else if(configName.equalsIgnoreCase("toggle")) {
			config.setKey_toggle(buttonkey);
		}else {
			System.out.println("trying to change a unknown key " + configName);
		}
		config.save();
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
}
