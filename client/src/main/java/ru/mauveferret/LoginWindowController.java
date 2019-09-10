package ru.mauveferret;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.control.Label;

public class LoginWindowController {


    @FXML
    private ImageView closeButton;
    @FXML
    private JFXTextField login;
    @FXML
    private JFXPasswordField password;
    @FXML
    private JFXButton connectButton;
    @FXML
    private Label statusLabel;

    private String host = "localhost";
    private int port = 4004;

    //Setters and Getters

    void  setConnectionStatus(String message)
    {
        statusLabel.setText(message);
    }

    String getLogin()
    {
        return login.getText();
    }

    String getPassword()
    {
        return password.getText();
    }

    //...

    void closeWindow()
    {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    //event handlers

    @FXML
    private void closeLoginWindows()
    {
        // get a handle to the stage
        Stage stage = (Stage) closeButton.getScene().getWindow();
        // do what you have to do
        stage.close();
        System.exit(0);
    }

    @FXML
    private void connectToServer()
    {
        if ((!"".equals(login.getText())) && (!"".equals(password.getText())))
            new Thread(() -> {
                new ClientConnector(host, port, this).start();
            }).start();
        else
            statusLabel.setText("enter login and password, please!");
    }
}
