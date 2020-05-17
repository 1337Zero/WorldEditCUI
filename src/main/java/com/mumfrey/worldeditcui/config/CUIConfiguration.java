package com.mumfrey.worldeditcui.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;

import org.lwjgl.glfw.GLFW;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.mumfrey.worldeditcui.InitialisationFactory;
import com.mumfrey.worldeditcui.LiteModWorldEditCUI;
import com.mumfrey.worldeditcui.render.ConfiguredColour;

/**
 * Stores and reads WorldEditCUI settings
 * 
 * @author yetanotherx
 * @author Adam Mummery-Smith
 */
public final class CUIConfiguration implements InitialisationFactory {
	private static final String CONFIG_FILE_NAME = "worldeditcui.config.json";

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	@Expose
	private boolean debugMode = false;
	@Expose
	private boolean ignoreUpdates = false;
	@Expose
	private boolean promiscuous = true;
	@Expose
	private boolean alwaysOnTop = true;
	@Expose
	private boolean clearAllOnKey = false;

	@Expose
	private Colour cuboidGridColor = ConfiguredColour.CUBOIDBOX.getDefault();
	@Expose
	private Colour cuboidEdgeColor = ConfiguredColour.CUBOIDGRID.getDefault();
	@Expose
	private Colour cuboidFirstPointColor = ConfiguredColour.CUBOIDPOINT1.getDefault();
	@Expose
	private Colour cuboidSecondPointColor = ConfiguredColour.CUBOIDPOINT2.getDefault();
	@Expose
	private Colour polyGridColor = ConfiguredColour.POLYGRID.getDefault();
	@Expose
	private Colour polyEdgeColor = ConfiguredColour.POLYBOX.getDefault();
	@Expose
	private Colour polyPointColor = ConfiguredColour.POLYPOINT.getDefault();
	@Expose
	private Colour ellipsoidGridColor = ConfiguredColour.ELLIPSOIDGRID.getDefault();
	@Expose
	private Colour ellipsoidPointColor = ConfiguredColour.ELLIPSOIDCENTRE.getDefault();
	@Expose
	private Colour cylinderGridColor = ConfiguredColour.CYLINDERGRID.getDefault();
	@Expose
	private Colour cylinderEdgeColor = ConfiguredColour.CYLINDERBOX.getDefault();
	@Expose
	private Colour cylinderPointColor = ConfiguredColour.CYLINDERCENTRE.getDefault();
	@Expose
	private Colour chunkBoundaryColour = ConfiguredColour.CHUNKBOUNDARY.getDefault();
	@Expose
	private Colour chunkGridColour = ConfiguredColour.CHUNKGRID.getDefault();

	@Expose
	private int delayedCommand = 1000;
	// Keys
	@Expose
	private int key_toggle = 290;
	@Expose
	private int key_clear = 293;
	@Expose
	private int key_chunk = 292;
	@Expose
	private int key_lshift = 340;
	@Expose
	private int key_rshift = 344;
	@Expose
	private int key_control = GLFW.GLFW_KEY_F12;
	
	//Messages
	@Expose
	private String message_wecui_options_title = "WorldEditCUI Options";
	@Expose
	private String message_wecui_keys_toggle = "Toggle CUI visibility";
	@Expose
	private String message_wecui_keys_clear = "Clear WorldEdit selection";
	@Expose
	private String message_wecui_keys_chunk = "Toggle Chunk Border";
	@Expose
	private String message_wecui_keys_category = "WorldEditCUI";
	@Expose
	private String message_gui_ok = "OK";
	@Expose
	private String message_colour_cuboidedge = "Cuboid Edge Colour";
	@Expose
	private String message_colour_cuboidgrid = "Cuboid Grid Colour";
	@Expose
	private String message_colour_cuboidpoint1 = "Cuboid Second Point Colour";
	@Expose
	private String message_colour_cuboidpoint2 = "Cuboid First Point Colour";
	@Expose
	private String message_colour_polygrid = "Polygon Grid Colour";
	@Expose
	private String message_colour_polyedge = "Polygon Edge Colour";
	@Expose
	private String message_colour_polypoint = "Polygon Point Colour";
	@Expose
	private String message_colour_ellipsoidgrid = "Ellipsoid Grid Colour";
	@Expose
	private String message_colour_ellipsoidpoint = "Ellipsoid Centre Point Colour";
	@Expose
	private String message_colour_cylindergrid = "Cylinder Grid Colour";
	@Expose
	private String message_colour_cylinderedge = "Cylinder Edge Colour";
	@Expose
	private String message_colour_cylinderpoint = "Cylinder Centre Point Colour";
	@Expose
	private String message_colour_chunkboundary = "Chunk Boundary";
	@Expose
	private String message_colour_chunkgrid = "Chunk Grid";	
	@Expose
	private String message_gui_options_compat_title = "Compatibility Options";
	@Expose
	private String message_gui_options_compat_spammy = "Promiscuous Mode (send /we cui all the time)";
	@Expose
	private String message_gui_options_compat_ontop = "Always on top (mod compatibility)";
	@Expose
	private String message_gui_options_colours_title = "Display Colours";	
	@Expose
	private String message_gui_options_extra_title = "Advanced Options";
	@Expose
	private String message_gui_options_extra_clearall = "Clear displayed regions when <§aClear§r> is pressed";
	
	public static CUIConfiguration instance;
	

	/**
	 * Copies the default config file to the proper directory if it does not exist.
	 * It then reads the file and sets each variable to the proper value.
	 */
	@Override
	public void initialise() {
		int index = 0;
		CUIConfiguration.instance = this;
		try {
			for (Field field : this.getClass().getDeclaredFields()) {
				if (field.getType() == Colour.class) {
					ConfiguredColour configuredColour = ConfiguredColour.values()[index++];
					Colour colour = Colour.firstOrDefault((Colour) field.get(this),configuredColour.getColour().getHex());
					field.set(this, colour);
					configuredColour.setColour(colour);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		this.save();
	}

	public boolean isDebugMode() {
		return this.debugMode;
	}

	public boolean ignoreUpdates() {
		return this.ignoreUpdates;
	}

	public boolean isPromiscuous() {
		return this.promiscuous;
	}

	public void setPromiscuous(boolean promiscuous) {
		this.promiscuous = promiscuous;
	}

	public boolean isAlwaysOnTop() {
		return this.alwaysOnTop;
	}

	public void setAlwaysOnTop(boolean alwaysOnTop) {
		this.alwaysOnTop = alwaysOnTop;
	}

	public boolean isClearAllOnKey() {
		return this.clearAllOnKey;
	}

	public void setClearAllOnKey(boolean clearAllOnKey) {
		this.clearAllOnKey = clearAllOnKey;
	}

	public static CUIConfiguration create() {
		File jsonFile = new File(LiteModWorldEditCUI.path, CUIConfiguration.CONFIG_FILE_NAME);

		if (jsonFile.exists()) {
			
			FileReader fileReader = null;

			try {
				fileReader = new FileReader(jsonFile);
				CUIConfiguration config = CUIConfiguration.GSON.fromJson(fileReader, CUIConfiguration.class);
				return config;
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				try {
					if (fileReader != null)
						fileReader.close();
				} catch (IOException ex) {
				}
			}
		}

		return new CUIConfiguration();
	}

	public int getDelayedCommand() {
		return delayedCommand;
	}

	public void save() {
		File jsonFile = new File(LiteModWorldEditCUI.path, CUIConfiguration.CONFIG_FILE_NAME);

		FileWriter fileWriter = null;

		try {
			fileWriter = new FileWriter(jsonFile);
			CUIConfiguration.GSON.toJson(this, fileWriter);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (fileWriter != null)
					fileWriter.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public int getKey_toggle() {
		return key_toggle;
	}

	public void setKey_toggle(int key_toggle) {
		this.key_toggle = key_toggle;
	}

	public int getKey_clear() {
		return key_clear;
	}

	public void setKey_clear(int key_clear) {
		this.key_clear = key_clear;
	}

	public int getKey_chunk() {
		return key_chunk;
	}

	public void setKey_chunk(int key_chunk) {
		this.key_chunk = key_chunk;
	}

	public int getKey_lshift() {
		return key_lshift;
	}

	public void setKey_lshift(int key_lshift) {
		this.key_lshift = key_lshift;
	}

	public int getKey_rshift() {
		return key_rshift;
	}

	public void setKey_rshift(int key_rshift) {
		this.key_rshift = key_rshift;
	}

	public int getKey_control() {
		return key_control;
	}

	public void setKey_control(int key_control) {
		this.key_control = key_control;
	}

	public Colour getPolyGridColor() {
		return polyGridColor;
	}

	public Colour getPolyEdgeColor() {
		return polyEdgeColor;
	}

	public Colour getPolyPointColor() {
		return polyPointColor;
	}

	public String getMessage_wecui_options_title() {
		return message_wecui_options_title;
	}

	public String getMessage_wecui_keys_toggle() {
		return message_wecui_keys_toggle;
	}

	public String getMessage_wecui_keys_clear() {
		return message_wecui_keys_clear;
	}

	public String getMessage_wecui_keys_chunk() {
		return message_wecui_keys_chunk;
	}

	public String getMessage_wecui_keys_category() {
		return message_wecui_keys_category;
	}

	public String getMessage_gui_ok() {
		return message_gui_ok;
	}

	public String getMessage_colour_cuboidedge() {
		return message_colour_cuboidedge;
	}

	public String getMessage_colour_cuboidgrid() {
		return message_colour_cuboidgrid;
	}

	public String getMessage_colour_cuboidpoint1() {
		return message_colour_cuboidpoint1;
	}

	public String getMessage_colour_cuboidpoint2() {
		return message_colour_cuboidpoint2;
	}

	public String getMessage_colour_polygrid() {
		return message_colour_polygrid;
	}

	public String getMessage_colour_polyedge() {
		return message_colour_polyedge;
	}

	public String getMessage_colour_polypoint() {
		return message_colour_polypoint;
	}

	public String getMessage_colour_ellipsoidgrid() {
		return message_colour_ellipsoidgrid;
	}

	public String getMessage_colour_ellipsoidpoint() {
		return message_colour_ellipsoidpoint;
	}

	public String getMessage_colour_cylindergrid() {
		return message_colour_cylindergrid;
	}

	public String getMessage_colour_cylinderedge() {
		return message_colour_cylinderedge;
	}

	public String getMessage_colour_cylinderpoint() {
		return message_colour_cylinderpoint;
	}

	public String getMessage_colour_chunkboundary() {
		return message_colour_chunkboundary;
	}

	public String getMessage_colour_chunkgrid() {
		return message_colour_chunkgrid;
	}

	public String getMessage_gui_options_compat_title() {
		return message_gui_options_compat_title;
	}

	public String getMessage_gui_options_compat_spammy() {
		return message_gui_options_compat_spammy;
	}

	public String getMessage_gui_options_compat_ontop() {
		return message_gui_options_compat_ontop;
	}

	public String getMessage_gui_options_colours_title() {
		return message_gui_options_colours_title;
	}

	public String getMessage_gui_options_extra_title() {
		return message_gui_options_extra_title;
	}

	public String getMessage_gui_options_extra_clearall() {
		return message_gui_options_extra_clearall;
	}
	public String getColourName(String key) {	
		switch(key) {				
			case "colour.cuboidedge": return getMessage_colour_cuboidedge();
			case "colour.cuboidgrid": return getMessage_colour_cuboidgrid();
			case "colour.cuboidpoint1": return getMessage_colour_cuboidpoint1();
			case "colour.cuboidpoint2": return getMessage_colour_cuboidpoint2();
			case "colour.polygrid": return getMessage_colour_polygrid();
			case "colour.polyedge": return getMessage_colour_polyedge();
			case "colour.polypoint": return getMessage_colour_polypoint();
			case "colour.ellipsoidgrid": return getMessage_colour_ellipsoidgrid();
			case "colour.ellipsoidpoint": return getMessage_colour_ellipsoidpoint();
			case "colour.cylindergrid": return getMessage_colour_cylindergrid();
			case "colour.cylinderedge": return getMessage_colour_cylinderedge();
			case "colour.cylinderpoint": return getMessage_colour_cylinderpoint();
			case "colour.chunkboundary": return getMessage_colour_chunkboundary();
			case "colour.chunkgrid": return getMessage_colour_chunkgrid();
		}
		return key;
	}
}
