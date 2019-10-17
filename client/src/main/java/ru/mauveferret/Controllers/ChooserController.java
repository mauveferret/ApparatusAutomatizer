package ru.mauveferret.Controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ru.mauveferret.SocketCryptedCommunicator;

import java.net.URL;

public class ChooserController {

    private SocketCryptedCommunicator communicator;
    public void setCommunicator(SocketCryptedCommunicator communicator)
    {
        this.communicator = communicator;
    }

    @FXML
    private ImageView closeButton;

    @FXML
    private void closeChooserWindow()
    {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
        System.exit(0);
    }

    @FXML
    private void menu() {}

    @FXML
    private void openVacuumWindow()
    {
        Platform.runLater((() ->
        {
            try {
                Stage vacuumStage = new Stage();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/Vacuum.fxml"));
                Parent root = loader.load();
                setMovable(root,vacuumStage);
                Scene vacuumScene  = new Scene(root);

                //FIXME
                URL url = this.getClass().getResource("fxml/css/vacuum.css");
                if (url == null) {
                    System.out.println("Resource not found. Aborting.");
                }
                String css = url.toExternalForm();
                vacuumScene.getStylesheets().add(css);
                vacuumStage.setScene(vacuumScene);
                vacuumStage.show();
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
                Stage console = new Stage();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/Console.fxml"));
                Parent root = loader.load();
                setMovable(root,console);
                console.setScene(new Scene(root));
                console.show();
                //((VacuumController) loader.getController()).initialize(communicator);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("shit shit");
            }
        }));

    }


    private void setMovable(Parent root, Stage stage)
    {
        final boolean[] resizebottom = {false};
        final double[] dx = new double[1];
        final double[] dy = new double[1];
        final double[] xOffset = new double[1];
        final double[] yOffset = new double[1];

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setResizable(true);
        stage.initModality(Modality.NONE);

        root.setOnMousePressed(event -> {
            if (event.getX() > stage.getWidth() - 10
                    && event.getX() < stage.getWidth() + 10
                    && event.getY() > stage.getHeight() - 10
                    && event.getY() < stage.getHeight() + 10) {
                resizebottom[0] = true;
                dx[0] = stage.getWidth() - event.getX();
                dy[0] = stage.getHeight() - event.getY();
            } else {
                resizebottom[0] = false;
                xOffset[0] = event.getSceneX();
                yOffset[0] = event.getSceneY();
            }
        });

        root.setOnMouseDragged(event -> {
            if (resizebottom[0] == false) {
                stage.setX(event.getScreenX() - xOffset[0]);
                stage.setY(event.getScreenY() - yOffset[0]);
            } else {
                stage.setWidth(event.getX() + dx[0]);
                stage.setHeight(event.getY() + dy[0]);
            }
        });
    }

}
