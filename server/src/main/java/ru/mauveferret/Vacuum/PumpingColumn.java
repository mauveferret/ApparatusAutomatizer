package ru.mauveferret.Vacuum;

import ru.mauveferret.Arduino;
import ru.mauveferret.Unit;

import java.util.ArrayList;

public class PumpingColumn {

    TMP tmp;
    Arduino arduino;
    GateControl gateControl;
    GuardianAngel angel;
    AutoPumping autoPumping;

    ArrayList<Unit> getAll()
    {
        ArrayList<Unit> all = new ArrayList<>();
        all.add(tmp);
        all.add(arduino);
        all.add(gateControl);
        all.add(angel);
        all.add(autoPumping);
        return all;
    }
}
