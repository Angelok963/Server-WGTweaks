package com.angelok963.interactprevention;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import com.sk89q.worldguard.bukkit.event.block.UseBlockEvent;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class Main extends JavaPlugin implements Listener {

	private static final WorldGuardPlugin wg = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
	private static BukkitTask Task;
	private static Main plugin;

	@Override
	public void onEnable() {

		Main.plugin = this;
		if (!new File(getDataFolder() + File.separator + "config.yml").exists()) {
			saveResource("config.yml", false);
			saveConfig();
			getServer().getConsoleSender()
					.sendMessage("§e(§cWGTweaks§e) §7Конфигурация плагина §cне найдена§7 и была создана по умолчанию.");
		}
		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "wg_tweaks");

		getServer().getPluginManager().registerEvents(this, this);

		getCommand("wgtweaksreload").setExecutor(this);

		Task = new Task(this, wg).runTaskTimerAsynchronously(this, 0, getConfig().getInt("timeUpdate"));

	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOW)
	public void allowBreak(BreakBlockEvent e) {

		List<String> cfg = getConfig().getStringList("allowBreakBlocks");

		for (int z = 0; z < cfg.size(); z++)
			cfg.set(z, cfg.get(z).toUpperCase());

		for (Block b : e.getBlocks()) {

			if (!cfg.contains(b.getType().name() + ":" + String.valueOf(b.getData()))
					&& !cfg.contains(b.getType().name() + ":*"))
				return;

		}
		e.setAllowed(true);
		e.setCancelled(false);
		e.setResult(Result.ALLOW);

	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOW)
	public void allowUse(UseBlockEvent e) {
		List<String> cfg = getConfig().getStringList("allowBreakBlocks");

		for (int z = 0; z < cfg.size(); z++)
			cfg.set(z, cfg.get(z).toUpperCase());

		for (Block b : e.getBlocks()) {

			if (!cfg.contains(b.getType().name() + ":" + String.valueOf(b.getData()))
					&& !cfg.contains(b.getType().name() + ":*"))
				return;

		}
		e.setAllowed(true);
		e.setCancelled(false);
		e.setResult(Result.ALLOW);

	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String str, String[] args) {

		if (!sender.hasPermission("wgtweaks.admin.reload")) {
			sender.sendMessage("§e(§cWGTweaks§e) §7У вас §cне достаточно §7прав!");
			return false;
		}

		reloadConfig();

		Task.cancel();

		Task = new Task(this, wg).runTaskTimerAsynchronously(this, 0, getConfig().getInt("timeUpdate"));

		sender.sendMessage("§e(§cWGTweaks§e) §7Конфигурация §aуспешно §7перезагружена!");
		return true;

	}

	public static String createInfo(Player p) {

		if (!plugin.getConfig().getBoolean("enabled"))
			return "";

		if (!p.hasPermission(plugin.getConfig().getString("permission")))
			return "";

		ArrayList<ProtectedRegion> list = new ArrayList<>(
				wg.getRegionManager(p.getWorld()).getApplicableRegions(p.getLocation()).getRegions());

		if (list.size() == 0) {

			return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("emptyFormat"));

		}

		String result = "";

		for (ProtectedRegion rg : list) {

			switch (plugin.getConfig().getString("displayType")) {
			case "DEFAULT":
				if (wg.canBuild(p, p.getLocation()))
					result = result + "\n" + ChatColor.translateAlternateColorCodes('&',
							plugin.getConfig().getString("allowFormat").replace("{REGION}", rg.getId()));
				else
					result = result + "\n" + ChatColor.translateAlternateColorCodes('&',
							plugin.getConfig().getString("denyFormat").replace("{REGION}", rg.getId()));
				break;

			case "USER":
				ArrayList<UUID> name = new ArrayList<>(rg.getOwners().getUniqueIds());
				
				if (name.size() == 0)
					return ChatColor.translateAlternateColorCodes('&',plugin.getConfig().getString("denyFormat").replace("{REGION}", "<Неизвестный хозяин>"));
				
			String nick = Bukkit.getOfflinePlayer(name.get(0)).hasPlayedBefore() ? Bukkit.getOfflinePlayer(name.get(0)).getName() : "<Неизвестный хозяин>";
				
				
				
				if (wg.canBuild(p, p.getLocation()))
					result = result + "\n" + ChatColor.translateAlternateColorCodes('&',
							plugin.getConfig().getString("allowFormat").replace("{REGION}", nick ));
				else
					result = result + "\n" + ChatColor.translateAlternateColorCodes('&',
							plugin.getConfig().getString("denyFormat").replace("{REGION}", nick ));
				break;

			default:
				return "";
			}

			if (plugin.getConfig().getBoolean("displayOneRegion"))
				break;

		}

		return result.trim();
	}

}
