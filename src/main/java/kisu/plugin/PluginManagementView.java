package kisu.plugin;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import mouse.KisuEnvironment;
import mouse.KisuPlugin;
import pinej.PineEnums;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class PluginManagementView extends HBox {
    private final TreeItem<String> rpcs3;
    private final TreeItem<String> pcsx2;

    private final Map<TreeItem<String>, SelectablePlugin> pluginMap = new IdentityHashMap<>();

    private record SelectablePlugin(CheckBox checkBox, CompiledPlugin plugin) {}

    public PluginManagementView() {
        TreeItem<String> rootItem = new TreeItem<>("Emulators");
        rpcs3 = new TreeItem<>("RPCS3");
        pcsx2 = new TreeItem<>("PCSX2");
        rootItem.getChildren().add(pcsx2);
        rootItem.getChildren().add(rpcs3);

        rpcs3.setExpanded(true);
        pcsx2.setExpanded(true);

        TreeView<String> treeView = new TreeView<>(rootItem);
        treeView.setShowRoot(false);
        Button load = new Button("Load plugins");
        load.setOnAction(a -> loadPlugins().forEach(this::addPlugin));

        VBox listBox = new VBox(treeView, load);
        
        Label details = new Label();
        details.setPrefWidth(300);
        details.setWrapText(true);
        
        treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        treeView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n == null || !pluginMap.containsKey(n)) {
                details.setText("");
            } else {
                details.setText(getDetails(pluginMap.get(n).plugin().information()));
            }
        });
        this.setSpacing(10);
        this.getChildren().addAll(listBox, details);
    }

    private String getDetails(CompiledPlugin.Information info) {
        return
                """
                %s
                
                Author: %s
                Version: %s
                
                Description:
                %s
                
                For:
                %s %s
                %s
                """.formatted(info.name(), info.author(), info.version(), info.description(), info.serial(), info.gameVersion() != null ? info.gameVersion() : "All versions", info.hash());
    }

    private void addPlugin(CompiledPlugin plugin) {
        CompiledPlugin.Information info = plugin.information();

        TreeItem<String> root;
        if (info.platform() == PineEnums.TargetPlatform.PS2) {
            root = pcsx2;
        } else {
            root = rpcs3;
        }

        TreeItem<String> game = root.getChildren().stream().filter(i -> Objects.equals(i.getValue(), info.serial())).findAny().orElseGet(() -> {
            TreeItem<String> item = new TreeItem<>(info.serial());
            root.getChildren().add(item);
            return item;
        });

        String versionString = info.gameVersion() != null ? info.gameVersion() : "All versions";
        TreeItem<String> version = game.getChildren().stream().filter(i -> Objects.equals(i.getValue(), versionString)).findAny().orElseGet(() -> {
            TreeItem<String> item = new TreeItem<>(versionString);
            game.getChildren().add(item);
            return item;
        });

        CheckBox checkBox = new CheckBox();
        TreeItem<String> moduleItem = new TreeItem<>(info.name(), checkBox);
        pluginMap.put(moduleItem, new SelectablePlugin(checkBox, plugin));
        version.getChildren().add(moduleItem);
    }

    private List<CompiledPlugin> loadPlugins() {
        // TODO: Implement actual loading
        List<CompiledPlugin> modules = new ArrayList<>(8);
        KisuPlugin testPlugin = new KisuPlugin() {
            @Override
            public void init() {

            }

            @Override
            public void onTick(KisuEnvironment env) {

            }

            @Override
            public void close() {

            }
        };
        modules.add(new CompiledPlugin(testPlugin, new CompiledPlugin.Information(PineEnums.TargetPlatform.PS3, "Cool plugin", "Testing author", "1.0", "This is a neat description", "BLUS12345", "01.00", null)));
        modules.add(new CompiledPlugin(testPlugin, new CompiledPlugin.Information(PineEnums.TargetPlatform.PS3, "Interesting plugin", "Testing author", "1.0", "This is a neat description", "BLUS12345", "01.00", null)));
        modules.add(new CompiledPlugin(testPlugin, new CompiledPlugin.Information(PineEnums.TargetPlatform.PS3, "Neat plugin", "Testing author", "1.0", "This is a neat description", "BLUS12345", "01.00", null)));
        modules.add(new CompiledPlugin(testPlugin, new CompiledPlugin.Information(PineEnums.TargetPlatform.PS3, "Fun plugin", "Testing author", "1.0", "This is a neat description", "BLUS12345",null , null)));
        return modules;
    }

    public List<KisuPlugin> getSelectedPlugins() {
        return pluginMap.values().stream()
                .filter(m -> m.checkBox.isSelected())
                .map(SelectablePlugin::plugin)
                .map(CompiledPlugin::plugin)
                .collect(Collectors.toList());
    }
}
