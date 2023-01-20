package cyberslas.handsoff.item;

import cyberslas.handsoff.config.Config;
import cyberslas.handsoff.mixin.MixinPoiRecordInterface;
import cyberslas.handsoff.mixin.MixinPoiSectionInterface;
import cyberslas.handsoff.mixin.MixinSectionStorageInterface;
import cyberslas.handsoff.network.ClientboundMarkResultPacket;
import cyberslas.handsoff.server.MarkedBlockManager;
import cyberslas.handsoff.server.util.ServerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.UUID;

public class BlockMarkerItem extends Item {
    public BlockMarkerItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        BlockHitResult hitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        ItemStack itemstack = player.getItemInHand(hand);

        if (!hitresult.getType().equals(HitResult.Type.MISS)) {
            if (!level.isClientSide) {
                ServerPlayer serverPlayer = (ServerPlayer)player;
                UUID uuid = serverPlayer.getUUID();
                BlockPos blockPos = hitresult.getBlockPos();
                BlockState blockState = level.getBlockState(blockPos);
                if (blockState.getBlock() instanceof BedBlock) {
                    if (blockState.getValue(BedBlock.PART) != BedPart.HEAD) {
                        blockPos = blockPos.relative(BedBlock.getConnectedDirection(blockState));
                    }
                }

                GlobalPos globalPos = GlobalPos.of(level.dimension(), blockPos);

                if (MarkedBlockManager.contains(globalPos)) {
                    if (!Config.COMMON.lockToPlayer.get() || Config.COMMON.lockToPlayer.get() && MarkedBlockManager.get(globalPos).equals(uuid)) {
                        MarkedBlockManager.remove(globalPos);

                        ServerLevel serverLevel = (ServerLevel) level;
                        PoiManager poiManager = serverLevel.getPoiManager();

                        // reset tickets in case any were taken and not cleared when marked
                        BlockPos finalBlockPos = blockPos;
                        ((MixinSectionStorageInterface) poiManager).invokeGetOrLoad(SectionPos.asLong(blockPos))
                                .ifPresent(poiSection -> {
                                    MixinPoiSectionInterface mixinPoiSectionInterface = (MixinPoiSectionInterface) poiSection;
                                    mixinPoiSectionInterface.invokeGetPoiRecord(finalBlockPos)
                                            .ifPresent(poiRecord -> {
                                                MixinPoiRecordInterface mixinPoiRecordInterface = (MixinPoiRecordInterface) poiRecord;
                                                mixinPoiRecordInterface.setFreeTickets(mixinPoiRecordInterface.getPoiType().getMaxTickets());
                                                mixinPoiRecordInterface.getSetDirty().run();
                                            });
                                    mixinPoiSectionInterface.getSetDirty().run();
                                });


                        ServerHelper.Network.sendMarkResult(serverPlayer, blockPos, ClientboundMarkResultPacket.Result.UNMARKED);
                    } else {
                        ServerHelper.Network.sendMarkResult(serverPlayer, blockPos, ClientboundMarkResultPacket.Result.MARKED_OTHER_PLAYER);

                        return InteractionResultHolder.fail(itemstack);
                    }
                } else {
                    if (MarkedBlockManager.put(globalPos, uuid)) {
                        ServerHelper.Network.sendMarkResult(serverPlayer, blockPos, ClientboundMarkResultPacket.Result.MARKED);
                    } else {
                        ServerHelper.Network.sendMarkResult(serverPlayer, blockPos, ClientboundMarkResultPacket.Result.INVALID);
                    }
                }
            }

            return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide);
        }

        return InteractionResultHolder.pass(itemstack);
    }
}
