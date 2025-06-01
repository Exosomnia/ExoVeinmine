package com.exosomnia.exoveinmine.networking.packets;

import com.exosomnia.exoveinmine.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class VeinMinerBreakPacket {

    private static final double MAX_SOUND_DISTANCE = 8.0 * 8.0;

    private int blockId;
    private BlockPos origin;
    private List<BlockPos> breakPositions;

    public VeinMinerBreakPacket(int blockId, BlockPos origin, List<BlockPos> breakPositions) {
        this.blockId = blockId;
        this.origin = origin;
        this.breakPositions = breakPositions;
    }

    public VeinMinerBreakPacket(FriendlyByteBuf buffer) {
        blockId = buffer.readInt();
        origin = buffer.readBlockPos();

        int positionCount = buffer.readInt();
        breakPositions = new ArrayList<>();
        for(int i = 0; i < positionCount; i++) {
            breakPositions.add(buffer.readBlockPos());
        }
    }

    public static void encode(VeinMinerBreakPacket packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.blockId);
        buffer.writeBlockPos(packet.origin);
        buffer.writeInt(packet.breakPositions.size());
        for (BlockPos position : packet.breakPositions) {
            buffer.writeBlockPos(position);
        }
    }

    public static void handle(VeinMinerBreakPacket packet, Supplier<NetworkEvent.Context> context) {
        if (Config.disableEffects) {
            context.get().setPacketHandled(true);
            return;
        }
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(packet, context));
    }

    @OnlyIn(Dist.CLIENT)
    public static void handleClient(VeinMinerBreakPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            NetworkDirection packetDirection = context.get().getDirection();
            if (packetDirection.equals(NetworkDirection.PLAY_TO_CLIENT)) {
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
            }
        });
        context.get().setPacketHandled(true);
    }
}
