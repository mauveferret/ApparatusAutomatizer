package ru.mauveferret;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.stage.Stage;

public class ConsoleController {

    @FXML
    private JFXButton sendButton;
    @FXML
    private JFXTextField command;
    @FXML
    private JFXTextField message;

    @FXML
    private void send()
    {

    }

    @FXML
    private void closeWindow()
    {
        Stage stage = (Stage) command.getScene().getWindow();
        stage.close();
    }


}
