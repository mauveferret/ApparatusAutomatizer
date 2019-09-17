package ru.mauveferret;


/*It is very important that bypass must be connected to some specific column number.
In order to minimaze amount of parameters in GateControl lets take as a rule that
bypass line ALWAYS accompany FIRST column number
 */
class Bypass extends ControlDevice {


   boolean isOpened =false;

    Bypass(String fileName) {
        super(fileName);
    }

    @Override
    void measureAndLog() {

    }
}
