package dev.chaws.xaeros.map.spigot;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class WorldMap extends JavaPlugin {
	public static WorldMap instance;
	public static Logger log;

	@Override
	public void onEnable() {
		log = getLogger();
		instance = this;

		log.info("Xaeros WorldMap enabled.");

		try {
			new Metrics(this, 16554);
		} catch (Throwable ignored) { }
	}

	public void onDisable() {
		log.info("Xaeros WorldMap disabled.");
	}
}
