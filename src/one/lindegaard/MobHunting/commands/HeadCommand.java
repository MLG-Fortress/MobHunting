package one.lindegaard.MobHunting.commands;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
//import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import one.lindegaard.MobHunting.Messages;
import one.lindegaard.MobHunting.MobHunting;
import one.lindegaard.MobHunting.mobs.MinecraftMob;
import one.lindegaard.MobHunting.util.Misc;

public class HeadCommand implements ICommand, Listener {

	public static final String MH_HEAD = "MH:Head";
	public static final String MH_REWARD = "MobHunting Reward";

	public HeadCommand(MobHunting instance) {
	}

	// Used case
	// /mh head give [toPlayer] [mobname|playername] [displayname] [amount] - to
	// give a head to a player.
	// /mh head rename [displayname] - to rename the head holding in the hand.

	@Override
	public String getName() {
		return "head";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "ph", "playerhead", "heads", "mobhead", "spawn" };
	}

	@Override
	public String getPermission() {
		return "mobhunting.head";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] {
				ChatColor.GOLD + label + ChatColor.GREEN + " give" + " [toPlayername] [playername|mobname]"
						+ ChatColor.YELLOW + " [displayname] [amount] [silent]" + ChatColor.WHITE + " - to give a head",
				ChatColor.GOLD + label + ChatColor.GREEN + " rename [new displayname]" + ChatColor.WHITE
						+ " - to rename the head in players name",
				ChatColor.GOLD + label + ChatColor.GREEN + " drop" + " [playername|mobname]" + ChatColor.YELLOW
						+ " [toPlayername] " + ChatColor.WHITE + " - to drop a head" };
	}

	@Override
	public String getDescription() {
		return Messages.getString("mobhunting.commands.head.description");
	}

	@Override
	public boolean canBeConsole() {
		return true;
	}

	@Override
	public boolean canBeCommandBlock() {
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args) {
		// /mh head give [toPlayername] [mobname|playername] [displayname]
		// [amount] [silent]
		if (args.length >= 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("spawn"))) {
			if (args.length >= 3) {
				OfflinePlayer offlinePlayer = null, toPlayer = null;
				String displayName;
				int amount = 1;

				// get toPlayerName
				toPlayer = Bukkit.getOfflinePlayer(args[1]);
				if (toPlayer == null || !toPlayer.isOnline()) {
					sender.sendMessage(Messages.getString("mobhunting.commands.head.online", "playername", args[1]));
					return true;
				}

				// get MobType / PlayerName
				MinecraftMob mob = MinecraftMob.getExtendedMobType(args[2]);
				if (mob == null) {
					offlinePlayer = Bukkit.getOfflinePlayer(args[2]);
					if (offlinePlayer != null) {
						mob = MinecraftMob.PvpPlayer;
					} else {
						sender.sendMessage(
								Messages.getString("mobhunting.commands.head.unknown_name", "playername", args[2]));
						return true;
					}
				}
				// get displayname
				if (args.length >= 4) {
					displayName = args[3].replace("_", " ");
				} else {
					if (mob != MinecraftMob.PvpPlayer)
						displayName = mob.getDisplayName().replace("_", " ");
					else
						displayName = offlinePlayer.getName();
				}
				// get amount
				if (args.length >= 5) {
					try {
						amount = Integer.valueOf(args[4]);
					} catch (NumberFormatException e) {
						sender.sendMessage(
								Messages.getString("mobhunting.commands.base.not_a_number", "number", args[4]));
						return false;
					}
				}
				// silent
				boolean silent = false;
				if (args.length >= 6 && (args[5].equalsIgnoreCase("silent") || args[5].equalsIgnoreCase("true")
						|| args[5].equalsIgnoreCase("1"))) {
					silent = true;
				}
				if (mob != null) {
					if (Misc.isMC18OrNewer()) {
						// Use GameProfile
						((Player) toPlayer).getWorld().dropItem(((Player) toPlayer).getLocation(),
								mob.getHead(displayName, mob.getHeadPrize()));
					} else {
						String cmdString = mob.getCommandString().replace("{player}", toPlayer.getName())
								.replace("{displayname}", displayName).replace("{lore}", MH_REWARD)
								.replace("{playerid}", mob.getPlayerUUID())
								.replace("{texturevalue}", mob.getTextureValue())
								.replace("{amount}", String.valueOf(amount)).replace("{playername}",
										offlinePlayer != null ? offlinePlayer.getName() : mob.getPlayerProfile());
						Messages.debug("%s Cmd=%s", mob.getDisplayName(), cmdString);
						Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), cmdString);
					}
					if (toPlayer.isOnline() && !silent) {
						((Player) toPlayer).sendMessage("You got a head of " + displayName + ".");
					}

				}
			}

			return true;

		} else if (args.length > 1 && (args[0].equalsIgnoreCase("rename"))) {
			// mh head rename [displayname] - to rename the head in hand.
			if (sender instanceof Player) {
				Player player = (Player) sender;
				ItemStack itemInHand = player.getItemInHand();
				if (itemInHand.hasItemMeta() && itemInHand.getItemMeta().hasLore()
						&& itemInHand.getItemMeta().getLore().get(0).equals(MH_REWARD)) {
					String displayname = "";
					for (int i = 1; i < args.length; i++) {
						if (i != (args.length - 1))
							displayname = displayname + args[i] + " ";
						else
							displayname = displayname + args[i];
					}
					ItemMeta im = itemInHand.getItemMeta();
					im.setDisplayName(displayname);
					itemInHand.setItemMeta(im);
				} else {
					sender.sendMessage(Messages.getString("mobhunting.commands.head.headmustbeinhand"));
				}
			} else {
				sender.sendMessage("You can only rename heads ingame.");
			}
			return true;
		} else if (args.length >= 1 && args[0].equalsIgnoreCase("drop") || args[0].equalsIgnoreCase("place")) {
			// /mh head drop <head>
			// /mh head drop <head> <player>
			if (sender.hasPermission("mobhunting.money.drop")) {
				// /mh head drop
				MinecraftMob mob = MinecraftMob.getExtendedMobType(args[1]);
				if (mob != null) {
					if (args.length == 2) {
						Player player = (Player) sender;
						Location location = Misc.getTargetBlock(player, 20).getLocation();
						Messages.debug("The head was dropped at %s", location);
						player.getWorld().dropItem(location, mob.getHead(mob.getName(), 0));

					} else if (args.length == 3 && Bukkit.getServer().getOfflinePlayer(args[2]).isOnline()) {
						Player player = ((Player) Bukkit.getServer().getOfflinePlayer(args[2]));
						Location location = Misc.getTargetBlock(player, 3).getLocation();
						Messages.debug("The head dropped at %s", location);
						player.getWorld().dropItem(location, mob.getHead(mob.getName(), 0));

					} else {
						sender.sendMessage(ChatColor.RED
								+ Messages.getString("mobhunting.commands.base.playername-missing", "player", args[2]));
					}
				} else {
					sender.sendMessage(
							Messages.getString("mobhunting.commands.head.unknown_name", "playername", args[1]));

				}

			} else {
				sender.sendMessage(ChatColor.RED + Messages.getString("mobhunting.commands.base.nopermission", "perm",
						"mobhunting.head", "command", "head"));
			}
			return true;
		}
		// show help
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String label, String[] args) {
		ArrayList<String> items = new ArrayList<String>();
		if (args.length == 1) {
			if (items.isEmpty()) {
				items.add("give");
				items.add("drop");
				items.add("rename");
			}
		} else if (args.length == 2 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("spawn"))) {
			String partial = args[1].toLowerCase();
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.getName().toLowerCase().startsWith(partial))
					items.add(player.getName());
			}
		} else if ((args.length == 3 && args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("spawn"))) {
			String partial = args[2].toLowerCase();
			for (MinecraftMob mob : MinecraftMob.values()) {
				if (mob.getFriendlyName().toLowerCase().startsWith(partial)
						|| mob.getDisplayName().toLowerCase().startsWith(partial))
					items.add(mob.getFriendlyName().replace(" ", "_"));
			}
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.getName().toLowerCase().startsWith(partial))
					items.add(player.getName());
			}
		} else if (args.length == 2 && args[0].equalsIgnoreCase("drop")) {
			String partial = args[1].toLowerCase();
			for (MinecraftMob mob : MinecraftMob.values()) {
				if (mob.getFriendlyName().toLowerCase().startsWith(partial)
						|| mob.getDisplayName().toLowerCase().startsWith(partial))
					items.add(mob.getFriendlyName().replace(" ", "_"));
			}
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.getName().toLowerCase().startsWith(partial))
					items.add(player.getName());
			}
		} else if (args.length == 3 && args[0].equalsIgnoreCase("drop")) {
			String partial = args[2].toLowerCase();
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.getName().toLowerCase().startsWith(partial))
					items.add(player.getName());
			}
		} 
		return items;
	}

}
