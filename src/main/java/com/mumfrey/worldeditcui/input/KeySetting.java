package com.mumfrey.worldeditcui.input;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;

public class KeySetting {

	private int key;
	private String configname;
	private KeyBinding binding;
	
	private long lastPressed;
		
	public KeySetting(int key,String configname){
		this.configname = configname;
		this.key = key;		
		load();
		lastPressed = System.currentTimeMillis();
	}
	
	private void load(){		
		for(KeyBinding lbinding : MinecraftClient.getInstance().options.keysAll) {
			if(lbinding.getDefaultKey().getCode() == key) {				
				binding = lbinding;
			}
		}
		if(binding == null) {
			binding = new KeyBinding("wecui.mod." + configname ,key , "key.categories.wecui");
		}
	}
	public boolean isPressed(){
		if(binding.isPressed() && System.currentTimeMillis() - lastPressed > 250) {
			lastPressed = System.currentTimeMillis();
			return binding.isPressed();
		}
		return false;
	}
	public boolean isKeyDown(){		
		return binding.isPressed();
	}
	public void replaceKey(String configname,int key){
		System.out.println("Replacing Keys not implemented yet!");
	}

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
		replaceKey(configname, key);
		if(MinecraftClient.getInstance().options.keyJump.getDefaultKey().getCode() == key) {
			binding = MinecraftClient.getInstance().options.keyJump;
		}else {
			binding = new KeyBinding("wecui.mod." + configname ,key , "key.categories.wecui");
		}	
	}

	public String getConfigname() {
		return configname;
	}

	public void setConfigname(String configname) {
		this.configname = configname;
	}

	@Override
	public String toString() {
		return "KeySetting [key=" + key + ", configname=" + configname + "]";
	}
}
