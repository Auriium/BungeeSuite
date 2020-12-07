package com.elytraforce.bungeesuite.menu;

import de.exceptionflug.protocolize.inventory.InventoryModule;
import de.exceptionflug.protocolize.inventory.event.InventoryClickEvent;
import de.exceptionflug.protocolize.inventory.event.InventoryCloseEvent;
import de.exceptionflug.protocolize.items.InventoryManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;

public class ChestGUI extends GUI {

    private ArrayList<GUIItem> items;

    public ChestGUI(String name, int size) {
        this.name = name;
        this.rows = size;
    }

    @Override
    public void createGUI() {

    }

    @Override
    public void show(ProxiedPlayer player) {
        InventoryModule.sendInventory(player,this.guiInventory);
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        
    }

    @Override
    public void onClose(InventoryCloseEvent event) {

    }

}
