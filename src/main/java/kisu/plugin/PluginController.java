package kisu.plugin;

import mouse.KisuPlugin;

import java.util.ArrayList;
import java.util.List;

public class PluginController {
    private final List<KisuPlugin> plugins = new ArrayList<>();

    public int getLoadedPlugins() {
        return plugins.size();
    }

    public void setPlugins(List<KisuPlugin> pluginsToAdd) {
        plugins.clear();
        plugins.addAll(pluginsToAdd);
    }
}
