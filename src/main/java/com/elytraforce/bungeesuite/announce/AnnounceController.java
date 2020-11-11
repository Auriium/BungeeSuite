package com.elytraforce.bungeesuite.announce;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.elytraforce.bungeesuite.Main;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;


public class AnnounceController {
	
	private static AnnounceController instance;
	
	private long nextRestart;
	private Map<Integer, String> warnings;
	
	static /* synthetic */ void access$1(final AnnounceController autoRestart, final long nextRestart) {
        autoRestart.nextRestart = nextRestart;
    }
	
	private void setup(final String arg0, final String arg1) {
        final int hours = Integer.parseInt(arg0.split(":")[0]);
        final int minutes = Integer.parseInt(arg0.split(":")[1]);
        final Calendar c = Calendar.getInstance(TimeZone.getTimeZone(arg1));
        c.add(5, 1);
        c.set(11, hours);
        c.set(12, minutes);
        c.set(13, 0);
        c.set(14, 0);
        if (c.getTimeInMillis() - System.currentTimeMillis() > 86400000L) {
            c.add(5, -1);
            this.nextRestart = c.getTimeInMillis();
            return;
        }
        this.nextRestart = c.getTimeInMillis();     
    }
	
	private AnnounceController() {
		
		Configuration config = Main.get().getConfig().getConfig();
        this.setup(config.getString("restartTime"), config.getString("restartTimeZone"));
        
        List<String> w = config.getStringList("warnings");
        if (w == null) {
            w = new ArrayList<String>();
        }
        
        this.warnings = new HashMap<Integer, String>();
        
        for (final String s : w) {
            this.warnings.put(Integer.parseInt(s.split(":")[0]), s.substring(s.split(":")[0].length() + 1));
        }
        
		Main.get().getProxy().getScheduler().schedule(Main.get(), new Runnable() {
			
			public void run() {
				final int seconds = (int)((AnnounceController.get().nextRestart - System.currentTimeMillis()) / 1000L);
	            if (seconds < 0) {
	            	final AnnounceController this$0 = instance;
	            	AnnounceController.access$1(this$0, nextRestart + 86400000L);
	            	return;
	            }
	            if (AnnounceController.get().warnings.containsKey(seconds)) {
	            	AnnounceController.get().announceString(AnnounceController.get().warnings.get(seconds));
	            	
	            }
	            if (seconds == 0) {
	            	AnnounceController.get().announceString("&cThe server is now restarting!");
	            }
	       }
		
	        
		},0L,20L,TimeUnit.MINUTES);
	}
	
	public void announceString(String string) {
		for (ProxiedPlayer player : Main.get().getProxy().getPlayers()) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.get().getConfig().getAnnouncePrefix() + string));
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
	
	
	public static AnnounceController get() {
		if (instance == null)  {
			return instance = new AnnounceController();
		} else {
			return instance;
		}
			
	}

}
