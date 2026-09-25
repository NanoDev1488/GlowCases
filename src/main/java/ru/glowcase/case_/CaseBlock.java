package ru.glowcase.case_;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

public class CaseBlock {

    private final String worldName;
    private final int x;
    private final int y;
    private final int z;
    private final String caseId;

    public CaseBlock(String worldName, int x, int y, int z, String caseId) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.caseId = caseId;
    }

    public CaseBlock(Location location, String caseId) {
        this.worldName = location.getWorld() != null ? location.getWorld().getName() : "world";
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.caseId = caseId;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public String getCaseId() {
        return caseId;
    }

    public Location getLocation() {
        World world = Bukkit.getWorld(worldName);
        if (world == null) return null;
        return new Location(world, x, y, z);
    }

    public boolean matches(Location loc) {
        if (loc == null || loc.getWorld() == null) return false;
        return loc.getWorld().getName().equalsIgnoreCase(worldName)
                && loc.getBlockX() == x
                && loc.getBlockY() == y
                && loc.getBlockZ() == z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CaseBlock caseBlock = (CaseBlock) o;
        return x == caseBlock.x && y == caseBlock.y && z == caseBlock.z && Objects.equals(worldName, caseBlock.worldName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(worldName, x, y, z);
    }
}
