package com.exosomnia.exoveinmine.client;

import com.exosomnia.exolib.ExoLib;
import com.exosomnia.exolib.utils.GUIUtils;
import com.exosomnia.exoveinmine.Config;
import com.exosomnia.exoveinmine.ExoVeinMine;
import com.exosomnia.exoveinmine.RegistrationHandler;
import com.exosomnia.exoveinmine.util.VeinMineHelper;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;

@EventBusSubscriber(modid = ExoVeinMine.MODID, value = Dist.CLIENT)
public class RegistrationHandlerClient {

    private static final ResourceLocation SPRITES = ResourceLocation.fromNamespaceAndPath(ExoVeinMine.MODID, "hud_sprites");
    private static final ResourceLocation NINE_SLICE = ResourceLocation.fromNamespaceAndPath(ExoLib.MODID, "simple_nine_slice");

    private static final int SPRITES_TEX_WIDTH = 182; //Width of bar and icon textures
    private static final int SPRITES_TEX_HEIGHT = 25; //Height of bar and icon textures

    public static final KeyMapping ACTIVATE = new KeyMapping("key.exoveinmine.activate", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_GRAVE_ACCENT, "key.categories.exoveinmine");

    @SubscribeEvent
    public static void registerKeyMappings(final RegisterKeyMappingsEvent event) {
        event.register(ACTIVATE);
    }

    @SubscribeEvent
    public static void registerGuiOverlay(final RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(ExoVeinMine.MODID, "vein_mine_charge"),
            (guiGraphics, partialTick) -> {
            Minecraft mc = Minecraft.getInstance();
                if (!mc.options.hideGui && mc.gameMode.getPlayerMode() != GameType.SPECTATOR) {
                    renderVeinMineCharge(mc, guiGraphics);
            }
        });
    }

    public static void renderVeinMineCharge(Minecraft mc, GuiGraphics guiGraphics) {
        LocalPlayer player = mc.player;
        if (player == null) return;

        if (VeinMineHelper.checkAccess(player, mc.level))  {
            double chargeAmount = player.getData(RegistrationHandler.VEIN_MINER_DATA);
            double chargePercentage = chargeAmount / Config.maxCharge;
            double chargePenalty = Math.min(Double.MAX_VALUE, Config.chargePerBlock * (1.0 / player.getAttributeValue(RegistrationHandler.VEIN_MINER_EFFICIENCY)));
            boolean toggle = ACTIVATE.isDown();

            if ((Config.enableCharge && (chargePercentage < 1.0 && !Config.hideBar)) || toggle) {
                int scaledHeight = guiGraphics.guiHeight() - (GUIUtils.getCurrentYShift(mc.gui) + 3);
                int scaledWidth = mc.getWindow().getGuiScaledWidth() / 2;
                int filledWidth = (int)(SPRITES_TEX_WIDTH * chargePercentage);

                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                if (Config.enableCharge) {
                    int leftAnchor = scaledWidth - (SPRITES_TEX_WIDTH / 2);
                    int barsDrawY = scaledHeight - 9;

                    guiGraphics.blitSprite(SPRITES, SPRITES_TEX_WIDTH, SPRITES_TEX_HEIGHT, 0, 0, leftAnchor, barsDrawY, SPRITES_TEX_WIDTH, 5);
                    guiGraphics.blitSprite(SPRITES, SPRITES_TEX_WIDTH, SPRITES_TEX_HEIGHT, 0, 5, leftAnchor, barsDrawY, filledWidth, 5);

                    //Charge amount and rate drawing if enabled
                    if (Config.showStats) {
                        RenderSystem.enableBlend();
                        String amountStat = String.format(I18n.get("gui.exoveinmine.charge_amount", (int) (chargeAmount / chargePenalty)));
                        int chargeAmountWidth = mc.font.width(amountStat);
                        guiGraphics.blitSprite(NINE_SLICE, leftAnchor , scaledHeight - 22, chargeAmountWidth + 5, 13);
                        guiGraphics.drawString(mc.font, amountStat, (leftAnchor) + 3, scaledHeight - 19, 0xFFFFFFFF, false);

                        RenderSystem.enableBlend();
                        String chargeStat = String.format(I18n.get("gui.exoveinmine.charge_rate", new DecimalFormat("0.##").format(((Config.rechargeAmount) * player.getAttributeValue(RegistrationHandler.VEIN_MINER_CHARGE)) / chargePenalty)));
                        int chargeRateWidth = mc.font.width(chargeStat);
                        int rateAnchor = (leftAnchor + SPRITES_TEX_WIDTH) - (chargeRateWidth + 5);
                        guiGraphics.blitSprite(NINE_SLICE, rateAnchor, scaledHeight - 22, chargeRateWidth + 5, 13);
                        guiGraphics.drawString(mc.font, chargeStat, rateAnchor + 3, scaledHeight - 19, 0xFFFFFFFF, false);
                    }
                }
                //Draw toggle icon
                guiGraphics.blitSprite(SPRITES, SPRITES_TEX_WIDTH, SPRITES_TEX_HEIGHT, !toggle ? 0 : 15, 10, scaledWidth - 8, scaledHeight - 14, 15, 15);
            }
        }
    }
}
