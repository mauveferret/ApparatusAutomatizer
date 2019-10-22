package ru.mauveferret.Vacuum;

import ru.mauveferret.Terminal;
import ru.mauveferret.Vacuum.*;

public class PumpingColumn  {


    int kurva;

    Gauge gauge;
    TMP tmp;
    AutoPumping autoPumping;
    GuardianAngel angel;
    GateControl gateControl;
    Terminal terminal;

    String gaugeName;
     String tmpName ;
     String gateControlName;
  String autoName;
   String angelName;

    void setUnits()
    {
        try {  gauge= (Gauge) (terminal.getDevice(gaugeName));} catch (Exception ignored) {}
        try {  tmp = (TMP) (terminal.getDevice(tmpName));} catch (Exception ignored) {}
        try {  gateControl = (GateControl) (terminal.getDevice(gateControlName));} catch (Exception ignored) {}
        try {  angel = (GuardianAngel) (terminal.getDevice(angelName));} catch (Exception ignored) {}
        try {  autoPumping = (AutoPumping) (terminal.getDevice(autoName));} catch (Exception ignored) {}
    }
}
