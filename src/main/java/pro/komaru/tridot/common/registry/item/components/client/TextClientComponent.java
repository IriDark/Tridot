package pro.komaru.tridot.common.registry.item.components.client;

import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.Font.*;
import net.minecraft.client.gui.screens.inventory.tooltip.*;
import net.minecraft.client.renderer.*;
import net.minecraft.locale.*;
import net.minecraft.network.chat.*;
import net.minecraft.util.*;
import net.minecraftforge.api.distmarker.*;
import org.joml.*;

import java.lang.Math;
import java.util.*;

@OnlyIn(Dist.CLIENT)
public class TextClientComponent implements ClientTooltipComponent{
    public final FormattedCharSequence lines;
    private final int paddingTop;
    private TextClientComponent(MutableComponent text, int paddingTop) {
        this.lines = text.getVisualOrderText();
        this.paddingTop = paddingTop;
    }

    public static ClientTooltipComponent create(MutableComponent text) {
        return new TextClientComponent(text, 0);
    }

    @Override
    public int getHeight() {
        return 10 + paddingTop;
    }

    @Override
    public int getWidth(Font pFont) {
        int width = 0;
        int lineWidth = pFont.width(this.lines);
        if (lineWidth > width) {
            width = lineWidth;
        }

        return width;
    }

   public void renderText(Font pFont, int pMouseX, int pMouseY, Matrix4f pMatrix, MultiBufferSource.BufferSource pBufferSource) {
       int y = pMouseY + paddingTop;
       pFont.drawInBatch(this.lines, (float)pMouseX, (float)y + 1, -1, true, pMatrix, pBufferSource, DisplayMode.NORMAL, 0, 15728880);
    }
}