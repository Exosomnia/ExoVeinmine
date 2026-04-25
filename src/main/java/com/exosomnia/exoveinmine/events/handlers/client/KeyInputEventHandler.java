package com.exosomnia.exoveinmine.events.handlers.client;

import com.exosomnia.exoveinmine.ExoVeinMine;
import com.exosomnia.exoveinmine.client.RegistrationHandlerClient;
import com.exosomnia.exoveinmine.networking.PacketHandler;
import com.exosomnia.exoveinmine.networking.packets.VeinMinerActivePacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;


@EventBusSubscriber(modid = ExoVeinMine.MODID, value = Dist.CLIENT)
public class KeyInputEventHandler {

    @SubscribeEvent
    public static void activateVeinMiner(InputEvent.Key event){
        Minecraft mc = Minecraft.getInstance();
        if (event.getKey() != RegistrationHandlerClient.ACTIVATE.getKey().getValue() || mc.screen != null) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        if (event.getAction() == InputConstants.PRESS) {
            PacketHandler.sendToServer(new VeinMinerActivePacket(true));
        } else if (event.getAction() == InputConstants.RELEASE) {
            PacketHandler.sendToServer(new VeinMinerActivePacket(false));
        }
    }
}
