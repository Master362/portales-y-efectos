package com.dedportals.registry;

import com.dedportals.DedPortalsMod;
import com.dedportals.block.PortalBlock;
import com.dedportals.block.PortalFrameBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, DedPortalsMod.MODID);

    public static final RegistryObject<Block> PORTAL_FRAME = BLOCKS.register("portal_frame",
            () -> new PortalFrameBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(-1.0F, 3600000.0F)
                    .sound(SoundType.METAL)
                    .lightLevel(s -> 8)
                    .noLootTable()));

    public static final RegistryObject<Block> PORTAL = BLOCKS.register("portal",
            () -> new PortalBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .noCollission()
                    .noOcclusion()
                    .strength(-1.0F, 3600000.0F)
                    .sound(SoundType.GLASS)
                    .lightLevel(s -> 11)
                    .pushReaction(PushReaction.BLOCK)
                    .noLootTable()));

    private ModBlocks() {}
}
