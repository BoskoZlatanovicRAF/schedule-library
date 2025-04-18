package model;

import implementation.Schedule;

public class Manager {
    private static Schedule obj;

    public static Schedule getSpecRasporedImpl(){
        return obj;
    }

    public static void setObj(Schedule obj) {
        Manager.obj = obj;
    }
}
