package com.elytraforce.bungeesuite.localchat;

import com.elytraforce.bungeesuite.Main;
import com.elytraforce.bungeesuite.antiswear.Filters;
import com.elytraforce.bungeesuite.config.PluginConfig;
import com.elytraforce.bungeesuite.discord.DiscordController;
import com.elytraforce.bungeesuite.localchat.model.ChatMode;
import com.elytraforce.bungeesuite.localchat.model.ChatPlayer;
import com.elytraforce.bungeesuite.localchat.model.Delta;
import com.elytraforce.bungeesuite.localchat.model.PlayerDelta;
import com.elytraforce.bungeesuite.punish.PunishController;
import com.elytraforce.bungeesuite.storage.SQLStorage;
import com.elytraforce.bungeesuite.util.AuriBungeeUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ChatController {
    private ArrayList<ChatPlayer> players;
    private static ChatController instance;
    private JedisPool pool;
    private Filters filters;

    private HashMap<ProxiedPlayer, Boolean> spyMap;
    public boolean getIsSpying(ProxiedPlayer player) {
        return spyMap.getOrDefault(player, false);
    }

    public void enableSpy(ProxiedPlayer player) {
        spyMap.put(player, true);
    }

    public void disableSpy(ProxiedPlayer player) {
        spyMap.put(player, false);
    }

    private static final String BUNGEE_CHANNEL = "channel_bungee";
    private static final String GUI_CHANNEL = "channel_gui_i";

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
                jedis.subscribe(new GUIReciever(),GUI_CHANNEL);
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

        filters = Main.get().getFilters();
        this.spyMap = new HashMap<>();
    }

    public void handleSpy(ChatEvent event) {
        if (event.isCommand()) {
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

    public void handleLogin(PostLoginEvent event) {
        SQLStorage.get().getOrDefaultPlayer(event.getPlayer().getUniqueId()).thenAccept(player -> {
            players.add(player);
            //the power of callbacks
        });
    }

    public void handleDC(PlayerDisconnectEvent event) {
        ChatPlayer player = getPlayer(event.getPlayer());
        if (player != null) {
            player.update();
            players.remove(player);

        }
    }

    //highest priority
    public void onChat(ChatEvent event) {
        PunishController.get().handleChat(event);
        handleSpy(event);

        if (event.isCommand()) { return; }

        filters.handleEvent(event);

        if (!event.isCancelled()) {
            event.setCancelled(true);

            //now we talk
            if (event.getSender() instanceof ProxiedPlayer) {
               ChatPlayer player = getPlayer((ProxiedPlayer) event.getSender());

                ChatMode mode = player.getChatMode();

                //TODO: replace this with something cleaner
                switch(mode) {
                    case NORMAL:
                        sendNormal(event.getMessage(),player);
                        break;
                    case MOD:
                        sendStaff(event.getMessage(),player);
                        break;
                    case ADMIN:
                        sendAdmin(event.getMessage(),player);
                        break;
                    case GLOBAL:
                        sendGlobal(event.getMessage(),player);
                        break;
                }
            }
        }
    }

    public void sendNormal(String message, ChatPlayer player) {
        player.asProxiedPlayer().getServer().getInfo().getPlayers()
                .forEach(player1 -> {
                    player1.sendMessage(formatNormal(message,player).create());
                });
                if (player.getSendDiscord()) { this.callDiscord(player.asProxiedPlayer(),message); }
    }

    private ComponentBuilder formatNormal(String message, ChatPlayer player) {

        BaseComponent[] first = TextComponent.fromLegacyText(AuriBungeeUtil.colorString(player.getLevel() + " &7| " + player.getOnlineGroupName()));
        BaseComponent[] second = TextComponent.fromLegacyText(AuriBungeeUtil.colorString("&r" + player.getNickname() + " &r&f»&f "));
        BaseComponent[] third;

        if (player.isOnlineDonator()) {
            third = TextComponent.fromLegacyText(AuriBungeeUtil.colorString(message
                    .trim().replaceAll(" +", " ")));
        } else {
            third = TextComponent.fromLegacyText(message
                    .trim().replaceAll(" +", " "));
        }

        BaseComponent[] hover = TextComponent.fromLegacyText( AuriBungeeUtil.colorString(
                player.getOnlineGroupName() + "&r" )
        );

        BaseComponent[] hover1 = TextComponent.fromLegacyText( AuriBungeeUtil.colorString(
                "&7" + player.getName() + "&7\n \n"
                        + "&7Level: &b" + player.getLevel() + "\n"
                        + "&7Balance: &e" + player.getMoney() + "⛃\n\n"
                        + "&7Currently playing on &7(&f" + player.getServerName() + "&7)" )
        );

        return new ComponentBuilder("").append(first).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,new ComponentBuilder("").append(hover).bold(false).append(hover1).create())).event(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,player.getName())).bold(false).append(second).append(third).event((HoverEvent) null);
    }

    public void sendStaff(String message, ChatPlayer player) {
        Main.get().getProxy().getPlayers().stream()
                .filter(p -> p.hasPermission("elytraforce.mod"))
                .forEach(player1 -> {
                    player1.sendMessage(formatStaff(message,player).create());
                });
    }

    private ComponentBuilder formatStaff(String message, ChatPlayer player) {
        BaseComponent[] first = TextComponent.fromLegacyText(AuriBungeeUtil.colorString("&b&lSTAFF"));
        BaseComponent[] second = TextComponent.fromLegacyText(AuriBungeeUtil.colorString(" &7>> &r" + player.getOnlineGroupName() + "&r" + player.getNickname() + "&r&f:&f "));
        BaseComponent[] third = TextComponent.fromLegacyText(AuriBungeeUtil.colorString(message
                .trim().replaceAll(" +", " ")));

        return new ComponentBuilder("").append(first).append(second).bold(false).append(third);
    }

    public void sendAdmin(String message, ChatPlayer player) {
        Main.get().getProxy().getPlayers().stream()
                .filter(p -> p.hasPermission("elytraforce.admin"))
                .forEach(player1 -> {
                    player1.sendMessage(formatAdmin(message,player).create());
                });
    }

    private ComponentBuilder formatAdmin(String message, ChatPlayer player) {
        BaseComponent[] first = TextComponent.fromLegacyText(AuriBungeeUtil.colorString("&4&lADMIN"));
        BaseComponent[] second = TextComponent.fromLegacyText(AuriBungeeUtil.colorString(" &7>> &r" + player.getOnlineGroupName() + "&r" + player.getNickname() + "&r&f:&f "));
        BaseComponent[] third = TextComponent.fromLegacyText(AuriBungeeUtil.colorString(message
                .trim().replaceAll(" +", " ")));

        return new ComponentBuilder("").append(first).append(second).bold(false).append(third);
    }

    public void sendGlobal(String message, ChatPlayer player) {
        if (filters.handleString(message)) {
            player.asProxiedPlayer().sendMessage(PluginConfig.get().getPrefix() + AuriBungeeUtil.colorString("&cPlease do not swear on ElytraForce!"));
            return;
        }

        Main.get().getProxy().getPlayers().stream()
                .forEach(player1 -> {
                    player1.sendMessage(formatGlobal(message,player).create());
                }); if (player.getSendDiscord()) { this.callDiscord(player.asProxiedPlayer(),message); }
    }

    private ComponentBuilder formatGlobal(String message, ChatPlayer player) {
        BaseComponent[] first = TextComponent.fromLegacyText(AuriBungeeUtil.colorString("&9&lGLOBAL"));
        BaseComponent[] second = TextComponent.fromLegacyText(AuriBungeeUtil.colorString(" &7>> &r" + player.getOnlineGroupName() + "&r" + player.getNickname() + "&r&f:&f "));
        BaseComponent[] third = TextComponent.fromLegacyText(AuriBungeeUtil.colorString(message
                .trim().replaceAll(" +", " ")));

        return new ComponentBuilder("").append(first).append(second).bold(false).append(third);
    }

    public void sendReply(String message, ChatPlayer player) {
        try {
            player.getPmReciever().getName();
        } catch (NullPointerException exception) {
            player.asProxiedPlayer().sendMessage(PluginConfig.get().getPrefix() + AuriBungeeUtil.colorString("&cYour target is invalid or offline!"));
            player.setPmReciever(null);
            return;
        }

        if (filters.handleString(message)) {
            player.asProxiedPlayer().sendMessage(PluginConfig.get().getPrefix() + AuriBungeeUtil.colorString("&cPlease do not swear on ElytraForce!"));
            return;
        }

        player.getPmReciever().asProxiedPlayer().sendMessage(formatPMIn(message,player.getPmReciever(),player).create());
        player.asProxiedPlayer().sendMessage(formatPMOut(message,player,player.getPmReciever()).create());
    }

    public void sendPM(String message, ChatPlayer player, ChatPlayer target) {
        try {
            target.getName();
        } catch (NullPointerException exception) {
            player.asProxiedPlayer().sendMessage(PluginConfig.get().getPrefix() + AuriBungeeUtil.colorString("&cYour target is invalid or offline!"));
            player.setPmReciever(null);
            return;
        }

        if (filters.handleString(message)) {
            player.asProxiedPlayer().sendMessage(PluginConfig.get().getPrefix() + AuriBungeeUtil.colorString("&cPlease do not swear on ElytraForce!"));
            return;
        }

        player.getPmReciever().asProxiedPlayer().sendMessage(formatPMIn(message,target,player).create());
        player.asProxiedPlayer().sendMessage(formatPMOut(message,player,target).create());
    }

    private ComponentBuilder formatPMOut(String message, ChatPlayer player, ChatPlayer target) {
        BaseComponent[] first = TextComponent.fromLegacyText(AuriBungeeUtil.colorString("&7You &7-> &e" + target.getName() + " &r"));
        BaseComponent[] second = TextComponent.fromLegacyText(AuriBungeeUtil.colorString(message
                .trim().replaceAll(" +", " ")));

        return new ComponentBuilder("").append(first).bold(false).append(second);
    }

    private ComponentBuilder formatPMIn(String message, ChatPlayer player, ChatPlayer target) {
        BaseComponent[] first = TextComponent.fromLegacyText(AuriBungeeUtil.colorString("&e" + target.getName() + " &7-> &7You" + " &r"));
        BaseComponent[] second = TextComponent.fromLegacyText(AuriBungeeUtil.colorString(message
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

    public PlayerDelta decryptPlayerDelta(String string) {
        String[] parts = string.split(":");
        UUID id = UUID.fromString(parts[0]);
        String action = parts[1];
        PlayerDelta.TargetEnum change = PlayerDelta.TargetEnum.valueOf(parts[2]);
        return new PlayerDelta(id,action,change);
    }

    public String encryptPlayerDelta(PlayerDelta playerDelta) {
        String adapt = playerDelta.getTarget().toString() + ":" + playerDelta.getAction() + ":" + playerDelta.getType().name();
        return adapt;
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
            Delta delta = decryptDelta(msg);
            if (getPlayer(delta.getTarget()) != null) {
                getPlayer(delta.getTarget()).adjust(delta);
            }
        }
    }

    public class GUIReciever extends JedisPubSub {
        @Override
        public void onMessage(String channel, final String msg) {
            //decrypt delta
            AuriBungeeUtil.logError("recieved PDELTA message with delta: " + msg);
            PlayerDelta delta = decryptPlayerDelta(msg);
            AuriBungeeUtil.logError(delta.getTarget().toString());
            if (getPlayer(delta.getTarget()) != null) {
                getPlayer(delta.getTarget()).adjustP(delta);
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
