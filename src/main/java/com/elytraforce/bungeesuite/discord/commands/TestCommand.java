package com.elytraforce.bungeesuite.discord.commands;

import com.elytraforce.bungeesuite.discord.DiscordController;
import org.javacord.api.event.message.MessageCreateEvent;

public class TestCommand extends DiscordCommand {

    public void onCommand(String[] args, MessageCreateEvent event) {
        event.getChannel().sendMessage(args.length + "");
        if (args.length != 2) {
            DiscordController.incorrectArgLength(event.getChannel(),2);
            return;
        }

        if (args[1].equalsIgnoreCase("hi")) {
            event.getChannel().sendMessage("Hi to you as well!");
        } else if (args[1].equalsIgnoreCase("die")) {
            event.getChannel().sendMessage("I will eat ur nipples!");
        } else {
            event.getChannel().sendMessage("CUM BITCH");
        }
    }

    @Override
    public String commandName() {
        return "test";
    }
}
