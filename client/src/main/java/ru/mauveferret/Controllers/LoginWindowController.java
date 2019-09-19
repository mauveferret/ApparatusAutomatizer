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
import ru.mauveferret.SectionsKeeper;

public class LoginWindowController {

    private boolean isMenuVisible = false;
    private String messages = "";

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
    @FXML
    private JFXTextField dischargeIP;
    @FXML
    private JFXTextField dischargePort;
    @FXML
    private JFXTextField diagnosticsIP;
    @FXML
    private JFXTextField diagnosticsPort;


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

    @FXML
    private void closeLoginWindows()
    {
        // get a handle to the stage
        Stage stage = (Stage) connectButton.getScene().getWindow();
        stage.close();
        System.exit(0);
    }

    public void closeWindow()
    {
        // get a handle to the stage
        Stage stage = (Stage) connectButton.getScene().getWindow();
        stage.close();
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
        if ((!"".equals(login.getText())) && (!"".equals(password.getText()))) {
            SectionsKeeper keeper = new SectionsKeeper();
            try
            {
                keeper.addSection(SectionsKeeper.VACUUM,vacuumIP.getText(),vacuumPort.getText());
            }
            catch (Exception e)
            {
                addMessage(SectionsKeeper.VACUUM+" host or port is incorrect");
            }
            try
            {
                keeper.addSection(SectionsKeeper.DISCHARGE,dischargeIP.getText(),dischargePort.getText());
            }
            catch (Exception e)
            {
                addMessage(SectionsKeeper.DISCHARGE+" host or port is incorrect");
            }
            try
            {
                keeper.addSection(SectionsKeeper.DIAGNOSTICS,diagnosticsIP.getText(),diagnosticsPort.getText());
            }
            catch (Exception e)
            {
                addMessage(SectionsKeeper.DIAGNOSTICS+" host or port is incorrect");
            }

            new Thread(() -> {
                new ClientConnector(keeper, this).start();
            }).start();
        }
        else
        {
            info.setVisible(true);
            addMessage("enter login and password, please.");
            //statusLabel.setText("enter login and password, please!");
        }
    }

    private void addMessage(String message)
    {
        messages+="\n"+message;
        infoTextArea.setText(messages);
    }
}
