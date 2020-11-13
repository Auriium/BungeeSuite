package com.elytraforce.bungeesuite.discord;

import com.elytraforce.bungeesuite.listeners.DiscordListener;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import com.elytraforce.bungeesuite.Main;
import com.vdurmont.emoji.EmojiParser;

import net.md_5.bungee.api.ChatColor;

import java.awt.Color;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.javacord.api.DiscordApi;

public class DiscordController {
	
	public static String token = null; // Bot token
	
	public static DiscordApi api = null; // Api Instance

	private static DiscordController instance;
	
	private DiscordController() {
		DiscordController.token = Main.get().getConfig().getDiscordToken();
		
		// Create an Instance of the DiscordApi
		try {
			api = new DiscordApiBuilder().setToken(token).login().join();
		} catch (CompletionException IllegalStateException) {
			Main.get().getLogger().info("Connection Error. Did you put a valid token in the config?");
		}
		
        // Print the invite url of the bot
        Main.get().getLogger().info("Bot Invite Link: " + api.createBotInvite());
        
        // Set Activity
        api.addMessageCreateListener(event -> {
            if (event.getChannel().getIdAsString().equalsIgnoreCase(Main.get().getConfig().getDiscordChannelID())) {
            	
            	if (event.getMessageAuthor().getId() == api.getClientId()) { return; }
            	
            	if (event.getMessageAuthor().asUser().get().isBot()) { return; }

            	if (Main.get().getFilters().handleString(event.getMessageContent())) {
					EmbedBuilder builder = new EmbedBuilder()
							.setColor(Color.red)
							.setTitle("WARNING")
							.setTimestampToNow()
							.setDescription("Please do not swear on the ElytraForce Network!");

            		event.getMessageAuthor().asUser().get().sendMessage(builder);
            		event.getMessage().delete();
            		return;
				}
            	
            	String sender = EmojiParser.removeAllEmojis(event.getMessageAuthor().getName()).replaceAll("\\s", "");
            	
            	//remove all emojis from broadcast too.
            	Main.get().broadcast(Main.get().getConfig().getDiscordPrefix() + 
            			ChatColor.translateAlternateColorCodes('&', EmojiParser.parseToAliases("&7(" + sender + "&7)&f " + event.getReadableMessageContent())), "elytraforce.default");
            }
        });
        
        // Add Reconnect Listener to re-add status
        api.addReconnectListener(event -> {
        	Main.get().getLogger().info(("Reconnected to Discord."));
        });
        
        api.addResumeListener(event -> {
        	Main.get().getLogger().info(("Resumed connection to Discord."));
        });
        
        api.setMessageCacheSize(10, 60*60);

        api.addMessageEditListener(event -> {
			if (event.getChannel().getIdAsString().equalsIgnoreCase(Main.get().getConfig().getDiscordChannelID())) {
				if (event.getMessageAuthor().get().asUser().get().isBot()) { return; }

				if (Main.get().getFilters().handleString(event.getNewContent())) {
					EmbedBuilder builder = new EmbedBuilder()
							.setColor(Color.red)
							.setTitle("WARNING")
							.setTimestampToNow()
							.setDescription("Please do not swear on the ElytraForce Network!");

					event.getMessageAuthor().get().asUser().get().sendMessage(builder);
					event.deleteMessage();
				}
			}
		});
        
        api.addMessageCreateListener(event -> {
            if (event.getMessageContent().startsWith("!changelog")) {
            	
            	if (!event.getMessageAuthor().isServerAdmin()) { return; }
            	
            	String formatted = event.getMessageContent().substring(10);
            			
            	EmbedBuilder builder = new EmbedBuilder()
            	.setTitle("CHANGELOG")
            		.setDescription(formatted).setTimestampToNow()
            		.setAuthor(EmojiParser.removeAllEmojis(event.getMessageAuthor().getName()));
            	
            	
            	api.getTextChannelById(Main.get().getConfig().getDiscordChangelogID()).get().sendMessage(builder);
                
            	event.getMessage().delete();
                
            }

            if (event.getMessageContent().startsWith("!todo_complete")) {
				if (!event.getMessageAuthor().canKickUsersFromServer()) { return; }

				String formatted = event.getMessageContent().substring(14);

				api.getMessageById(formatted, event.getChannel()).whenComplete((m,e) -> {
					if (e != null) {
						EmbedBuilder builder = new EmbedBuilder()
								.setTitle("ERROR")
								.setColor(Color.red)
								.setDescription("No such message ID exists!");
						event.getChannel().sendMessage(builder);
					} else {

						m.getEmbeds().forEach(c -> {
							c.toBuilder().setColor(Color.green);
						});
					}
				});
			}
            
            if (event.getMessageContent().startsWith("!todo")) {
            	
            	if (!event.getMessageAuthor().canKickUsersFromServer()) { return; }
            	
            	String formatted = event.getMessageContent().substring(5);
            			
            	EmbedBuilder builder = new EmbedBuilder()
            	.setTitle("TODO")
            		.setDescription(formatted).setTimestampToNow()
            		.setAuthor(EmojiParser.removeAllEmojis(event.getMessageAuthor().getName()))
            		.setColor(Color.RED);

					api.getTextChannelById(Main.get().getConfig().getDiscordTodoID()).ifPresent(e -> {
						e.sendMessage(builder);
					});
                
            	event.getMessage().delete();
                
            }
        });
        
        Main.get().getProxy().getScheduler().schedule(Main.get(), new Runnable() {

			@Override
			public void run() {
				api.updateActivity(ActivityType.PLAYING, "ElytraForce | " + Main.get().getProxy().getOnlineCount() + " are online!");
			}
        	
        }, 0L, 20L, TimeUnit.SECONDS);

		Main.get().getProxy().getPluginManager().registerListener(Main.get(), new DiscordListener(Main.get(), Main.get().getConfig().getDiscordChannelID()));
	}
	
	public static String getBotOwner(MessageCreateEvent event) {
    	String bot_owner = "<@";
    	try {
			bot_owner += Long.toString(event.getApi().getApplicationInfo().get().getOwnerId());
			bot_owner += ">";
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return bot_owner;
	}
	
	// Sets the footer, done here to keep it standardised.
	public static void setFooter(EmbedBuilder embed) {
		embed.setFooter("AuriDiscord | " + Main.get().getDescription().getVersion().toString());
	}

	public static DiscordController get() {
		if (instance == null) {
			return instance = new DiscordController();
		} else {
			return instance;
		}
	}
	
	
	
}