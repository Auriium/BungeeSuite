package com.elytraforce.bungeesuite.menu;

import de.exceptionflug.protocolize.inventory.Inventory;
import de.exceptionflug.protocolize.inventory.InventoryType;
import de.exceptionflug.protocolize.inventory.event.InventoryClickEvent;
import de.exceptionflug.protocolize.inventory.event.InventoryCloseEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public abstract class GUI {

    protected Inventory guiInventory;
    protected int rows;
    protected String name;


    public abstract void createGUI();

    public abstract void show(ProxiedPlayer player);

    public abstract void onClick(InventoryClickEvent event);

    public abstract void onClose(InventoryCloseEvent event);

    public Inventory getInventory() {
        if (guiInventory == null) {
            switch (rows) {
                case 1:
                    this.guiInventory = new Inventory(InventoryType.GENERIC_9X1, new TextComponent(name));
                case 2:
                    this.guiInventory = new Inventory(InventoryType.GENERIC_9X2, new TextComponent(name));
                case 3:
                    this.guiInventory = new Inventory(InventoryType.GENERIC_9X3, new TextComponent(name));
                case 4:
                    this.guiInventory = new Inventory(InventoryType.GENERIC_9X4, new TextComponent(name));
                case 5:
                    this.guiInventory = new Inventory(InventoryType.GENERIC_9X5, new TextComponent(name));
                case 6:
                    this.guiInventory = new Inventory(InventoryType.GENERIC_9X6, new TextComponent(name));
                default:
                    this.guiInventory = new Inventory(InventoryType.GENERIC_9X1, new TextComponent(name));
            }

        }

        return guiInventory;
    }

}
