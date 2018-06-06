package com.github.llamas77.aofuentes;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    public static Main instance;

    public Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        instance = this;
        Parent root = FXMLLoader.load(getClass().getResource("/views/Main.fxml"));
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("AO Fuentes");
        primaryStage.setResizable(false);
        primaryStage.show();
        stage = primaryStage;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
