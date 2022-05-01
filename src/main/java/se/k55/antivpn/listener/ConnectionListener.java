package se.k55.antivpn.listener;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import se.k55.antivpn.AntiVPN;

import java.util.concurrent.CompletableFuture;

public class ConnectionListener {
    
    private final AntiVPN plugin;
    private final boolean useKick;
    private final String kickMessage;
    private final boolean useCommand;
    private final String command;
    private final boolean useTelegram;
    private final String telegramMessage;
    
    public ConnectionListener(AntiVPN plugin, Toml config) {
        this.plugin = plugin;

        useKick = config.getBoolean("kick.enabled", true);
        kickMessage = config.getString("kick.message", "We do not allow the usage of proxies on our servers.");
        
        useCommand = config.getBoolean("command.enabled", false);
        command = config.getString("command.command", "kick %s We do not allow the usage of proxies on our servers.");
        
        useTelegram = config.getBoolean("telegram.enabled", false);
        telegramMessage = config.getString("telegram.message", "%s connected using a proxy and was therefore kicked.");
    }

    @Subscribe
    public void onLogin(LoginEvent event) {
        Player player = event.getPlayer();
        String name = player.getUsername();
        String address = player.getRemoteAddress().getAddress().getHostAddress();
        
        if (!player.hasPermission("avpn.bypass") && !name.startsWith("-")) {
            plugin.getLookupService().fetch(address, player.getUniqueId()).thenAccept(usingProxy -> {
                plugin.getLogger().info("Player " + name + " logged in with address " + address + " VPN=" + usingProxy);
                
                if (usingProxy) {
                    if (useCommand) {
                        plugin.getProxyServer().getCommandManager().executeAsync(plugin.getProxyServer().getConsoleCommandSource(), String.format(command, name));
                    }
                    
                    if (useTelegram) {
                        CompletableFuture.runAsync(() -> plugin.getTelegramService().send(String.format(telegramMessage, name)));
                    }

                    if (useKick) {
                        player.disconnect(Component.text(kickMessage));
                    }
                }
            });
        }
    }
}
