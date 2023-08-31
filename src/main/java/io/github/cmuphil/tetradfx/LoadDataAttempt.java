package io.github.cmuphil.tetradfx;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.data.SimpleDataLoader;
import edu.pitt.dbmi.data.reader.Delimiter;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class LoadDataAttempt extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        RadioButton continuousBtn = new RadioButton("Continuous Dataset");
        RadioButton discreteBtn = new RadioButton("Discrete Dataset");
        ToggleGroup toggleGroup = new ToggleGroup();
        continuousBtn.setToggleGroup(toggleGroup);
        discreteBtn.setToggleGroup(toggleGroup);
        continuousBtn.setSelected(true);  // Default selected radio button

        Button loadDataBtn = new Button("Load Data");
        loadDataBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            File selectedFile = fileChooser.showOpenDialog(primaryStage);

            if (selectedFile != null) {
                System.out.println("File selected: " + selectedFile.getAbsolutePath());

                DataSet dataSet;

                if (continuousBtn.isSelected()) {
                    try {
                        dataSet = SimpleDataLoader.loadContinuousData(selectedFile, "//", '\"',
                                "*", true, Delimiter.TAB);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    try {
                        dataSet = SimpleDataLoader.loadDiscreteData(selectedFile, "//", '\"',
                                "*", true, Delimiter.TAB);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                System.out.println(dataSet);
            } else {
                System.out.println("File selection cancelled.");
            }
        });

        HBox choice = new HBox(10, continuousBtn, discreteBtn);
        VBox layout = new VBox(10, choice, loadDataBtn);
        Scene scene = new Scene(layout, 300, 150);

        primaryStage.setTitle("Dataset Dialog");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

