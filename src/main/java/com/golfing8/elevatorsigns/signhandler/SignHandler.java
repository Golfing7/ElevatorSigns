package com.golfing8.elevatorsigns.signhandler;

import com.golfing8.elevatorsigns.ElevatorSignsRevamped;
import com.golfing8.elevatorsigns.Version;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;
import org.bukkit.block.Sign;
import org.bukkit.Location;
import org.bukkit.material.Openable;
import org.bukkit.Material;
import org.bukkit.block.Block;
import java.util.HashSet;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import com.golfing8.elevatorsigns.Color;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import com.golfing8.elevatorsigns.config.annotation.Configurable;
import com.golfing8.elevatorsigns.config.ConfigManager;
import java.util.UUID;
import java.util.Set;
import org.bukkit.plugin.Plugin;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;

public abstract class SignHandler implements Listener, CommandExecutor
{
    private Plugin plugin;
    private Set<UUID> recentTries;
    private ConfigManager configManager;
    @Configurable(path = "messages.", name = "use")
    protected String useMessage;
    @Configurable(path = "messages.", name = "create")
    protected String createMessage;
    @Configurable(path = "sounds.", name = "use")
    protected String onUseSound;
    @Configurable(path = "sounds.", name = "create")
    protected String onCreateSound;
    @Configurable(path = "messages.", name = "invalid-location")
    protected String invalidLocationMessage;
    @Configurable(path = "messages.", name = "no-permission-use")
    protected String noPermissionUseMessage;
    @Configurable(path = "messages.", name = "no-permission-create")
    protected String noPermissionCreateMessage;
    @Configurable(path = "messages.", name = "invalid-sign")
    protected String invalidSignMessage;
    @Configurable(path = "permissions.", name = "create")
    protected String createPermission;
    @Configurable(path = "permissions.", name = "use")
    protected String usePermission;
    @Configurable(path = "permissions.", name = "reload")
    protected String reloadPermission;
    @Configurable(name = "elevator-line-format")
    protected String elevatorLineFormat;
    @Configurable(name = "up-keyword")
    protected String elevatorUpFormat;
    @Configurable(name = "down-keyword")
    protected String elevatorDownFormat;
    @Configurable(name = "match-updown-lowercase")
    protected boolean matchLowercase;
    @Configurable(name = "floors-enabled")
    protected boolean floorsEnabled;
    @Configurable(name = "max-distance-away")
    protected double maxDistance;
    
    protected void sendSound(Player player, String sound) {
        if (sound == null || sound.isEmpty()) {
            return;
        }
        String[] split = sound.split(":");
        try {
            player.playSound(player.getLocation(), Sound.valueOf(split[0]), Float.parseFloat(split[1]), Float.parseFloat(split[2]));
        }
        catch (IllegalArgumentException ex) {}
    }
    
    protected boolean checkPermission(Player player, String toCheck) {
        return toCheck == null || toCheck.isEmpty() || player.hasPermission(toCheck);
    }
    
    protected void msg(Player player, String toInform) {
        if (toInform == null || toInform.isEmpty()) {
            return;
        }
        player.sendMessage(Color.c(toInform));
    }

    //Clears the cooldown set every full second.
    private void runTask() {
        new BukkitRunnable() {
            public void run() {
                SignHandler.this.recentTries.clear();
            }
        }.runTaskTimer(this.plugin, 0L, 20L);
    }
    
    public SignHandler(Plugin plugin) {
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
        this.runTask();
        this.configManager = new ConfigManager(this, plugin);
        this.recentTries = new HashSet<UUID>();
        this.configManager.reloadConfig();
    }

    /**
     * We use this method instead of the isPassable method in newer versions so that we can retain backwards compat.
     * @param block the block to check
     * @return true if the block is passable, false if not.
     */
    protected boolean blockIsPassable(Block block) {
        Material material = block.getType();
        if (material == Material.AIR) {
            return true;
        }
        if (block.isLiquid()) {
            return true;
        }
        if (!material.isBlock()) {
            return true;
        }
        if (material.toString().equals("GRASS_BLOCK") || material.toString().equals("GRASS_PATH")) {
            return false;
        }
        //Works for 1.18 servers.
        if (material.toString().contains("VOID_AIR") || material.toString().contains("GRASS")) {
            return true;
        }
        String name = material.name();
        if (block.getState() instanceof Openable) {
            Openable openable = (Openable)block.getState();
            return openable.isOpen();
        }
        return name.contains("BANNER") || name.contains("SIGN") || name.contains("FLOWER") || name.contains("DOOR");
    }
    
    protected SignInfo fromSign(Location from, UUID uuid, Block block) {
        //Not a sign?
        if (!block.getType().toString().contains("SIGN")) {
            return null;
        }
        //Clicking too fast?
        if (this.recentTries.contains(uuid)) {
            return null;
        }

        Vector signLoc = block.getLocation().add(0.5, 0.5, 0.5).toVector();

        Vector playerLoc = from.toVector();

        double distance = signLoc.distance(playerLoc);

        //Too far away?
        if (distance > this.maxDistance) {
            return null;
        }

        //Add them to the cooldown set
        this.recentTries.add(uuid);
        Sign sign = (Sign)block.getState();
        SignInfo signInfo;
        try {
            //Valid sign?
            signInfo = new SignInfo(sign, this.floorsEnabled);
        }
        catch (IllegalStateException | IllegalArgumentException ex2) {
            return null;
        }
        return signInfo;
    }

    /**
     * Gets the max world height of the world provided by the location.
     * @param location the location to get the world from
     * @return the max world height
     */
    private int getMaxWorldHeight(Location location)
    {
        return location.getWorld().getMaxHeight();
    }

    /**
     * Gets the min world height of the world provided by the location.
     * If we're on or after 1.18, we use the getMinHeight method, if not, we know it's 1.
     * @param location the location of the world we're in.
     * @return the min height.
     */
    private int getMinWorldHeight(Location location)
    {
        return ElevatorSignsRevamped.getRunningVersion().isAtOrAfter(Version.v1_18) ? location.getWorld().getMinHeight() : 1;
    }


    protected Location getLocation(SignInfo signInfo) {
        switch (signInfo.getDirection()) {
            case UP: {
                return this.getUp(signInfo);
            }
            case DOWN: {
                return this.getDown(signInfo);
            }
            default: {
                return null;
            }
        }
    }

    /**
     * Gets the next valid location from a sign's information going up.
     * @param signInfo the sign information.
     * @return the next valid location. (Null if not valid)
     */
    private Location getUp(SignInfo signInfo) {
        Location mainLoc = signInfo.getSignLocation();
        Block lastFloor = null;
        int sinceLastFloor = 0;
        Location toReturn = null;
        for (int z = 1; z <= signInfo.getSpaces(); ++z) {
            toReturn = null;
            for (int y = mainLoc.getBlockY(); y <= getMaxWorldHeight(mainLoc); ++y) {
                Block blockAt = mainLoc.getWorld().getBlockAt(mainLoc.getBlockX(), y, mainLoc.getBlockZ());

                boolean b = this.blockIsPassable(blockAt);
                if (!b) {
                    sinceLastFloor = 0;
                    lastFloor = blockAt;
                }
                else if (lastFloor != null) {
                    if (++sinceLastFloor >= 2) {
                        sinceLastFloor = 0;
                        lastFloor = null;
                        mainLoc = blockAt.getLocation();
                        toReturn = blockAt.getLocation().add(0.5, -1.0, 0.5);
                        break;
                    }
                }
            }
        }
        return toReturn;
    }

    /**
     * Gets the next valid location from a sign's information going down.
     * @param signInfo the sign information.
     * @return the next valid location. (Null if not valid)
     */
    private Location getDown(SignInfo signInfo) {
        Location mainLoc = signInfo.getSignLocation();
        Block lastCeiling = null;
        int sinceLastCeiling = 0;
        Location toReturn = null;
        for (int z = 1; z <= signInfo.getSpaces(); ++z) {
            toReturn = null;
            for (int y = mainLoc.getBlockY(); y > getMinWorldHeight(mainLoc); --y) {
                Block blockAt = mainLoc.getWorld().getBlockAt(mainLoc.getBlockX(), y, mainLoc.getBlockZ());
                boolean b = this.blockIsPassable(blockAt);
                if (!b) {
                    sinceLastCeiling = 0;
                    lastCeiling = blockAt;
                }
                else if (lastCeiling != null) {
                    if (++sinceLastCeiling >= 2 && !this.blockIsPassable(blockAt.getRelative(BlockFace.DOWN))) {
                        sinceLastCeiling = 0;
                        lastCeiling = null;
                        toReturn = blockAt.getLocation().add(0.5, 0.0, 0.5);
                        mainLoc = blockAt.getLocation();
                        break;
                    }
                }
            }
        }
        return toReturn;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        //No permission to reload
        if (!sender.hasPermission(this.reloadPermission)) {
            sender.sendMessage(Color.c("&cYou don't have permission to use this!"));
            return true;
        }
        //Not using a valid command
        if (args.length == 0) {
            return false;
        }
        //Are they trying to reload?
        if (args[0].equalsIgnoreCase("reload")) {
            long timeNow = System.currentTimeMillis();
            this.configManager.reloadConfig();
            long timeThen = System.currentTimeMillis();
            sender.sendMessage(Color.c("&aReloaded the config in &e" + (timeThen - timeNow) + "ms &a!"));
            return true;
        }
        return false;
    }
    
    protected class SignInfo
    {
        private final int spaces;
        private final Direction direction;
        private final Location signLocation;
        
        public SignInfo(Sign sign, boolean floorsEnabled) {
            String line1 = sign.getLine(0);
            if (line1 == null || !line1.equals(Color.c(elevatorLineFormat))) {
                throw new IllegalStateException();
            }
            String line2 = sign.getLine(2);
            this.spaces = ((floorsEnabled && line2 != null && !line2.equals("")) ? Integer.parseInt(line2) : 1);
            this.direction = getDirectionFromString(sign.getLine(1));
            this.signLocation = sign.getLocation();
        }
        
        public Location getSignLocation() {
            return this.signLocation;
        }
        
        public Direction getDirection() {
            return this.direction;
        }
        
        public int getSpaces() {
            return this.spaces;
        }
    }

    protected final Direction getDirectionFromString(String str){
        if(str.equals(Color.c(elevatorUpFormat)))
            return Direction.UP;
        else if(str.equals(Color.c(elevatorDownFormat)))
            return Direction.DOWN;
        else
            throw new IllegalArgumentException();
    }
    
    protected enum Direction
    {
        UP, 
        DOWN;
    }
}
 