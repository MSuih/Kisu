package kisu;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import kisu.plugin.PluginController;
import kisu.plugin.PluginManagementView;
import pinej.PineClient;
import pinej.PineEnums;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import kisu.exceptions.ConnectFailedException;

import java.util.Optional;

public class MainView extends VBox {
    private final MainController mainController = new MainController();
    private final PluginController pluginController = new PluginController();

    private final RadioButton rpcs3;
    private final RadioButton pcsx2;
    private final Button activate;


    private final Label status;
    private final Label game;
    private final Button refresh;

    private final Label plugins;
    private final Button enable;

    private final ToggleGroup emuButtonGroup;

    public MainView() {
        this.setPrefWidth(250);
        this.setPadding(new Insets(10));
        this.setSpacing(3);

        rpcs3 = new RadioButton("RPCS3");
        rpcs3.setUserData(PineEnums.TargetPlatform.PS3);
        pcsx2 = new RadioButton("PCSX2");
        pcsx2.setUserData(PineEnums.TargetPlatform.PS2);

        emuButtonGroup = new ToggleGroup();
        rpcs3.setToggleGroup(emuButtonGroup);
        pcsx2.setToggleGroup(emuButtonGroup);

        activate = new Button("Connect");
        activate.setDisable(true);

        TitledPane emuPane = createPane("Emulator", rpcs3, pcsx2, activate);

        status = new Label("Not connected");
        game = new Label("-");

        refresh = new Button("Refresh");
        refresh.setDisable(true);

        TitledPane statusPane = createPane("PINE Status", status, game, refresh);

        plugins = new Label("0 plugins selected");
        Button manage = new Button("Manage plugins");
        enable = new Button("Activate");
        enable.setDisable(true);

        TitledPane pluginPane = createPane("Kisu Plugins", plugins, new HBox(enable, manage));

        emuButtonGroup.selectedToggleProperty()
                .addListener((obs, oldValue, newValue) -> activate.setDisable(newValue == null));
        activate.setOnAction(this::handleActivate);
        refresh.setOnAction(a -> update());
        manage.setOnAction(a -> showPluginManager());

        this.getChildren().addAll(emuPane, statusPane, pluginPane);
    }

    private TitledPane createPane(String title, Node... nodes) {
        VBox moduleBox = new VBox(nodes);
        moduleBox.setSpacing(3);
        TitledPane modulePane = new TitledPane(title, moduleBox);
        modulePane.setCollapsible(false);
        return modulePane;
    }

    private void update() {
        if (!mainController.isConnected()) {
            status.setText("Not connected");
            game.setText("-");
            return;
        }

        PineEnums.Status emuStatus = mainController.getStatus();
        status.setText(switch (emuStatus) {
            case ERROR -> "Could not connect to emulator";
            case PAUSED -> "Game is paused";
            case RUNNING -> "Game is running";
            case SHUTDOWN -> "No game launched";
        });
        if (emuStatus != PineEnums.Status.RUNNING && emuStatus != PineEnums.Status.PAUSED) {
            game.setText("-");
            enable.setDisable(true);
        } else {
            String title = mainController.getGame().map(PineClient.GameInfo::title).orElse("No game found");
            String serial = mainController.getGame().map(PineClient.GameInfo::id).map(s -> "(" + s + ")").orElse("(?)");
            game.setText(title + " " + serial);
            enable.setDisable(false);
        }
    }

    private void handleActivate(ActionEvent event) {
        if (mainController.isConnected()) {
            disconnect();
        } else {
            connect();
        }
    }

    private void connect() {
        Toggle selectedToggle = emuButtonGroup.getSelectedToggle();
        if (selectedToggle == null) {
            throw new IllegalStateException("Nothing selected");
        }
        try {
            refresh.setDisable(false);
            activate.setText("Disconnect");
            rpcs3.setDisable(true);
            pcsx2.setDisable(true);
            mainController.onConnect((PineEnums.TargetPlatform) selectedToggle.getUserData());
            update();
        } catch (ConnectFailedException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
            alert.show();
        }
    }

    private void disconnect() {
        activate.setText("Activate");
        rpcs3.setDisable(false);
        pcsx2.setDisable(false);
        refresh.setDisable(true);
        mainController.onDisconnect();
    }

    private void showPluginManager() {
        PluginManagementView pluginManagementView = new PluginManagementView();
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Kisu Plugin Manager");
        dialog.getDialogPane().setContent(pluginManagementView);

        ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().add(save);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        Optional<ButtonType> buttonType = dialog.showAndWait();
        if (buttonType.isPresent() && buttonType.get() == save) {
            pluginController.setPlugins(pluginManagementView.getSelectedModules());
        }
        plugins.setText(pluginController.getLoadedPlugins() + " plugins selected");
    }
}
