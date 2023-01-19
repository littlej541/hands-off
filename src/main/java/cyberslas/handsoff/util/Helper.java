package cyberslas.handsoff.util;

import cyberslas.handsoff.registry.Items;
import net.minecraft.world.entity.player.Player;

public class Helper {
    public static int getMaxBlockRenderRange(int renderDistance) {
        return Math.min(Constants.MAX_DRAW_OUTLINE_RANGE, renderDistance * Constants.BLOCKS_PER_SECTION_SIDE);
    }

    public static boolean playerHoldingBlockMarker(Player player) {
        return player.getMainHandItem().getItem().equals(Constants.MARKER_ITEM) || player.getOffhandItem().getItem().equals(Constants.MARKER_ITEM);
    }
}
