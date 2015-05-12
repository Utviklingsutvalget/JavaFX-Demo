package no.swact.ufo.notepadfx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class NotepadFX extends Application {

    @FXML
    private Menu progress;
    @FXML
    private MenuItem saveFile;
    @FXML
    private MenuItem openFile;
    @FXML
    private TextArea textArea;
    @FXML
    private ToggleGroup fontSizeGroup;

    private ScheduledService<Void> service;

    private ObjectProperty<File> selectedFile = new SimpleObjectProperty<>();

    public static void main(String[] args) {
        Application.launch(NotepadFX.class);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        URL resource = ClassLoader.getSystemClassLoader().getResource("no/swact/ufo/notepadfx/NotepadFX.fxml");
        assert resource != null;
        FXMLLoader loader = new FXMLLoader(resource);
        Parent root = loader.load();
        NotepadFX controller = loader.getController();
        primaryStage.setScene(new Scene(root));
        controller.setup();

        createOnCloseListener(primaryStage);

        primaryStage.show();
    }

    private void createOnCloseListener(final Stage primaryStage) {
        Platform.setImplicitExit(false);

        primaryStage.setOnCloseRequest(event -> {
            Alert choiceDialog = new Alert(Alert.AlertType.CONFIRMATION, "Would you like to save before exiting?", ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
            Optional<ButtonType> optionalResponse = choiceDialog.showAndWait();

            ButtonType response = optionalResponse.get();

            if (response == ButtonType.CANCEL) {
                event.consume();
            } else {
                Platform.exit();
            }
        });
    }

    private void saveFile(File target, String content) {
        try (FileOutputStream output = new FileOutputStream(target)) {
            IOUtils.write(content, output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String loadFile(File target) {
        try (FileInputStream input = new FileInputStream(target)) {
            return IOUtils.toString(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setup() {
        textArea.setFont(getFontFromMenuItem((RadioMenuItem) fontSizeGroup.getSelectedToggle()));
        createFontListener();
        setupLoadFile();
        setupSaveFile();
        selectedFile.addListener((observable, oldValue, newValue) -> {
            File file = observable.getValue();
            if (newValue == null) {
                return;
            }
            if (service != null) {
                service.cancel();
            }
            service = new ScheduledService<Void>() {
                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            System.out.println("Saving");
                            saveFile(file, textArea.getText());
                            System.out.println("It seems we have saved a file");

                            Thread.sleep(1000L);
                            succeeded();
                            return null;
                        }
                    };
                }
            };
            service.setPeriod(Duration.millis(5000));
            service.setOnRunning(event1 -> progress.setVisible(true));
            service.setOnSucceeded(event -> {
                System.out.println("'sup?");
                progress.setVisible(false);
            });
            service.start();
        });
    }


    private void setupSaveFile() {
        saveFile.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select file to save");
            File file = chooser.showSaveDialog(null);
            if (file == null) {
                return;
            }
            selectedFile.setValue(file);
        });
    }

    private void setupLoadFile() {
        openFile.setOnAction(event -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select file to load");
            File file = chooser.showOpenDialog(null);
            if (file == null) {
                return;
            }
            textArea.setText(loadFile(file));
        });
    }

    private void createFontListener() {
        fontSizeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                return;
            }
            final Font value = getFontFromMenuItem((RadioMenuItem) newValue);
            textArea.setFont(value);
        });
    }

    private Font getFontFromMenuItem(final RadioMenuItem newValue) {
        final String text = newValue.getText();
        final Double size = Double.valueOf(text);
        return new Font(size);
    }
}
