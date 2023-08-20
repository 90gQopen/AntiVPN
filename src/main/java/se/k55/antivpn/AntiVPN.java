package se.k55.antivpn;

import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import se.k55.antivpn.listener.ConnectionListener;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Plugin(id = "antivpn",
        name = "AntiVPN",
        version = "3.5",
        description = "Verifies that users are not utilizing VPN services when connecting to the server", 
        authors = {"1ssh", "Maartin"})
public final class AntiVPN {
    
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    
    private LookupService lookupService;
    private TelegramService telegramService;
    
    @Inject
    public AntiVPN(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        saveDefaultConfig();
        
        Toml config = new Toml().read(getConfigFile());
        lookupService = new LookupService(config);
        telegramService = new TelegramService(config);
        
        getProxyServer().getEventManager().register(this, new ConnectionListener(this, config));
    }
    
    public ProxyServer getProxyServer() {
        return server;
    }
    
    public Logger getLogger() {
        return logger;
    }
    
    public Path getDataDirectory() {
        return dataDirectory;
    }
    
    public LookupService getLookupService() {
        return lookupService;
    }
    
    public TelegramService getTelegramService() {
        return telegramService;
    }
    
    public File getConfigFile() {
        return new File(getDataDirectory().toFile(), "config.toml");
    }
    
    public void saveDefaultConfig() {
        File directory = getDataDirectory().toFile();
        
        if (!directory.exists() && !directory.mkdirs()) {
            throw new RuntimeException("Could not create file " + directory.getPath());
        }
        File configFile = getConfigFile();    
        
        if (!configFile.exists()) {
            try (InputStream inputStream = getClass().getResourceAsStream("/config.toml")) {
                if (inputStream == null) {
                    throw new RuntimeException("Could not read config.toml from plugin resources");
                }
                Files.copy(inputStream, configFile.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
