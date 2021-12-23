package com.golfing8.elevatorsigns;

import com.golfing8.elevatorsigns.signhandler.HandlerV1_7_V1_18;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ElevatorSignsRevamped extends JavaPlugin
{
    private static Version runningVersion;

    public static Version getRunningVersion() {
        return ElevatorSignsRevamped.runningVersion;
    }

    public void onEnable() {
        //Save our default config
        this.saveDefaultConfig();

        //Load the version we're running on.
        ElevatorSignsRevamped.runningVersion = Version.fromBukkitPackageName(Bukkit.getServer().getClass().getName().split("\\.")[3]);

        if(runningVersion == Version.UNKNOWN)
        {
            Bukkit.getLogger().info(String.format("[%s] - Unknown server version! Plugin may be unstable!", getName()));
        }

        //Create our handler. (Listener registering is done by the class itself)
        HandlerV1_7_V1_18 handler = new HandlerV1_7_V1_18(this);

        //Register it as a command.
        this.getCommand("elevatorsigns").setExecutor(handler);
    }
}
