package com.elytraforce.bungeesuite.localchat;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.config.PluginConfig;
import com.elytraforce.bungeesuite.discord.DiscordController;
import com.elytraforce.bungeesuite.localchat.model.ChatMode;
import com.elytraforce.bungeesuite.localchat.model.ChatPlayer;
import com.elytraforce.bungeesuite.localchat.model.Delta;
import com.elytraforce.bungeesuite.punish.PunishController;
import com.elytraforce.bungeesuite.storage.SQLStorage;
import com.elytraforce.bungeesuite.util.AuriBungeeUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.apache.commons.lang3.ArrayUtils;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ChatController {
    private ArrayList<ChatPlayer> players;
    private static ChatController instance;
    private JedisPool pool;

    private static final String BUNGEE_CHANNEL = "channel_bungee";

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
                AuriBungeeUtil.logError("Error connecting to redis - " + e.getMessage());
                AuriBungeeUtil.logError("Broken redis pool");
            }
            AuriBungeeUtil.logError("Registered the listener!");
        });

        Main.get().getProxy().getScheduler().schedule(Main.get(), () -> {
            for (ChatPlayer player : players) {
                player.update();
            }
        },2L,TimeUnit.MINUTES);


        this.players = new ArrayList<>();
    }

    public void handleLogin(PostLoginEvent event) {
        SQLStorage.get().getOrDefaultPlayer(event.getPlayer().getUniqueId()).thenAccept(player -> {
            players.add(player);
            //the power of callbacks
        });
    }

    public void handleDC(PlayerDisconnectEvent event) {
        ChatPlayer player = getPlayer(event.getPlayer());
        player.update();
        players.remove(player);
    }

    //highest priority
    public void onChat(ChatEvent event) {
        PunishController.get().handleChat(event);
        Main.get().getFilters().handleEvent(event);

        if (event.isCommand()) { return; }
        if (!event.isCancelled()) {
            event.setCancelled(true);

            //now we talk
            if (event.getSender() instanceof ProxiedPlayer) {
               ChatPlayer player = getPlayer((ProxiedPlayer) event.getSender());

                ChatMode mode = player.getChatMode();

                //TODO: replace this with something cleaner
                switch(mode) {
                    case NORMAL:
                        for (ProxiedPlayer player1 : ((ProxiedPlayer) event.getSender()).getServer().getInfo().getPlayers()) {
                            player1.sendMessage(formatNormal(event.getMessage(),player).create());
                        }
                        break;
                    case MOD:
                        break;
                    case ADMIN:
                        break;
                    case GLOBAL:
                        break;
                    case PM:
                        if (player.getPmReciever() == null) { player.setChatMode(ChatMode.NORMAL); return; }
                        break;
                }
            }
        }
    }

    public ComponentBuilder formatNormal(String message, ChatPlayer player) {
        BaseComponent[] first;
        BaseComponent[] second;
        BaseComponent[] third;
        if (player.isOnlineDonator()) {
            first = TextComponent.fromLegacyText(AuriBungeeUtil.colorString(player.getLevel() + " &7| " + player.getOnlineGroupName()));
            second = TextComponent.fromLegacyText(AuriBungeeUtil.colorString("&r" + player.getNickname() + " &r&f»&f "));
            third = TextComponent.fromLegacyText(AuriBungeeUtil.colorString(message
                    .trim().replaceAll(" +", " ")));
        } else {
            first = TextComponent.fromLegacyText(AuriBungeeUtil.colorString(player.getLevel() + " &7| " + player.getOnlineGroupName()));
            second = TextComponent.fromLegacyText(AuriBungeeUtil.colorString("&r" + player.getNickname() + " &r&f»&f "));
            third = TextComponent.fromLegacyText(message
                    .trim().replaceAll(" +", " "));
        }

        BaseComponent[] hover = TextComponent.fromLegacyText("CUM GOD CUM GOD CUM GOD \n CUM GOD LINE 2");
        BaseComponent[] hover1 = TextComponent.fromLegacyText("");

        return new ComponentBuilder("").append(first).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,hover)).bold(false).append(second).append(third).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,hover1));
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

    public ChatPlayer getPlayer(UUID uuid) {
        for (ChatPlayer p : this.players) {
            if (p.getUUID().equals(uuid)) {
                return p;
            }
        }
        return null;
    }

    public ChatPlayer getPlayer(ProxiedPlayer player) {
        return getPlayer(player.getUniqueId());
    }

    public class BungeeReciever extends JedisPubSub {
        @Override
        public void onMessage(String channel, final String msg) {
            //decrypt delta
            AuriBungeeUtil.logError("recieved message with delta: " + msg);
            Delta delta = decryptDelta(msg);
            AuriBungeeUtil.logError(delta.getTarget().toString());
            if (getPlayer(delta.getTarget()) != null) {
                AuriBungeeUtil.logError("target not null");
                getPlayer(delta.getTarget()).adjust(delta);
            }
        }
    }

    /*ONLY USE this if it is required, blocks thread, must be used async*/
    public ChatPlayer getPlayerAbsolute(UUID id) {
        if (Main.get().getProxy().getPlayer(id) != null) {
            return this.getPlayer(id);
        } else {
            return SQLStorage.get().getOrDefaultPlayer(id).join();
        }
    }

    public static ChatController get() {
        return Objects.requireNonNullElseGet(instance, () -> instance = new ChatController());
    }

    public void shutdown() {
        for (ChatPlayer player : players) {
            player.update();
        }
    }

}
