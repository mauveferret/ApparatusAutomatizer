package ru.mauveferret;

import ru.mauveferret.Controllers.VacuumController;

class Vacuum extends  Thread {

    VacuumController controller;
    SocketCryptedCommunicator communicator;
    Vacuum(VacuumController controller, SocketCryptedCommunicator communicator) {
        this.controller = controller;
        this.communicator = communicator;
    }


    @Override
    public void run() {
        while (true) {
            String message = communicator.makeRequest("nocommand", true);
            int time = Integer.parseInt(message.split(" ")[0]);
            double value = Double.parseDouble(message.split(" ")[1]);
            System.out.println(time + " " + value);
             //Platform.runLater((() -> controller.addData(time,value)));
             try {
                 Thread.sleep(500);
             }
             catch (Exception ignored){}
        }
    }
}
