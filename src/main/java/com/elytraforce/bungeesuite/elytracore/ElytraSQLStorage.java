package com.elytraforce.bungeesuite.elytracore;

import com.elytraforce.bungeesuite.config.PluginConfig;
import com.elytraforce.bungeesuite.rappu_b.Database;
import com.elytraforce.bungeesuite.util.AuriBungeeUtil;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Objects;

public class ElytraSQLStorage {
    private static ElytraSQLStorage instance;

    private final Database database;

    private ElytraSQLStorage() {
        AuriBungeeUtil.logError("Starting ElytraStorage!");
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
                //comment of testingness
            } else {

            }
        });
    }

    public void shutdown() {
        database.close();
    }

    public static ElytraSQLStorage get() { return Objects.requireNonNullElseGet(instance, () -> instance = new ElytraSQLStorage());}
    
}
