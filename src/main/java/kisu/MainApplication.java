package kisu;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import kisu.exceptions.CustomExceptionHandler;

public class MainApplication extends Application {

    static {
        Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler());
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Scene scene = new Scene(new MainView());
        stage.setTitle("Kisu");
        stage.setScene(scene);
        stage.show();
    }
}
