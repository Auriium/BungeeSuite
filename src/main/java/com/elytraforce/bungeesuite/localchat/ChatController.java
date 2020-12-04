package com.elytraforce.bungeesuite.localchat;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.config.PluginConfig;
import com.elytraforce.bungeesuite.discord.DiscordController;
import com.elytraforce.bungeesuite.localchat.model.ChatPlayer;
import com.elytraforce.bungeesuite.localchat.model.Delta;
import com.elytraforce.bungeesuite.punish.PunishController;
import com.elytraforce.bungeesuite.storage.SQLStorage;
import com.elytraforce.bungeesuite.util.AuriBungeeUtil;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
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

        Main.get().getProxy().getScheduler().runAsync(Main.get(), new Runnable() {
            @Override
            public void run() {
                try (Jedis jedis = pool.getResource()){
                    jedis.subscribe(new BungeeReciever(),BUNGEE_CHANNEL);
                } catch (Exception e) {
                    AuriBungeeUtil.logError("Error connecting to redis - " + e.getMessage());
                    AuriBungeeUtil.logError("Broken redis pool");
                }
                AuriBungeeUtil.logError("Registered the listener!");
            }
        });


        this.players = new ArrayList<>();
    }

    public void handleEvent(PostLoginEvent event) {
        SQLStorage.get().getOrDefaultPlayer(event.getPlayer().getUniqueId()).thenAccept(player -> {
            players.add(player);
            //this probably isnt gonna work and spout some bullshit about local variables enclosed in a defining scope or some bs
            //if it sends messages then i think im ok
        });
    }

    //highest priority
    public void onChat(ChatEvent event) {
        PunishController.get().handleChat(event);

        if (event.isCommand()) { return; }
        if (!event.isCancelled()) {
            event.setCancelled(true);

            //now we talk
            if (event.getSender() instanceof ProxiedPlayer) {
               ChatPlayer player = getPlayer((ProxiedPlayer) event.getSender());

                AuriBungeeUtil.logError(player.getExperience() + "");
                AuriBungeeUtil.logError(player.getDiscordEnabled() + "");


            }
        }
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
    public ChatPlayer getPlayerAbsolute(UUID id) throws InterruptedException, ExecutionException, TimeoutException {
        if (Main.get().getProxy().getPlayer(id) != null) {
            return this.getPlayer(id);
        } else {
            return SQLStorage.get().getOrDefaultPlayer(id).get(1, TimeUnit.SECONDS);
        }
    }

    public static ChatController get() {
        return Objects.requireNonNullElseGet(instance, () -> instance = new ChatController());
    }



}
