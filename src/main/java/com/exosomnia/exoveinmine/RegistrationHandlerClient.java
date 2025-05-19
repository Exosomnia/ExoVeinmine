package com.exosomnia.exoveinmine;

import com.exosomnia.exoveinmine.capabilities.veinminer.IVeinMinerStorage;
import com.exosomnia.exoveinmine.capabilities.veinminer.VeinMinerProvider;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = ExoVeinMine.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistrationHandlerClient {

    private static final ResourceLocation CHARGE_BAR_EMPTY = ResourceLocation.fromNamespaceAndPath(ExoVeinMine.MODID, "textures/gui/resource_bar_empty.png");
    private static final ResourceLocation CHARGE_BAR_FILLED = ResourceLocation.fromNamespaceAndPath(ExoVeinMine.MODID, "textures/gui/resource_bar_filled.png");
    private static final ResourceLocation CHARGE_TOGGLE_OFF = ResourceLocation.fromNamespaceAndPath(ExoVeinMine.MODID, "textures/gui/vein_toggle_off.png");
    private static final ResourceLocation CHARGE_TOGGLE_ON = ResourceLocation.fromNamespaceAndPath(ExoVeinMine.MODID, "textures/gui/vein_toggle_on.png");

    private static final int barWidth = 182; //Width of bar empty and filled textures
    private static final int iconWidth = 24; //Width of on/off icon

    public static final KeyMapping ACTIVATE = new KeyMapping("key.exoveinmine.activate", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_GRAVE_ACCENT, "key.categories.exoveinmine");

    @SubscribeEvent
    public static void registerKeyMappings(final RegisterKeyMappingsEvent event) {
        event.register(ACTIVATE);
    }

    @SubscribeEvent
    public static void registerGuiOverlay(final RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("vein_mine_charge", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
            Minecraft mc = gui.getMinecraft();
            if (!mc.options.hideGui) {
                gui.setupOverlayRenderState(true, false);

                if (mc.gameMode.getPlayerMode() != GameType.SPECTATOR) {
                    renderVeinMineCharge(mc, guiGraphics, Math.max(gui.leftHeight, gui.rightHeight));
                }
            }
        });
    }

    public static void renderVeinMineCharge(Minecraft mc, GuiGraphics guiGraphics, int yShift) {
        LocalPlayer player = mc.player;
        if (player == null) return;

        if ( Config.globalEnable || player.getTags().contains(Config.tagName) ||
                (Config.enableEnchant && player.getMainHandItem().getEnchantmentLevel(RegistrationHandler.VEIN_MINER_ENCHANTMENT.get()) > 0))  {

            player.getCapability(VeinMinerProvider.VEIN_MINER).ifPresent(data -> {
                double chargeAmount = data.getCharge() / IVeinMinerStorage.MAX_CHARGE;
                boolean toggle = ACTIVATE.isDown();

                if ((chargeAmount < 1.0 && !Config.hideBar) || toggle) {
                    int scaledHeight = guiGraphics.guiHeight() - Math.max(yShift, 59);
                    int scaledWidth = mc.getWindow().getGuiScaledWidth() / 2;
                    int filledWidth = (int) (barWidth * chargeAmount);

                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    guiGraphics.blit(CHARGE_BAR_EMPTY, scaledWidth - barWidth / 2, scaledHeight - 9, 0, 0, barWidth, 5, barWidth, 5);
                    guiGraphics.blit(CHARGE_BAR_FILLED, scaledWidth - barWidth / 2, scaledHeight - 9, 0, 0, filledWidth, 5, barWidth, 5);
                    guiGraphics.blit(toggle ? CHARGE_TOGGLE_ON : CHARGE_TOGGLE_OFF, (scaledWidth - iconWidth / 2) + 4, scaledHeight - 14, 0, 0, 15, 15, 15, 15);
                }
            });
        }
    }
}
