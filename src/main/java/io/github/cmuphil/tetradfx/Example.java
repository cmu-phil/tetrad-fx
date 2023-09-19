package io.github.cmuphil.tetradfx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Example extends Application {

    @Override
    public void start(Stage primaryStage) {
        TabPane outerTabPane = new TabPane();

        // Create three tabs each containing an inner TabPane
        for (int i = 1; i <= 3; i++) {
            Tab outerTab = new Tab("Outer Tab " + i);

            TabPane innerTabPane = new TabPane();
            for (int j = 1; j <= 3; j++) {
                Tab innerTab = new Tab("Inner Tab " + j);
                innerTab.setContent(new Label("Content of Inner Tab " + j));
                innerTabPane.getTabs().add(innerTab);
            }

            outerTab.setContent(innerTabPane);
            outerTabPane.getTabs().add(outerTab);
        }

        StackPane root = new StackPane(outerTabPane);
        Scene scene = new Scene(root, 400, 300);

        primaryStage.setScene(scene);
        primaryStage.show();

        // Get the content of the selected inner TabPane
        outerTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                TabPane innerPane = (TabPane) newTab.getContent();
                Tab selectedInnerTab = innerPane.getSelectionModel().getSelectedItem();
                if (selectedInnerTab != null) {
                    System.out.println("Selected content: " + ((Label) selectedInnerTab.getContent()).getText());
                }
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}



