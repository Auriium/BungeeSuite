package com.elytraforce.bungeesuite.discord.commands;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;

public class ShitDevCommand extends DiscordCommand {

    @Override
    public void onCommand(String[] args, MessageCreateEvent event) {
        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.pink)
                .setTitle("UWU OWO CREDITS")
                .setTimestampToNow()
                .setDescription("This shitty discord bot (and server) brought to you by shit dev Aurium_ and friends co.");
        event.getChannel().sendMessage(builder);
    }

    @Override
    public String commandName() {
        return "shitdev";
    }
}
