package com.exosomnia.exoveinmine;

import com.exosomnia.exoveinmine.capabilities.veinminer.IVeinMinerStorage;
import com.exosomnia.exoveinmine.capabilities.veinminer.VeinMinerProvider;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GameType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;

@Mod.EventBusSubscriber(modid = ExoVeinMine.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistrationHandlerClient {

    private static final ResourceLocation VEIN_MINE_GUI = ResourceLocation.fromNamespaceAndPath(ExoVeinMine.MODID, "textures/gui/vein_mine_gui.png");
//    private static final ResourceLocation CHARGE_BAR_EMPTY = ResourceLocation.fromNamespaceAndPath(ExoVeinMine.MODID, "textures/gui/resource_bar_empty.png");
//    private static final ResourceLocation CHARGE_BAR_FILLED = ResourceLocation.fromNamespaceAndPath(ExoVeinMine.MODID, "textures/gui/resource_bar_filled.png");
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
                    renderVeinMineCharge(mc, guiGraphics, gui, Math.max(gui.leftHeight, gui.rightHeight));
                }
            }
        });
    }

    public static void renderVeinMineCharge(Minecraft mc, GuiGraphics guiGraphics, ForgeGui forgeGui, int yShift) {
        LocalPlayer player = mc.player;
        if (player == null) return;

        if ( Config.globalEnable || player.getTags().contains(Config.tagName) ||
                (Config.enableEnchant && player.getMainHandItem().getEnchantmentLevel(RegistrationHandler.VEIN_MINER_ENCHANTMENT.get()) > 0))  {

            player.getCapability(VeinMinerProvider.VEIN_MINER).ifPresent(data -> {
                double chargeAmount = data.getCharge();
                double chargePercentage = chargeAmount / Config.maxCharge;
                double chargePenalty = Math.min(Double.MAX_VALUE, Config.chargePerBlock * (1.0 / player.getAttributeValue(RegistrationHandler.VEIN_MINER_EFFICIENCY.get())));
                boolean toggle = ACTIVATE.isDown();

                if ((chargePercentage < 1.0 && !Config.hideBar) || toggle) {
                    forgeGui.leftHeight += 16;
                    forgeGui.rightHeight += 16;

                    int scaledHeight = guiGraphics.guiHeight() - Math.max(yShift, 59);
                    int scaledWidth = mc.getWindow().getGuiScaledWidth() / 2;
                    int filledWidth = (int) (barWidth * chargePercentage);

                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    if (Config.enableCharge) {
                        //Begin drawing resource bar
                        guiGraphics.blit(VEIN_MINE_GUI, scaledWidth - barWidth / 2, scaledHeight - 9, 0, 0, barWidth, 5, barWidth, 20);
                        guiGraphics.blit(VEIN_MINE_GUI, scaledWidth - barWidth / 2, scaledHeight - 9, 0, 5, filledWidth, 5, barWidth, 20);
                        //Charge amount and rate drawing if enabled
                        if (Config.showStats) {
                            forgeGui.leftHeight += 8;
                            forgeGui.rightHeight += 8;

                            String amountStat = String.format(I18n.get("gui.exoveinmine.charge_amount", (int) (chargeAmount / chargePenalty)));
                            int chargeAmountWidth = mc.font.width(amountStat);
                            guiGraphics.blitNineSlicedSized(VEIN_MINE_GUI, (scaledWidth - chargeAmountWidth) - 13, scaledHeight - 21, chargeAmountWidth + 4, 12, 3, 3, 9, 9, 0, 10, barWidth, 20);
                            guiGraphics.drawString(mc.font, amountStat, (scaledWidth - chargeAmountWidth) - 11, scaledHeight - 19, 0xFFFFFFFF);

                            RenderSystem.enableBlend();
                            String chargeStat = String.format(I18n.get("gui.exoveinmine.charge_rate", new DecimalFormat("0.##").format(((Config.rechargeAmount) * player.getAttributeValue(RegistrationHandler.VEIN_MINER_CHARGE.get())) / chargePenalty)));
                            int chargeRateWidth = mc.font.width(chargeStat);
                            guiGraphics.blitNineSlicedSized(VEIN_MINE_GUI, scaledWidth + 9, scaledHeight - 21, chargeRateWidth + 4, 12, 3, 3, 9, 9, 0, 10, barWidth, 20);
                            guiGraphics.drawString(mc.font, chargeStat, scaledWidth + 11, scaledHeight - 19, 0xFFFFFFFF);
                        }
                    }
                    //Draw toggle icon
                    guiGraphics.blit(toggle ? CHARGE_TOGGLE_ON : CHARGE_TOGGLE_OFF, (scaledWidth - iconWidth / 2) + 4, scaledHeight - 14, 0, 0, 15, 15, 15, 15);
                }
            });
        }
    }
}
