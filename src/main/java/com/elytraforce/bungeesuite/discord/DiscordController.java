package com.elytraforce.bungeesuite.discord;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.discord.commands.ChangelogCommand;
import com.elytraforce.bungeesuite.discord.commands.TestCommand;
import com.elytraforce.bungeesuite.discord.commands.TodoCommand;
import com.elytraforce.bungeesuite.discord.reactions.AntiswearEditReaction;
import com.elytraforce.bungeesuite.discord.reactions.AntiswearReaction;
import com.elytraforce.bungeesuite.listeners.DiscordListener;
import org.apache.commons.lang3.StringUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.activity.ActivityType;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.*;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class DiscordController {
	
	public static String token = null; // Bot token
	public static DiscordApi api = null; // Api Instance
	private static DiscordController instance;

	public static void incorrectArgProvided(TextChannel chan, int position) {
		EmbedBuilder builder = new EmbedBuilder()
				.setColor(Color.red)
				.setTitle("ERROR")
				.setDescription("Incorrect argument provided at arg " + position + 1);
		chan.sendMessage(builder);
	}

	public static void incorrectArgLength(TextChannel chan, int length) {
		EmbedBuilder builder = new EmbedBuilder()
				.setColor(Color.red)
				.setTitle("ERROR")
				.setDescription("Incorrect command length! Command should be " + length + " args long!");
		chan.sendMessage(builder);
	}

	public static void incorrectMessageID(TextChannel chan) {
		EmbedBuilder builder = new EmbedBuilder()
				.setColor(Color.red)
				.setTitle("ERROR")
				.setDescription("That is an invalid message ID!");
		chan.sendMessage(builder);
	}

	public static void noPermissions(TextChannel chan) {
		EmbedBuilder builder = new EmbedBuilder()
				.setColor(Color.red)
				.setTitle("ERROR")
				.setDescription("You do not have permissions to do that!");
		chan.sendMessage(builder);
	}

	public static void unknownSubCommand(TextChannel chan, String unknownCommand, String... correctCommands) {
		String result = StringUtils.join(correctCommands, ", ");

		EmbedBuilder builder = new EmbedBuilder()
				.setColor(Color.red)
				.setTitle("ERROR")
				.setFooter("Commands: " + result)
				.setDescription("" + unknownCommand + " is not a valid subcommand! Correct commands displayed below:");
		chan.sendMessage(builder);
	}

	// real shit starts here
	
	private DiscordController() {
		DiscordController.token = Main.get().getConfig().getDiscordToken();
		try {
			api = new DiscordApiBuilder().setToken(token).login().join();
		} catch (CompletionException IllegalStateException) {
			Main.get().getLogger().info("Connection Error. Did you put a valid token in the config?");
		}
        Main.get().getLogger().info("Bot Invite Link: " + api.createBotInvite());

        api.addReconnectListener(event -> { Main.get().getLogger().info(("Reconnected to Discord.")); });
        api.addResumeListener(event -> { Main.get().getLogger().info(("Resumed connection to Discord.")); });
        api.setMessageCacheSize(10, 60*60);

        api.addMessageCreateListener(new ChangelogCommand());
        api.addMessageCreateListener(new TestCommand());
        api.addMessageCreateListener(new TodoCommand());

        api.addMessageCreateListener(new AntiswearReaction());
        api.addMessageEditListener(new AntiswearEditReaction());

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
		} catch (InterruptedException | ExecutionException e) {
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