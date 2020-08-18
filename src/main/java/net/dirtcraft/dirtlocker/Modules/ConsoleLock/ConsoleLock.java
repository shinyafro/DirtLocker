package net.dirtcraft.dirtlocker.Modules.ConsoleLock;

import com.google.common.reflect.TypeToken;
import net.dirtcraft.dirtlocker.API.ConsoleLock.SecuredSource;
import net.dirtcraft.dirtlocker.Modules.Module;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.slf4j.Logger;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class ConsoleLock implements Module {

    private HashSet<String> blacklist = new HashSet<>();
    private Path defaultConfig;
    private Logger logger;

    public ConsoleLock(Path defaultConfig, Logger logger){
        this.defaultConfig = defaultConfig;
        this.logger = logger;
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        ConfigurationOptions options = ConfigurationOptions.defaults().setShouldCopyDefaults(true);
        ConfigurationLoader<CommentedConfigurationNode> loader =
                HoconConfigurationLoader.builder().setPath(defaultConfig.getParent().resolve("CommandLock.cfg")).build();
        try {
            CommentedConfigurationNode node = loader.load(options);
            Config config = node.getValue(new TypeToken<Config>() {}, new Config());
            blacklist.addAll(config.commands);
            loader.save(node);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
        }
        logger.info("DirtLocker - ConsoleLock ENABLED");
    }

    @Listener
    public void onCommandSent(SendCommandEvent event){
        Object source = event.getSource();
        String command;
        if (source instanceof SecuredSource || source instanceof Player) return;
        if (!blacklist.contains(command = event.getCommand())) return;
        event.setCancelled(true);
        logger.error("ERROR: Non-validated source attempted to execute secure command \"" + command + "\" with arguments \"" + event.getArguments() + "\". \nSource: \"" + source + "\"");
    }

    @Override
    public String getModuleName() {
        return "ConsoleLock";
    }

    @ConfigSerializable
    private static class Config{
        @Setting(comment = "Commands to be blacklisted. Only secure sources such as players can send these commands. Each line is a new command, which commas at the end with exception for the last command.")
        public List<String> commands = Arrays.asList("lp", "luckperms", "luckperm", "perm", "kill", "op", "deop", "cs", "csign", "csigns", "commandsign", "commandsigns");
    }

}
