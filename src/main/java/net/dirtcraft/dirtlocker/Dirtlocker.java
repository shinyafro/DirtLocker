package net.dirtcraft.dirtlocker;

import com.google.inject.Inject;
import net.dirtcraft.dirtlocker.Modules.AutoOp.AutoOp;
import net.dirtcraft.dirtlocker.Modules.ConsoleLock.ConsoleLock;
import net.dirtcraft.dirtlocker.Modules.SignGuard.SignGuard;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.Plugin;

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

    private SignGuard signGuard;
    private ConsoleLock commandLock;
    private AutoOp autoOp;

    @Listener(order = Order.PRE)
    public void onServerStart(GamePreInitializationEvent event) {
        logger.info("Dirt-Locker ENABLED");
        Sponge.getEventManager().registerListeners(this, signGuard = new SignGuard(defaultConfig, logger));
        Sponge.getEventManager().registerListeners(this, commandLock = new ConsoleLock(defaultConfig, logger));
        Sponge.getEventManager().registerListeners(this, autoOp = new AutoOp(defaultConfig, logger));
    }
}
