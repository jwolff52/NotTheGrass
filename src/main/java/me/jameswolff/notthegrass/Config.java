package me.jameswolff.notthegrass;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = NotTheGrass.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.ConfigValue<List<? extends String>> TAG_LIST = BUILDER
        .comment("List of block tags that should be processed by the mod.")
        .defineListAllowEmpty("tagList", List.of("minecraft:sword_efficient"), Config::validateTagKey);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCK_IGNORE_LIST = BUILDER
        .comment("List of blocks to always process as normal. Even if they have a tag in the tag list.")
        .defineListAllowEmpty("blockIgnoreList", List.of("minecraft:carved_pumpkin"), Config::validateBlockName);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static Set<ResourceLocation> tagList;
    public static Set<Block> blockIgnoreList;

    private static boolean validateTagKey(final Object obj)
    {
        return obj instanceof String tagKey && BuiltInRegistries.BLOCK.getTags().anyMatch(tag -> tag.toString().equals(tagKey));
    }

    private static boolean validateBlockName(final Object obj)
    {
        return obj instanceof String blockName && BuiltInRegistries.BLOCK.containsKey(new ResourceLocation(blockName));
    }

    // This method is called when the config is loaded
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        NotTheGrass.LOGGER.info("Loading config");

        // Load the config values into the fields
        tagList = TAG_LIST.get().stream()
            .map(tag -> new ResourceLocation(tag))
            .collect(Collectors.toSet());
        NotTheGrass.LOGGER.info("Tag list loaded:");
        tagList.forEach(tag -> NotTheGrass.LOGGER.info(tag.toString()));

        blockIgnoreList = BLOCK_IGNORE_LIST.get().stream()
            .map(block -> BuiltInRegistries.BLOCK.get(new ResourceLocation(block)))
            .collect(Collectors.toSet());
        NotTheGrass.LOGGER.info("Block ignore list loaded:");
        blockIgnoreList.forEach(block -> NotTheGrass.LOGGER.info(block.toString()));

        NotTheGrass.LOGGER.info("Config loaded");
    }
}
