package com.exosomnia.exoveinmine.events;

import com.exosomnia.exoveinmine.Config;
import com.exosomnia.exoveinmine.ExoVeinMiner;
import com.exosomnia.exoveinmine.RegistrationHandler;
import com.exosomnia.exoveinmine.capabilities.veinminer.VeinMinerProvider;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExoVeinMiner.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RenderGameOverlayEventHandler {

    private static final ResourceLocation CHARGE_BAR_EMPTY = new ResourceLocation(ExoVeinMiner.MODID, "textures/gui/resource_bar_empty.png");
    private static final ResourceLocation CHARGE_BAR_FILLED = new ResourceLocation(ExoVeinMiner.MODID, "textures/gui/resource_bar_filled.png");
    private static final ResourceLocation CHARGE_TOGGLE_OFF = new ResourceLocation(ExoVeinMiner.MODID, "textures/gui/vein_toggle_off.png");
    private static final ResourceLocation CHARGE_TOGGLE_ON = new ResourceLocation(ExoVeinMiner.MODID, "textures/gui/vein_toggle_on.png");

    private static final int barWidth = 182; //Width of bar empty and filled textures
    private static final int iconWidth = 24; //Width of on/off icon

    @SubscribeEvent
    public static void renderHotbarEvent(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        if ( player.getTags().contains(Config.tagName) ||
             (Config.enableEnchant && player.getMainHandItem().getEnchantmentLevel(RegistrationHandler.VEIN_MINER_ENCHANTMENT.get()) > 0))  {

            player.getCapability(VeinMinerProvider.VEIN_MINER).ifPresent(data -> {
                GuiGraphics gui = event.getGuiGraphics();

                double chargeAmount = data.getCharge() / data.MAX_CHARGE;
                boolean toggle = RegistrationHandler.ACTIVATE.isDown();
                if ((chargeAmount < 1.0 && !Config.hideBar) || toggle) {

                    int scaledHeight = mc.getWindow().getGuiScaledHeight();
                    int scaledWidth = mc.getWindow().getGuiScaledWidth() / 2;
                    int filledWidth = (int) (barWidth * chargeAmount);

                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    gui.blit(CHARGE_BAR_EMPTY, scaledWidth - barWidth / 2, scaledHeight - 56, 0, 0, barWidth, 5, barWidth, 5);
                    gui.blit(CHARGE_BAR_FILLED, scaledWidth - barWidth / 2, scaledHeight - 56, 0, 0, filledWidth, 5, barWidth, 5);
                    gui.blit(toggle ? CHARGE_TOGGLE_ON : CHARGE_TOGGLE_OFF, (scaledWidth - iconWidth / 2) + 4, scaledHeight - 61, 0, 0, 15, 15, 15, 15);
                }
            });
        }
    }
}
