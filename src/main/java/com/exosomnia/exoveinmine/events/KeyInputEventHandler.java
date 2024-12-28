package com.exosomnia.exoveinmine.events;

import com.exosomnia.exoveinmine.ExoVeinMiner;
import com.exosomnia.exoveinmine.RegistrationHandler;
import com.exosomnia.exoveinmine.networking.PacketHandler;
import com.exosomnia.exoveinmine.networking.packets.VeinMinerActivePacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExoVeinMiner.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KeyInputEventHandler {

    @SubscribeEvent
    public static void activateVeinMiner(InputEvent.Key event){
        Minecraft mc = Minecraft.getInstance();
        if (event.getKey() != RegistrationHandler.ACTIVATE.getKey().getValue() || mc.isPaused() || mc.screen != null) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        if (event.getAction() == InputConstants.PRESS) {
            //Minecraft.getInstance().player.playSound(RegistrationHandler.SOUND_VEIN_MINER_ON.get(), 0.5f, 1.0f); //Gets annoying, so it's removed.
            PacketHandler.sendToServer(new VeinMinerActivePacket(true));
        } else if (event.getAction() == InputConstants.RELEASE) {
            //Minecraft.getInstance().player.playSound(RegistrationHandler.SOUND_VEIN_MINER_OFF.get(), 0.5f, 1.0f); //Gets annoying, so it's removed.
            PacketHandler.sendToServer(new VeinMinerActivePacket(false));
        }
    }
}
