package com.elytraforce.bungeesuite.discord.commands;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.config.PluginConfig;
import com.elytraforce.bungeesuite.discord.DiscordController;
import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.lang3.ArrayUtils;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

public class ChangelogCommand extends DiscordCommand {
    @Override
    public void onCommand(String[] args, MessageCreateEvent event) {
        if (args.length < 2) { DiscordController.incorrectArgLength(event.getChannel(),2); return; }
        if (!event.getMessageAuthor().canKickUsersFromServer()) { DiscordController.noPermissions(event.getChannel()); return; }

        String str = afterArgs(args,1);

        EmbedBuilder builder = new EmbedBuilder()
                .setTitle("CHANGELOG")
                .setDescription(str).setTimestampToNow()
                .setAuthor(EmojiParser.removeAllEmojis(event.getMessageAuthor().getName()));


        DiscordController.api.getTextChannelById(PluginConfig.get().getDiscordChangelogID()).ifPresent(e -> e.sendMessage(builder));
    }

    @Override
    public String commandName() {
        return "changelog";
    }
}
