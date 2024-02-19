package me.jameswolff.notthegrass;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(NotTheGrass.MODID)
public class NotTheGrass
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "notthegrass";
    // Directly reference a slf4j logger
    protected static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public NotTheGrass(IEventBus modEventBus)
    {
        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    // Checks to make sure the block clicked is not in the {@link Config#blockIgnoreList} and then whether it has one of the tags
    // in the {@link Config#tagList}, if so cancel the event.
    @SubscribeEvent
    public void onLeftClickBlockEvent(LeftClickBlock event)
    {
        if (event == null) return;
        if (event.getAction() == LeftClickBlock.Action.START)
        {
            LOGGER.info("Left click block start");
            if(event.getLevel() == null) return;
            BlockState blockState = event.getLevel().getBlockState(event.getPos());
            if (blockState == null) return;
            if (blockState.getTags() == null) return;
            if (Config.blockIgnoreList.contains(blockState.getBlock())) return;
            if (blockState.getTags()
                .filter(tag -> tag.isFor(BuiltInRegistries.BLOCK.key()))
                .noneMatch(tag -> Config.tagList.contains(tag.location())))
                return;
            LOGGER.info("Cancelling left click block event");
            event.setCanceled(true);
        }
    }
}
