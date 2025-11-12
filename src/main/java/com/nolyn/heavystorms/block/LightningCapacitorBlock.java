package com.nolyn.heavystorms.block;

import com.nolyn.heavystorms.blockentity.HeavyStormsBlockEntities;
import com.nolyn.heavystorms.blockentity.LightningCapacitorBlockEntity;
import com.nolyn.heavystorms.item.HeavyStormsItems;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.LootParams;
import org.jetbrains.annotations.Nullable;

public class LightningCapacitorBlock extends Block implements EntityBlock {
    public LightningCapacitorBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LightningCapacitorBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == HeavyStormsBlockEntities.LIGHTNING_CAPACITOR.get()
                ? (lvl, pos, blockState, blockEntity) -> LightningCapacitorBlockEntity.tick(lvl, pos, blockState, (LightningCapacitorBlockEntity) blockEntity)
                : null;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return List.of(HeavyStormsItems.LIGHTNING_CAPACITOR.get().getDefaultInstance());
    }
}
