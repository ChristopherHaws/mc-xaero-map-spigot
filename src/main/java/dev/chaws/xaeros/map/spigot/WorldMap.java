package dev.chaws.xaeros.map.spigot;

import org.bstats.bukkit.Metrics;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Logger;

public class WorldMap extends JavaPlugin implements PluginMessageListener {
	public static WorldMap instance;
	public static Logger log;

	@Override
	public void onEnable() {
		log = getLogger();
		instance = this;

		log.info("Xaeros WorldMap enabled.");
		var worldId = this.getWorldId();
		log.info("Found world id: " + worldId);

		// https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "xaeroworldmap:main");
		this.getServer().getMessenger().registerIncomingPluginChannel(this, "xaeroworldmap:main", this);

		try {
			new Metrics(this, 16554);
		} catch (Throwable ignored) { }
	}

	public void onDisable() {
		this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
		this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
		log.info("Xaeros WorldMap disabled.");
	}

	@Override
	public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, @NotNull byte[] message) {
		log.info("Message Received on channel " + channel + " for player " + player.getDisplayName());
	}

	private int getWorldId() {
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
