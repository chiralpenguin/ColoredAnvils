package me.flamingkatana.coloredanvils;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorHandler {
	public static final int INPUT_LEFT = 0;
	public static final int OUTPUT = 2;

	public static boolean hasColor(String s, ChatColor c) {
		return s.contains(c.toString());
	}

	public static boolean hasColorPermission(Player p, char c) {
		return (!ColoredAnvils.usingPermissions()) || (p.hasPermission("coloredanvils.*")) || (p.hasPermission("coloredanvils.color.*")) || (p.hasPermission("coloredanvils.color." + c)) || (p.hasPermission("coloredanvils.color.&" + c));
	}

	public static ItemStack getTranslatedItem(Player p, AnvilInventory inv) {
		ItemStack outputItem = inv.getItem(2);
		if ((outputItem != null) && (outputItem.hasItemMeta())) {
			ItemMeta outputItemMeta = outputItem.getItemMeta();
			if (outputItemMeta.hasDisplayName()) {
				ItemStack inputItem = inv.getItem(0);
				if ((inputItem != null) && (inputItem.hasItemMeta())) {
					ItemMeta inputItemMeta = inputItem.getItemMeta();
					if (inputItemMeta.hasDisplayName()) {
						if (outputItemMeta.getDisplayName().replaceAll("&", "").replaceAll("§", "").equals(inputItemMeta.getDisplayName().replaceAll("§", ""))) {
							outputItemMeta.setDisplayName(ChatColor.RESET + inputItemMeta.getDisplayName());
							outputItem.setItemMeta(outputItemMeta);
							return !ColoredAnvils.usingPermissionsForNonNameChanges() ? outputItem : colorItemWithPermissions(outputItem, p);
						}
					}
				}
				return colorItemWithPermissions(outputItem, p);
			}
		}
		return outputItem;
	}

	public static ItemStack colorItemWithPermissions(ItemStack item, Player p) {
		ItemMeta itemMeta = item.getItemMeta();
		String coloredName = parseName(p, itemMeta.getDisplayName());
		for (int i = 0; i < coloredName.length(); i++) {
			if (coloredName.charAt(i) == '§') {
				char c = coloredName.charAt(i + 1);
				if (!hasColorPermission(p, c)) {
					coloredName = coloredName.replaceAll("§" + c, "&" + c);
				}
			}
		}
		itemMeta.setDisplayName(coloredName);
		item.setItemMeta(itemMeta);
		return item;
	}

	/*
	Credit for this method and hex colour code conversion goes to https://github.com/Kikisito/ColoredAnvils/commit/a3159b89723fda2fa5a3695aa960413703938694
	This fork maintains this method without the broken behaviour that would repetitively append "&r" to the item name
	in plaintext if the player did not have the correct permission nodes.
	 */

	public static String parseName(Player player, String displayName){
		String finalmessage;
		Integer version = null;
		// Check version
		Pattern n = Pattern.compile("^(\\d)\\.(\\d+)");
		Matcher nm = n.matcher(ColoredAnvils.getPlugin().getServer().getBukkitVersion());
		while(nm.find()){
			version = Integer.parseInt(nm.group(2));
		}
		// Minimum version: 1.16
		if(version >= 16 && player.hasPermission("coloredanvils.*") || player.hasPermission("coloredanvils.color.*")) {
			Pattern pattern = Pattern.compile("&#([0-9a-fA-F]){6}");
			Matcher matcher = pattern.matcher(displayName);
			StringBuffer sb = new StringBuffer();
			while(matcher.find()){
				String hex = matcher.group();
				matcher.appendReplacement(sb, ChatColor.of(hex.substring(1)).toString());
			}
			matcher.appendTail(sb);
			finalmessage = ChatColor.translateAlternateColorCodes('&', sb.toString());
		} else {
			finalmessage = org.bukkit.ChatColor.translateAlternateColorCodes('&', displayName);
		}
		return finalmessage;
	}
}
