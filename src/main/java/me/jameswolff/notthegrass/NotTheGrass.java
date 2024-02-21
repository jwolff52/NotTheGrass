package me.jameswolff.notthegrass;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;

import java.util.List;
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
    // in the {@link Config#tagList}, if so check to see if that block contains any entities and if so attack the first one.
    // If the config option {@link Config#cancelAction} is true then cancel the original LeftClickBlockEvent.
    @SubscribeEvent
    public void onLeftClickBlockEvent(LeftClickBlock event)
    {
        if (event == null) return;
        if (event.getAction() == LeftClickBlock.Action.START)
        {
            if(event.getLevel() == null) return;
            BlockState blockState = event.getLevel().getBlockState(event.getPos());
            if (blockState == null) return;
            if (blockState.getTags() == null) return;
            if (Config.blockIgnoreList.contains(blockState.getBlock())) return;
            if (blockState.getTags()
                .filter(tag -> tag.isFor(BuiltInRegistries.BLOCK.key()))
                .noneMatch(tag -> Config.tagList.contains(tag.location())))
                return;
            Player player = event.getEntity();
            List<Entity> entitiesInBlock = event.getLevel().getEntities(player, new AABB(event.getPos()));
            if (entitiesInBlock == null || entitiesInBlock.isEmpty()) return;
            entitiesInBlock.forEach(entity -> LOGGER.info("Entity in block: " + entity + (entity instanceof ItemEntity)));
            boolean onlyItemEnities = true;
            for (Entity entity : entitiesInBlock)
            {
                if (entity instanceof ItemEntity)
                {
                    continue;
                }
                else
                {
                    onlyItemEnities = false;
                    LOGGER.info("Attacking entity: " + entity);
                    player.attack(entity);
                    break;
                }
            }
            if (onlyItemEnities) return;
            event.setCanceled(Config.cancelAction);
        }
    }
}
