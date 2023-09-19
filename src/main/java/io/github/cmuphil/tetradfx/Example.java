package io.github.cmuphil.tetradfx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Example extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        Menu dynamicMenu = new Menu("Dynamic");

        dynamicMenu.setOnAction(event -> {
            if (dynamicMenu.getItems().isEmpty()) {
                populateMenu(dynamicMenu);
            }
        });

        fileMenu.getItems().add(dynamicMenu);
        menuBar.getMenus().add(fileMenu);

        BorderPane pane = new BorderPane();
        pane.setTop(menuBar);
        Scene scene = new Scene(pane, 300, 200);

        primaryStage.setTitle("Dynamic Menu Example");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void populateMenu(Menu menu) {
        for (int i = 1; i <= 3; i++) {
            int _i = i;
            MenuItem item = new MenuItem("Dynamic Item " + _i);
            item.setOnAction(actionEvent -> System.out.println("Selected: Dynamic Item " + _i));
            menu.getItems().add(item);
        }
    }
}


