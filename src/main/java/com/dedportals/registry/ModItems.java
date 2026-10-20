package com.dedportals.registry;

import com.dedportals.DedPortalsMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, DedPortalsMod.MODID);

    public static final RegistryObject<Item> PORTAL_FRAME = ITEMS.register("portal_frame",
            () -> new BlockItem(ModBlocks.PORTAL_FRAME.get(), new Item.Properties()));

    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(PORTAL_FRAME.get());
        }
    }

    private ModItems() {}
}
