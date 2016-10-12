package com.github.tnerevival.commands.shop;

import com.github.tnerevival.core.Message;
import com.github.tnerevival.utils.MISCUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.github.tnerevival.TNE;
import com.github.tnerevival.commands.TNECommand;
import com.github.tnerevival.core.material.MaterialHelper;
import com.github.tnerevival.core.shops.Shop;
import com.github.tnerevival.core.shops.ShopEntry;
import com.github.tnerevival.serializable.SerializableItemStack;

public class ShopAddCommand extends TNECommand {

	public ShopAddCommand(TNE plugin) {
		super(plugin);
	}

	@Override
	public String getName() {
		return "add";
	}

	@Override
	public String[] getAliases() {
		return new String[] { "+i" };
	}

	@Override
	public String getNode() {
		return "tne.shop.add";
	}

	@Override
	public boolean console() {
		return false;
	}

	@Override
	public void help(CommandSender sender) {
		sender.sendMessage(ChatColor.GOLD + "/shop add <shop> [amount:#] [item name[:damage]] [type:(sell/buy)] [stock:#/unlimited] [gold:#] [trade:name:amount(default 1):damage(default 1)]  - Add a new item to your shop for [gold] and/or [trade]. Leave out item name to use currently held item.");
	}
	
	@Override
	public boolean execute(CommandSender sender, String command, String[] arguments) {
		if(sender instanceof Player && arguments.length >= 1) {
		  if(Shop.exists(arguments[0], MISCUtils.getWorld(getPlayer(sender)))) {
		    if(Shop.canModify(arguments[0], (Player)sender)) {
          Player p = (Player)sender;
          Shop s = Shop.getShop(arguments[0], MISCUtils.getWorld(getPlayer(sender)));
          ItemStack item = p.getInventory().getItemInMainHand().clone();
          short damage = 0;
          int amount = 1;
          double cost = 50.00;
          int stock = 0;
          boolean buy = false;
          boolean unlimited = false;
          ItemStack trade = new ItemStack(Material.AIR);

          if(arguments.length >= 2) {
            Material mat;
            for (int i = 1; i < arguments.length; i++) {
              if(arguments[i].contains(":")) {
                String[] split = arguments[i].toLowerCase().split(":");
                switch(split[0]) {
                  case "gold":
                    try {
                      cost = Double.parseDouble(split[1]);
                    } catch(NumberFormatException e) {
                      getPlayer(sender).sendMessage(new Message("Messages.Shop.InvalidCost").translate());
                      return false;
                    }
                    break;
                  case "trade":
                    mat = MaterialHelper.getMaterial(split[1]);
                    if(mat.equals(Material.AIR)) {
                      Message invalidItem = new Message("Messages.Shop.InvalidTrade");
                      invalidItem.addVariable("$item", split[1]);
                      getPlayer(sender).sendMessage(invalidItem.translate());
                      return false;
                    }
                    trade = new ItemStack(mat);
                    try {
                      Integer tradeAmount = (split.length == 3)? Integer.parseInt(split[2]) : 1;
                      Short tradeDamage = (split.length == 4)? Short.parseShort(split[3]) : 1;
                      trade.setDurability(tradeDamage);
                      trade.setAmount(tradeAmount);
                    } catch(NumberFormatException e) {
                      getPlayer(sender).sendMessage(new Message("Messages.Shop.InvalidTradeAmount").translate());
                      return false;
                    }
                    break;
                  case "stock":
                    if(split[1].equalsIgnoreCase("unlimited") && s.isAdmin()) {
                      unlimited = true;
                      stock = 0;
                      continue;
                    }
                    try {
                      stock = Integer.parseInt(split[1]);
                    } catch(NumberFormatException e) {
                      getPlayer(sender).sendMessage(new Message("Messages.Shop.InvalidStock").translate());
                      return false;
                    }
                    break;
                  case "type":
                    buy = !split[1].equals("buy");
                    break;
                  case "amount":
                    try {
                      amount = Integer.parseInt(split[1]);
                    } catch(NumberFormatException e) {
                      getPlayer(sender).sendMessage(new Message("Messages.Shop.InvalidAmount").translate());
                      return false;
                    }
                    break;
                  default:
                    mat = MaterialHelper.getMaterial(split[0]);
                    if(mat == null || mat.equals(Material.AIR)) {
                      Message invalidItem = new Message("Messages.Shop.ItemInvalid");
                      invalidItem.addVariable("$item", arguments[i]);
                      getPlayer(sender).sendMessage(invalidItem.translate());
                      return false;
                    }
                    item = new ItemStack(mat);
                    try {
                      item.setDurability(Short.valueOf(split[1]));
                    } catch(NumberFormatException e) {
                      help(sender);
                      return false;
                    }
                    break;
                }
                continue;
              }
              mat = MaterialHelper.getMaterial(arguments[i]);
              if(mat == null || mat.equals(Material.AIR)) {
                Message invalidItem = new Message("Messages.Shop.ItemInvalid");
                invalidItem.addVariable("$item", arguments[i]);
                getPlayer(sender).sendMessage(invalidItem.translate());
                return false;
              }
              item = new ItemStack(mat);
            }
          }
          item.setAmount(amount);
          if(!buy || MISCUtils.getItemCount(p.getUniqueId(), item) >= stock) {
            ShopEntry entry = new ShopEntry(new SerializableItemStack(s.getItems().size(), item), cost, ((buy)? stock : 0), buy, unlimited, new SerializableItemStack(1, trade));
            if(!buy) {
              entry.setMaxstock(stock);
            }
            if(s.addItem(entry)) {
              if(buy) {
                //TODO: Move this to a transaction possibly?
                ItemStack temp = item.clone();
                temp.setAmount(stock);
                p.getInventory().removeItem(temp);
              }
              Message added = new Message("Messages.Shop.ItemAdded");
              added.addVariable("$shop", s.getName());
              added.addVariable("$item", item.getType().name());
              getPlayer(sender).sendMessage(added.translate());
              return true;
            }
            Message wrong = new Message("Messages.Shop.ItemWrong");
            wrong.addVariable("$shop", s.getName());
            wrong.addVariable("$item", item.getType().name());
            getPlayer(sender).sendMessage(wrong.translate());
            return false;
          }
          Message invalidStock = new Message("Messages.Shop.NotEnough");
          invalidStock.addVariable("$amount", stock + "");
          invalidStock.addVariable("$item", item.getType().name());
          getPlayer(sender).sendMessage(invalidStock.translate());
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