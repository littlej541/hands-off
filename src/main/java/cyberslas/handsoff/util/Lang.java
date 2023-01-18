package cyberslas.handsoff.util;

public record Lang(String key, int args) {
    public static final Lang BLOCK_MARKED = new Lang("message." + Constants.MODID + ".block_marked", 2);
    public static final Lang BLOCK_UNMARKED = new Lang("message." + Constants.MODID + ".block_unmarked", 2);
    public static final Lang BLOCK_INVALID = new Lang("message." + Constants.MODID + ".block_invalid", 2);
    public static final Lang BLOCK_MARKED_OTHER_PLAYER = new Lang("message." + Constants.MODID + ".block_marked_other_player", 2);

    public static final Lang CONFIG_MESSAGE = new Lang("config." + Constants.MODID + ".config_message", 0);
    public static final Lang CONFIG_OUTLINE = new Lang("config." + Constants.MODID + ".config_outline", 0);
    public static final Lang CONFIG_OUTLINE_RANGE = new Lang("config." + Constants.MODID + ".config_outline_range", 0);
    public static final Lang CONFIG_OUTLINE_THICKNESS = new Lang("config." + Constants.MODID + ".config_outline_thickness", 0);
    public static final Lang CONFIG_UNLOCKED_OUTLINE_RED = new Lang("config." + Constants.MODID + ".config_unlocked_outline_red", 0);
    public static final Lang CONFIG_UNLOCKED_OUTLINE_GREEN = new Lang("config." + Constants.MODID + ".config_unlocked_outline_green", 0);
    public static final Lang CONFIG_UNLOCKED_OUTLINE_BLUE = new Lang("config." + Constants.MODID + ".config_unlocked_outline_blue", 0);
    public static final Lang CONFIG_UNLOCKED_OUTLINE_ALPHA = new Lang("config." + Constants.MODID + ".config_unlocked_outline_alpha", 0);
    public static final Lang CONFIG_LOCKED_OUTLINE_RED = new Lang("config." + Constants.MODID + ".config_locked_outline_red", 0);
    public static final Lang CONFIG_LOCKED_OUTLINE_GREEN = new Lang("config." + Constants.MODID + ".config_locked_outline_green", 0);
    public static final Lang CONFIG_LOCKED_OUTLINE_BLUE = new Lang("config." + Constants.MODID + ".config_locked_outline_blue", 0);
    public static final Lang CONFIG_LOCKED_OUTLINE_ALPHA = new Lang("config." + Constants.MODID + ".config_locked_outline_alpha", 0);

    public static final Lang CONFIG_POI_TYPES = new Lang("config." + Constants.MODID + ".config_poi_types", 0);
    public static final Lang CONFIG_POI_REGISTER_TO_PLAYER = new Lang("config." + Constants.MODID + ".config_lock_to_player", 0);

    public int getArgs() {
        return this.args;
    }

    public String getKey() {
        return this.key;
    }
}
