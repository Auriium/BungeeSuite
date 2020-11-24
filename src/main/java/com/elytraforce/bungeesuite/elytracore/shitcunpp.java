package com.elytraforce.bungeesuite.elytracore;

import com.elytraforce.bungeesuite.config.PluginConfig;
import dev.magicmq.rappu.Database;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Objects;

public class shitcunpp {
    private static shitcunpp instance;

    private final Database database;



    private shitcunpp() {
        database = Database.newDatabase()
                .withUsername(PluginConfig.get().getECDatabaseUser())
                .withPassword(PluginConfig.get().getECDatabasePassword())
                .withConnectionInfo(PluginConfig.get().getECDatabase(), PluginConfig.get().getECDatabasePort(), PluginConfig.get().getECDatabaseName(), false)
                .withDefaultProperties()
                .open();
    }

    public void loadPlayer(ProxiedPlayer player) {
        String sql = "SELECT * FROM `levels_player` ";
        sql += "WHERE `player_uuid` = ?;";

        database.queryAsync(sql, new Object[]{player.getUniqueId().toString()}, resultSet -> {
            if (resultSet.next()) {

            } else {

            }
        });
    }

    public void shutdown() {
        database.close();
    }

    public static shitcunpp get() { return Objects.requireNonNullElseGet(instance, () -> instance = new shitcunpp());}
    
}
