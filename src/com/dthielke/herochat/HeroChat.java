package com.dthielke.herochat;

import com.dthielke.herochat.command.CommandHandler;
import com.dthielke.herochat.command.commands.*;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public class HeroChat extends JavaPlugin {
    private static final Logger log = Logger.getLogger("Minecraft");
    private static final CommandHandler commandHandler = new CommandHandler();
    private static final ChannelManager channelManager = new ChannelManager();
    private static final ChatterManager chatterManager = new ChatterManager();
    private static final MessageHandler messageHandler = new MessageHandler();
    private static Chat chatService;
    private static Permission permissionService;

    private final ConfigManager configManager = new ConfigManager();

    public static Chat getChatService() {
        return chatService;
    }

    public static Permission getPermissionService() {
        return permissionService;
    }

    public static ChannelManager getChannelManager() {
        return channelManager;
    }

    public static ChatterManager getChatterManager() {
        return chatterManager;
    }

    public static CommandHandler getCommandHandler() {
        return commandHandler;
    }

    public static MessageHandler getMessageHandler() {
        return messageHandler;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        return commandHandler.dispatch(sender, label, args);
    }

    @Override
    public void onDisable() {
        log.info(getDescription().getName() + " version " + getDescription().getVersion() + " is disabled.");

        if (channelManager.getStorage() != null)
            channelManager.getStorage().update();
        if (chatterManager.getStorage() != null)
            chatterManager.getStorage().update();
    }

    @Override
    public void onEnable() {
        log.info(getDescription().getName() + " version " + getDescription().getVersion() + " is enabled.");

        registerCommands();
        registerEvents();
        if (!setupChat() || !setupPermissions()) {
            log.severe("[HeroChat] Vault not found! Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        setupStorage();
        channelManager.loadChannels();
        configManager.load(new File(getDataFolder(), "config.yml"));
        channelManager.getStorage().update();
        for (Player player : getServer().getOnlinePlayers())
            chatterManager.addChatter(player);
    }

    private void registerCommands() {
        commandHandler.addCommand(new FocusCommand());
        commandHandler.addCommand(new JoinCommand());
        commandHandler.addCommand(new LeaveCommand());
        commandHandler.addCommand(new QuickMsgCommand());
        commandHandler.addCommand(new IgnoreCommand());
        commandHandler.addCommand(new MsgCommand());
        commandHandler.addCommand(new ListCommand());
        commandHandler.addCommand(new WhoCommand());
        commandHandler.addCommand(new CreateCommand());
        commandHandler.addCommand(new RemoveCommand());
        commandHandler.addCommand(new SetCommand());
        commandHandler.addCommand(new InfoCommand());
        commandHandler.addCommand(new MuteCommand());
        commandHandler.addCommand(new KickCommand());
        commandHandler.addCommand(new BanCommand());
        commandHandler.addCommand(new ModCommand());
        commandHandler.addCommand(new HelpCommand());
    }

    private void registerEvents() {
        PluginManager pm = this.getServer().getPluginManager();
        HCPlayerListener pcl = new HCPlayerListener();
        pm.registerEvent(Type.PLAYER_JOIN, pcl, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_QUIT, pcl, Priority.Normal, this);
        pm.registerEvent(Type.PLAYER_CHAT, pcl, Priority.Lowest, this);
        pm.registerEvent(Type.PLAYER_COMMAND_PREPROCESS, pcl, Priority.Normal, this);
    }

    private Boolean setupChat() {
        RegisteredServiceProvider<Chat> svc = getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        if (svc != null) {
            chatService = svc.getProvider();
            return true;
        }
        return false;
    }

    private Boolean setupPermissions() {
        RegisteredServiceProvider<Permission> svc = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (svc != null) {
            permissionService = svc.getProvider();
            return true;
        }
        return false;
    }

    private void setupStorage() {
        File channelFolder = new File(getDataFolder(), "channels");
        channelFolder.mkdirs();
        ChannelStorage channelStorage = new YMLChannelStorage(channelFolder);
        channelManager.setStorage(channelStorage);

        File chatterFolder = new File(getDataFolder(), "chatters");
        chatterFolder.mkdirs();
        ChatterStorage chatterStorage = new YMLChatterStorage(chatterFolder);
        chatterManager.setStorage(chatterStorage);
    }
}
