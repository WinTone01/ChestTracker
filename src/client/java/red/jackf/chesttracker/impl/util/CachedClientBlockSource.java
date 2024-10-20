package red.jackf.chesttracker.impl.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import red.jackf.chesttracker.api.ClientBlockSource;
import red.jackf.jackfredlib.api.base.Memoizer;

public class CachedClientBlockSource implements ClientBlockSource {
    private final Level level;
    private final BlockPos pos;
    private final Memoizer<BlockState> state;
    private final Memoizer<BlockEntity> blockEntity;

    public CachedClientBlockSource(Level level, BlockPos pos) {
        this.level = level;
        this.pos = pos.immutable();
        this.state = Memoizer.of(() -> level.getBlockState(pos));
        this.blockEntity = Memoizer.of(() -> level.getBlockEntity(pos));
    }

    public CachedClientBlockSource(Level level, BlockPos pos, BlockState state) {
        this.level = level;
        this.pos = pos.immutable();
        this.state = Memoizer.of(() -> state);
        this.blockEntity = Memoizer.of(() -> level.getBlockEntity(pos));
    }

    @Override
    public int x() {
        return pos.getX();
    }

    @Override
    public int y() {
        return pos.getY();
    }

    @Override
    public int z() {
        return pos.getZ();
    }

    @Override
    public BlockPos pos() {
        return pos;
    }

    @Override
    public BlockState blockState() {
        return state.get();
    }

    @Override
    public BlockEntity blockEntity() {
        return blockEntity.get();
    }

    @Override
    public Level level() {
        return level;
    }
}
