package dev.chaws.xaeros.map.spigot;

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

public class WorldMap extends JavaPlugin implements Listener {
	private static final String channel = "xaeroworldmap:main";

	public static Logger log;

	private int worldId;

	@Override
	public void onEnable() {
		log = getLogger();
		this.worldId = this.initializeWorldId();

		this.getServer().getMessenger().registerOutgoingPluginChannel(this, channel);
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
		if (!event.getChannel().equals(channel)) {
			return;
		}

		sendPlayerWorldId(event.getPlayer());
	}

	@EventHandler
	public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
		sendPlayerWorldId(event.getPlayer());
	}

	private void sendPlayerWorldId(Player player) {
		var bytes = ByteStreams.newDataOutput();
		bytes.writeByte(0);
		bytes.writeInt(this.worldId);

		player.sendPluginMessage(this, channel, bytes.toByteArray());
	}

	private int initializeWorldId() {
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
				try (var fr = new FileReader(xaeromapFile); var br = new BufferedReader(fr)) {
					var line = br.readLine();
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
