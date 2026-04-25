package com.exosomnia.exoveinmine.networking.packets;

import com.exosomnia.exoveinmine.Config;
import com.exosomnia.exoveinmine.ExoVeinMine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record VeinMinerBreakPacket(int blockId, BlockPos origin, List<BlockPos> breakPositions) implements CustomPacketPayload {

    private static final double MAX_SOUND_DISTANCE = 8.0 * 8.0;

    public static final CustomPacketPayload.Type<VeinMinerBreakPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ExoVeinMine.MODID, "vein_mine_break_packet"));

    public static final StreamCodec<RegistryFriendlyByteBuf, VeinMinerBreakPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT,
                    VeinMinerBreakPacket::blockId,
                    BlockPos.STREAM_CODEC,
                    VeinMinerBreakPacket::origin,
                    BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()),
                    VeinMinerBreakPacket::breakPositions,
                    VeinMinerBreakPacket::new
            );

    @Override
    public CustomPacketPayload.Type<VeinMinerBreakPacket> type() {
        return TYPE;
    }

    @OnlyIn(Dist.CLIENT)
    public static void handle(VeinMinerBreakPacket packet, IPayloadContext context) {
        if (Config.disableEffects) { return; }

        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            Level level = Minecraft.getInstance().level;
            if (level == null || player == null) return;

            BlockState blockState = Block.stateById(packet.blockId);

            if (!blockState.isAir() && player.blockPosition().distToCenterSqr(packet.origin.getX(), packet.origin.getY(), packet.origin.getZ()) < MAX_SOUND_DISTANCE) {
                SoundType soundtype = blockState.getSoundType(level, packet.origin, null);
                level.playLocalSound(packet.origin, soundtype.getBreakSound(), SoundSource.BLOCKS, (soundtype.getVolume()) / 2.0F, soundtype.getPitch() * 0.8F, false);
            }

            for (BlockPos position : packet.breakPositions) level.addDestroyBlockEffect(position, blockState);
        });
    }
}
