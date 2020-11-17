package com.elytraforce.bungeesuite.discord.commands;

import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public abstract class DiscordCommand implements MessageCreateListener {

    public abstract void onCommand(String[] args, MessageCreateEvent event);

    public abstract String commandName();

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        String[] splitArray = event.getMessageContent().split("\\s+");
        if (splitArray[0].equalsIgnoreCase("!" + commandName())) {
            event.getMessage().delete();
            onCommand(splitArray, event);
        }
    }

    public String afterArgs(String[] args, int start) {
        StringBuilder sb = new StringBuilder();
        for(int i = start; i < args.length; i++) {
            if (i > 0) sb.append(" ");
            sb.append(args[i]);
        }

        return sb.toString();
    }

}
