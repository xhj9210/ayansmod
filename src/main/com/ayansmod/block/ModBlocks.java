package com.ayansmod.entity;

import com.ayansmod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ChairSeatEntity extends Entity {
    private BlockPos chairPos;

    public ChairSeatEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public ChairSeatEntity(Level level, BlockPos pos) {
        this(ModEntities.CHAIR_SEAT.get(), level);
        this.chairPos = pos;
        this.setPos(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            // 检查椅子方块是否还存在
            if (chairPos != null) {
                BlockState state = this.level().getBlockState(chairPos);
                if (!state.is(ModBlocks.POWER_CHAIR.get())) {
                    this.discard();
                    return;
                }
            }

            // 如果没有乘客，移除实体
            if (this.getPassengers().isEmpty()) {
                this.discard();
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        // 空实现，这个实体不需要同步数据
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("ChairX") && tag.contains("ChairY") && tag.contains("ChairZ")) {
            this.chairPos = new BlockPos(tag.getInt("ChairX"), tag.getInt("ChairY"), tag.getInt("ChairZ"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (this.chairPos != null) {
            tag.putInt("ChairX", this.chairPos.getX());
            tag.putInt("ChairY", this.chairPos.getY());
            tag.putInt("ChairZ", this.chairPos.getZ());
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    @Override
    public boolean shouldRiderSit() {
        return true;
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0.0D;
    }
}