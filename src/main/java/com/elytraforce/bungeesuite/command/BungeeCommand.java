package com.elytraforce.bungeesuite.command;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.config.PluginConfig;
import com.elytraforce.bungeesuite.storage.SQLStorage;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public abstract class BungeeCommand extends Command implements TabExecutor {

    private final Main plugin;
    private PluginConfig config;
    private final SQLStorage storage;

    public BungeeCommand(Main plugin, String name, String permission) {
        super(name, permission);
        this.plugin = plugin;
        this.config = PluginConfig.get();
        this.storage = SQLStorage.get();
    }

    public Main getPlugin() {
        return plugin;
    }
    public PluginConfig getConfig() { return this.config; }
    public SQLStorage getStorage() { return  this.storage; }

    @Override
    public final void execute(final CommandSender commandSender, final String[] args) {
        plugin.getProxy().getScheduler().runAsync(plugin, () -> onCommand(commandSender, args));
    }

    public abstract void onCommand(CommandSender sender, String[] args);

    protected String getReasonFromArgs(int index, String[] args) {
        return Arrays.stream(args, index, args.length).collect(Collectors.joining(" "));
    }

    protected CompletableFuture<UUID> getUuidFromArg(int index, String[] args) {
        return getUuidFromArg(args[index]);
    }

    protected CompletableFuture<UUID> getUuidFromArg(String arg) {
        return  storage.getIDFromUsername(arg);
    }

    protected CompletableFuture<String> getNameFromUuid(UUID uuid) {
        return storage.getUsernameFromID(uuid);
    }


    @Override
    public Iterable<String> onTabComplete(final CommandSender sender, final String[] args) {
        if (args.length > 1) {
            return Collections.emptyList();
        }

        return ProxyServer.getInstance().getPlayers().stream().filter(p -> {
            String lower = (args.length == 0) ? "" : args[0].toLowerCase();
            return p.getName().toLowerCase().startsWith(lower);
        }).map(CommandSender::getName).collect(Collectors.toList());
    }
}