package ru.mauveferret.Vacuum;

import ru.mauveferret.Arduino;
import ru.mauveferret.Server;
import ru.mauveferret.Terminal;
import ru.mauveferret.Unit;
import ru.mauveferret.Vacuum.Gauges.PfeifferGauge;
import ru.mauveferret.Vacuum.Gauges.ThyracontGauge;
import ru.mauveferret.Vacuum.Pumps.LeyboldTMP;

import java.io.File;
import java.util.ArrayList;

public class VacuumUnits extends Unit {

    public VacuumUnits(String fileName) {
        super(fileName);
    }



    @Override
    protected void initialize() {
        super.initialize();
    }

//just initializes  all vacuum devices


   public void loadVacuumUnits() {


        ArrayList<Unit> allUnits = new ArrayList<>();
        Gauge gla = null;
        for (String g: LoadedUnits.gauge.keySet())
        {
            if (gla != LoadedUnits.gauge.get(g)) {
                allUnits.add(LoadedUnits.gauge.get(g));
                gla = LoadedUnits.gauge.get(g);
            }
        }
        allUnits.addAll(LoadedUnits.column1.getAll());
        allUnits.addAll(LoadedUnits.column2.getAll());
        for (Unit u: allUnits)
        {
            terminalSample.addDevice(u);
        }
       terminalSample.addDevice(LoadedUnits.server);
    }

    @Override
    protected void chooseImportCommand(String line) {

        try {
           if (thyracontGaugeWasCreated == null) thyracontGaugeWasCreated = false;
        }
        catch (Exception ignored){}

        String[] command = line.toLowerCase().split(" ");
        switch (command[0])
        {
            case "gauge": gauge(command);
            break;
            case "arduino" : arduino(command);
            break;
            case "tmp" : tmp(command);
            break;
            case "gatecontrol" : control(command);
            break;
            case "angel" : angel(command);
            break;
            case "auto" : auto(command);
            break;
            case "server" : server(command);
            break;
        }
        super.chooseImportCommand(line);
    }


    private Boolean thyracontGaugeWasCreated;
    String gaugeName;
    private void gauge(String[] params)
    {
        switch (params[1].toLowerCase())
        {
            case "thyracontgauge":  {
                Gauge local;
                if (!thyracontGaugeWasCreated)
                {
                    gaugeName = params[2];
                    local = new ThyracontGauge(config.name+File.separator+params[3]);
                }
                else
                    local = LoadedUnits.gauge.get(gaugeName);
                LoadedUnits.gauge.put(params[2],local);
                thyracontGaugeWasCreated = true;
            }
            break;
            case "pfeiffergauge": {
            LoadedUnits.gauge.put(params[2], new PfeifferGauge(config.name+File.separator+params[3]));
            }
            break;
        }
    }

    private void arduino(String[] params)
    {
        if (Integer.parseInt(params[1]) == 1) {
            LoadedUnits.column1.arduino = new Arduino(config.name+File.separator+params[2]);
        }
        else
            LoadedUnits.column2.arduino = new Arduino(config.name+File.separator+params[2]);
    }

    private void tmp(String[] params)
    {
        switch (params[2])
        {
            case "leyboldtmp":  {
                if (Integer.parseInt(params[1]) == 1) {
                    LoadedUnits.column1.tmp = new LeyboldTMP(config.name + File.separator + params[3]);
                }
                else {
                    LoadedUnits.column2.tmp = new LeyboldTMP(config.name + File.separator + params[3]);
                }
            }
            break;

        }
    }

    private void angel(String[] params)
    {
        if (Integer.parseInt(params[1]) == 1)
            LoadedUnits.column1.angel = new GuardianAngel(config.name+File.separator+params[2]);
        else
            LoadedUnits.column2.angel = new GuardianAngel(config.name+File.separator+params[2]);
    }

    private void control(String[] params)
    {
        if (Integer.parseInt(params[1]) == 1)
            LoadedUnits.column1.gateControl = new GateControl(config.name+File.separator+params[2]);
        else
            LoadedUnits.column2.gateControl = new GateControl(config.name+File.separator+params[2]);
    }

    private void auto(String[] params)
    {
        if (Integer.parseInt(params[1]) == 1) {
            LoadedUnits.column1.autoPumping = new AutoPumping(config.name + File.separator + params[2]);
        }
        else
            LoadedUnits.column2.autoPumping = new AutoPumping(config.name+File.separator+params[2]);
    }

    private void server(String[] params)
    {
        LoadedUnits.server = new VacuumServer(config.name+File.separator+params[1]);
    }

    @Override
    protected void measureAndLog() {

    }
}
