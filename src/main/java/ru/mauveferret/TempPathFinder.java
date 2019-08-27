package ru.mauveferret;

public class TempPathFinder {

    String author;
    String terminal;
    String TMP;
    String GateControl;
    String angel;
    String Gauge;
    String Arduino;

    public TempPathFinder(boolean isWorkSpace) {
        if (isWorkSpace)
            author = "dgbulgadaryan.2133-20115";
        else
            author = "mauve";
        terminal = "C:\\Users\\"+author+"\\Git\\ApparatusAutomatizer\\resources\\terminal";
        TMP = "C:\\Users\\"+author+"\\Git\\ApparatusAutomatizer\\resources\\TMP";
        GateControl = "C:\\Users\\"+author+"\\Git\\ApparatusAutomatizer\\resources\\gate";
        angel = "C:\\Users\\"+author+"\\Git\\ApparatusAutomatizer\\resources\\angel";
        Gauge = "C:\\Users\\"+author+"\\Git\\ApparatusAutomatizer\\resources\\Gauge";
        Arduino = "C:\\Users\\"+author+"\\Git\\ApparatusAutomatizer\\resources\\Arduino";
    }

}
