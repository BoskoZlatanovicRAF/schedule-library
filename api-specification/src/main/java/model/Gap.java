package model;

import lombok.Getter;
import lombok.Setter;
import model.Room;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class Gap {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Set<Room> rooms = new HashSet<>();
    public Gap(LocalDateTime startTime, LocalDateTime endTime, Room room) {
        this.startTime = startTime;
        this.endTime = endTime;
        rooms.add(room);
    }

    public Gap(LocalDateTime startTime, LocalDateTime endTime, Set<Room> rooms) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.rooms = rooms;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof Gap){

            Gap gap = (Gap) obj;

            return startTime.isEqual(gap.startTime) && endTime.isEqual(gap.endTime);
        }
        else return false;
    }

    @Override
    public String toString() {
        return "Gap{" +
                "startTime=" + startTime +
                ", endTime=" + endTime +
                ", rooms=" + rooms +
                '}';
    }
}
