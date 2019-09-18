package ru.mauveferret;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
public class Main extends Application {


    @Override
    public void start(Stage primaryStage)  throws Exception {

        //System.out.println(getClass().getResource("fxml/Login.fxml"));
        Parent root = FXMLLoader.load(getClass().getResource("Controllers/fxml/Login.fxml"));
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(new Scene(root, 800, 430));
        primaryStage.show();
    }


    public static void main(String[] args) {

        launch(args);
        //new ClientCommunicator("localhost", 4004).start();
        System.out.println("?!");
    }
}