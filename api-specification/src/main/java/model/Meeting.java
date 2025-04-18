package model;

import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class Meeting {
    private LocalDateTime timeStart;
    private LocalDateTime timeEnd;
    private Room room;
    private HashMap<String, String> additionalAttributes;

    private DayOfWeek dayOfWeek;

    public Meeting(){
        this.additionalAttributes = new HashMap<>();
    }

    public Meeting(LocalDateTime timeStart, LocalDateTime timeEnd, Room room, HashMap<String, String> additionalAttributes, DayOfWeek dayOfWeek) {
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.room = room;
        this.additionalAttributes = additionalAttributes;
        this.dayOfWeek = dayOfWeek;
    }

    public Meeting(LocalDateTime timeStart, LocalDateTime timeEnd, Room room) {
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.room = room;
        this.additionalAttributes = new HashMap<>();
    }

    public Meeting(LocalDateTime timeStart, LocalDateTime timeEnd, Room room, HashMap<String, String> additionalAttributes){
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.room = room;
        this.additionalAttributes = additionalAttributes;
    }

    @Override
    public String toString() {
        return "Meeting{" +
                "timeStart=" + timeStart +
                ", timeEnd=" + timeEnd +
                ", room=" + room +
                ", additionalAttributes=" + additionalAttributes +
                ", dayOfWeek=" + dayOfWeek +
                '}';
    }

    public boolean overlapsWith(Meeting meeting){
        if (this.equals(meeting) || (meeting.getTimeStart().isBefore(this.getTimeEnd()) &&
                (meeting.getTimeEnd().isAfter(this.getTimeStart()) && !meeting.getTimeEnd().isEqual(this.getTimeStart()) && meeting.getDayOfWeek().equals(this.getDayOfWeek())) && meeting.getRoom().equals(this.getRoom()))) return true;
        return false;
    }

    public boolean equals(Object obj) {

        if (obj instanceof Meeting){
            Meeting meeting = (Meeting)obj;

            return this.getTimeStart().isEqual(meeting.getTimeStart()) && this.getTimeEnd().isEqual(meeting.getTimeEnd()) &&  meeting.getRoom().equals(this.getRoom());
        }
        return false;
    }

    public boolean isOnSameDay(LocalDate date) {
        return this.getTimeStart().toLocalDate().isEqual(date);
    }


    public boolean inDateRange(LocalDate start, LocalDate end){

        return ((this.getTimeStart().toLocalDate().isEqual(start) || this.getTimeStart().toLocalDate().isAfter(start)) && (this.getTimeEnd().toLocalDate().isEqual(end) || this.getTimeEnd().toLocalDate().isAfter(end)));
    }

    public boolean inDateRange(LocalDateTime start, LocalDateTime end){

        return ((this.getTimeStart().isEqual(start) || this.getTimeStart().isAfter(start)) && (this.getTimeEnd().isEqual(end) || this.getTimeEnd().isAfter(end)));
    }


    public boolean hasAdditionalAttribute(HashMap<String,Object> valsToSearch,String type) {

        HashMap<String,String> attributes = null;
        if (type.equals("room")){
            attributes = this.getRoom().getFeatures();
        }
        else {
            attributes = this.getAdditionalAttributes();
        }
        for (Map.Entry<String, Object> entry  : valsToSearch.entrySet()) {
            String key = entry.getKey();
            Object val = entry.getValue();

            if (!attributes.containsKey(key)){
                return false;
            }
            else if (attributes.containsKey(key)){
                if (!attributes.get(key).equals(val)){
                    return false;
                }
            }
        }
        return true;

    }

}
