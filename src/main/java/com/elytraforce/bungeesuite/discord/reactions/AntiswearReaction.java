package com.elytraforce.bungeesuite.discord.reactions;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.discord.DiscordController;
import com.vdurmont.emoji.EmojiParser;
import net.md_5.bungee.api.ChatColor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.awt.*;

public class AntiswearReaction implements MessageCreateListener {
    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getChannel().getIdAsString().equalsIgnoreCase(Main.get().getConfig().getDiscordChannelID())) {

            if (event.getMessageAuthor().getId() == DiscordController.api.getClientId()) { return; }

            if (event.getMessageAuthor().asUser().get().isBot()) { return; }

            if (Main.get().getFilters().handleString(event.getMessageContent())) {
                EmbedBuilder builder = new EmbedBuilder()
                        .setColor(Color.red)
                        .setTitle("WARNING")
                        .setTimestampToNow()
                        .setDescription("Please do not swear on the ElytraForce Network!");

                event.getMessageAuthor().asUser().ifPresent(u -> u.sendMessage(builder));
                event.getMessage().delete();
                return;
            }

            String sender = EmojiParser.removeAllEmojis(event.getMessageAuthor().getName()).replaceAll("\\s", "");

            //remove all emojis from broadcast too.
            Main.get().broadcast(Main.get().getConfig().getDiscordPrefix() +
                    ChatColor.translateAlternateColorCodes('&', EmojiParser.parseToAliases("&7(" + sender + "&7)&f " + event.getReadableMessageContent())), "elytraforce.default");
        }
    }
}
