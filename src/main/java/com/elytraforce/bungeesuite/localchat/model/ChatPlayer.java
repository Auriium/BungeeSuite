package com.elytraforce.bungeesuite.localchat.model;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.storage.SQLStorage;
import com.elytraforce.bungeesuite.util.AuriBungeeUtil;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ChatPlayer {
    private final UUID uniqueId;
    private int level;
    private int experience;
    private int money;
    private boolean discord_in;
    private boolean discord_out;
    private boolean pms;
    private String nickname;
    private ChatColor color;

    private ChatMode chatMode;
    private ChatPlayer messageReceiver;

    private boolean inDatabase;

    private User permissionsUser;

    /*not offline safe*/
    public ProxiedPlayer asProxiedPlayer() {
        if (isOnline()) {
            return Main.get().getProxy().getPlayer(uniqueId);
        } else {
            return null;
        }
    }

    //make these relay delta related information. ( e.g. getBalance needs to return balance PLUS the combined balances stored in all deltas)
    public UUID getUUID() { return uniqueId; }
    public Integer getLevel() { return level;  }
    public Integer getExperience() { return experience; }
    public Integer getMoney() { return this.money; }
    public boolean getSendDiscord() { return this.discord_out; }
    public boolean getRecieveDiscord() { return this.discord_in; }
    public boolean getPmsEnabled() { return this.pms; }
    public ChatMode getChatMode() { return this.chatMode; }
    public ChatPlayer getPmReciever() { return this.messageReceiver; }
    public ChatColor getChatColor() { return this.color; }

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

    public String getOnlineGroupName() {
        CachedMetaData metaData = permissionsUser.getCachedData().getMetaData();
        return Objects.requireNonNullElse(metaData.getPrefix(), "");
    }

    public void setPmReciever(ChatPlayer rec) {this.messageReceiver = rec; }
    public void setChatMode(ChatMode mode) { this.chatMode = mode; }
    public void setInDatabase(boolean s) { inDatabase  = s; }
    public void setNickname(String string) { this.nickname = string; }

    /*not offline safe*/
    public String getNickname() {
        if (nickname == null) {
            return asProxiedPlayer().getDisplayName();
        } else {
            return AuriBungeeUtil.colorString("&7~" + nickname);

        }
    }

    /*not offline safe*/
    public String getName() {
        return asProxiedPlayer().getDisplayName();
    }

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

    public ChatPlayer(UUID uuid, Integer level, Integer experience, Integer money, String nickname, boolean discord_in, boolean discord_out, boolean pms, boolean inDatabase, ChatColor color) {
        this.uniqueId = uuid;
        this.level = level;
        this.experience = experience;
        this.money = money;
        this.nickname = nickname;
        this.discord_in = discord_in;
        this.discord_out = discord_out;
        this.pms = pms;
        this.color = color;
        this.chatMode = ChatMode.NORMAL;
        this.inDatabase = inDatabase;

        this.permissionsUser = LuckPermsProvider.get().getUserManager().getUser(uuid);

    }


    public void adjust(Delta delta) {
        int amount = delta.getAmount(); if (delta.getChange().equals(Delta.DeltaEnum.DECREASE)) { amount = Math.negateExact(amount); }
        //interpret delta and adjust based on it
        switch (delta.getType()) {
            case XP:
                this.experience = this.experience + amount;
                break;
            case LEVEL:
                this.level = this.level + amount;
                break;
            case MONEY:
                this.money = this.money + amount;
                break;
        }
    }

    public void adjustP(PlayerDelta delta) {
        switch (delta.getType()) {
            case CHAT_COLOR:
                this.color = ChatColor.valueOf(delta.getAction());
            case DISCORD_IN:
                this.discord_in = Boolean.getBoolean(delta.getAction());
            case PM_ENABLED:
                this.pms = Boolean.getBoolean(delta.getAction());
            case DISCORD_OUT:
                this.discord_out = Boolean.getBoolean(delta.getAction());
        }
    }

    public void update() {
        if (inDatabase) {
            SQLStorage.get().updatePlayer(this);
        } else {
            SQLStorage.get().insertPlayer(this);
        }
    }

    public boolean isOnline() {
        return Main.get().getProxy().getPlayer(uniqueId) != null;
    }
}
