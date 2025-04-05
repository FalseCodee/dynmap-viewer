package me.falsecode.dynmapnametags;


public class Main {

    private static NametagModule nametags;
    public static void init() {
        nametags = new NametagModule();
    }

    public static NametagModule getNametagModule() {
        return nametags;
    }
}
