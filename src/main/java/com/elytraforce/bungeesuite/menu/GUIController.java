package com.elytraforce.bungeesuite.menu;

import net.md_5.bungee.api.plugin.Listener;

import java.util.ArrayList;
import java.util.Objects;

public class GUIController implements Listener {

    private ArrayList<GUI> guis;

    private GUIController() {

    }

    public void registerGUI(GUI gui) {

    }

    private static GUIController instance;

    public static GUIController get() {
        return Objects.requireNonNullElseGet(instance, GUIController::new);
    }

}
