package com.elytraforce.bungeesuite.antiswear;

import java.util.ArrayList;
import java.util.List;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.antiswear.filters.BasicIntelligentFilter;

import com.elytraforce.bungeesuite.util.AuriBungeeUtil;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;


public class Filters {

    private List<Filter> filters = new ArrayList<Filter>();

    public Filters() {
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
            ((ProxiedPlayer) event.getSender()).sendMessage(Main.get().getConfig().getPrefix() + AuriBungeeUtil.colorString("&cPlease do not swear on ElytraForce!"));
            event.setCancelled(true);
        }

    }

}
