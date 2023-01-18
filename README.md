# hands-off
Minecraft mod that allows you to stop villagers from claiming your beds and job blocks.

## Configuration

### Client

Toggle messages when marking blocks and outlines denoting marked blocks. Also change range at which outlines appear and the color of said outlines.

- `showMessage` - Display chat messages when player marks/unmarks blocks
- `showOutlines` - Display outlines on marked blocks
- `showOutlinesRange` - Max range that outlines are drawn to, in blocks
- `showOutlinesThickness` - Thickness of outlines
- `unlockedOutline*` - Color components of outline for blocks that are marked and can be unmarked
- `LockedOutline*` - Color components of outline for blocks that are marked and cannot be unmarked

### Common

Toggle a player based block marking lock and specify POI types for mod compatibility.

- `lockToPlayer` - Blocks others from changing the marked status of blocks the did not mark themselves.
    - This does **NOT** stop players from simply breaking the block to remove the status, this should be accomplished through other means.
- `extraPoiTypes` Allows extra POI types be specified as able to be marked.
    - It is a list of registry names for POI types. These values may be difficult to find without viewing source code. Format is of the form:
        - `modid:name`
    - Examples entries include:
        - `"morevillagers:oceanographer"` - Oceanographer POI from `morevillagers` mod
        - `"morevillagers:miner"` - Miner POI from `morevillagers` mod
        - `"immersiveengineering:workbench"` - Workbench POI from `immersiveengineering` mod
        - `"immersiveengineering:craftingtable"` - Crafting Table POI from `immersiveengineering` mod