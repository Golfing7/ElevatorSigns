package com.golfing8.elevatorsigns;

/**
 * Gets the server version from a package name.
 */
public enum Version
{
    UNKNOWN(-1),
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
        String[] split = name.split("_R");

        try{
            return valueOf(split[0].toLowerCase());
        }catch(IllegalArgumentException exc)
        {
            return UNKNOWN;
        }
    }
}
 