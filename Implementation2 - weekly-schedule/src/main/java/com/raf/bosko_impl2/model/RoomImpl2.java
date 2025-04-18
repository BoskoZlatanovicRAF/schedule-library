package com.raf.bosko_impl2.model;

import model.Room;

import java.util.HashMap;

public class RoomImpl2 extends Room {

    public RoomImpl2() {
        super();
    }

    public RoomImpl2(String name) {
        super(name);
    }

    public RoomImpl2(String name, HashMap<String, String> features) {
        super(name, features);
    }
}
