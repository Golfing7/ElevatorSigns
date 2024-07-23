package com.golfing8.elevatorsigns;

import org.bukkit.Bukkit;

/**
 * Gets the server version from a package name.
 */
public enum Version
{
    UNKNOWN(100),
    v1_7(7),
    v1_8(8), 
    v1_9(9), 
    v1_10(10), 
    v1_11(11), 
    v1_12(12), 
    v1_13(13), 
    v1_14(14), 
    v1_15(15), 
    v1_16(16), 
    v1_17(17),
    v1_18(18),
    v1_19(19),
    v1_20(20),
    v1_21(21),
    ;
    
    private final int value;
    
    private Version(int value) {
        this.value = value;
    }
    
    public boolean isAtOrAfter(Version version) {
        return this.value >= version.value;
    }
    
    public boolean isAtOrBefore(Version version) {
        return this.value <= version.value;
    }
    
    public static Version fromBukkitPackageName(String name) {
        String bukkitVersion = Bukkit.getBukkitVersion();
        String version = bukkitVersion.substring(0, bukkitVersion.indexOf("-"));
        String[] split = version.split("\\.");
        int major = Integer.parseInt(String.valueOf(split[1]));

        try{
            for (Version v : Version.values()) {
                if (v.value == major)
                    return v;
            }
            return UNKNOWN;
        }catch(IllegalArgumentException exc)
        {
            return UNKNOWN;
        }
    }
}
 