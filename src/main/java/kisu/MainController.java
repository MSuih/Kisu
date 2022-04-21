package kisu;

import pinej.PineClient;
import pinej.PineEnums;
import kisu.exceptions.ConnectFailedException;
import mouse.KisuPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MainController {
    private final Logger logger = LoggerFactory.getLogger(MainController.class);

    private Path path = Path.of("pine_c.dll").toAbsolutePath();
    private PineClient pineClient;
    private final List<KisuPlugin> activeModules = new ArrayList<>();

    public void setDllPath(Path path) {
        this.path = path.toAbsolutePath();
    }

    public boolean isConnected() {
        return pineClient != null;
    }

    public void onConnect(PineEnums.TargetPlatform platform) throws ConnectFailedException {
        logger.info("Initializing IPC library at {}", path);
        try {
            pineClient = new PineClient(path, platform);
        } catch (IllegalArgumentException e) {
            throw new ConnectFailedException("Could not find or load pine_c library, make sure it exists at " + path, e);
        } catch (IllegalStateException e) {
            throw new ConnectFailedException("Could not load library, make sure you have '--enable-native-access IPC2J' set", e);
        }
    }

    public void onDisconnect() {
        pineClient.close();
        pineClient = null;
        activeModules.clear();
        logger.info("Disconnected from IPC");
    }

    public void onStartup() {
        Objects.requireNonNull(pineClient, "IPC client must be started first");
        activeModules.forEach(KisuPlugin::init);
    }

    public Optional<PineClient.GameInfo> getGame() {
        return pineClient.getGameInfo();
    }

    public PineEnums.Status getStatus() {
        return pineClient.getStatus();
    }

}
