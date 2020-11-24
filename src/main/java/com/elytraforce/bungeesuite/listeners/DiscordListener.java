package com.elytraforce.bungeesuite.listeners;

import java.awt.Color;

import com.elytraforce.bungeesuite.config.PluginConfig;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.discord.DiscordController;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class DiscordListener implements Listener{

	private Main instance;

	private TextChannel channel;
	
	public DiscordListener(Main plugin, String id) {
		this.instance = plugin;
		this.channel = DiscordController.api.getTextChannelById(id).get();
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PostLoginEvent event) {
		
			
		
			if (PluginConfig.get().getMaintenance()) {
				if (!event.getPlayer().hasPermission("elytraforce.helper")) { return; }
			}
		
			EmbedBuilder builder = new EmbedBuilder()
					.setColor(Color.CYAN)
					.setDescription("**" + event.getPlayer().getName() + "**" + " joined the ElytraForce Network");
			channel.sendMessage(builder);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLeave(PlayerDisconnectEvent event) {
		
		if (PluginConfig.get().getMaintenance()) {
			if (!event.getPlayer().hasPermission("elytraforce.helper")) { return; }
		}
		
		EmbedBuilder builder = new EmbedBuilder()
				.setColor(Color.CYAN)
				.setDescription("**" + event.getPlayer().getName() + "**" + " left the ElytraForce Network");
		channel.sendMessage(builder);
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChat(ChatEvent event) {
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		
		if (event.isCommand()) { return; }
		if (event.isCancelled()) { return; }

		
		String msg = event.getMessage().replaceAll("@everyone", "*snip*").replaceAll("@here", "*snip*");
		
		EmbedBuilder builder = new EmbedBuilder()
				.setColor(Color.CYAN)
				.setTitle(player.getName())
				.setThumbnail("https://minotar.net/avatar/" + player.getName() + "/40")
				.setDescription(msg);
		channel.sendMessage(builder);
	}
}
