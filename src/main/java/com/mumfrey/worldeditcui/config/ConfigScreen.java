package com.mumfrey.worldeditcui.config;

import com.mumfrey.worldeditcui.gui.CUIConfigPanel;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import net.minecraft.client.gui.screen.Screen;

public class ConfigScreen implements ConfigScreenFactory<Screen>{

	@Override
	public Screen create(Screen parent) {
		System.out.println("new CUIConfigPanel");
		return new CUIConfigPanel();
	}


}
