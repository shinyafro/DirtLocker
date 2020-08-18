package net.dirtcraft.dirtlocker.Modules.SignGuard;

import com.google.common.reflect.TypeToken;
import net.dirtcraft.dirtlocker.Modules.Module;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class SignGuard implements Module {

    private Path defaultConfig;
    private Config config;
    private Logger logger;

    public SignGuard(Path defaultConfig, Logger logger){
        this.defaultConfig = defaultConfig;
        this.logger = logger;
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        ConfigurationOptions options = ConfigurationOptions.defaults().setShouldCopyDefaults(true);
        ConfigurationLoader<CommentedConfigurationNode> loader =
                HoconConfigurationLoader.builder().setPath(defaultConfig.getParent().resolve("SignGuard.cfg")).build();
        try {
            CommentedConfigurationNode node = loader.load(options);
            config = node.getValue(new TypeToken<Config>(){}, new Config());
            loader.save(node);
        } catch (IOException | ObjectMappingException e) {
            e.printStackTrace();
        }
        logger.info("DirtLocker - SignGuard ENABLED");
    }

    @Listener
    public void interact(InteractBlockEvent event){
        Optional<Location<World>> optLocation = event.getTargetBlock().getLocation();
        Optional<DataView> nbt = optLocation.flatMap(Location::getTileEntity)
                .map(TileEntity::toContainer)
                .flatMap(view->view.getView(DataQuery.of("UnsafeData")));
        if (!nbt.isPresent()) return;

        boolean hasClickEvent = Stream.of("Text1", "Text2", "Text3", "Text4").anyMatch(ln->hasClickEvent(nbt.get(), ln));
        if (hasClickEvent){
            event.setCancelled(true);
            event.getTargetBlock().getLocation().get().setBlock(BlockState.builder().blockType(BlockTypes.AIR).build());
            Player player = event.getCause().first(Player.class).orElse(null);
            Location<World> location = optLocation.get();
            if (player == null) return;

            config.commands.forEach(cmd->{
                String command = cmd
                        .replace("{{x}}", String.valueOf(location.getBlockX()))
                        .replace("{{y}}", String.valueOf(location.getBlockY()))
                        .replace("{{z}}", String.valueOf(location.getBlockZ()))
                        .replace("{{world}}", location.getExtent().getName())
                        .replace("{{worldId}}", location.getExtent().getUniqueId().toString())
                        .replace("{{player}}", player.getName())
                        .replace("{{playerId}}", player.getUniqueId().toString());
                Sponge.getCommandManager().process(Sponge.getServer().getConsole(), command);
            });
        }
    }

    private boolean hasClickEvent(DataView nbt, String line) {
        return nbt.getString(DataQuery.of(line))
                .map(s -> s.contains("\"clickEvent\":{"))
                .orElse(false);
    }

    @Override
    public String getModuleName() {
        return "SignGuard";
    }

    @ConfigSerializable
    private static class Config{
        @Setting(comment = "Commands to be ran in the case of a detected sign, after it is deleted. {{x}}, {{y}}, {{z}}, {{world}}, {{worldId}}, {{player}}, {{playerId}} will be replaced with relevant data.")
        public List<String> commands = Collections.singletonList("");
    }

}
