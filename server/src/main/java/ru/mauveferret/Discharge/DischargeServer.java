package ru.mauveferret.Discharge;

import ru.mauveferret.Server;

class DischargeServer extends Server {

    DischargeServer(String fileName) {
        super(fileName);
    }



    //TODO divide commands handler into separete pieces in Terminal style
    //Here you'll have several 'devices' like vacuum, discharge, console (all that is in Chooser.fxml in client)
    //First word will be a 'device' command
    //TODO make packages

    //Fixme vacuumResponse. make methods for all?
    @Override
    public String createResponse(String request)
    {
        String response = "";
        return response;
    }


}
