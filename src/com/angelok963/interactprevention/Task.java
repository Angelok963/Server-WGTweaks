package com.angelok963.interactprevention;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class Task extends BukkitRunnable {

	private static HashMap<Player, Boolean> canBreak = new HashMap<>();
	private static HashMap<Player, Boolean> canPlace = new HashMap<>();
	private static HashMap<Player, Boolean> canUse = new HashMap<>();
	private static HashMap<Player, Boolean> canInteract = new HashMap<>();
	private static HashMap<Player, Boolean> canChestAccess = new HashMap<>();
	private static HashMap<Player, Boolean> canAll = new HashMap<>();
	private static HashMap<Player, Set<ProtectedRegion>> regions = new HashMap<>();
	private static Main plugin;
	private static WorldGuardPlugin wg;

	public Task(Main plugin, WorldGuardPlugin wg) {
		Task.plugin = plugin;
		Task.wg = wg;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void run() {

		for (Player p : Bukkit.getOnlinePlayers()) {

			Block loc = p.getTargetBlock(null, 5);

			ItemStack item = p.getItemInHand();

			boolean canBreak = (Task.canBreak.containsKey(p)) ? Task.canBreak.get(p) : false;
			boolean canBreakPast = isAllow(p, loc, DefaultFlag.BLOCK_BREAK);

			boolean canChestAccess = (Task.canChestAccess.containsKey(p)) ? Task.canChestAccess.get(p) : false;
			boolean canChestAccessPast = isAllow(p, loc, DefaultFlag.CHEST_ACCESS);

			boolean canPlace = (Task.canPlace.containsKey(p)) ? Task.canPlace.get(p) : false;
			boolean canPlacePast = isAllow(p, loc, DefaultFlag.BLOCK_PLACE);

			boolean canUse = (Task.canUse.containsKey(p)) ? Task.canUse.get(p) : false;
			boolean canUsePast = isAllow(p, loc, DefaultFlag.USE);

			boolean canInteract = (Task.canInteract.containsKey(p)) ? Task.canInteract.get(p) : false;
			boolean canInteractPast = isAllow(p, loc, DefaultFlag.INTERACT);

			boolean canAll = (Task.canAll.containsKey(p)) ? Task.canAll.get(p) : false;

			
			Set<ProtectedRegion> regionsPast = wg.getRegionManager(p.getWorld()).getApplicableRegions(p.getLocation())
					.getRegions();

			if (!Task.regions.containsKey(p)) {
				Task.regions.put(p, regionsPast);

				sendPacket(p);
			}

			Set<ProtectedRegion> regions = Task.regions.get(p);

			if (item.getType() == Material.ENDER_PEARL) {

				if (isAllow(p, loc, DefaultFlag.ENDERPEARL))
					canInteractPast = true;
			}
                   
				boolean allow = false;

				for (String i : plugin.getConfig().getStringList("allowClickItems")) {

					String j[] = i.split(":");

					if (!j[0].equalsIgnoreCase(item.getType().name()))
						continue;

					if (j[1].equals("*")) {

						allow = true;
						break;
					} else {
						if (Short.valueOf(j[1]) == item.getDurability()) {
							allow = true;
						break;
						}
					}

				}
				
				for (String i : plugin.getConfig().getStringList("allowBreakBlocks")) {

					String j[] = i.split(":");

					if (!j[0].equalsIgnoreCase(loc.getType().name()))
						continue;

					if (j[1].equals("*")) {

						allow = true;
						break;
					} else {
						if (Byte.valueOf(j[1]) == loc.getData()) {
							allow = true;
						break;
						}
					}
				}
				
				if(allow != canAll) {
					Task.canAll.put(p, allow);
					sendPacket(p);
				} 
			

			try {

				if (canBreakPast != canBreak) {

					Task.canBreak.put(p, canBreakPast);

					sendPacket(p);

				}

				if (canPlacePast != canPlace) {
					Task.canPlace.put(p, canPlacePast);

					sendPacket(p);

				}

				if (canUsePast != canUse) {

					Task.canUse.put(p, canUsePast);

					sendPacket(p);

				}

				if (canInteractPast != canInteract) {

					Task.canInteract.put(p, canInteractPast);

					sendPacket(p);
				}

				if (canChestAccessPast != canChestAccess) {

					Task.canChestAccess.put(p, canChestAccessPast);

					sendPacket(p);
				}

				if (canChestAccessPast != canChestAccess) {

					Task.canChestAccess.put(p, canChestAccessPast);

					sendPacket(p);
				}

				if (!regionsPast.equals(regions)) {

					Task.regions.put(p, regionsPast);

					sendPacket(p);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	public boolean isAllow(Player p, Block b, StateFlag flag) {

		ApplicableRegionSet d = wg.getRegionManager(p.getWorld()).getApplicableRegions(p.getLocation());

		LocalPlayer lp = wg.wrapPlayer(p);

		State q = d.queryValue(lp, flag);

		if (!wg.canBuild(p, b) && q != State.ALLOW)
			return false;

		if (q != State.DENY)
			return true;

		return false;
	}

	public void sendPacket(Player p) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(bos);

			dos.writeBoolean(canBreak.containsKey(p) ? canBreak.get(p) : false);
			dos.writeBoolean(canPlace.containsKey(p) ? canPlace.get(p) : false);
			dos.writeBoolean(canUse.containsKey(p) ? canUse.get(p) : false);
			dos.writeBoolean(canInteract.containsKey(p) ? canInteract.get(p) : false);
			dos.writeBoolean(canChestAccess.containsKey(p) ? canChestAccess.get(p) : false);
			dos.writeBoolean(canAll.containsKey(p) ? canAll.get(p) : false);
			dos.writeInt(plugin.getConfig().getInt("X"));// Координата экрана
			dos.writeInt(plugin.getConfig().getInt("Y"));// Координата экрана
			dos.writeUTF(Main.createInfo(p));// Списочек регионов

			p.sendPluginMessage(plugin, "wg_tweaks", bos.toByteArray());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
