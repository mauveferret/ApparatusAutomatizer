package ru.mauveferret.Discharge;

import ru.mauveferret.Server;
import ru.mauveferret.SocketCryptedCommunicator;

import java.util.HashMap;

class DischargeServer extends Server {

    DischargeServer(String fileName) {
        super(fileName);
    }

    @Override
    protected void convertDataFromInitializeToLocalType(HashMap<String, String> initializeData) {

    }

    @Override
    protected void communicate(SocketCryptedCommunicator communicator) {

    }


    //TODO divide commands handler into separete pieces in Terminal style
    //Here you'll have several 'devices' like vacuum, discharge, console (all that is in Chooser.fxml in client)
    //First word will be a 'device' command
    //TODO make packages



}
