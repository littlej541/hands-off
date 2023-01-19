package cyberslas.handsoff.util;

import cyberslas.handsoff.registry.Items;
import net.minecraft.world.item.Item;

public class Constants {
    public static final String MODID = "BUILDTOKEN_MODID";

    public final static int MIN_DRAW_OUTLINE_RANGE = 1;
    public final static int MAX_DRAW_OUTLINE_RANGE = 256;
    public final static int DEFAULT_DRAW_OUTLINE_RANGE = 16;
    public final static double MIN_OUTLINE_THICKNESS = 1.0d;
    public final static double MAX_OUTLINE_THICKNESS = 10.0d;
    public final static double DEFAULT_OUTLINE_THICKNESS = 2.0d;
    public final static int MIN_COLOR_VALUE = 0;
    public final static int MAX_COLOR_VALUE = 255;
    public final static int DEFAULT_UNLOCKED_RED_VALUE = 255;
    public final static int DEFAULT_UNLOCKED_GREEN_VALUE = 255;
    public final static int DEFAULT_UNLOCKED_BLUE_VALUE = 255;
    public final static int DEFAULT_UNLOCKED_ALPHA_VALUE = 255;
    public final static int DEFAULT_LOCKED_RED_VALUE = 255;
    public final static int DEFAULT_LOCKED_GREEN_VALUE = 0;
    public final static int DEFAULT_LOCKED_BLUE_VALUE = 0;
    public final static int DEFAULT_LOCKED_ALPHA_VALUE = 255;

    public final static int BLOCKS_PER_SECTION_SIDE = 16;
    public final static Item MARKER_ITEM = Items.BLOCK_MARKER.get();
}
