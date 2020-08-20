package net.dirtcraft.dirtlocker;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import net.dirtcraft.dirtlocker.Modules.AutoOp.AutoOp;
import net.dirtcraft.dirtlocker.Modules.ConsoleLock.ConsoleLock;
import net.dirtcraft.dirtlocker.Modules.SignGuard.SignGuard;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Path;

@Plugin(
        id = "dirtlocker",
        description = "n/a",
        name = "DirtLocker"
)
public class Dirtlocker {

    @Inject
    @DefaultConfig(sharedRoot = false)
    private Path defaultConfig;

    @Inject
    private Logger logger;

    private Config config;
    private SignGuard signGuard;
    private ConsoleLock consoleLock;
    private AutoOp autoOp;

    @Listener(order = Order.PRE)
    public void onServerStart(GamePreInitializationEvent event) {
        ConfigurationOptions options = ConfigurationOptions.defaults().setShouldCopyDefaults(true);
        ConfigurationLoader<CommentedConfigurationNode> loader =
                HoconConfigurationLoader.builder().setPath(defaultConfig).build();
        try {
            CommentedConfigurationNode node = loader.load(options);
            config = node.getValue(new TypeToken<Config>() {}, new Config());
            loader.save(node);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
        }
        consoleLock = new ConsoleLock(defaultConfig, logger);
        signGuard = new SignGuard(defaultConfig, logger);
        autoOp = new AutoOp(defaultConfig, logger);
        if (config.consoleLock) Sponge.getEventManager().registerListeners(this, consoleLock);
        if (config.signGuard) Sponge.getEventManager().registerListeners(this, signGuard);
        if (config.autoOp) Sponge.getEventManager().registerListeners(this, autoOp);
        logger.info("Dirt-Locker ENABLED");
    }

    @ConfigSerializable
    private static class Config{
        @Setting(comment = "Locks console down to only players and trusted sources")
        public boolean consoleLock = false;

        @Setting(comment = "Stops signs with command elements via NBT from executing console commands")
        public boolean signGuard = true;

        @Setting(comment = "Manages OP status based on a permission node. (\"dirtlocker.autoop.operator\")")
        public boolean autoOp = true;
    }
}
