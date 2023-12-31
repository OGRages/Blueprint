package me.rages.blueprint.data.blueprint;

import com.google.common.io.Files;
import lombok.Getter;
import me.lucko.helper.serialize.BlockPosition;
import me.rages.blueprint.data.Points;
import me.rages.blueprint.util.Util;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;


public class Blueprint {

    @Getter private String name;

    @Getter private Map<BlueprintDirection, List<BlueprintBlock>> blockPositions;
    @Getter private Map<BlueprintDirection, Points<Vector, Vector>> points;

    private Map<UUID, Set<BlockPosition>> outlineCache = new HashMap<>();

    public Blueprint(String name) {
        this.name = Files.getNameWithoutExtension(name);
        this.blockPositions = new HashMap<>();
        this.points = new HashMap<>();
        for (BlueprintDirection direction : BlueprintDirection.values()) {
            blockPositions.put(direction, new ArrayList<>());
        }
    }

    public void sendOutline(Player player, Block block, BlueprintDirection direction) {
        clearOutlines(player);
        Points<Vector, Vector> data = points.get(direction);
        Set<BlockPosition> positions = new HashSet<>();
        Util.getHollowCube(
                        data.getMin().getBlockX(), data.getMin().getBlockY(), data.getMin().getBlockZ(),
                        data.getMax().getBlockX(), data.getMax().getBlockY(), data.getMax().getBlockZ())
                .forEach(vec -> {
                    Block outlineBlock = block.getLocation().add(vec.getBlockX(), vec.getBlockY(), vec.getBlockZ()).getBlock();
                    if (outlineBlock.isLiquid() || outlineBlock.getType() == Material.AIR) {
                        positions.add(BlockPosition.of(outlineBlock));
                        player.sendBlockChange(
                                outlineBlock.getLocation(),
                                Material.LIME_STAINED_GLASS.createBlockData()
                        );
                    }
                });
        outlineCache.put(player.getUniqueId(), positions);
    }

    public void clearOutlines(Player player) {
        if (outlineCache.containsKey(player.getUniqueId())) {
            outlineCache.get(player.getUniqueId()).forEach(pos -> {
                if (pos.toLocation().getBlock().isLiquid() || pos.toLocation().getBlock().getType().isAir()) {
                    player.sendBlockChange(
                            pos.toLocation(),
                            Material.AIR.createBlockData()
                    );
                }
            });
            outlineCache.remove(player.getUniqueId());
        }
    }


}