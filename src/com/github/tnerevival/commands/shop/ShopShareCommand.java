package com.github.tnerevival.commands.shop;

import com.github.tnerevival.TNE;
import com.github.tnerevival.commands.TNECommand;
import com.github.tnerevival.core.Message;
import com.github.tnerevival.core.shops.ShareEntry;
import com.github.tnerevival.core.shops.Shop;
import com.github.tnerevival.utils.MISCUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ShopShareCommand extends TNECommand {

	public ShopShareCommand(TNE plugin) {
		super(plugin);
	}

	@Override
	public String getName() {
		return "share";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "+p", "s" };
	}

	@Override
	public String getNode() {
		return "tne.shop.share";
	}

	@Override
	public boolean console() {
		return false;
	}

	@Override
	public void help(CommandSender sender) {
		sender.sendMessage(ChatColor.GOLD + "/shop share <name> <player> [percent](decimal) - Allow/disallow profit sharing with another player.");
	}
	
	@Override
	public boolean execute(CommandSender sender, String command, String[] arguments) {
		if(sender instanceof Player && arguments.length >= 1) {
			if(Shop.exists(arguments[0], MISCUtils.getWorld(getPlayer(sender)))) {
				if(Shop.canModify(arguments[0], getPlayer(sender))) {
					Shop s = Shop.getShop(arguments[0], MISCUtils.getWorld(getPlayer(sender)));
					UUID target = MISCUtils.getID(arguments[1]);
					if(!s.isAdmin()) {
						if(!TNE.instance.api.getBoolean("Core.Shops.Shares.Enabled", s.getWorld(), s.getOwner())) {
						  getPlayer(sender).sendMessage(new Message("Messages.Shop.ShareNone").translate());
              return false;
            }

            if(s.getShares().size() >= TNE.instance.api.getInteger("Core.Shops.Shares.Max", s.getWorld(), s.getOwner())) {
              getPlayer(sender).sendMessage(new Message("Messages.Shop.ShareMax").translate());
              return false;
            }

						if(Shop.shares(arguments[0], target)) {
							s.removeShares(target);

							Message hidden = new Message("Messages.Shop.ShareRemoved");
							hidden.addVariable("$player", MISCUtils.getPlayer(target).getDisplayName());
							getPlayer(sender).sendMessage(hidden.translate());
							return true;
						} else {
							double percent = (arguments.length >= 3)? Double.parseDouble(arguments[2]) : 0.1;
							
							if(percent <= s.canBeShared()) {
								ShareEntry entry = new ShareEntry(target, percent);
								
								s.addShares(entry);
								Message hidden = new Message("Messages.Shop.ShareAdded");
								hidden.addVariable("$player", MISCUtils.getPlayer(target).getDisplayName());
								getPlayer(sender).sendMessage(hidden.translate());
								return true;
							} else {
								getPlayer(sender).sendMessage(new Message("Messages.Shop.ShareGreater").translate());
								return false;
							}
						}
					}

					getPlayer(sender).sendMessage(new Message("Messages.Shop.ShareAdmin").translate());
					return false;
				}
				getPlayer(sender).sendMessage(new Message("Messages.Shop.Permission").translate());
				return false;
			}
			getPlayer(sender).sendMessage(new Message("Messages.Shop.None").translate());
			return false;
		} else {
			help(sender);
		}
		return false;
	}
}