package com.elytraforce.bungeesuite.discord.reactions;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.config.PluginConfig;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageEditEvent;
import org.javacord.api.listener.message.MessageEditListener;

import java.awt.*;

public class AntiswearEditReaction implements MessageEditListener {
    @Override
    public void onMessageEdit(MessageEditEvent event) {
        if (event.getChannel().getIdAsString().equalsIgnoreCase(PluginConfig.get().getDiscordChannelID())) {

            if (event.getMessageAuthor().get().asUser().get().isBot()) return;

            if (Main.get().getFilters().handleString(event.getNewContent())) {
                EmbedBuilder builder = new EmbedBuilder()
                        .setColor(Color.red)
                        .setTitle("WARNING")
                        .setTimestampToNow()
                        .setDescription("Please do not swear on the ElytraForce Network!");

                event.getMessageAuthor().get().asUser().ifPresent(u -> u.sendMessage(builder));
                event.deleteMessage();
            }
        }
    }
}
