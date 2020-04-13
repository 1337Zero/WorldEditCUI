package com.mumfrey.worldeditcui.input;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.KeyBinding;

public class KeySetting {

	private int key;
	private String configname;
	private KeyBinding binding;
		
	public KeySetting(int key,String configname){
		this.configname = configname;
		this.key = key;		
		load();
	}
	
	private void load(){
		
		for(KeyBinding lbinding : MinecraftClient.getInstance().options.keysAll) {
			if(lbinding.getDefaultKeyCode().getKeyCode() == key) {
				
				System.out.println("detected the same key as " + lbinding.getName() +  " ... using " + lbinding.getName()  + " as key");
				binding =lbinding;
				System.out.println("binding to key " + key);
			}	
		}
		if(binding == null) {
			binding = new KeyBinding("zombe.mod." + configname ,key , "key.categories.zombe");
			System.out.println("new binding to key " + key);
		}
		
	}
	public boolean isPressed(){
		/*if(binding.isPressed()) {
			System.out.println("key (" + configname + " is isPressed");
		}*/
		return binding.isPressed();
	}
	public boolean isKeyDown(){
		/*if(binding.isKeyDown()) {
			System.out.println("key (" + configname + " is down");
		}*/
		return binding.isPressed();
	}
	public void replaceKey(String configname,int key){
		//LiteModMain.config.replaceData(configname, key + "");
		System.out.println("Replacing Keys not implemented yet!");
	}

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
		replaceKey(configname, key);
		//binding.func_197984_a(key);
		if(MinecraftClient.getInstance().options.keyJump.getDefaultKeyCode().getKeyCode() == key) {
			System.out.println("detected the same key as jump ... using jump as key");
			binding = MinecraftClient.getInstance().options.keyJump;
			System.out.println("binding to new key " + key);
		}else {
			binding = new KeyBinding("zombe.mod." + configname ,key , "key.categories.zombe");
			System.out.println("binding to new key " + key);
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
