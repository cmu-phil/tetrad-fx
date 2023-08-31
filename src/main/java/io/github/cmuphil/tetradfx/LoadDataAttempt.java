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

    public void start1(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Data File");
        File selectedFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedFile != null) {
            System.out.println("File selected: " + selectedFile.getAbsolutePath());
        } else {
            System.out.println("File selection cancelled.");
        }

        DataSet dataSet;

        try {
            dataSet = SimpleDataLoader.loadContinuousData(selectedFile, "//", '\"',
                    "*", true, Delimiter.TAB);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println(dataSet);
    }

    @Override
    public void start(Stage primaryStage) {
        // Radio buttons setup
        RadioButton continuousBtn = new RadioButton("Continuous Dataset");
        RadioButton discreteBtn = new RadioButton("Discrete Dataset");
        ToggleGroup toggleGroup = new ToggleGroup();
        continuousBtn.setToggleGroup(toggleGroup);
        discreteBtn.setToggleGroup(toggleGroup);
        continuousBtn.setSelected(true);  // Default selected radio button

        // Load data button setup
        Button loadDataBtn = new Button("Load Data");
        loadDataBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            File selectedFile = fileChooser.showOpenDialog(primaryStage);

            if (selectedFile != null) {
                System.out.println("File selected: " + selectedFile.getAbsolutePath());

                DataSet dataSet;

                // You can add further processing based on the type of dataset chosen.
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

        // Putting everything together
        HBox choice = new HBox(10, continuousBtn, discreteBtn);
        VBox layout = new VBox(10, choice, loadDataBtn);
        Scene scene = new Scene(layout, 300, 150);

        primaryStage.setTitle("Dataset Dialog");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

