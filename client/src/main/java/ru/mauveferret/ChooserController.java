package ru.mauveferret;

import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.stage.Stage;

public class ChooserController {


    @FXML
    private JFXButton console;


    @FXML
    private void closeWindow()
    {
        Stage stage = (Stage) console.getScene().getWindow();
        stage.close();
        System.exit(0);
    }

    @FXML
    private void openConsole()
    {

    }

}
