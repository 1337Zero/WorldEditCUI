package com.mumfrey.worldeditcui;

import io.netty.buffer.Unpooled;

import java.io.File;
import java.util.List;

import org.dimdev.rift.listener.MessageAdder;
import org.dimdev.rift.listener.client.ClientTickable;
import org.dimdev.rift.network.Message;
import org.dimdev.riftloader.listener.InitializationListener;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.mumfrey.worldeditcui.config.CUIConfiguration;
import com.mumfrey.worldeditcui.event.listeners.CUIListenerChannel;
import com.mumfrey.worldeditcui.event.listeners.CUIListenerWorldRender;
import com.mumfrey.worldeditcui.input.KeySetting;
import com.mumfrey.worldeditcui.pluginchannel.TestMessage;

/**
 * Main litemod entry point
 * 
 * @author Adam Mummery-Smith
 */
//public class LiteModWorldEditCUI implements Tickable, InitCompleteListener, PluginChannelListener, PostRenderListener, Configurable, JoinGameListener, Messenger
/*
 * Habe: - Tickable - InitCompleteListener - PostRenderListener (mixin) Brauche:
 * - Configurable (unnötig nur Liteloader interface) - JoinGameListener (vll
 * dirty fix) - Messenger (zurückgestellt)
 * 
 */
public class LiteModWorldEditCUI implements ClientTickable, InitializationListener , MessageAdder{
	private static final int DELAYED_HELO_TICKS = 10;

	public static String path = "";

	// worldedit:cui
	// private static final String CHANNEL_WECUI = "WECUI";

	private static final String SUB_CHANNEL_WECUI = "cui";
	private static final String CHANNEL_WECUI = "worldedit";

	private WorldEditCUI controller;
	public static LiteModWorldEditCUI instance;

	private WorldClient lastWorld;
	private EntityPlayerSP lastPlayer;
	private static boolean joinedServer = false;
	private boolean init = false;

	// private KeyBinding keyBindToggleUI = new KeyBinding("wecui.keys.toggle",
	// Keyboard.KEY_NONE, "wecui.keys.category");
	private KeySetting keyBindToggleUI;
	// private KeyBinding keyBindClearSel = new KeyBinding("wecui.keys.clear",
	// Keyboard.KEY_NONE, "wecui.keys.category");
	private KeySetting keyBindClearSel;
	// private KeyBinding keyBindChunkBorder = new KeyBinding("wecui.keys.chunk",
	// Keyboard.KEY_NONE, "wecui.keys.category");
	private KeySetting keyBindChunkBorder;

	// LShift
	private KeySetting keyBindLShift;
	// RShift
	private KeySetting keyBindRShift;

	private boolean visible = true;
	private boolean alwaysOnTop = false;

	private CUIListenerWorldRender worldRenderListener;
	private CUIListenerChannel channelListener;

	private int delayedHelo = 0;

	
	public void init(File configPath) {
		init = true;
		LiteModWorldEditCUI.instance = this;
		keyBindToggleUI = new KeySetting(290, "wecui.keys.toggle");
		keyBindClearSel = new KeySetting(291, "wecui.keys.clear");
		keyBindChunkBorder = new KeySetting(292, "wecui.keys.chunk");
		keyBindLShift = new KeySetting(340, "wecui.keys.control.lshift");
		keyBindRShift = new KeySetting(344, "wecui.keys.control.rshift");
		path = System.getProperty("user.dir");
		path = path + System.getProperty("file.separator") + "mods" + System.getProperty("file.separator") + "";

	}

	public void upgradeSettings(String version, File configPath, File oldConfigPath) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.mumfrey.liteloader.InitCompleteListener#onInitCompleted(net.minecraft.
	 * client.Minecraft, com.mumfrey.liteloader.core.LiteLoader)
	 */
	public void onInitCompleted() {
		Minecraft minecraft = Minecraft.getInstance();
		this.controller = new WorldEditCUI();
		this.controller.initialise(minecraft);

		this.worldRenderListener = new CUIListenerWorldRender(this.controller, minecraft);
		this.channelListener = new CUIListenerChannel(this.controller);
	}

	
	public void onJoinGame() {
		System.out.println("onJoinGame");

		this.visible = true;
		this.controller.getDebugger().debug("Joined game, sending initial handshake");
		this.helo();
	}

	/**
	 * 
	 */
	private void helo() {
		register();
		PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
		String message = "v|" + WorldEditCUI.PROTOCOL_VERSION;
		buffer.writeBytes(message.getBytes(Charsets.UTF_8));
		// ClientPluginChannels.sendMessage(CHANNEL_WECUI, buffer,
		// ChannelPolicy.DISPATCH_ALWAYS);
		System.out.println("Sending CustomPayLoad Packet with data = '" + message + "' to channel: " + CHANNEL_WECUI+ ":" + SUB_CHANNEL_WECUI);
		sendCustomPayLoadMessage(buffer);
		Minecraft.getInstance().player.sendChatMessage("/we cui");
	}

	protected PacketBuffer getRegistrationData() {
		// If any mods have registered channels, send the REGISTER packet
			StringBuilder channelList = new StringBuilder();

			for (String channel : getChannels()) {					
				channelList.append(channel);
				channelList.append("\0");
			}

			PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
			buffer.writeBytes(channelList.toString().getBytes(Charsets.UTF_8));			
			return buffer;					
	}

	private void register() {
			CPacketCustomPayload register =new CPacketCustomPayload(new ResourceLocation("minecraft:register"), getRegistrationData());
			Minecraft.getInstance().player.connection.sendPacket(register);		
	}

	private void sendCustomPayLoadMessage(PacketBuffer data) {
		CPacketCustomPayload payload = new CPacketCustomPayload(new ResourceLocation(CHANNEL_WECUI, SUB_CHANNEL_WECUI),data);
		Minecraft.getInstance().player.connection.sendPacket(payload);
	}

	/*public List<String> getMessageChannels() {
		//return ImmutableList.<String>of("worldedit:cui");
		return ImmutableList.<String>of("worldedit:cui");
	}*/

	public void receiveMessage(String message,String channel) {
		this.channelListener.onMessage(message);
		 /* if (channel.equalsIgnoreCase("wecui:wecui")) { 
			  try {
				  //s|cuboid
				  if (!message.contains("|")) {
					  this.channelListener.onMessage(message); 
				  } else if (message.contains("|")) {
					  //p|1|93|63|178|9
					  this.channelListener.onMessage(Joiner.on('|').join(list)); 
				  } else {
					  System.out.println("WHAT ?");
				  }
			  } catch(Exception ex) {
				  
			  } 
		  }		*/ 
	}

	public List<String> getChannels() {
		return ImmutableList.<String>of(LiteModWorldEditCUI.CHANNEL_WECUI + ":" + SUB_CHANNEL_WECUI);
	}

	public void onCustomPayload(String channel, PacketBuffer data) {
		try {
			int readableBytes = data.readableBytes();
			if (readableBytes > 0) {
				byte[] payload = new byte[readableBytes];
				data.readBytes(payload);
				this.channelListener.onMessage(new String(payload, Charsets.UTF_8));
			} else {
				this.controller.getDebugger().debug("Warning, invalid (zero length) payload received from server");
			}
		} catch (Exception ex) {
		}
	}

	public void onTick(Minecraft mc, float partialTicks, boolean inGame, boolean clock) {
		if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.connection != null && !LiteModWorldEditCUI.joinedServer) {
			// Joined a server ?
			LiteModWorldEditCUI.joinedServer = true;
			onJoinGame();
		}

		CUIConfiguration config = this.controller.getConfiguration();

		if (inGame && mc.currentScreen == null) {

			if (this.keyBindToggleUI.isPressed()) {
				// if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ||
				// Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
				if (keyBindLShift.isKeyDown() || keyBindRShift.isKeyDown()) {
					config.setAlwaysOnTop(!config.isAlwaysOnTop());
				} else {
					this.visible = !this.visible;
				}
			}

			if (this.keyBindClearSel.isPressed()) {
				if (mc.player != null) {
					mc.player.sendChatMessage("//sel");
				}

				if (config.isClearAllOnKey()) {
					this.controller.clearRegions();
				}
			}

			if (this.keyBindChunkBorder.isPressed()) {
				this.controller.toggleChunkBorders();
			}
		}

		if (inGame && clock && this.controller != null) {
			this.alwaysOnTop = config.isAlwaysOnTop();

			if (mc.world != this.lastWorld || mc.player != this.lastPlayer) {
				this.lastWorld = mc.world;
				this.lastPlayer = mc.player;

				this.controller.getDebugger().debug("World change detected, sending new handshake");
				this.controller.clear();
				this.helo();
				this.delayedHelo = LiteModWorldEditCUI.DELAYED_HELO_TICKS;
				if (mc.player != null && config.isPromiscuous()) {
					mc.player.sendChatMessage("/we cui"); // Tricks WE to send the current selection
				}
			}

			if (this.delayedHelo > 0) {
				this.delayedHelo--;
				if (this.delayedHelo == 0) {
					this.helo();
					Minecraft.getInstance().player.sendChatMessage("/we cui");
				}
			}
		}
	}

	public String getName() {
		return "WorldEditCUI";
	}

	public String getVersion() {
		return "1.13.2";
	}

	/*
	 * public Class<? extends ConfigPanel> getConfigPanelClass() { return
	 * CUIConfigPanel.class; }
	 */

	public void onPostRenderEntities(float partialTicks) {
		if (init && this.worldRenderListener != null) {
			if (this.visible && !this.alwaysOnTop) {
				RenderHelper.disableStandardItemLighting();
				this.worldRenderListener.onRender(partialTicks);
				RenderHelper.enableStandardItemLighting();
			}
		}

	}

	public void onPostRender(float partialTicks) {
		if (this.visible && this.alwaysOnTop) {
			this.worldRenderListener.onRender(partialTicks);
		}
	}

	public WorldEditCUI getController() {
		return this.controller;
	}

	@Override
	public void onInitialization() {
		System.out.println("booting mixin");
		LiteModWorldEditCUI.instance = this;
		MixinBootstrap.init();
		Mixins.addConfiguration("mixin.zombe.json");
	}

	@Override
	public void clientTick(Minecraft client) {
		if (Minecraft.getInstance() != null) {
			if (!init) {
				init(null);
			}
			if (Minecraft.getInstance().player != null) {
				onTick(client, 0, Minecraft.getInstance().player != null, true);
			}

		}
	}

	@Override
	public void registerMessages(IRegistry<Class<? extends Message>> registry) {
		//registry.putObject(new ResourceLocation("minecraft", "cui"), TestMessage.class);
		registry.put(new ResourceLocation("minecraft", "cui"), TestMessage.class);
	}
}
