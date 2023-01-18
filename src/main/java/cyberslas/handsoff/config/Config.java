package cyberslas.handsoff.config;

import cyberslas.handsoff.util.Constants;
import cyberslas.handsoff.util.Lang;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class Config {
    private static final ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();
    public static final Client CLIENT = new Client(clientBuilder);
    public static final ForgeConfigSpec CONFIG_SPEC_CLIENT = clientBuilder.build();
    private static final ForgeConfigSpec.Builder commonBuilder = new ForgeConfigSpec.Builder();
    public static final Common COMMON = new Common(commonBuilder);
    public static final ForgeConfigSpec CONFIG_SPEC_COMMON = commonBuilder.build();

    public static class Client {
        public final ForgeConfigSpec.ConfigValue<Boolean> showMessage;
        public final ForgeConfigSpec.ConfigValue<Boolean> showOutlines;
        public final ForgeConfigSpec.ConfigValue<Integer> showOutlinesRange;
        public final ForgeConfigSpec.ConfigValue<Double> showOutlinesThickness;
        public final ForgeConfigSpec.ConfigValue<Integer> showUnlockedOutlinesRed;
        public final ForgeConfigSpec.ConfigValue<Integer> showUnlockedOutlinesGreen;
        public final ForgeConfigSpec.ConfigValue<Integer> showUnlockedOutlinesBlue;
        public final ForgeConfigSpec.ConfigValue<Integer> showUnlockedOutlinesAlpha;
        public final ForgeConfigSpec.ConfigValue<Integer> showLockedOutlinesRed;
        public final ForgeConfigSpec.ConfigValue<Integer> showLockedOutlinesGreen;
        public final ForgeConfigSpec.ConfigValue<Integer> showLockedOutlinesBlue;
        public final ForgeConfigSpec.ConfigValue<Integer> showLockedOutlinesAlpha;

        public Client(ForgeConfigSpec.Builder builder) {
            showMessage = builder
                    .comment("Write messages to chat when block mark state is changed with tool by player.",
                            "Default: true")
                    .translation(Lang.CONFIG_MESSAGE.key())
                    .define("showMessage", true);

            showOutlines = builder
                    .comment("Draw highlighted outline on marked blocks while holding tool.",
                            "Default: true")
                    .translation(Lang.CONFIG_OUTLINE.key())
                    .define("showOutlines", true);
            showOutlinesRange = builder
                    .comment("Range in blocks that outline is rendered.",
                            "Default: " + Constants.DEFAULT_DRAW_OUTLINE_RANGE)
                    .translation(Lang.CONFIG_OUTLINE_RANGE.key())
                    .defineInRange("showOutlinesRange", Constants.DEFAULT_DRAW_OUTLINE_RANGE, Constants.MIN_DRAW_OUTLINE_RANGE, Constants.MAX_DRAW_OUTLINE_RANGE);
            showOutlinesThickness = builder
                    .comment("Line thickness for outline.",
                            "Default: " + Constants.DEFAULT_OUTLINE_THICKNESS)
                    .translation(Lang.CONFIG_OUTLINE_THICKNESS.key())
                    .defineInRange("showOutlinesThickness", Constants.DEFAULT_OUTLINE_THICKNESS, Constants.MIN_OUTLINE_THICKNESS, Constants.MAX_OUTLINE_THICKNESS);
            showUnlockedOutlinesRed = builder
                    .comment("Unlocked line red color component.",
                            "Default: " + Constants.DEFAULT_UNLOCKED_RED_VALUE)
                    .translation(Lang.CONFIG_UNLOCKED_OUTLINE_RED.key())
                    .defineInRange("showOutlinesUnlockedRed", Constants.DEFAULT_UNLOCKED_RED_VALUE, Constants.MIN_COLOR_VALUE, Constants.MAX_COLOR_VALUE);
            showUnlockedOutlinesGreen = builder
                    .comment("Unlocked line green color component.",
                            "Default: " + Constants.DEFAULT_UNLOCKED_GREEN_VALUE)
                    .translation(Lang.CONFIG_UNLOCKED_OUTLINE_GREEN.key())
                    .defineInRange("showOutlinesUnlockedGreen", Constants.DEFAULT_UNLOCKED_GREEN_VALUE, Constants.MIN_COLOR_VALUE, Constants.MAX_COLOR_VALUE);
            showUnlockedOutlinesBlue = builder
                    .comment("Unlocked line blue color component.",
                            "Default: " + Constants.DEFAULT_UNLOCKED_BLUE_VALUE)
                    .translation(Lang.CONFIG_UNLOCKED_OUTLINE_BLUE.key())
                    .defineInRange("showOutlinesUnlockedBlue", Constants.DEFAULT_UNLOCKED_BLUE_VALUE, Constants.MIN_COLOR_VALUE, Constants.MAX_COLOR_VALUE);
            showUnlockedOutlinesAlpha = builder
                    .comment("Unlocked line alpha color component.",
                            "Default: " + Constants.DEFAULT_UNLOCKED_ALPHA_VALUE)
                    .translation(Lang.CONFIG_UNLOCKED_OUTLINE_ALPHA.key())
                    .defineInRange("showOutlinesUnlockedAlpha", Constants.DEFAULT_UNLOCKED_ALPHA_VALUE, Constants.MIN_COLOR_VALUE, Constants.MAX_COLOR_VALUE);
            showLockedOutlinesRed = builder
                    .comment("Locked line red color component.",
                            "Default: " + Constants.DEFAULT_LOCKED_RED_VALUE)
                    .translation(Lang.CONFIG_LOCKED_OUTLINE_RED.key())
                    .defineInRange("showOutlinesLockedRed", Constants.DEFAULT_LOCKED_RED_VALUE, Constants.MIN_COLOR_VALUE, Constants.MAX_COLOR_VALUE);
            showLockedOutlinesGreen = builder
                    .comment("Locked line green color component.",
                            "Default: " + Constants.DEFAULT_LOCKED_GREEN_VALUE)
                    .translation(Lang.CONFIG_LOCKED_OUTLINE_GREEN.key())
                    .defineInRange("showOutlinesLockedGreen", Constants.DEFAULT_LOCKED_GREEN_VALUE, Constants.MIN_COLOR_VALUE, Constants.MAX_COLOR_VALUE);
            showLockedOutlinesBlue = builder
                    .comment("Locked line blue color component.",
                            "Default: " + Constants.DEFAULT_LOCKED_BLUE_VALUE)
                    .translation(Lang.CONFIG_LOCKED_OUTLINE_BLUE.key())
                    .defineInRange("showOutlinesLockedBlue", Constants.DEFAULT_LOCKED_BLUE_VALUE, Constants.MIN_COLOR_VALUE, Constants.MAX_COLOR_VALUE);
            showLockedOutlinesAlpha = builder
                    .comment("Locked line alpha color component.",
                            "Default: " + Constants.DEFAULT_LOCKED_ALPHA_VALUE)
                    .translation(Lang.CONFIG_LOCKED_OUTLINE_ALPHA.key())
                    .defineInRange("showOutlinesLockedAlpha", Constants.DEFAULT_LOCKED_ALPHA_VALUE, Constants.MIN_COLOR_VALUE, Constants.MAX_COLOR_VALUE);
        }
    }

    public static class Common {
        public final ForgeConfigSpec.ConfigValue<Boolean> lockToPlayer;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> extraPoiTypes;

        public Common(ForgeConfigSpec.Builder builder) {
            lockToPlayer = builder
                    .comment("Blocks others from changing the mark status of blocks they did not mark",
                            "This does *NOT* stop players from simply breaking the block to remove the status.",
                            "Default: false")
                    .translation(Lang.CONFIG_POI_REGISTER_TO_PLAYER.key())
                    .define("lockToPlayer", false);
            extraPoiTypes = builder
                    .comment("A list of registry names for extra POI types to be added to checks. Format: modid:name",
                            "Examples: \"morevillagers:oceanographer\", \"morevillagers:miner\", \"immersiveengineering:workbench\", \"immersiveengineering:craftingtable\"",
                            "Each entry must be surrounded by double quotes")
                    .translation(Lang.CONFIG_POI_TYPES.key())
                    .defineList("extraPoiTypes", new ArrayList<>(), dontcare -> true);
        }
    }
}
