package net.dirtcraft.dirtlocker.Modules.AutoOp;

import net.dirtcraft.dirtlocker.Modules.Module;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.slf4j.Logger;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.nio.file.Path;

public class AutoOp implements Module {

    final private Path defaultConfig;
    final private Logger logger;
    final FMLCommonHandler commonHandler;
    final MinecraftServer server;
    private PlayerList playerList;

    public AutoOp(Path defaultConfig, Logger logger){
        this.defaultConfig = defaultConfig;
        this.logger = logger;
        this.commonHandler = FMLCommonHandler.instance();
        this.server = commonHandler.getMinecraftServerInstance();
        logger.info("DirtLocker - AutoOp ENABLED");
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event, @Root Player spongePlayer){
        if (playerList == null) playerList = server.getPlayerList();
        EntityPlayerMP forgePlayer = (EntityPlayerMP) spongePlayer;
        if (spongePlayer.hasPermission("dirtlocker.autoop.operator")){
            if (!isOpped(forgePlayer)) playerList.addOp(forgePlayer.getGameProfile());
        } else {
            if (isOpped(forgePlayer)) playerList.removeOp(forgePlayer.getGameProfile());
        }
    }

    private boolean isOpped(EntityPlayerMP player){
        UserListOpsEntry entry = playerList.getOppedPlayers().getEntry(player.getGameProfile());
        return entry != null;
    }

    @Override
    public String getModuleName() {
        return "AutoOp";
    }
}
