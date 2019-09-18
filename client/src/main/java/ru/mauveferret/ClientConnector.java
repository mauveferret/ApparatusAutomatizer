package ru.mauveferret;


import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ru.mauveferret.Controllers.ChooserController;
import ru.mauveferret.Controllers.LoginWindowController;

public class ClientConnector extends Thread{


    public ClientConnector(String host, int port, LoginWindowController controller) {
        loginWindow = controller;
        communicator = new SocketCryptedCommunicator(host,port);
        String serverStatus = (communicator.connectToServer()) ? "server is ONLINE" : "server is OFFLINE";
        Platform.runLater((() -> loginWindow.setConnectionStatus(serverStatus)));
    }

    private SocketCryptedCommunicator communicator;
    private LoginWindowController loginWindow;
    //TODO
    private int accessType;



    @Override
    public synchronized void start() {

        if (communicator.createSecureCommunicationLine()) {
            //Platform.runLater((() -> loginWindow.setConnectionStatus("data encryption enabled")));+
            Platform.runLater((() -> communicator.writeEncryption(loginWindow.getLogin()+" "+loginWindow.getPassword())));
           // System.out.println("ewaf "+loginANdPassword);
            boolean isGranted = "granted".equals(communicator.readEncryption());
            if (isGranted)
            {
                Platform.runLater((() -> loginWindow.setConnectionStatus("Access GRANTED.")));
                Platform.runLater((() ->
                {try {
                        Stage windowsChooser = new Stage();
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("Controllers/fxml/Chooser.fxml"));
                        windowsChooser.initStyle(StageStyle.UNDECORATED);
                        windowsChooser.setScene(new Scene(loader.load(), 800, 430));
                        windowsChooser.show();
                        ((ChooserController) loader.getController()).setCommunicator(communicator);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        System.out.println("shit shit");
                    }}
                ));
                Platform.runLater((() -> loginWindow.closeWindow()));
            }
            else
            {
                Platform.runLater((() -> loginWindow.setConnectionStatus("Access DENIED.")));
            }
        }
        else
        {
            sendMessage("WTF");
        }
    }




     void sendMessage(String message)
    {
        System.out.println(message);
    }

}
