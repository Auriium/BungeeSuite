package com.elytraforce.bungeesuite.localchat.player;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.UUID;

public class Settings {

    private boolean discordIn;
    private boolean discordOut;
    private boolean pmsEnabled;
    private ChatColor chatColor;
    private boolean chatEnabled;
    private ArrayList<UUID> ignoredPlayers;

    public ArrayList<UUID> getIgnoredPlayers() {
        return ignoredPlayers;
    }

    public void setIgnoredPlayers(ArrayList<UUID> ignoredPlayers) {
        this.ignoredPlayers = ignoredPlayers;
    }

    public boolean isDiscordIn() {
        return discordIn;
    }

    public void setDiscordIn(boolean discordIn) {
        this.discordIn = discordIn;
    }

    public boolean isDiscordOut() {
        return discordOut;
    }

    public void setDiscordOut(boolean discordOut) {
        this.discordOut = discordOut;
    }

    public boolean isPmsEnabled() {
        return pmsEnabled;
    }

    public void setPmsEnabled(boolean pmsEnabled) {
        this.pmsEnabled = pmsEnabled;
    }

    public ChatColor getChatColor() {
        return chatColor;
    }

    public void setChatColor(ChatColor chatColor) {
        this.chatColor = chatColor;
    }

    public boolean isChatEnabled() {
        return chatEnabled;
    }

    public void setChatEnabled(boolean chatEnabled) {
        this.chatEnabled = chatEnabled;
    }

    public Settings(boolean discordIn, boolean discordOut, boolean pmsEnabled, ChatColor color, boolean chatEnabled, ArrayList<UUID> ignoredPlayers) {
        this.discordIn = discordIn;
        this.discordOut = discordOut;
        this.pmsEnabled = pmsEnabled;
        this.chatColor = color;
        this.chatEnabled = chatEnabled;
        this.ignoredPlayers = ignoredPlayers;
    }

}
