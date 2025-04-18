package com.raf;

import implementation.Schedule;
import importExport.CSVImportExport;
import importExport.JSONImportExport;
import importExport.PDF_Export;
import lombok.Getter;
import lombok.SneakyThrows;
import model.Gap;
import model.Manager;
import model.Meeting;
import model.Room;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;



@Getter
public class SchedulingService extends Schedule {

    static {
        Manager.setObj(new SchedulingService());
    }

    public SchedulingService() {

    }

    @Override
    public SchedulingService innitSchedule() {
        return this;
    }



    @Override
    public boolean addMeeting(Meeting meeting) {

        if (this.getMeetings().size() == 0){
            this.getMeetings().add(meeting);

            if (!getRooms().contains(meeting.getRoom())){
                getRooms().add(meeting.getRoom());
            }
            return true;
        }

        Stream<Meeting> s = this.getMeetings().stream();

        if (s.anyMatch(existingMeeteng -> existingMeeteng.overlapsWith(meeting) && existingMeeteng.getDayOfWeek().equals(meeting.getDayOfWeek()))){
            return false;
        }
        else {
            this.getMeetings().add(meeting);
        }

        if (!getRooms().contains(meeting.getRoom())){
            getRooms().add(meeting.getRoom());
        }
        return true;
    }

    public boolean removeMeeting(Meeting meeting) {
        if (this.getMeetings().contains(meeting)){
            this.getMeetings().remove(meeting);
            return true;
        }
        return false;
    }

    @Override
    public List<Gap> filterMeetingsGapsByTimeSpan(DayOfWeek dayOfWeek, LocalDate startDay, LocalDate endDay, LocalTime startTime, LocalTime endTime) {

        List<Gap> gaps = new ArrayList<>();
        for (LocalDate day = startDay; day.isBefore(endDay) || day.isEqual(endDay); day = day.plusDays(1)){

            if (dayOfWeek.equals(day.getDayOfWeek())){
                LocalDate finalDay = day;
                List<Meeting> meetings = (List<Meeting>) this.getMeetings().stream().filter(meeting -> ((Meeting)meeting).isOnSameDay(finalDay)).collect(Collectors.toList());
                if (meetings.size() == 0){
                    Gap allDayGap = new Gap(LocalDateTime.of(day,startTime),LocalDateTime.of(day,endTime),new HashSet<>(getRooms()));
                    gaps.add(allDayGap);
                    continue;
                }
                Map<Room,List<Meeting>> groupByRoom = groupMeetingsByRoom(meetings);
                List<Gap> gapsFound = findGaps(groupByRoom,startTime,startTime);
                gaps.addAll(gapsFound);
            }
        }

        return gaps;

    }

    @Override
    public List<Meeting> filterMeetingsByTimeSpan(DayOfWeek dayOfWeek, LocalDate startDay, LocalDate endDay, LocalTime startTime, LocalTime endTime) {
        List<Meeting> returnMeetings = new ArrayList<>();
        for (LocalDate day = startDay; day.isBefore(endDay) || day.isEqual(endDay); day = day.plusDays(1)){

            if (dayOfWeek.equals(day.getDayOfWeek())){
                LocalDate finalDay = day;
                List<Meeting> meetings = (List<Meeting>) this.getMeetings().stream().filter(meeting -> ((Meeting)meeting).isOnSameDay(finalDay)).collect(Collectors.toList());
                if (meetings.size()>0){
                    returnMeetings.addAll(meetings);
                }
            }
        }

        return returnMeetings;
    }


    @Override
    public List<Gap> filterMeetingsGaps(LocalDate localDateTime) {
        List<Meeting> meetings = filterMeetings(localDateTime);
        Map<Room,List<Meeting>> groupedMeetings = groupMeetingsByRoom(meetings);

        return findGaps(groupedMeetings,this.getScheduleTimeStart(),getScheduleTImeEnd());
    }


   @Override
    public List<Meeting> filterMeetings(LocalDate localDateTime) {
        return  (List<Meeting>) getMeetings().stream()
                .filter(meeting -> ((Meeting)meeting).isOnSameDay(localDateTime))
                .sorted(Comparator.comparing(Meeting::getTimeStart))
                .collect(Collectors.toList());
    }

    public boolean importSchedule(String fileDest,String type) {
        if (type.equals("csv")){
            CSVImportExport csvImportExport = new CSVImportExport();
            try {
                List <Meeting> meetingList = csvImportExport.importData(fileDest, "src/main/resources/config.txt");

                this.setMeetings(meetingList);

                return true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {

            JSONImportExport jsonImportExport = new JSONImportExport();
            try {
                List <Meeting> meetingList = jsonImportExport.importData(fileDest,"src/main/resources/config.json");
                this.setMeetings(meetingList);

                return true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

    }

    @Override
    public boolean importSchedule(String configPath, String FilePath, String type) {
        if (type.equals("csv")){
            CSVImportExport csvImportExport = new CSVImportExport();
            try {
                List <Meeting> meetingList = csvImportExport.importData(FilePath, configPath);

                this.setMeetings(meetingList);

                return true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {

            JSONImportExport jsonImportExport = new JSONImportExport();
            try {
                List <Meeting> meetingList = jsonImportExport.importData(FilePath,configPath);
                this.setMeetings(meetingList);

                return true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    @SneakyThrows
    @Override
    public boolean exportSchedule(String dest, String type) {
        if(type.equals("pdf")){
            PDF_Export pdfExport = new PDF_Export();
            pdfExport.exportData(dest,this.getMeetings());
            return true;
        }
        else {
            JSONImportExport jsonImportExport = new JSONImportExport();
            jsonImportExport.exportData(dest,this.getMeetings());
            return true;
        }
    }

    @Override
    public boolean rescheduleMeeting(Meeting meeting, LocalDateTime timeStart, LocalDateTime timeEnd,DayOfWeek dayOfWeek) {

        Meeting newMeeting = new Meeting(timeStart,timeEnd,meeting.getRoom());
        newMeeting.setDayOfWeek(timeStart.getDayOfWeek());
        newMeeting.setAdditionalAttributes(meeting.getAdditionalAttributes());

        if (this.addMeeting(newMeeting)){

            this.getMeetings().remove(meeting);
            return true;
        }

        return false;
    }

    @Override
    public boolean rescheduleMeeting(LocalDateTime oldTimeStart, LocalDateTime oldTimeEnd, String room, LocalDateTime timeStart, LocalDateTime timeEnd,DayOfWeek dayOfWeek) {
        List<Meeting> m = (List<Meeting>) getMeetings().stream().filter(meeting -> ((Meeting)meeting).getTimeStart().equals(oldTimeStart) &&
                        ((Meeting)meeting).getTimeEnd().equals(oldTimeEnd) && ((Meeting)meeting).getRoom().getName().equals(room))
                .collect(Collectors.toList());

        if (m.size() ==0){
            return false;
        }
        Meeting meeting = m.get(0);

        return rescheduleMeeting(meeting,timeStart,timeEnd,dayOfWeek);
    }

    private Map<Room, List<Meeting>> groupMeetingsByRoom(List<Meeting> meetings) {
        return meetings.stream()
                .collect(Collectors.groupingBy(Meeting::getRoom));
    }

}