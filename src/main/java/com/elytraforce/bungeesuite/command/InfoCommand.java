package com.elytraforce.bungeesuite.command;

import com.elytraforce.bungeesuite.Main;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class InfoCommand extends BungeeCommand {

    private static final int ENTRIES_PER_PAGE = 10;
    private final DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");

    public InfoCommand(Main plugin) {
        super(plugin, "info", "elytraforce.helper");
    }

    @SuppressWarnings("deprecation")
	@Override
    public void onCommand(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Usage: /info <player> [page]");
            return;
        }

        int page;
        try {
            page = args.length == 1 ? 0 : Integer.parseInt(args[1]) - 1;
        } catch (NumberFormatException e) {
            sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Please enter a valid page number");
            sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Usage: /info <player> [page]");
            return;
        }

        //TODO: pages
        getUuidFromArg(0,args).thenAccept(uuid -> {
            if (uuid == null) {
                sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "That player has never joined the server");
            } else {
                sender.sendMessage(getConfig().getPrefix() + ChatColor.GRAY + "Fetching punishment information...");
                getStorage().getPunishments(uuid).thenAccept(results -> {

                    if (results.size() == 0) {
                        sender.sendMessage(ChatColor.RED + "Player has no punishments on record!");
                    }

                    if (page + 1 > this.calculatePages(results.size()) || page < 0) {
                        sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "You must enter a page number between 1 and " + this.calculatePages(results.size()));
                        sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Usage: /info <player> [page]");
                        return;
                    }

                    sender.sendMessage(getConfig().getPrefix() + ChatColor.translateAlternateColorCodes('&',
                            String.format("&cPunishments of %s &7(Page %d/%d)", args[0], page + 1, this.calculatePages(results.size()))));


                        for (ComponentBuilder b : sort(results).get(page)) {
                            sender.sendMessage(b.create()); }

                });
            }
        });
    }

    private List<List<ComponentBuilder>> sort(ArrayList<ComponentBuilder> components) {
        return Lists.partition(components,10);
    }

    private int calculatePages(int amount) {
        return (int) Math.ceil(amount / 10.0);
    }

}
