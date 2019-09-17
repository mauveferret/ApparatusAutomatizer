package ru.mauveferret;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ChooserController {

    private SocketCryptedCommunicator communicator;
    void setCommunicator(SocketCryptedCommunicator communicator)
    {
        this.communicator = communicator;
        System.out.println("получилось!");
    }

    @FXML
    private JFXButton console;
    @FXML
    private JFXButton vacuum;


    @FXML
    private void closeWindow()
    {
        Stage stage = (Stage) console.getScene().getWindow();
        stage.close();
        System.exit(0);
    }

    @FXML
    private void openVacuumWindow()
    {
        Platform.runLater((() ->
        {
            try {
                Stage windowsChooser = new Stage();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/Vacuum.fxml"));
                //windowsChooser.initStyle(StageStyle.UNDECORATED);
                windowsChooser.setResizable(true);
                windowsChooser.initModality(Modality.NONE);
                windowsChooser.setScene(new Scene(loader.load()));
                windowsChooser.show();
                ((VacuumController) loader.getController()).initialize(communicator);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("shit shit");
            }
        }));
    }

    @FXML
    private void openConsole()
    {
        Platform.runLater((() ->
        {
            try {
                Stage windowsChooser = new Stage();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/Console.fxml"));
                //windowsChooser.initStyle(StageStyle.UNDECORATED);
                windowsChooser.setResizable(true);
                windowsChooser.initModality(Modality.NONE);
                windowsChooser.setScene(new Scene(loader.load()));
                windowsChooser.show();
                //((VacuumController) loader.getController()).initialize(communicator);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("shit shit");
            }
        }));

    }

}
