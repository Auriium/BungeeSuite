package com.elytraforce.bungeesuite.localchat.model;

import java.util.UUID;

public class PlayerDelta {

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
