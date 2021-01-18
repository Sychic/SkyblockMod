package me.Danker.graphics;


import me.Danker.graphics.enums.FakeEntity;
import me.Danker.graphics.enums.Location;
import me.Danker.utils.Utils;
import me.Danker.utils.enums.Damage;
import me.Danker.utils.graphics.ScreenRenderer;
import me.Danker.utils.graphics.SmartFontRenderer;
import me.Danker.utils.graphics.colors.CommonColors;
import me.Danker.utils.NumberFormat;
import me.Danker.utils.graphics.colors.CustomColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import tv.twitch.chat.Chat;

import java.util.Random;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.client.renderer.GlStateManager.*;

public class DamageSplashEntity extends FakeEntity {

    private static final ScreenRenderer renderer = new ScreenRenderer();
    private static final WeakHashMap<String, UUID> added = new WeakHashMap<>();

    String displayText;
    private float scale = 1f;
    private CustomColor color;
    private boolean love = false;

    public DamageSplashEntity(String damage, Location currentLocation) {
        super(currentLocation);

        Matcher symbolMatcher = Pattern.compile("\\d+(.?)").matcher(damage);
        if (symbolMatcher.matches()) {
            String symbol = symbolMatcher.group(1);
            if (symbolMatcher.group(1).contains("♥")) {
                love = true;
                symbol.replace("♥", "");
                damage.replace("♥","");
            }
            color = Damage.fromSymbol(symbol).getColor();
            damage = damage.replace(symbol,"");
        }


        displayText = NumberFormat.format(Long.parseLong(damage));

        if (love) {
            displayText += "♥";
        }

        UUID uuid = new UUID(Utils.getRandom().nextLong(), Utils.getRandom().nextLong());

        if (added.containsValue(uuid)) {
            remove();
            return;
        }

        added.put(displayText, uuid);
    }

    @Override
    public String getName() {
        return "EntityDamageSplash";
    }

    @Override
    public void tick(float partialTicks, Random r, EntityPlayerSP player) {
        int maxLiving = 150;
        if (livingTicks > maxLiving) {
            remove();
            return;
        }

        float initialScale = 2.5f;

        // makes the text goes down and resize
        currentLocation.subtract(0, 2 / (double)maxLiving, 0);
        scale = initialScale - ((livingTicks * initialScale) / maxLiving);
    }

    @Override
    public void render(float partialTicks, RenderGlobal context, RenderManager render) {
        boolean thirdPerson = render.options.thirdPersonView == 2;

        renderer.setRendering(true);
        {
            { // setting up
                rotate(-render.playerViewY, 0f, 1f, 0f); // rotates yaw
                rotate((float) (thirdPerson ? -1 : 1) * render.playerViewX, 1.0F, 0.0F, 0.0F); // rotates pitch
                scale(-0.025F, -0.025F, 0.025F); // size the text to the same size as a nametag

                scale(scale, scale, scale);

                color(1.0f, 1.0f, 1.0f, 1.0f);
            }

            renderer.drawString(displayText, 0, 0, color/*CommonColors.RAINBOW*/,
                    SmartFontRenderer.TextAlignment.MIDDLE, SmartFontRenderer.TextShadow.NONE);
        }
        renderer.setRendering(false);
    }
}
