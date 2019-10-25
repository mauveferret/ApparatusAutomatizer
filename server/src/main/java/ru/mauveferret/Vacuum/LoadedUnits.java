package ru.mauveferret.Vacuum;

import ru.mauveferret.Server;

import java.util.ArrayList;
import java.util.HashMap;

public class LoadedUnits {

    public LoadedUnits() {
        column1 = new PumpingColumn();
        column2 = new PumpingColumn();
        gauge = new HashMap<>();
    }

    static PumpingColumn column1;
    static PumpingColumn column2;

    static HashMap<String,Gauge> gauge;
    static Server server;


}
