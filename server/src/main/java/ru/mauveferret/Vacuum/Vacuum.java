package ru.mauveferret.Vacuum;

import ru.mauveferret.Terminal;
import ru.mauveferret.Vacuum.Gauges.PfeifferGauge;
import ru.mauveferret.Vacuum.Gauges.ThyracontGauge;
import ru.mauveferret.Vacuum.Pumps.LeyboldTMP;

public class Vacuum {

    //just initializes  all vacuum devices


    public Vacuum(Terminal terminal) {
        LeyboldTMP leyboldTMP = new LeyboldTMP("tmp");
        ThyracontGauge thyracontGauge = new ThyracontGauge("thyracontGauge");
        PfeifferGauge pfeifferGauge = new PfeifferGauge("pfeifferGauge");
        Bypass bypass = new Bypass("bypass");
        GateControl gateControl = new GateControl("gateControl");
        VacuumServer server = new VacuumServer("server");
        GuardianAngel guardianAngel = new GuardianAngel("angel");
        terminal.addDevice(server);
        terminal.addDevice(thyracontGauge);
        terminal.addDevice(pfeifferGauge);
        terminal.addDevice(leyboldTMP);
        terminal.addDevice(bypass);
        terminal.addDevice(gateControl);
        terminal.addDevice(guardianAngel);
    }
}
