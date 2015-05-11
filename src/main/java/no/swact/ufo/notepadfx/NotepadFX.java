package no.swact.ufo.notepadfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class NotepadFX extends Application {

    public static void main(String[] args) {
        Application.launch(NotepadFX.class);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        URL resource = ClassLoader.getSystemClassLoader().getResource("no/swact/ufo/notepadfx/NotepadFX.fxml");
        assert resource != null;
        Parent root = FXMLLoader.load(resource);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
