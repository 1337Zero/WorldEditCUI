package com.mumfrey.worldeditcui;

import io.netty.buffer.Unpooled;

import java.io.File;
import java.util.List;

/*
import org.dimdev.rift.listener.MessageAdder;
import org.dimdev.rift.listener.client.ClientTickable;
import org.dimdev.rift.network.Message;
import org.dimdev.riftloader.listener.InitializationListener;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;*/

import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.math.Vec3d;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.client.MinecraftClient;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.mumfrey.worldeditcui.config.CUIConfiguration;
import com.mumfrey.worldeditcui.event.listeners.CUIListenerChannel;
import com.mumfrey.worldeditcui.event.listeners.CUIListenerWorldRender;
import com.mumfrey.worldeditcui.input.KeySetting;

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
public class LiteModWorldEditCUI implements ModInitializer, PacketConsumer, HudRenderCallback, ClientTickCallback {
	/*
	 * Tickable ? ServerSidePacketRegistry ? statt MessageAdder
	 * ClientSidePacketRegistry ? statt MessageAdder
	 */
	private static final int DELAYED_HELO_TICKS = 10;

	public static String path = "";

	// worldedit:cui
	// private static final String CHANNEL_WECUI = "WECUI";

	private static final String SUB_CHANNEL_WECUI = "cui";
	private static final String CHANNEL_WECUI = "worldedit";

	private WorldEditCUI controller;
	public static LiteModWorldEditCUI instance;

	private ClientWorld lastWorld;
	private PlayerEntity lastPlayer;

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
	private boolean alwaysOnTop = true;

	private CUIListenerWorldRender worldRenderListener;
	private CUIListenerChannel channelListener;

	private int delayedHelo = 0;

	private boolean registered = false;
	private BlockMark testmark;

	public void init(File configPath) {
		init = true;
		LiteModWorldEditCUI.instance = this;
		keyBindToggleUI = new KeySetting(290, "wecui.keys.toggle");
		keyBindClearSel = new KeySetting(291, "wecui.keys.clear");
		keyBindChunkBorder = new KeySetting(292, "wecui.keys.chunk");
		keyBindLShift = new KeySetting(340, "wecui.keys.control.lshift");
		keyBindRShift = new KeySetting(344, "wecui.keys.control.rshift");
	}

	public void upgradeSettings(String version, File configPath, File oldConfigPath) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.mumfrey.liteloader.InitCompleteListener#onInitCompleted(net.
	 * MinecraftClient. client.MinecraftClient,
	 * com.mumfrey.liteloader.core.LiteLoader)
	 */
	public void onInitCompleted() {
		path = System.getProperty("user.dir");
		path = path + System.getProperty("file.separator") + "mods" + System.getProperty("file.separator") + "";
		System.out.println("init");
		MinecraftClient minecraft = MinecraftClient.getInstance();
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
		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		String message = "v|" + WorldEditCUI.PROTOCOL_VERSION;
		buffer.writeBytes(message.getBytes(Charsets.UTF_8));
		// ClientPluginChannels.sendMessage(CHANNEL_WECUI, buffer,
		// ChannelPolicy.DISPATCH_ALWAYS);
		System.out.println("Sending CustomPayLoad Packet with data = '" + message + "' to channel: " + CHANNEL_WECUI
				+ ":" + SUB_CHANNEL_WECUI);
		sendCustomPayLoadMessage(buffer);
		MinecraftClient.getInstance().player.sendChatMessage("/we cui");
	}

	protected PacketByteBuf getRegistrationData() {
		// If any mods have registered channels, send the REGISTER packet
		StringBuilder channelList = new StringBuilder();

		for (String channel : getChannels()) {
			channelList.append(channel);
			channelList.append("\0");
		}

		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		buffer.writeBytes(channelList.toString().getBytes(Charsets.UTF_8));
		return buffer;
	}

	private void register() {
		if (!registered) {
			/*
			 * CustomPayloadC2SPacket register = new CustomPayloadC2SPacket(new
			 * Identifier("minecraft:register"), getRegistrationData());
			 * ClientSidePacketRegistry.INSTANCE.register(new
			 * Identifier(LiteModWorldEditCUI.CHANNEL_WECUI + ":" + SUB_CHANNEL_WECUI),
			 * this); System.out.println("minecraft:register -> " +
			 * ClientSidePacketRegistry.INSTANCE.canServerReceive(new
			 * Identifier(LiteModWorldEditCUI.CHANNEL_WECUI + ":" + SUB_CHANNEL_WECUI)));;
			 * System.out.println(MinecraftClient.getInstance().getNetworkHandler());
			 * MinecraftClient.getInstance().getNetworkHandler().sendPacket(register);
			 */

			Identifier id = new Identifier(LiteModWorldEditCUI.CHANNEL_WECUI + ":" + SUB_CHANNEL_WECUI);

			ServerSidePacketRegistry.INSTANCE.register(id, this);

			ClientSidePacketRegistry.INSTANCE
					.register(new Identifier(LiteModWorldEditCUI.CHANNEL_WECUI + ":" + SUB_CHANNEL_WECUI), this);

			registered = true;

			// Register that u want to use the wecui channel
			MinecraftClient.getInstance().player.sendChatMessage("/we cui");
		}
	}

	private void sendCustomPayLoadMessage(PacketByteBuf data) {
		CustomPayloadS2CPacket payload = new CustomPayloadS2CPacket(new Identifier(CHANNEL_WECUI, SUB_CHANNEL_WECUI),
				data);
		MinecraftClient.getInstance().getNetworkHandler().sendPacket(payload);
	}

	/*
	 * public List<String> getMessageChannels() { //return
	 * ImmutableList.<String>of("worldedit:cui"); return
	 * ImmutableList.<String>of("worldedit:cui"); }
	 */

	public void receiveMessage(String message, String channel) {
		/*
		 * if recieved message from minecraft: 'worldedit:cui -> register
		 * 
		 */

		if (channel.trim().equalsIgnoreCase("minecraft")) {
			System.out.println("recieved message from " + channel + ": '" + message.trim());
			if (message.trim().equalsIgnoreCase("worldedit:cui")) {
				System.out.println("register worldedit:cui");
				register();
			} else {
				System.out.println("unknown message '" + message + "'");
			}
			// this.channelListener.onMessage(message);
		}

		/*
		 * if (channel.equalsIgnoreCase("wecui:wecui")) { try { //s|cuboid if
		 * (!message.contains("|")) { this.channelListener.onMessage(message); } else if
		 * (message.contains("|")) { //p|1|93|63|178|9
		 * this.channelListener.onMessage(Joiner.on('|').join(list)); } else {
		 * System.out.println("WHAT ?"); } } catch(Exception ex) {
		 * 
		 * } }
		 */
	}

	public List<String> getChannels() {
		return ImmutableList.<String>of(LiteModWorldEditCUI.CHANNEL_WECUI + ":" + SUB_CHANNEL_WECUI);
	}

	public void onCustomPayload(String channel, PacketByteBuf data) {
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

	public void onTick(MinecraftClient mc, float partialTicks, boolean inGame, boolean clock) {
		System.out.println("ontick");
		if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().getNetworkHandler() != null
				&& !LiteModWorldEditCUI.joinedServer) {
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
					MinecraftClient.getInstance().player.sendChatMessage("/we cui");
				}
			}
		}
	}

	public String getName() {
		return "WorldEditCUI - Fabric";
	}

	public String getVersion() {
		return "1.15.2";
	}

	/*
	 * public Class<? extends ConfigPanel> getConfigPanelClass() { return
	 * CUIConfigPanel.class; }
	 */

	/*
	 * public void onPostRenderEntities(float partialTicks) { if (init &&
	 * this.worldRenderListener != null) { if (this.visible && !this.alwaysOnTop) {
	 * // RenderHelper.disableStandardItemLighting();
	 * this.worldRenderListener.onRender(partialTicks); //
	 * RenderHelper.enableStandardItemLighting(); } }
	 * 
	 * }
	 */

	public void onPostRender(float partialTicks) {
		if (this.visible && this.alwaysOnTop) {
			this.worldRenderListener.onRender(partialTicks);
		}
	}

	public WorldEditCUI getController() {
		return this.controller;
	}

	@Override
	public void onInitialize() {
		LiteModWorldEditCUI.instance = this;
		MixinBootstrap.init();
		Mixins.addConfiguration("mixins.wecui.json");
		this.onInitCompleted();
		HudRenderCallback.EVENT.register(this);
		ClientTickCallback.EVENT.register(this);
	}

	/*
	 * public void tick() { System.out.println("tick"); // Called by Tickable
	 * MinecraftClient client = MinecraftClient.getInstance(); if (client != null) {
	 * if (!init) { init(null); } if (MinecraftClient.getInstance().player != null)
	 * { onTick(client, 0, MinecraftClient.getInstance().player != null, true); }
	 * float f = MinecraftClient.getInstance().getLastFrameDuration();
	 * this.onPostRenderEntities(MinecraftClient.getInstance().getLastFrameDuration(
	 * )); this.onPostRender(MinecraftClient.getInstance().getLastFrameDuration());
	 * } }
	 */

	@Override
	public void accept(PacketContext context, PacketByteBuf buffer) {
		onCustomPayload("unused channel", buffer);
	}

	@Override
	public void tick(MinecraftClient client) {
		/*
		 * System.out.println("tick"); // Called by Tickable if (client != null) { if
		 * (!init) { init(null); } if (MinecraftClient.getInstance().player != null) {
		 * onTick(client, 0, MinecraftClient.getInstance().player != null, true); } }
		 */
	}

	public void onHudRender(float tickDelta) {
		
		  System.out.println("cameraEntity: " +
		  MinecraftClient.getInstance().cameraEntity.getPos());
		  System.out.println("CameraPosVec: " +
		  MinecraftClient.getInstance().player.getCameraPosVec(tickDelta));
		  System.out.println("playerpos: " +
		  MinecraftClient.getInstance().player.getPos());
		 
		 Vec3d playerpos = MinecraftClient.getInstance().player.getPos().subtract(MinecraftClient.getInstance().player.getCameraPosVec(tickDelta));
		 System.out.println(playerpos.subtract(MinecraftClient.getInstance().player.getCameraPosVec(tickDelta)));
		 
		 
		 System.out.println(MinecraftClient.getInstance().cameraEntity.getRotationVec(tickDelta));
		 
		 System.out.println("gameRenderer: " + MinecraftClient.getInstance().gameRenderer.getCamera().getPos());
		 
		if (this.visible && this.alwaysOnTop) {
			this.worldRenderListener.onRender(tickDelta);
		}
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if (testmark == null) {
			testmark = new BlockMark((int) player.getX(), (int) player.getY(), (int) player.getZ(),
			MinecraftClient.getInstance(), 1.0F, 1.0F, 0.0F, 1.0F);
		}
		testmark.draw(tickDelta);
	}

	/*
	 * @Override public void registerMessages(IRegistry<Class<? extends Message>>
	 * registry) { //registry.putObject(new ResourceLocation("MinecraftClient",
	 * "cui"), TestMessage.class); registry.put(new
	 * ResourceLocation("MinecraftClient", "cui"), TestMessage.class); }
	 */

}
