package com.itz_arnavfalke197.dynamicstockmarket;

import java.util.Collections;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class DSMCommand implements CommandExecutor, TabCompleter {
    private DynamicStockMarket plugin;

    public DSMCommand(DynamicStockMarket plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("dsm.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            this.plugin.reloadPlugin();
            sender.sendMessage(ChatColor.GREEN + "Dynamic StockMarket reloaded!");
            return true;
        }

        sender.sendMessage(ChatColor.RED + "Usage: /dsm reload");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1 && "reload".startsWith(args[0].toLowerCase())) {
            return Collections.singletonList("reload");
        }
        return Collections.emptyList();
    }
}