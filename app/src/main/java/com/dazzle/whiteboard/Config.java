package com.dazzle.whiteboard;

public class Config {
    final public static int SCREEN_WIDTH = 3840;// *  (Config.is4K() ? 2:1);
    final public static int SCREEN_HEIGHT = 2160;// * (Config.is4K() ? 2:1);

    public static boolean is4K() {
        return true;
    }

}
