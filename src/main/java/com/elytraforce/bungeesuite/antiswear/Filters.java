package com.elytraforce.bungeesuite.antiswear;

import com.elytraforce.aUtils.chat.BChat;
import com.elytraforce.bungeesuite.antiswear.filters.BasicIntelligentFilter;
import com.elytraforce.bungeesuite.config.PluginConfig;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;

import java.util.ArrayList;
import java.util.List;


public class Filters {

    private List<Filter> filters = new ArrayList<>();
    private final PluginConfig config;

    public Filters() {
        this.config = PluginConfig.get();
    	filters.add(new BasicIntelligentFilter());
    }

    public void add(Filter filter) {
        filters.add(filter);
    }

    public List<Filter> getFilters() {
        return new ArrayList<Filter>(this.filters);
    }
    
    public boolean handleString(String string) {
        
        int vl = 0;
 
        // Overlap/apply filters
        for (Filter filter : filters) {
        	if (filter.filter(string)) {
        		vl = vl + filter.getVls();
        	}
        }

        return vl >= 10;
    }

    public void handleEvent(ChatEvent event) {
        String message = event.getMessage();
        
        int vl = 0;
 
        // Overlap/apply filters
        for (Filter filter : filters) {

        	if (filter.filter(message)) {
        		vl = vl + filter.getVls();
        	}

        }
        
        if (vl >= 10) {
            ((ProxiedPlayer) event.getSender()).sendMessage(config.getPrefix() + BChat.colorString("&cPlease do not swear on ElytraForce!"));
            event.setCancelled(true);
        }

    }

}
