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
        loadVacuumUnits();
    }

//just initializes  all vacuum devices


   private void loadVacuumUnits() {
        terminalSample.addDevice(LoadedUnits.server);
        ArrayList<Unit> allUnits = new ArrayList<>();
        for (String g: LoadedUnits.gauge.keySet())
        {
            if (!allUnits.contains(LoadedUnits.gauge.get(g))) allUnits.add(LoadedUnits.gauge.get(g));
        }
        allUnits.addAll(LoadedUnits.column1.getAll());
        allUnits.addAll(LoadedUnits.column2.getAll());
        for (Unit u: allUnits) terminalSample.addDevice(u);
    }

    @Override
    protected void chooseImportCommand(String line) {
        LoadedUnits units = new LoadedUnits();
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


    private void gauge(String[] params)
    {
        switch (params[1])
        {
            case "thyracontgauge":  {
                LoadedUnits.gauge.put(params[2], new ThyracontGauge(config.name+File.separator+params[2]));
            }
            break;
            case "pfeiffergauge": {
            LoadedUnits.gauge.put(params[2], new PfeifferGauge(config.name+File.separator+params[2]));
            }
        }
    }

    private void arduino(String[] params)
    {
        if (Integer.parseInt(params[1]) == 1)
            LoadedUnits.column1.arduino = new Arduino(config.name+File.separator+params[2]);
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
