package com.elytraforce.bungeesuite.discord.commands;

import com.elytraforce.bungeesuite.config.PluginConfig;
import com.elytraforce.bungeesuite.discord.DiscordController;
import com.vdurmont.emoji.EmojiParser;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;
import java.util.ArrayList;

public class TodoCommand extends DiscordCommand {
    @Override
    public void onCommand(String[] args, MessageCreateEvent event) {
        if (args.length < 3) {
            DiscordController.incorrectArgLength(event.getChannel(),3); return;
        }
        if (!event.getMessageAuthor().canKickUsersFromServer()) { DiscordController.noPermissions(event.getChannel()); return; }

        if (args[1].equalsIgnoreCase("add")) {

            EmbedBuilder builder = new EmbedBuilder()
                    .setTitle("TODO | " + EmojiParser.removeAllEmojis(event.getMessageAuthor().getName()))
                    .setDescription(afterArgs(args,2)).setTimestampToNow()
                    .setColor(Color.RED);

            DiscordController.api.getTextChannelById(PluginConfig.get().getDiscordTodoID()).ifPresent(e -> e.sendMessage(builder));

            event.getMessage().delete();

        } else if (args[1].equalsIgnoreCase("complete")){
            if (!isNumeric(args[2])) { DiscordController.incorrectArgProvided(event.getChannel(), 2); }

            DiscordController.api.getMessageById(args[2], DiscordController.api.getTextChannelById(PluginConfig.get().getDiscordTodoID()).get()).whenComplete((m, e) -> {
                if (e != null) {
                    DiscordController.incorrectMessageID(event.getChannel());
                } else {
                    ArrayList<EmbedBuilder> blist = new ArrayList<>();

                    m.getEmbeds().forEach(c -> {
                        blist.add(c.toBuilder().setColor(Color.green));
                        if (!c.getTitle().get().contains("TODO")) {
                            EmbedBuilder builder = new EmbedBuilder()
                                    .setTitle("ERROR")
                                    .setColor(Color.red)
                                    .setDescription("That is not a TODO item!");
                            event.getChannel().sendMessage(builder);
                        }
                    });

                    m.edit(blist.get(0));
                }
            });
        } else if (args[1].equalsIgnoreCase("revoke")){
            if (!isNumeric(args[2])) { DiscordController.incorrectArgProvided(event.getChannel(), 2); }

            DiscordController.api.getMessageById(args[2], DiscordController.api.getTextChannelById(PluginConfig.get().getDiscordTodoID()).get()).whenComplete((m, e) -> {
                if (e != null) {
                    DiscordController.incorrectMessageID(event.getChannel());
                } else {
                    ArrayList<EmbedBuilder> blist = new ArrayList<>();

                    m.getEmbeds().forEach(c -> {
                        blist.add(c.toBuilder().setColor(Color.red));
                        if (!c.getTitle().get().contains("TODO")) {
                            EmbedBuilder builder = new EmbedBuilder()
                                    .setTitle("ERROR")
                                    .setColor(Color.red)
                                    .setDescription("That is not a TODO item!");
                            event.getChannel().sendMessage(builder);
                        }
                    });

                    m.edit(blist.get(0));
                }
            });
        } else if (args[1].equalsIgnoreCase("progress")){
            if (!isNumeric(args[2])) { DiscordController.incorrectArgProvided(event.getChannel(), 2); }

            DiscordController.api.getMessageById(args[2], DiscordController.api.getTextChannelById(PluginConfig.get().getDiscordTodoID()).get()).whenComplete((m, e) -> {
                if (e != null) {
                    DiscordController.incorrectMessageID(event.getChannel());
                } else {
                    ArrayList<EmbedBuilder> blist = new ArrayList<>();

                    m.getEmbeds().forEach(c -> {
                        blist.add(c.toBuilder().setColor(Color.orange));
                        if (!c.getTitle().get().contains("TODO")) {
                            EmbedBuilder builder = new EmbedBuilder()
                                    .setTitle("ERROR")
                                    .setColor(Color.red)
                                    .setDescription("That is not a TODO item!");
                            event.getChannel().sendMessage(builder);
                        }
                    });

                    m.edit(blist.get(0));
                }
            });
        } else {
            DiscordController.unknownSubCommand(event.getChannel(),args[1],"add","complete", "revoke", "progress");
        }
    }

    @Override
    public String commandName() {
        return "todo";
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}

