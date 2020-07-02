package com.mumfrey.worldeditcui;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.mumfrey.worldeditcui.config.ConfigScreen;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;

public class ModMenuProvider implements ModMenuApi{
	
	
	
	@Override
	public String getModId() {		
		return LiteModWorldEditCUI.getName();
	}
	@Override
	public ConfigScreenFactory<Screen> getModConfigScreenFactory() {	
		return new ConfigScreen();			
	}
	@Override
	public Map<String, ConfigScreenFactory<?>> getProvidedConfigScreenFactories() {
		return ImmutableMap.of("minecraft", parent -> new TitleScreen());
		//return null;
	}
}
