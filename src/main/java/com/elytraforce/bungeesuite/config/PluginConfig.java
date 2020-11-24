package com.elytraforce.bungeesuite.config;

import java.io.File;
import java.io.IOException;
import java.util.List;
import com.elytraforce.bungeesuite.Main;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
@SuppressWarnings("unused")
public class PluginConfig {

	private Configuration config;
	private static PluginConfig instance;
	private List<String> commandList;

	private File file;


	private PluginConfig() {
		instance = this;

		try {
	    	file = new File(Main.get().getDataFolder(), "config.yml");

	        if (file.exists()) {
	            this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
	            return;
	        }

	        // Create the file to save
	        if (!file.getParentFile().exists()) {
	            file.getParentFile().mkdirs();
	        }
	        file.createNewFile();

	        // Load the default provided configuration and save it to the file
	        Configuration config = ConfigurationProvider.getProvider(YamlConfiguration.class)
	                .load(Main.get().getResourceAsStream("config.yml"));
	        ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
	        this.config = config;
	    } catch (IOException exception) {
	    	exception.printStackTrace();
	    }

		this.activate();

	}

	//idk why i have to do this
	public void activate() {
		this.commandList = this.config.getStringList("blocked-commands");
	}

    public boolean getMaintenance() {
    	return config.getBoolean("maintenance");
    }

    public void setMaintenance(boolean yes) {
    	config.set("maintenance", yes);
    	try {
			ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public Configuration getConfig() {
    	return config;
    }

    public String getPack() {
    	return config.getString("resource-pack-url");
    }

    public String getPrefix() {
    	return ChatColor.translateAlternateColorCodes('&', config.getString("prefix"));
    }

    public String getDiscordPrefix() {
    	return ChatColor.translateAlternateColorCodes('&', config.getString("discord-prefix"));
    }

    public String getAnnouncePrefix() {
    	return ChatColor.translateAlternateColorCodes('&', config.getString("announce-prefix"));
    }

    public String getDiscordToken() {
    	return config.getString("discord.token");
    }

    public String getDiscordChannelID() {
    	return config.getString("discord.monitor-chan-id");
    }

    public String getDiscordChangelogID() {
    	return config.getString("discord.changelog-chan-id");
    }

    public String getDiscordTodoID() {
    	return config.getString("discord.todo-chan-id");
    }

	public String getECDatabase() {
		return config.getString("ec_database.host");
	}
	public String getECDatabaseName() { return config.getString("ec_database.database"); }
	public int getECDatabasePort() {
		return config.getInt("ec_database.port");
	}
	public String getECDatabaseUser() {
		return config.getString("ec_database.user");
	}
	public String getECDatabasePassword() {
		return config.getString("ec_database.pass");
	}

	public String getDatabase() {
		return config.getString("ec_database.host");
	}
	public String getDatabaseName() { return config.getString("ec_database.database"); }
	public int getDatabasePort() {
		return config.getInt("ec_database.port");
	}
	public String getDatabaseUser() {
		return config.getString("ec_database.user");
	}
	public String getDatabasePassword() {
		return config.getString("ec_database.pass");
	}

    public List<String> getMuteCommands() {
    	return config.getStringList("mute-commands");
    }

    public String getTopMOTD() {
    	return config.getString("motd.top");
    }

    public String getBottomMOTD() {
    	return config.getString("motd.bottom");
    }

    public List<String> getBlockedCommands() {
    	return this.commandList;
    }

    public String getRedisIP() {
    	return config.getString("redis.ip");
    }

    public int getRedisPort() {
    	return config.getInt("redis.port");
    }

    public String getRedisPassword() {
    	return config.getString("redis.password");
    }

    public List<String> getSwearWords() {
    	return config.getStringList("blacklisted-words");
    }

    public List<String> getSafeWords() {
    	return config.getStringList("whitelisted-words");
    }

    public List<String> getAnnouncements() { return config.getStringList("announcements"); }

    public int getAnnouncementCooldown() { return config.getInt("announcement-interval"); }

    public boolean useElytraCoreSupport() { return config.getBoolean("use-elytracore-support"); }

    public static PluginConfig get() {
		if (instance == null) {
			return instance = new PluginConfig();
		} else {
			return instance;
		}
	}

}
