package ru.mauveferret;


import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import ru.mauveferret.Controllers.ChooserController;
import ru.mauveferret.Controllers.LoginWindowController;

public class ClientConnector extends Thread{


    public ClientConnector(SectionsKeeper keeper, LoginWindowController controller) {
        loginWindow = controller;
        this.keeper = keeper;
        vacuum = keeper.getCommunicator(SectionsKeeper.VACUUM);
        //FIXME for other servers too
        String serverStatus = (vacuum.connectToServer()) ? "server is ONLINE" : "server is OFFLINE";
        Platform.runLater((() -> loginWindow.setConnectionStatus(serverStatus)));
    }

    private SectionsKeeper keeper;
    private LoginWindowController loginWindow;
    //TODO
    private int accessType;
    private SocketCryptedCommunicator vacuum;
    private SocketCryptedCommunicator discharge;
    private SocketCryptedCommunicator diagnostics;



    @Override
    public synchronized void start() {

        if (vacuum.createSecureCommunicationLine()) {
            //Platform.runLater((() -> loginWindow.setConnectionStatus("data encryption enabled")));+
            Platform.runLater((() -> vacuum.writeEncryption(loginWindow.getLogin()+" "+loginWindow.getPassword())));
           // System.out.println("ewaf "+loginANdPassword);
            boolean isGranted = "granted".equals(vacuum.readEncryption());
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
                        ((ChooserController) loader.getController()).setCommunicator(vacuum);
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
