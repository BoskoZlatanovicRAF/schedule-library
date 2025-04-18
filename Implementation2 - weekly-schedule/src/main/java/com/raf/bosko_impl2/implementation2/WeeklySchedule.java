package com.raf.bosko_impl2.implementation2;

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

public class WeeklySchedule extends Schedule<WeeklySchedule> {
    static {
        Manager.setObj(getInstance());
    }

    private List<LocalDate> exceptions = new ArrayList<>();
    // Singleton pattern lazy synchronized
    private static class Loader {
        static final WeeklySchedule INSTANCE = new WeeklySchedule();
    }


    private WeeklySchedule() {

    }

    @Override
    public void setExceptions(List<LocalDate> exceptions) {
        this.exceptions = exceptions;
    }

    public static WeeklySchedule getInstance() {
        return Loader.INSTANCE;
    }

    public static WeeklySchedule getInstance(LocalDate start, LocalDate end) {

        WeeklySchedule weeklySchedule = getInstance();
        weeklySchedule.setTimeValidFrom(start);
        weeklySchedule.setTimeValidTo(end);
        return weeklySchedule;
    }

    public void setMeetings(List<Meeting> meetings) {

        for(Meeting m: meetings){

            this.addMeeting(m);
        }
        validateMeetings();
    }

    @Override
    public List<Gap> filterMeetingsGapsByTimeSpan(DayOfWeek dayOfWeek, LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {


        List<Gap> gaps = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)){

            if (exceptions.contains(date)){
                continue;
            }
            LocalDate finalDate = date;
            List<Meeting> tempList = this.getMeetings().stream().filter(meeting -> ((Meeting) meeting).getTimeStart().toLocalDate().
                            equals(meeting.getTimeEnd().toLocalDate()) && meeting.getTimeStart().getDayOfWeek().equals(dayOfWeek) &&
                            meeting.getTimeStart().toLocalDate().equals(finalDate)).
                    sorted(Comparator.comparing(Meeting::getTimeStart)).
                    collect(Collectors.toList());


            if (tempList.isEmpty()){
                continue;
            }
            Map<Room, List<Meeting>> groupedMeetings = groupMeetingsByRoom(tempList);

            List<Gap> gapsToBeAdded = super.findGaps(groupedMeetings,startTime,endTime);
            gaps.addAll(gapsToBeAdded);
        }

        List<Meeting> scheduledMeetings = this.getMeetings().stream().filter(meeting -> ((Meeting) meeting).getDayOfWeek().equals(dayOfWeek) &&
                !meeting.getTimeStart().toLocalDate().isEqual(meeting.getTimeEnd().toLocalDate())).
                sorted(Comparator.comparing(Meeting::getTimeStart)).
                collect(Collectors.toList());


        Map<Room, List<Meeting>> groupedMeetings = groupMeetingsByRoom(scheduledMeetings);
        List<Gap> newGaps = findGaps(groupedMeetings,startTime,endTime);

        gaps.addAll(newGaps);


        return gaps;
    }

    @Override
    public List<Meeting> filterMeetingsByTimeSpan(DayOfWeek dayOfWeek, LocalDate startDate, LocalDate endDate, LocalTime startTime, LocalTime endTime) {


        List<Meeting> weeklyMeetings = this.getMeetings().stream().filter(meeting -> ((Meeting) meeting).getDayOfWeek().
                        equals(dayOfWeek) && !meeting.getTimeStart().toLocalDate().isEqual(meeting.getTimeEnd().toLocalDate())).
                sorted(Comparator.comparing(Meeting::getTimeStart)).
                collect(Collectors.toList());


        List<Meeting> oneTimeMeetings = this.getMeetings().stream().filter(meeting -> meeting.inDateRange(startDate,endDate)
                && (meeting.getTimeStart().toLocalTime().isAfter(startTime) || meeting.getTimeStart().toLocalTime().equals(startTime)) &&
                (meeting.getTimeEnd().toLocalTime().isAfter(endTime) || meeting.getTimeEnd().toLocalTime().equals(endTime))
                && meeting.getDayOfWeek().equals(dayOfWeek)).collect(Collectors.toList());


        weeklyMeetings.addAll(oneTimeMeetings);

        return  weeklyMeetings;

    }

    @Override
    public List<Gap> filterMeetingsGaps(LocalDate localDate) {
        List<Gap> gaps = new ArrayList<>();
        DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        if (exceptions.contains(localDate)){
            return null;
        }

        List<Meeting> tempList = this.getMeetings().stream().filter(meeting -> ((Meeting) meeting).getTimeStart().toLocalDate().
                        equals(meeting.getTimeEnd().toLocalDate()) && meeting.getTimeStart().getDayOfWeek().equals(dayOfWeek) &&
                        meeting.getTimeStart().toLocalDate().equals(localDate)).
                sorted(Comparator.comparing(Meeting::getTimeStart)).
                collect(Collectors.toList());

        Map<Room, List<Meeting>> groupedMeetings = groupMeetingsByRoom(tempList);

        List<Gap> gapsToBeAdded = super.findGaps(groupedMeetings,this.getScheduleTimeStart(),this.getScheduleTImeEnd());
        gaps.addAll(gapsToBeAdded);


        List<Meeting> scheduledMeetings = this.getMeetings().stream().filter(meeting -> ((Meeting) meeting).getDayOfWeek().equals(dayOfWeek) &&
                        !meeting.getTimeStart().toLocalDate().isEqual(meeting.getTimeEnd().toLocalDate())).
                sorted(Comparator.comparing(Meeting::getTimeStart)).
                collect(Collectors.toList());


        Map<Room, List<Meeting>> groupedMeetings2 = groupMeetingsByRoom(scheduledMeetings);
        List<Gap> newGaps = findGaps(groupedMeetings2,this.getScheduleTimeStart(),this.getScheduleTImeEnd());

        gaps.addAll(newGaps);


        return gaps;

    }

    @Override
    public List<Meeting> filterMeetings(LocalDate localDate) {
        DayOfWeek dayOfWeek = localDate.getDayOfWeek();
        List<Meeting> weeklyMeetings = this.getMeetings().stream().filter(meeting -> ((Meeting) meeting).getDayOfWeek().
                        equals(dayOfWeek) && !meeting.getTimeStart().toLocalDate().isEqual(meeting.getTimeEnd().toLocalDate())).
                sorted(Comparator.comparing(Meeting::getTimeStart)).
                collect(Collectors.toList());

        List<Meeting> oneTimeMeetings = this.getMeetings().stream().filter(meeting -> meeting.isOnSameDay(localDate)
                && meeting.getDayOfWeek().equals(dayOfWeek)).collect(Collectors.toList());

        weeklyMeetings.addAll(oneTimeMeetings);
        return weeklyMeetings;

    }

    @Override
    public boolean importSchedule(String fileDest,String type) {
        if (type.equals("csv")){
            CSVImportExport csvImportExport = new CSVImportExport();
            try {
                List <Meeting> meetingList = csvImportExport.importData(fileDest, "src\\main\\resources\\config.txt");

                this.setMeetings(meetingList);

                return true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {

            JSONImportExport jsonImportExport = new JSONImportExport();
            
            try {
                List <Meeting> meetingList = jsonImportExport.importData(fileDest,"C:\\Users\\User\\IdeaProjects\\Implementacija2_SK1_master\\src\\main\\resources\\config.json");
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
    public boolean addMeeting(Meeting meeting) {

        if (this.getMeetings().size() == 0){
            this.getMeetings().add(meeting);
            if (!getRooms().contains(meeting.getRoom())){
                getRooms().add(meeting.getRoom());
            }
            return true;
        }

        Stream<Meeting> s = this.getMeetings().stream();

        if (s.anyMatch(existingMeeteng -> existingMeeteng.overlapsWith(meeting))){
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

    @Override
    public WeeklySchedule innitSchedule() {
        return getInstance();
    }


    @Override
    public boolean rescheduleMeeting(Meeting meeting, LocalDateTime startTime, LocalDateTime endTime,DayOfWeek dayOfWeek) {


        Meeting newMeeting = new Meeting(startTime,endTime,meeting.getRoom());
        newMeeting.setDayOfWeek(dayOfWeek);
        newMeeting.setAdditionalAttributes(meeting.getAdditionalAttributes());

        if (this.addMeeting(newMeeting)){

            this.getMeetings().remove(meeting);
            return true;
        }

        return false;

    }

    @Override
    public boolean rescheduleMeeting(LocalDateTime oldTimeStart, LocalDateTime oldTimeEnd, String room, LocalDateTime timeStart, LocalDateTime timeEnd,DayOfWeek dayOfWeek) {

        List<Meeting> m = getMeetings().stream().filter(meeting -> meeting.getTimeStart().equals(oldTimeStart) &&
                        meeting.getTimeEnd().equals(oldTimeEnd) && meeting.getRoom().getName().equals(room) && meeting.getDayOfWeek().equals(dayOfWeek))
                .collect(Collectors.toList());

        if (m.size() ==0){
            return false;
        }
        Meeting meeting = m.get(0);

        return rescheduleMeeting(meeting,timeStart,timeEnd,dayOfWeek);

    }

    private void validateMeetings() {
        for (Meeting meeting : WeeklySchedule.getInstance().getMeetings()) {
            if (meeting.getTimeStart().toLocalDate().isEqual(LocalDate.of(1000, 1, 1))) {
                meeting.setTimeStart(LocalDateTime.of(getTimeValidFrom(), meeting.getTimeStart().toLocalTime()));
                meeting.setTimeEnd(LocalDateTime.of(getTimeValidTo(), meeting.getTimeEnd().toLocalTime()));
            }
        }
    }

    private Map<Room, List<Meeting>> groupMeetingsByRoom(List<Meeting> meetings) {
        return meetings.stream()
                .collect(Collectors.groupingBy(Meeting::getRoom));
    }
}
