package com.elytraforce.bungeesuite.command;

import com.elytraforce.bungeesuite.Main;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.UUID;

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

        getUuidFromArg(0,args).thenAccept(uuid -> {
            if (uuid == null) {
                sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "That player has never joined the server");
            } else {
                sender.sendMessage(getConfig().getPrefix() + ChatColor.GRAY + "Fetching punishment information...");
                getStorage().getPunishments(uuid).thenAccept(results -> {

                    if (page + 1 > this.calculatePages(results.size()) || page < 0) {
                        sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "You must enter a page number between 1 and " + this.calculatePages(results.size()));
                        sender.sendMessage(getConfig().getPrefix() + ChatColor.RED + "Usage: /info <player> [page]");
                        return;
                    }

                    if (results.size() == 0) {
                        sender.sendMessage(ChatColor.RED + " None!");
                    } else {
                        for (ComponentBuilder builder : results) {
                            sender.sendMessage(builder.create());
                        }
                    }
                });
            }
        });
    }

    private int calculatePages(int amount) {
        return (int) Math.ceil(amount / 10.0);
    }

    private int getMaxPages(Connection connection, UUID id) throws SQLException {
        try (PreparedStatement maxPages = connection.prepareStatement("SELECT count(*) / ? max_pages  " +
                "FROM player_punish " +
                "WHERE banned_id = ?")) {
            maxPages.setInt(1, ENTRIES_PER_PAGE);
            maxPages.setString(2, id.toString());
            try (ResultSet rs = maxPages.executeQuery()) {
                return rs.next() ? (int) Math.ceil(rs.getDouble("max_pages")) : -1;
            }
        }
    }
}
