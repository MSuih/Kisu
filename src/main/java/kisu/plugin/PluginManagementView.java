package kisu.plugin;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import mouse.AbstractKisuPlugin;
import mouse.KisuPlugin;
import mouse.PluginInformation;
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

    private record SelectablePlugin(CheckBox checkBox, KisuPlugin module) {}

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
        load.setOnAction(a ->  {
            loadPlugins().forEach(this::addPlugin);
        });

        VBox listBox = new VBox(treeView, load);
        
        Label details = new Label();
        details.setPrefWidth(300);
        details.setWrapText(true);
        
        treeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        treeView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n == null || !pluginMap.containsKey(n)) {
                details.setText("");
            } else {
                details.setText(getDetails(pluginMap.get(n).module.getInfo()));
            }
        });
        this.setSpacing(10);
        this.getChildren().addAll(listBox, details);
    }

    private String getDetails(PluginInformation info) {
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
                """.formatted(info.name(), info.author(), info.moduleVersion(), info.description(), info.gameSerial(), info.gameVersion() != null ? info.gameVersion() : "All versions", info.gameHash());
    }

    private void addPlugin(KisuPlugin plugin) {
        PluginInformation info = plugin.getInfo();

        TreeItem<String> root;
        if (PineEnums.TargetPlatform.PS2 == info.platform()) {
            root = pcsx2;
        } else {
            root = rpcs3;
        }

        TreeItem<String> game = root.getChildren().stream().filter(i -> Objects.equals(i.getValue(), info.gameSerial())).findAny().orElseGet(() -> {
            TreeItem<String> item = new TreeItem<>(info.gameSerial());
            item.setGraphic(new Rectangle());
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

    private List<KisuPlugin> loadPlugins() {
        // TODO: Implement actual loading
        List<KisuPlugin> modules = new ArrayList<>(8);
        modules.add(new AbstractKisuPlugin(new PluginInformation(PineEnums.TargetPlatform.PS3, "Cool plugin", "Test", "1.0", "BLES12345", "asdf", null, "This is the description for this particular item\n\nIt supports newlines too, and and wrapping on long lines!")){});
        modules.add(new AbstractKisuPlugin(new PluginInformation(PineEnums.TargetPlatform.PS3, "Sweet plugin", "Test", "1.0", "BLES12345", "asdf", null, "This is the description for this particular item\n\nIt supports newlines too, and and wrapping on long lines!")){});
        modules.add(new AbstractKisuPlugin(new PluginInformation(PineEnums.TargetPlatform.PS3, "Neat plugin", "Test", "1.0", "BLES22222", "asdf", null, "This is the description for this particular item\n\nIt supports newlines too, and and wrapping on long lines!")){});
        modules.add(new AbstractKisuPlugin(new PluginInformation(PineEnums.TargetPlatform.PS3, "Amazing plugin", "Test", "1.0", "BLES12345", "asdf", "01.00", "This is the description for this particular item\n\nIt supports newlines too, and and wrapping on long lines!")){});

        modules.add(new AbstractKisuPlugin(new PluginInformation(PineEnums.TargetPlatform.PS2, "Odd plugin", "Test", "1.0", "SLES12345", "asdf", null, "This is the description for this particular item\n\nIt supports newlines too, and and wrapping on long lines!")){});
        modules.add(new AbstractKisuPlugin(new PluginInformation(PineEnums.TargetPlatform.PS2, "Questionable plugin", "Test", "1.0", "SLES12345", "asdf", null, "This is the description for this particular item\n\nIt supports newlines too, and and wrapping on long lines!")){});
        modules.add(new AbstractKisuPlugin(new PluginInformation(PineEnums.TargetPlatform.PS2, "Strange plugin", "Test", "1.0", "SLES44444", "asdf", null, "This is the description for this particular item\n\nIt supports newlines too, and and wrapping on long lines!")){});
        modules.add(new AbstractKisuPlugin(new PluginInformation(PineEnums.TargetPlatform.PS2, "Weird plugin", "Test", "1.0", "SLES12345", "asdf", null, "This is the description for this particular item\n\nIt supports newlines too, and and wrapping on long lines!")){});
        return modules;
    }

    public List<KisuPlugin> getSelectedModules() {
        return pluginMap.values().stream()
                .filter(m -> m.checkBox.isSelected())
                .map(SelectablePlugin::module)
                .collect(Collectors.toList());
    }
}
