package com.github.tnerevival.commands.shop;

import com.github.tnerevival.TNE;
import com.github.tnerevival.commands.TNECommand;
import com.github.tnerevival.core.Message;
import com.github.tnerevival.core.shops.Shop;
import com.github.tnerevival.core.transaction.TransactionType;
import com.github.tnerevival.utils.AccountUtils;
import com.github.tnerevival.utils.MISCUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ShopCreateCommand extends TNECommand {

	public ShopCreateCommand(TNE plugin) {
		super(plugin);
	}

	@Override
	public String getName() {
		return "create";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "+" };
	}

	@Override
	public String getNode() {
		return "tne.shop.create";
	}

	@Override
	public boolean console() {
		return true;
	}

	@Override
	public void help(CommandSender sender) {
		sender.sendMessage(ChatColor.GOLD + "/shop create <name> [admin] [hidden] - Create a new shop. [admin] true/false, [hidden] true/false");
	}
	
	@Override
	public boolean execute(CommandSender sender, String command, String[] arguments) {
		if(arguments.length >= 1) {
			if(!Shop.exists(arguments[0], MISCUtils.getWorld(getPlayer(sender)))) {
				if(arguments[0].length() > 16) {
          getPlayer(sender).sendMessage(new Message("Messages.Shop.Long").translate());
					return false;
				}

				UUID owner = null;
				if(sender instanceof Player) {
					owner = MISCUtils.getID((Player)sender);
				}
				
				if(arguments.length >= 2 && arguments[1].equalsIgnoreCase("true")) {
					owner = null;
				}

        Shop s = new Shop(arguments[0], MISCUtils.getWorld(getPlayer(sender)));
				s.setOwner(owner);
        if(owner == null) {
          s.setAdmin(true);
        }

        if(!s.isAdmin() && Shop.amount(s.getOwner()) >= TNE.instance.api.getInteger("Core.Shops.Max", s.getWorld(), s.getOwner().toString())) {
          getPlayer(sender).sendMessage(new Message("Messages.Shop.Max").translate());
          return false;
        }
				
				if(arguments.length >= 3 && arguments[2].equalsIgnoreCase("true")) {
					s.setHidden(true);
				}

				if(!s.isAdmin() && !AccountUtils.transaction(s.getOwner().toString(), null,
            TNE.instance.api.getDouble("Core.Shops.Cost", s.getWorld(), s.getOwner().toString()),
            TransactionType.MONEY_INQUIRY, MISCUtils.getWorld(getPlayer(sender)))) {

				  Message insufficient = new Message("Messages.Money.Insufficient");

          insufficient.addVariable("$amount", MISCUtils.formatBalance(
              MISCUtils.getWorld(getPlayer(sender)),
              TNE.instance.api.getDouble("Core.Shops.Cost", s.getWorld(), s.getOwner().toString())
          ));
				  return false;
        }
        if(!s.isAdmin()) {
          AccountUtils.transaction(s.getOwner().toString(), null,
              TNE.instance.api.getDouble("Core.Shops.Cost", s.getWorld(), s.getOwner().toString()),
              TransactionType.MONEY_REMOVE, MISCUtils.getWorld(getPlayer(sender)));
        }
				TNE.instance.manager.shops.put(s.getName() + ":" + s.getWorld(), s);
				Message created = new Message("Messages.Shop.Created");
				created.addVariable("$shop", s.getName());
				getPlayer(sender).sendMessage(created.translate());
				return true;
			}
			getPlayer(sender).sendMessage(new Message("Messages.Shop.Already").translate());
			return false;
		} else {
			help(sender);
		}
		return false;
	}

}