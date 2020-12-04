package com.elytraforce.bungeesuite.localchat.model;

import com.elytraforce.bungeesuite.Main;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class ChatPlayer {
    private final UUID uniqueId;
    private int level;
    private int experience;
    private int money;
    private boolean discord;
    private boolean pms;
    private String nickname;

    private ChatMode chatMode;
    private ChatPlayer messageReceiver;

    /*not offline safe*/
    public ProxiedPlayer asProxiedPlayer() {
        if (Main.get().getProxy().getPlayer(uniqueId) != null) {
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
    public boolean getDiscordEnabled() { return this.discord; }
    public boolean getPmsEnabled() { return this.pms; }
    public ChatMode getChatMode() { return this.chatMode; }

    /*not offline safe*/
    private String getNickname() {
        return Objects.requireNonNullElse(nickname, Main.get().getProxy().getPlayer(uniqueId).getDisplayName());
    }

    @Override
    public boolean equals(Object toCompare) {
        if (!(toCompare instanceof ChatPlayer))
            return false;
        return uniqueId.equals(((ChatPlayer) toCompare).uniqueId);
    }

    public ChatPlayer(UUID uuid, Integer level, Integer experience, Integer money, String nickname, boolean discord, boolean pms) {
        this.uniqueId = uuid;
        this.level = level;
        this.experience = experience;
        this.money = money;
        this.nickname = nickname;
        this.discord = discord;
        this.pms = pms;
        this.chatMode = ChatMode.NORMAL;
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

    public void updateOrInsert() {

    }
}
