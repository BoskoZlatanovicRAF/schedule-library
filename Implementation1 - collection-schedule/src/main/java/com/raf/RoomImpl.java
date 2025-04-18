package com.raf;

import model.Room;

public class RoomImpl extends Room {
    public RoomImpl(String name) {
        super(name);
    }


    @Override
    public boolean equals(Object obj) {

        if (obj instanceof Room){
           return this.getName().equals(((Room)obj).getName());
        }
        return false;
    }
}
