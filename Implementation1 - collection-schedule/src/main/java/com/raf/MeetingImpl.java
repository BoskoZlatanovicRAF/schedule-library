package com.raf;

import model.Meeting;
import model.Room;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


public class MeetingImpl extends Meeting {

    public MeetingImpl(LocalDateTime timeStart, LocalDateTime timeEnd, Room room) {
        super(timeStart, timeEnd, room);
    }

}
