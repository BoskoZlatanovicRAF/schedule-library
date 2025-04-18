package implementation;

import lombok.Getter;
import lombok.Setter;
import model.Gap;
import model.Meeting;
import model.Room;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class Schedule<T> {
    LocalDate timeValidFrom;
    LocalDate timeValidTo;
    List<LocalDate> exceptions = new ArrayList<>();
    List<Meeting> meetings = new ArrayList<>();
    List<Room> rooms = new ArrayList<>();
    private LocalTime scheduleTimeStart = LocalTime.of(9,0);
    private LocalTime scheduleTImeEnd = LocalTime.of(21,0);



    public T addRoom(Room room){
        if(!rooms.contains(room)){
            rooms.add(room);
        }
        return null;
    }

    public abstract boolean addMeeting(Meeting meeting);

    public List<Meeting> filterMeetingByParameters(HashMap<String,Object> additionalAttributes,String type){

        return this.getMeetings().stream().filter(meeting -> ((Meeting)meeting).hasAdditionalAttribute(additionalAttributes,type))
                .sorted(Comparator.comparing(Meeting::getTimeStart))
                .collect(Collectors.toList());
    }
    public List<Meeting> filterRoomsByAtribute(HashMap<String,Object> meetingParameters, HashMap<String,Object> roomParameters) {
        return (List<Meeting>) this.getMeetings().stream().filter(meeting -> ((Meeting)meeting).hasAdditionalAttribute(meetingParameters,"meeting")).filter(m -> ((Meeting)m).hasAdditionalAttribute(roomParameters,"room"))
                .sorted(Comparator.comparing(Meeting::getTimeStart))
                .collect(Collectors.toList());
    }

    public abstract T innitSchedule();
    public abstract List<Gap> filterMeetingsGapsByTimeSpan(DayOfWeek dayOfWeek, LocalDate dateStart, LocalDate dateEnd, LocalTime timeStart, LocalTime timeEnd);
    public abstract List<Meeting> filterMeetingsByTimeSpan(DayOfWeek dayOfWeek, LocalDate dateStart, LocalDate dateEnd, LocalTime timeStart, LocalTime timeEnd);

    public abstract List<Gap> filterMeetingsGaps(LocalDate timeStart);
    public abstract List<Meeting> filterMeetings(LocalDate timeStart);

    public abstract boolean importSchedule(String fileDest,String type);
    public abstract boolean importSchedule(String configPath, String FilePath, String type);

    public abstract boolean exportSchedule(String filePath, String type);

    public abstract boolean rescheduleMeeting(Meeting meeting, LocalDateTime startTime, LocalDateTime endTime,DayOfWeek dayOfWeek); //premeštanje termina - brisanje i dodavanje novog termina sa istim vezanim podacima
    public abstract boolean rescheduleMeeting(LocalDateTime oldStartTime, LocalDateTime oldEndTime, String roomName, LocalDateTime startTime, LocalDateTime endTime,DayOfWeek dayOfWeek);


    protected List<Gap> findGaps(Map<Room, List<Meeting>> groupedByRoom,LocalTime startTime,LocalTime finalEndTime) {
        List<Gap> gaps = new ArrayList<>();



        for (Room room: groupedByRoom.keySet()){

            LocalTime tempStartTime = startTime;

            List<Meeting> meetings = groupedByRoom.get(room);
            meetings.sort(Comparator.comparing(Meeting::getTimeStart));

            for (Meeting meeting:meetings){

                if (meeting.getTimeStart().toLocalTime().isAfter(tempStartTime) &&meeting.getTimeStart().toLocalTime().isBefore(finalEndTime)){

                    LocalDateTime timeStart = LocalDateTime.of(meeting.getTimeStart().toLocalDate(),tempStartTime);
                    LocalDateTime endTime = LocalDateTime.of(meeting.getTimeEnd().toLocalDate(),meeting.getTimeStart().toLocalTime());
                    Gap gap = new Gap(timeStart, endTime,room);
                    if (gaps.contains(gap)){
                        gaps.get(gaps.indexOf(gap)).getRooms().add(room);
                    }
                    else {
                        gaps.add(gap);
                    }

                }
                else {

                    Meeting nextMeeting = null;
                    int index = meetings.indexOf(meeting);

                    if (index < meetings.size()-1){

                        nextMeeting = meetings.get(index+1);


                        if (nextMeeting.getTimeStart().toLocalTime().isAfter(tempStartTime)){
                            LocalDateTime timeStart = LocalDateTime.of(meeting.getTimeStart().toLocalDate(),tempStartTime);
                            LocalDateTime endTime = meeting.getTimeStart();
                            Gap gap = new Gap(timeStart, endTime,room);

                            if (gaps.contains(gap)){
                                gaps.get(gaps.indexOf(gap)).getRooms().add(room);
                            }
                            else {
                                gaps.add(gap);
                            }
                        }
                    }


                    else {
                        LocalDateTime timeStart = null;
                        if (tempStartTime.equals(startTime)){
                            timeStart = LocalDateTime.of(meeting.getTimeStart().toLocalDate(),tempStartTime);
                        }
                        else {
                            timeStart = meeting.getTimeEnd();
                        }


                        LocalDateTime endTime = null;
                        if (meeting.getTimeEnd().toLocalTime().isAfter(finalEndTime)){
                            tempStartTime = meeting.getTimeEnd().toLocalTime();
                            break;
                        }
                        endTime = LocalDateTime.of(meeting.getTimeEnd().toLocalDate(),finalEndTime);
                        Gap gap = new Gap(timeStart, endTime,room);

                        if (gaps.contains(gap)){
                            gaps.get(gaps.indexOf(gap)).getRooms().add(room);
                        }
                        else {
                            gaps.add(gap);
                        }
                        tempStartTime = endTime.toLocalTime();
                        break;
                    }

                }
                tempStartTime = meeting.getTimeEnd().toLocalTime();
            }

            if (tempStartTime.isBefore(finalEndTime)){

                LocalDateTime timeStart = LocalDateTime.of(meetings.get(0).getTimeStart().toLocalDate(),tempStartTime);
                LocalDateTime endTime = LocalDateTime.of(meetings.get(0).getTimeEnd().toLocalDate(),finalEndTime);
                Gap gap = new Gap(timeStart, endTime,room);

                if (gaps.contains(gap)){
                    gaps.get(gaps.indexOf(gap)).getRooms().add(room);
                }
                else {
                    gaps.add(gap);
                }
            }


        }

        gaps.sort(Comparator.comparing(Gap::getStartTime));
        return gaps;
    }

    private Map<Room, List<Meeting>> groupMeetingsByRoom(List<Meeting> meetings) {
        return meetings.stream()
                .collect(Collectors.groupingBy(Meeting::getRoom));
    }

}
