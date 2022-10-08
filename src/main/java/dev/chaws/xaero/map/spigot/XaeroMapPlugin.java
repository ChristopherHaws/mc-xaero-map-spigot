package dev.chaws.xaero.map.spigot;

import com.google.common.io.ByteStreams;
import org.bstats.bukkit.Metrics;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Logger;

public class XaeroMapPlugin extends JavaPlugin implements Listener {
	private static final String worldmapChannel = "xaeroworldmap:main";
	private static final String minimapChannel = "xaerominimap:main";

	public static Logger log;

	private int serverLevelId;

	@Override
	public void onEnable() {
		log = getLogger();
		this.serverLevelId = this.initializeServerLevelId();

		this.getServer().getMessenger().registerOutgoingPluginChannel(this, worldmapChannel);
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, minimapChannel);
		this.getServer().getPluginManager().registerEvents(this, this);

		try {
			new Metrics(this, 16554);
		} catch (Throwable ignored) { }
	}

	public void onDisable() {
		this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
	}

	// Use PlayerRegisterChannelEvent instead of PlayerLoginEvent because
	// the client mod might not have registered to events on the channel yet
	// so the packet won't get picked up by the mod.
	@EventHandler
	public void onPlayerRegisterChannel(PlayerRegisterChannelEvent event) {
		var channel = event.getChannel();
		if (!channel.equals(worldmapChannel) &&
			!channel.equals(minimapChannel)) {
			return;
		}

		this.sendPlayerWorldId(event.getPlayer(), channel);
	}

	@EventHandler
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
		var player = event.getPlayer();
		this.sendPlayerWorldId(player, worldmapChannel);
		this.sendPlayerWorldId(player, minimapChannel);
	}

	private void sendPlayerWorldId(Player player, String channel) {
		var bytes = ByteStreams.newDataOutput();
		bytes.writeByte(0);
		bytes.writeInt(this.serverLevelId);

		player.sendPluginMessage(this, channel, bytes.toByteArray());
	}

	private int initializeServerLevelId() {
		try {
			var worldFolder = getServer().getWorldContainer().getCanonicalPath();
			var xaeromapFile = new File(worldFolder + File.separator + "xaeromap.txt");
			if (!xaeromapFile.exists()) {
				try {
					try (var xaeromapFileStream = new FileOutputStream(xaeromapFile, false)) {
						var id = (new Random()).nextInt();
						var idString = "id:" + id;
						xaeromapFileStream.write(idString.getBytes());

						return id;
					}
				} catch (Exception ex) {
					log.warning("Failed to create xaeromap.txt: " + ex);
				}
			} else {
				try (var fileReader = new FileReader(xaeromapFile);
					 var bufferedReader = new BufferedReader(fileReader)) {
					var line = bufferedReader.readLine();
					var args = line.split(":");
					if (!Objects.equals(args[0], "id")) {
						throw new Exception("Failed to read id from xaeromap.txt");
					}

					return Integer.parseInt(args[1]);
				} catch (Exception ex) {
					log.warning("Failed to read xaeromap.txt: " + ex);
				}
			}
		} catch (Exception ex) {
			log.warning("Failed to get world ID: " + ex);
		}

		return 0;
	}
}
