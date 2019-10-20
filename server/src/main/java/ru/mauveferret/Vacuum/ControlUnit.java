package ru.mauveferret.Vacuum;

import ru.mauveferret.RecordingUnit;

//for units which need access to the another units
public abstract class ControlUnit extends RecordingUnit {


    public ControlUnit(String fileName) {
        super(fileName);
    }



}
