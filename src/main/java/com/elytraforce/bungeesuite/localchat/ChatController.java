package com.elytraforce.bungeesuite.localchat;

import com.elytraforce.aUtils.chat.BChat;
import com.elytraforce.aUtils.logger.BLogger;
import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.antiswear.Filters;
import com.elytraforce.bungeesuite.config.PluginConfig;
import com.elytraforce.bungeesuite.discord.DiscordController;
import com.elytraforce.bungeesuite.localchat.model.Delta;
import com.elytraforce.bungeesuite.localchat.player.ChatPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.awt.*;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

@SuppressWarnings("deprecated")
public class ChatController {

    private static ChatController instance;
    private final JedisPool pool;
    private final Filters filters;
    private final HashMap<ProxiedPlayer, Boolean> spyMap;

    public boolean getIsSpying(ProxiedPlayer player) {
        return spyMap.getOrDefault(player, false);
    }

    public void enableSpy(ProxiedPlayer player) {
        spyMap.put(player, true);
    }

    public void disableSpy(ProxiedPlayer player) {
        spyMap.put(player, false);
    }

    private final String BUNGEE_CHANNEL = "channel_bungee";

    private ChatController() {
        String password = PluginConfig.get().getRedisPassword();
        String ip = PluginConfig.get().getRedisIP();
        int port = PluginConfig.get().getRedisPort();

        if (password == null || password.equals(""))
            pool = new JedisPool(new JedisPoolConfig(), ip, port, 0);
        else
            pool = new JedisPool(new JedisPoolConfig(), ip, port, 0, password);

        Main.get().getProxy().getScheduler().runAsync(Main.get(), () -> {
            try (Jedis jedis = pool.getResource()){
                jedis.subscribe(new BungeeReciever(),BUNGEE_CHANNEL);

            } catch (Exception e) {
                BLogger.error("Error connecting to redis - " + e.getMessage());
                BLogger.error("Broken redis pool");
            }
            BLogger.error("Registered the listener!");
        });

        filters = Main.get().getFilters();
        this.spyMap = new HashMap<>();
    }

    public void handleCommandSpy(ChatEvent event) {
        if (!event.isCommand()) {
            if (event.getMessage().startsWith("/mc") || event.getMessage().startsWith("/ac")) { return; }
        }

        ProxiedPlayer sender = (ProxiedPlayer) event.getSender();

        for (ProxiedPlayer player : Main.get().getProxy().getPlayers()) {

            if (sender.equals(player)) { continue; }

            if (getIsSpying(player)) {
                if (sender.hasPermission("elytraforce.admin")) {
                    if (player.hasPermission("elytraforce.owner")) {
                        player.sendMessage(PluginConfig.get().getPrefix() + ChatColor.translateAlternateColorCodes(
                                '&', "&7(" + sender.getServer().getInfo().getName() + "&7) &b" + sender.getName() + "&f: &7" + event.getMessage()));
                    } else {
                        return;
                    }
                } else {
                    player.sendMessage(PluginConfig.get().getPrefix() + ChatColor.translateAlternateColorCodes(
                            '&', "&7(" + sender.getServer().getInfo().getName() + "&7) &b" + sender.getName() + "&f: &7" + event.getMessage()));
                }

            }
        }
    }


    public void sendNormal(String message, ChatPlayer player) {
        player.asProxiedPlayer().getServer().getInfo().getPlayers().stream()
                .filter(p -> !player.isIgnored(p))
                .forEach(player1 -> {
                    player1.sendMessage(formatNormal(message,player).create());
                });
                if (player.getSettings().isDiscordOut()) { this.callDiscord(player.asProxiedPlayer(),message); }
    }

    private ComponentBuilder formatNormal(String message, ChatPlayer player) {

        BaseComponent[] first = TextComponent.fromLegacyText(BChat.colorString(player.getStats().getLevel() + " &7| " + player.getOnlineGroupName()));
        BaseComponent[] second = TextComponent.fromLegacyText(BChat.colorString("&r" + player.getNickname() + " &r&f»&f "));
        BaseComponent[] third;

        if (player.isOnlineDonator()) {
            third = TextComponent.fromLegacyText(BChat.colorString(message
                    .trim().replaceAll(" +", " ")));
        } else {
            third = TextComponent.fromLegacyText(message
                    .trim().replaceAll(" +", " "));
        }

        BaseComponent[] hover = TextComponent.fromLegacyText( BChat.colorString(
                player.getOnlineGroupName() + "&r" )
        );

        BaseComponent[] hover1 = TextComponent.fromLegacyText( BChat.colorString(
                "&7" + player.getName() + "&7\n \n"
                        + "&7Level: &b" + player.getStats().getLevel() + "\n"
                        + "&7Balance: &e" + player.getStats().getMoney() + "⛃\n\n"
                        + "&7Currently playing on &7(&f" + player.getServerName() + "&7)" )
        );

        return new ComponentBuilder("").append(first).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("").append(hover).bold(false).append(hover1).create())).event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,player.getName())).bold(false).append(second).append(third).event((HoverEvent) null);
    }

    public void sendStaff(String message, ChatPlayer player) {
        Main.get().getProxy().getPlayers().stream()
                .filter(p -> p.hasPermission("elytraforce.helper"))
                .forEach(player1 -> player1.sendMessage(formatStaff(message,player).create()));
    }

    private ComponentBuilder formatStaff(String message, ChatPlayer player) {
        BaseComponent[] first = TextComponent.fromLegacyText(BChat.colorString("&b&lSTAFF&r "));
        BaseComponent[] second = TextComponent.fromLegacyText(BChat.colorString("&r&7>> &r" + player.getOnlineGroupName()));
        BaseComponent[] third = TextComponent.fromLegacyText(BChat.colorString("&r" + player.getNickname() + "&r&f:&f "));
        BaseComponent[] fourth = TextComponent.fromLegacyText(BChat.colorString(message
                .trim().replaceAll(" +", " ")));

        return new ComponentBuilder("").bold(true).append(first).bold(false).append(second).bold(false).append(third).bold(false).append(fourth);
    }

    public void sendAdmin(String message, ChatPlayer player) {
        Main.get().getProxy().getPlayers().stream()
                .filter(p -> p.hasPermission("elytraforce.admin"))
                .forEach(player1 -> player1.sendMessage(formatAdmin(message,player).create()));
    }

    private ComponentBuilder formatAdmin(String message, ChatPlayer player) {
        BaseComponent[] first = TextComponent.fromLegacyText(BChat.colorString("&4&lADMIN&r "));
        BaseComponent[] second = TextComponent.fromLegacyText(BChat.colorString("&r&7>> &r" + player.getOnlineGroupName()));
        BaseComponent[] third = TextComponent.fromLegacyText(BChat.colorString("&r" + player.getNickname() + "&r&f:&f "));
        BaseComponent[] fourth = TextComponent.fromLegacyText(BChat.colorString(message
                .trim().replaceAll(" +", " ")));

        return new ComponentBuilder("").bold(true).append(first).bold(false).append(second).bold(false).append(third).bold(false).append(fourth);
    }

    public void sendGlobal(String message, ChatPlayer player) {
        if (filters.handleString(message)) {
            player.asProxiedPlayer().sendMessage(PluginConfig.get().getPrefix() + BChat.colorString("&cPlease do not swear on ElytraForce!"));
            return;
        }

        Main.get().getProxy().getPlayers().stream()
                .filter(p -> !player.isIgnored(p))
                .forEach(player1 -> player1.sendMessage(formatGlobal(message,player).create())); if (player.getSettings().isDiscordOut()) { this.callDiscord(player.asProxiedPlayer(),message); }
    }

    private ComponentBuilder formatGlobal(String message, ChatPlayer player) {
        BaseComponent[] first = TextComponent.fromLegacyText(BChat.colorString("&9&lGLOBAL&r "));
        BaseComponent[] second = TextComponent.fromLegacyText(BChat.colorString("&r&7>> &r" + player.getOnlineGroupName()));
        BaseComponent[] third = TextComponent.fromLegacyText(BChat.colorString("&r" + player.getNickname() + "&r&f:&f "));
        BaseComponent[] fourth = TextComponent.fromLegacyText(BChat.colorString(message
                .trim().replaceAll(" +", " ")));

        return new ComponentBuilder("").bold(true).append(first).bold(false).append(second).bold(false).append(third).bold(false).append(fourth);
    }

    public void sendReply(String message, ChatPlayer player) {
        try {
            player.getPmReciever().getName();
        } catch (NullPointerException exception) {
            player.asProxiedPlayer().sendMessage(PluginConfig.get().getPrefix() + BChat.colorString("&cYour target is invalid or offline!"));
            player.setPmReciever(null);
            return;
        }

        if (filters.handleString(message)) {
            player.asProxiedPlayer().sendMessage(PluginConfig.get().getPrefix() + BChat.colorString("&cPlease do not swear on ElytraForce!"));
            return;
        }

        if (player.isIgnored(player.getPmReciever())) {
            player.asProxiedPlayer().sendMessage(PluginConfig.get().getPrefix() + BChat.colorString("&cYou have your target set as ignored!"));
            return;
        }

        if (!player.getPmReciever().isIgnored(player)) {
            player.getPmReciever().asProxiedPlayer().sendMessage(formatPMIn(message,player.getPmReciever(),player).create());
        }
        player.asProxiedPlayer().sendMessage(formatPMOut(message,player,player.getPmReciever()).create());
    }

    public void sendPM(String message, ChatPlayer player, ChatPlayer target) {
        try {
            target.getName();
        } catch (NullPointerException exception) {
            player.asProxiedPlayer().sendMessage(PluginConfig.get().getPrefix() + BChat.colorString("&cYour target is invalid or offline!"));
            player.setPmReciever(null);
            return;
        }

        if (filters.handleString(message)) {
            player.asProxiedPlayer().sendMessage(PluginConfig.get().getPrefix() + BChat.colorString("&cPlease do not swear on ElytraForce!"));
            return;
        }

        if (player.isIgnored(target)) {
            player.asProxiedPlayer().sendMessage(PluginConfig.get().getPrefix() + BChat.colorString("&cYou have your target set as ignored!"));
            return;
        }

        if (!player.getPmReciever().isIgnored(player)) {
            player.getPmReciever().asProxiedPlayer().sendMessage(formatPMIn(message,target,player).create());
        }
        player.asProxiedPlayer().sendMessage(formatPMOut(message,player,target).create());
    }

    private ComponentBuilder formatPMOut(String message, ChatPlayer player, ChatPlayer target) {
        BaseComponent[] first = TextComponent.fromLegacyText(BChat.colorString("&7You &7-> &e" + target.getName() + " &r"));
        BaseComponent[] second = TextComponent.fromLegacyText(BChat.colorString(message
                .trim().replaceAll(" +", " ")));

        return new ComponentBuilder("").append(first).bold(false).append(second);
    }

    private ComponentBuilder formatPMIn(String message, ChatPlayer player, ChatPlayer target) {
        BaseComponent[] first = TextComponent.fromLegacyText(BChat.colorString("&e" + target.getName() + " &7-> &7You" + " &r"));
        BaseComponent[] second = TextComponent.fromLegacyText(BChat.colorString(message
                .trim().replaceAll(" +", " ")));

        return new ComponentBuilder("").append(first).bold(false).append(second);
    }


    public void callDiscord(ProxiedPlayer player, String message) {
        String msg = message.replaceAll("@everyone", "*snip*").replaceAll("@here", "*snip*");

        EmbedBuilder builder = new EmbedBuilder()
                .setColor(Color.CYAN)
                .setTitle(player.getName())
                .setThumbnail("https://minotar.net/avatar/" + player.getName() + "/40")
                .setDescription(msg);
        DiscordController.getChan().sendMessage(builder);
    }

    public Delta decryptDelta(String string) {
        String[] parts = string.split(":");
        UUID id = UUID.fromString(parts[0]);
        int amount = Integer.parseInt(parts[1]);
        Delta.DeltaEnum change = Delta.DeltaEnum.valueOf(parts[2]);
        Delta.ValueEnum value = Delta.ValueEnum.valueOf(parts[3]);
        return new Delta(id,amount,change,value);
    }

    public ChatPlayer.PlayerDelta decryptPlayerDelta(String string) {
        String[] parts = string.split(":");
        UUID id = UUID.fromString(parts[0]);
        String action = parts[1];
        ChatPlayer.PlayerDelta.TargetEnum change = ChatPlayer.PlayerDelta.TargetEnum.valueOf(parts[2]);
        return new ChatPlayer.PlayerDelta(id,action,change);
    }

    public String encryptPlayerDelta(ChatPlayer.PlayerDelta playerDelta) {
        return playerDelta.getTarget().toString() + ":" + playerDelta.getAction() + ":" + playerDelta.getType().name();
    }



    public class BungeeReciever extends JedisPubSub {
        @Override
        public void onMessage(String channel, final String msg) {
            //decrypt delta
            
            Delta delta = decryptDelta(msg);
            if (PlayerController.get().getPlayer(delta.getTarget()) != null) {
                PlayerController.get().getPlayer(delta.getTarget()).adjust(delta);
            }
        }
    }

    /*ONLY USE this if it is required, blocks thread, must be used async*/


    public static ChatController get() {
        return Objects.requireNonNullElseGet(instance, () -> instance = new ChatController());
    }



}
