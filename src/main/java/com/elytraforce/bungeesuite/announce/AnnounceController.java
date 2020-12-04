package com.elytraforce.bungeesuite.announce;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.config.PluginConfig;
import com.elytraforce.bungeesuite.util.AuriBungeeUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class AnnounceController {

	private static AnnounceController instance;

	private Main main;
	private ArrayList<ArrayList<String>> announcements;
	private PluginConfig config;

	private AnnounceController() {

		main = Main.get();
		config = PluginConfig.get();

		announcements = new ArrayList<>();
		List<String> tempList = config.getAnnouncements();

		for (String s : tempList) {
			ArrayList<String> temp = new ArrayList<>();
			for (String s1 : s.split("/")) {temp.add("&r" + s1); }
			announcements.add(temp);
		}

		this.setupRunnables();
	}

	private void setupRunnables() {
		Main.get().getProxy().getScheduler().schedule(Main.get(), new Runnable() {
			public void run() {
				Collections.shuffle(announcements);
				ArrayList<String> list = announcements.get(0);
				for (ProxiedPlayer p : main.getProxy().getPlayers()) {
					list.forEach(s -> p.sendMessage(AuriBungeeUtil.centerMessage(s)));

				}
			}
		},0L,PluginConfig.get().getAnnouncementCooldown(),TimeUnit.MINUTES);
	}

	public void announceString(String string) {
		for (ProxiedPlayer player : Main.get().getProxy().getPlayers()) {
			player.sendMessage(AuriBungeeUtil.colorString(config.getAnnouncePrefix() + string));
		}
	}

	public void announceTitle(String string) {
		Title title = ProxyServer.getInstance().createTitle();
		title.reset();
		title.fadeIn(0);
		title.fadeOut(10);
		title.stay(10);
		title.title(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', string)));

		for (ProxiedPlayer player : Main.get().getProxy().getPlayers()) {
			title.send(player);
		}
	}

	public void announceActionbar(String string) {
		for (ProxiedPlayer player : Main.get().getProxy().getPlayers()) {
			player.sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(AuriBungeeUtil.colorString(string)));
		}
	}


	public static AnnounceController get() {
		return Objects.requireNonNullElseGet(instance, () -> instance = new AnnounceController());
	}

}
