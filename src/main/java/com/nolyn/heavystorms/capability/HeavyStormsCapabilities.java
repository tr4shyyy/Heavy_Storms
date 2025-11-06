package com.nolyn.heavystorms.capability;

import com.nolyn.heavystorms.block.HeavyStormsBlocks;
import com.nolyn.heavystorms.blockentity.LightningCapacitorBlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class HeavyStormsCapabilities {
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlock(
                Capabilities.EnergyStorage.BLOCK,
                (level, pos, state, blockEntity, direction) -> blockEntity instanceof LightningCapacitorBlockEntity capacitor
                        ? capacitor.getEnergyStorage(direction)
                        : null,
                HeavyStormsBlocks.LIGHTNING_CAPACITOR.get()
        );
    }

    private HeavyStormsCapabilities() {}
}
