package kisu.plugin;

import mouse.KisuPlugin;
import pinej.PineEnums;

public record CompiledPlugin(KisuPlugin plugin, Information information) {
    public record Information(PineEnums.TargetPlatform platform, String name, String author, String version, String description, String serial, String gameVersion, String hash) {

    }
}
