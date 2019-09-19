package ru.mauveferret.Controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.control.Label;
import ru.mauveferret.ClientConnector;

public class LoginWindowController {

    private boolean isMenuVisible = false;

    @FXML
    private JFXTextField login;
    @FXML
    private JFXPasswordField password;
    @FXML
    private JFXButton connectButton;
    @FXML
    private Label statusLabel;
    @FXML
    private JFXTextArea infoTextArea;
    @FXML
    private Pane info;
    @FXML
    private Pane setup;
    @FXML
    private JFXTextField vacuumIP;
    @FXML
    private JFXTextField vacuumPort;





    //Setters and Getters

    public void  setConnectionStatus(String message)
    {
        statusLabel.setText(message);
    }

    public String getLogin()
    {
        return login.getText();
    }

    public String getPassword()
    {
        return password.getText();
    }

    //...

    public void closeWindow()
    {
        Stage stage = (Stage) connectButton.getScene().getWindow();
        stage.close();
    }

    //event handlers

    @FXML
    private void closeLoginWindows()
    {
        // get a handle to the stage
        Stage stage = (Stage) connectButton.getScene().getWindow();
        stage.close();
        System.exit(0);
    }

    @FXML
    private void menu()
    {
        isMenuVisible=!isMenuVisible;
        setup.setVisible(isMenuVisible);
    }

    @FXML
    private void connectToServer()
    {
        if ((!"".equals(login.getText())) && (!"".equals(password.getText())))
            new Thread(() -> {
                new ClientConnector(vacuumIP.getText(), Integer.parseInt(vacuumPort.getText()), this).start();
            }).start();
        else
        {
            info.setVisible(true);
            infoTextArea.setText("enter login and password, please!");
            //statusLabel.setText("enter login and password, please!");
        }
    }
}
