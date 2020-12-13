package com.elytraforce.bungeesuite.localchat.player;

import com.elytraforce.aUtils.chat.BChat;
import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.localchat.model.ChatMode;
import com.elytraforce.bungeesuite.localchat.model.Delta;
import com.elytraforce.bungeesuite.storage.SQLStorage;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ChatPlayer {
    private Settings settings;
    private Stats stats;

    private UUID uniqueId;
    private String nickname;
    private String name;

    private ChatMode chatMode;
    private ChatPlayer messageReceiver;

    private ArrayList<UUID> ignoredPlayers;

    private boolean inDatabase;

    private User permissionsUser;

    public boolean isInDatabase() { return this.inDatabase; }
    /*not offline safe*/
    public ProxiedPlayer asProxiedPlayer() {
        if (isOnline()) {
            return Main.get().getProxy().getPlayer(uniqueId);
        } else {
            return null;
        }
    }

    public void setPmReciever(ChatPlayer rec) {this.messageReceiver = rec; }
    public void setChatMode(ChatMode mode) { this.chatMode = mode; }
    public void setInDatabase(boolean s) { inDatabase  = s; }
    public void setNickname(String string) { this.nickname = string; }
    public void setName(String name) { this.name = name; }

    public Settings getSettings() { return this.settings; }
    public Stats getStats() { return this.stats; }

    //mainly just use setSettings
    public void setSettings(Settings n) { this.settings = n; }
    public void setStats(Stats s) { this.stats = s; }

    public UUID getUUID() { return uniqueId; }
    public String getName() { return this.name; }
    public ChatMode getChatMode() { return this.chatMode; }
    public ChatPlayer getPmReciever() { return this.messageReceiver; }
    public String getNickname() {
        if (nickname == null) {
            return asProxiedPlayer().getDisplayName();
        } else {
            return BChat.colorString("&7~" + nickname);
        }
    }

    public boolean isIgnored(UUID id) {
        return this.getSettings().getIgnoredPlayers().contains(id);
    }

    public boolean isIgnored(ChatPlayer p) {
        return this.getSettings().getIgnoredPlayers().contains(p.getUUID());
    }

    public boolean isIgnored(ProxiedPlayer p) {
        return this.getSettings().getIgnoredPlayers().contains(p.getUniqueId());
    }

    public User getOnlinePermissions() { return this.permissionsUser; }

    public boolean isOnlineDonator() {
        return asProxiedPlayer().hasPermission("elytraforce.donator");
    }

    public CompletableFuture<Boolean> isOfflineDonator() {
        return LuckPermsProvider.get().getUserManager().loadUser(uniqueId)
                .thenApplyAsync(user -> {
                    return user.getNodes().stream().anyMatch(n -> n.equals(Node.builder("elytraforce.donator").build()));
                });
    }

    public CompletableFuture<User> getOfflineUser() {
        return LuckPermsProvider.get().getUserManager().loadUser(uniqueId);
    }

    public String getOnlineGroupName() {
        CachedMetaData metaData = permissionsUser.getCachedData().getMetaData();
        return Objects.requireNonNullElse(metaData.getPrefix(), "");
    }



    /*not offline safe*/
    public String getServerName() { return asProxiedPlayer().getServer().getInfo().getName(); }

    //DO NOT USE
    public String getNicknameInternal() {
        return nickname;
    }

    @Override
    public boolean equals(Object toCompare) {

        if (!(toCompare instanceof ChatPlayer))
            return false;
        return uniqueId.equals(((ChatPlayer) toCompare).uniqueId);
    }

    public ChatPlayer(UUID uuid, String name, String nickname, Settings settings, Stats stats, boolean inDatabase) {
        this.uniqueId = uuid;
        this.name = name;
        this.nickname = nickname;
        this.stats = stats;
        this.settings = settings;

        this.chatMode = ChatMode.NORMAL;
        this.inDatabase = inDatabase;

        this.permissionsUser = LuckPermsProvider.get().getUserManager().getUser(uuid);
    }


    public void adjust(Delta delta) {
        int amount = delta.getAmount(); if (delta.getChange().equals(Delta.DeltaEnum.DECREASE)) { amount = Math.negateExact(amount); }
        //interpret delta and adjust based on it
        switch (delta.getType()) {
            case XP:
                this.stats.setXp(this.stats.getXp() + amount);
                break;
            case LEVEL:
                this.stats.setLevel(this.stats.getLevel() + amount);
                break;
            case MONEY:
                this.stats.setMoney(this.stats.getMoney() + amount);
                break;
        }
    }

    public void adjustP(PlayerDelta delta) {
        switch (delta.getType()) {
            case CHAT_COLOR:
                this.settings.setChatColor(ChatColor.valueOf(delta.getAction()));
            case DISCORD_IN:
                this.settings.setDiscordIn(Boolean.getBoolean(delta.getAction()));
            case PM_ENABLED:
                this.settings.setPmsEnabled(Boolean.getBoolean(delta.getAction()));
            case DISCORD_OUT:
                this.settings.setDiscordOut(Boolean.getBoolean(delta.getAction()));
        }
    }

    public void update() {
            SQLStorage.get().updatePlayer(this);
    }

    public static class PlayerDelta {

        private final String action;
        private final UUID target;
        private final PlayerDelta.TargetEnum type;

        public PlayerDelta(UUID target, String action, PlayerDelta.TargetEnum type) {
            this.action = action;
            this.type = type;
            this.target = target;
        }

        public String getAction() { return this.action; }
        public UUID getTarget() { return target; }
        public PlayerDelta.TargetEnum getType() { return type; }

        public enum TargetEnum {
            DISCORD_IN,DISCORD_OUT,PM_ENABLED,CHAT_COLOR
        }

    }

    public static class Builder {

        private Settings settings;
        private Stats stats;

        private UUID id;
        private String name;
        private String nickname;

        private boolean inDatabase;

        public Builder() {
            this.settings = null;
            this.stats = null;
            this.id = null;
            this.nickname = null;
            this.name = null;
            this.inDatabase = false;
        }

        public Builder setName(String str) {
            this.name = str;
            return this;
        }

        public Builder setNickname(String str) {
            this.nickname = str;
            return this;
        }

        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder setSettings(Settings settings) {
            this.settings = settings;
            return this;
        }

        public Builder setStats(Stats stats) {
            this.stats = stats;
            return this;
        }

        public Builder setInDatabase(boolean inDatabase) {
            this.inDatabase = inDatabase;
            return this;
        }

        public ChatPlayer build() {
            return new ChatPlayer(id,name,nickname,settings,stats,inDatabase);
        }
    }

    public boolean isOnline() {
        return Main.get().getProxy().getPlayer(uniqueId) != null;
    }
}
