package ru.mauveferret.Controllers;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ru.mauveferret.SocketCryptedCommunicator;

public class ChooserController {

    private SocketCryptedCommunicator communicator;
    public void setCommunicator(SocketCryptedCommunicator communicator)
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
                Parent root = loader.load();
                windowsChooser.initStyle(StageStyle.UNDECORATED);
                windowsChooser.setResizable(true);
                windowsChooser.initModality(Modality.NONE);

                final boolean[] resizebottom = {false};
                final double[] dx = new double[1];
                final double[] dy = new double[1];
                final double[] xOffset = new double[1];
                final double[] yOffset = new double[1];

                root.setOnMousePressed(new EventHandler<MouseEvent>() {
                    public void handle(MouseEvent event) {
                        if (event.getX() > windowsChooser.getWidth() - 10
                                && event.getX() < windowsChooser.getWidth() + 10
                                && event.getY() > windowsChooser.getHeight() - 10
                                && event.getY() < windowsChooser.getHeight() + 10) {
                            resizebottom[0] = true;
                            dx[0] = windowsChooser.getWidth() - event.getX();
                            dy[0] = windowsChooser.getHeight() - event.getY();
                        } else {
                            resizebottom[0] = false;
                            xOffset[0] = event.getSceneX();
                            yOffset[0] = event.getSceneY();
                        }
                    }
                });

                root.setOnMouseDragged(event -> {
                    if (resizebottom[0] == false) {
                        windowsChooser.setX(event.getScreenX() - xOffset[0]);
                        windowsChooser.setY(event.getScreenY() - yOffset[0]);
                    } else {
                        windowsChooser.setWidth(event.getX() + dx[0]);
                        windowsChooser.setHeight(event.getY() + dy[0]);
                    }
                });


                Scene scene = new Scene(root);
                windowsChooser.setScene(scene);
                //scene.getStylesheets().add(getClass().getResource("fxml/css/Caspian.css").toExternalForm());
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
