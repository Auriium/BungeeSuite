package com.elytraforce.bungeesuite.hook;

import codecrafter47.bungeetablistplus.api.bungee.Variable;
import com.elytraforce.bungeesuite.localchat.ChatController;
import com.elytraforce.bungeesuite.localchat.model.ChatPlayer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class TabListVar extends Variable {

    public TabListVar() {
        super("bs_nickname");
    }

    @Override
    public String getReplacement(ProxiedPlayer proxiedPlayer) {
        ChatPlayer player = ChatController.get().getPlayer(proxiedPlayer);
        if (player != null) {
            return player.getNickname();
        } else {
            return null;
        }
    }
}
