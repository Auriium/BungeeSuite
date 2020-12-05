package com.elytraforce.bungeesuite.command;

import com.elytraforce.bungeesuite.Main;
import net.md_5.bungee.api.CommandSender;

public class SettingsCommand extends BungeeCommand {


    public SettingsCommand(Main plugin) {
        super(plugin, "settings", "elytraforce.default");
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {

    }
}
