package com.raf.bosko_tihi.test_app;

import implementation.Schedule;
import importExport.CSVImportExport;
import importExport.ConfigMapping;
import importExport.JSONImportExport;
import importExport.PDF_Export;
import model.Manager;
import model.Meeting;
import model.Room;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;


public class Main {

    public static void main(String[] args) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Schedule schedule;
        Scanner scanner = new Scanner(System.in);
        String implAnswer;

        System.out.println();
        System.out.println("Na koji nacin zelite da cuvate fajl?");
        System.out.println("1. Raspored se čuva kao kolekcija konkretnih termina u vremenu i prostoru");
        System.out.println("2. Raspored se čuva na nedeljnom nivou za zadati period");
        implAnswer = scanner.nextLine();
        if (implAnswer.equals("1")) {
            try {
                Class.forName("com.raf.SchedulingService");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else if (implAnswer.equals("2")) {
            try {
                Class.forName("com.raf.bosko_impl2.implementation2.WeeklySchedule");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("Pogresan unos");
            return;
        }
        schedule = Manager.getSpecRasporedImpl();

        System.out.println("Unesite datum pocetka rasporeda (Format | yyyy-MM-dd):");
        String datum = scanner.nextLine();
        String[] split = datum.split("-");
        int year = Integer.parseInt(split[0]);
        int month = Integer.parseInt(split[1]);
        int day = Integer.parseInt(split[2]);
        schedule.setTimeValidFrom(LocalDate.of(year, month, day));
        System.out.println("Unesite datum kraja rasporeda (Format | yyyy-MM-dd):");
        datum = scanner.nextLine();
        split = datum.split("-");
        year = Integer.parseInt(split[0]);
        month = Integer.parseInt(split[1]);
        day = Integer.parseInt(split[2]);
        schedule.setTimeValidTo(LocalDate.of(year, month, day));

        System.out.println("Unesite radno vreme(npr 9-21)");
        String vreme = scanner.nextLine();
        split = vreme.split("-");
        schedule.setScheduleTimeStart(LocalTime.of(Integer.parseInt(split[0]), 0));
        schedule.setScheduleTImeEnd(LocalTime.of(Integer.parseInt(split[1]), 0));

        System.out.println("Unesite ime fajla za ucitavanje raspored (csv,json)");
        String filePath = scanner.nextLine();
        System.out.println("Unesite tip fajla (csv,json)");
        String type = scanner.nextLine();
        schedule.importSchedule(filePath, type);


        while (true) {
            System.out.println("\n-------------------------------------------------\n");
            System.out.println("Unesite redni broj datih opcija");
            System.out.println("\n-------------------------------------------------\n");
            System.out.println("1. Add meeting");
            System.out.println("2. Remove meeting");
            System.out.println("3. Filtriraj po tacnoj vrednosti");
            System.out.println("4. Filtriraj po tacnom datumu");
            System.out.println("5. Filtriraj po datumu u nekom periodu, vremenu u periodu");
            System.out.println("6. Filtriraj slobodne termine za prostoriju");
            System.out.println("7. Reschedule - Premesti termin");
            System.out.println("8. Stampaj raspored");
            System.out.println("9. Export");
            System.out.println("0. Exit");
            String opcija = scanner.nextLine();

            List<Meeting> meetingList = new ArrayList<>();
            switch (opcija){
                case ("1"):{
                    Meeting meeting = new Meeting();
                    int k =((Meeting)schedule.getMeetings().get(0)).getAdditionalAttributes().size();
                    System.out.println("Unesite redom headere/osobine");
                    String atribut;
                    for(int i = 0; i<k+4; i++){
                        if(i == 0){
                            System.out.println("Unesi ime sobe");
                            atribut = scanner.nextLine();

                            String finalAtribut = atribut;
                            List<Room> rooms = (List<Room>) schedule.getRooms().stream().filter(r -> ((Room)r).getName().equals(finalAtribut)).collect(Collectors.toList());
                            if(rooms.isEmpty()){
                                schedule.addRoom(new Room(atribut));
                                meeting.setRoom(new Room(atribut));
                            }
                            else {
                                meeting.setRoom(rooms.get(0));
                            }
                        }

                        if(i == 1){
                            System.out.println("Unesi pocetak termina u formatu yyyy-MM-dd HH:mm");
                            atribut = scanner.nextLine();
                            split = atribut.split(" ");
                            year = Integer.parseInt(split[0].split("-")[0]);
                            month = Integer.parseInt(split[0].split("-")[1]);
                            day = Integer.parseInt(split[0].split("-")[2]);
                            int hour = Integer.parseInt(split[1].split(":")[0]);
                            int minute = Integer.parseInt(split[1].split(":")[1]);
                            meeting.setTimeStart(LocalDateTime.of(year, month, day, hour, minute));
                        }
                        if(i == 2){
                            System.out.println("Unesi kraj termina u formatu yyyy-MM-dd HH:mm");
                            atribut = scanner.nextLine();
                            split = atribut.split(" ");
                            year = Integer.parseInt(split[0].split("-")[0]);
                            month = Integer.parseInt(split[0].split("-")[1]);
                            day = Integer.parseInt(split[0].split("-")[2]);
                            int hour = Integer.parseInt(split[1].split(":")[0]);
                            int minute = Integer.parseInt(split[1].split(":")[1]);
                            meeting.setTimeEnd(LocalDateTime.of(year, month, day, hour, minute));
                        }
                        if(i == 3){
                            System.out.println("Unesi dan u nedelji NA ENGLESKOM");
                            atribut = scanner.nextLine();
                            meeting.setDayOfWeek(DayOfWeek.valueOf(atribut.toUpperCase()));
                        }
                        if(i >= 4){
                            System.out.println("Unesi vrednost za osobinu " + ((Meeting)schedule.getMeetings().get(0)).getAdditionalAttributes().keySet().toArray()[i-4]);
                            atribut = scanner.nextLine();
                            meeting.getAdditionalAttributes().put(((Meeting)schedule.getMeetings().get(0)).getAdditionalAttributes().keySet().toArray()[i-4].toString(), atribut);
                        }
                    }

                    schedule.addMeeting(meeting);

                    printMeetings(schedule.getMeetings());

                    break;
                }
                case ("2"):{
                    //TODO dodati remove meeting
                    printMeetings(schedule.getMeetings());
                    break;
                }
                case ("3"):{
                    System.out.println("Unesite ime osobine");
                    String imeOsobine = scanner.nextLine();
                    System.out.println("Unesite vrednost osobine");
                    String vrednostOsobine = scanner.nextLine();
                    for(Meeting m: (List<Meeting>)schedule.getMeetings())
                        if(m.getAdditionalAttributes().get(imeOsobine).equals(vrednostOsobine))
                            System.out.println(m.toString());
                    break;
                }
                case ("4"):{
                    System.out.println("Unesite tacan datum formatu yyyy-MM-dd");
                    String datum1 = scanner.nextLine();
                    meetingList = schedule.filterMeetings(LocalDate.parse(datum1, formatter));
                    printMeetings(meetingList);
                    break;
                }
                case ("5"):{
                    System.out.println("Unesite pocetni datum formatu yyyy-MM-dd");
                    String datum1 = scanner.nextLine();
                    System.out.println("Unesite krajnji datum formatu yyyy-MM-dd");
                    String datum2 = scanner.nextLine();
                    System.out.println("Unesite pocetno vreme formatu HH:mm");
                    String vreme1 = scanner.nextLine();
                    System.out.println("Unesite krajnje vreme formatu HH:mm");
                    String vreme2 = scanner.nextLine();
                    System.out.println("Unesite dan u nedelji NA ENGLESKOM");
                    String danUNedelji = scanner.nextLine();
                    meetingList = schedule.filterMeetingsByTimeSpan(DayOfWeek.valueOf(danUNedelji.toUpperCase()), LocalDate.parse(datum1, formatter), LocalDate.parse(datum2, formatter), LocalTime.parse(vreme1), LocalTime.parse(vreme2));
//                    meetingList = schedule.filterMeetingsByTimeSpan(, LocalDate.parse(datum1, formatter), LocalDate.parse(datum2, formatter), LocalTime.parse(vreme1), LocalTime.parse(vreme2));
                    printMeetings(meetingList);
                    break;
                }
                case ("6"):{


                    break;
                }
                case ("7"):{
                    for(Meeting m: (List<Meeting>)schedule.getMeetings())
                        System.out.println(m);
                    break;
                }
                case ("8"):{
                    printMeetings(schedule.getMeetings());
                    break;
                }

                case ("9"): {
                    System.out.println("Unesite ime fajla za export (primer: export.csv)");
                    String imeFajla = scanner.nextLine();
                    System.out.println("Unesite tip fajla (csv,json,pdf)");
                    String tipFajla = scanner.nextLine();
                    try {
                        if (tipFajla.equals("pdf")) {
                            PDF_Export pdf_export = new PDF_Export();
                            pdf_export.exportData(imeFajla, schedule.getMeetings());
                        }
                        else if (tipFajla.equals("csv")) {
                            CSVImportExport csvImportExport = new CSVImportExport();
                            csvImportExport.exportData(imeFajla, schedule.getMeetings());
                        }
                        else if (tipFajla.equals("json")) {
                            JSONImportExport jsonImportExport = new JSONImportExport();
                            jsonImportExport.exportData(imeFajla, schedule.getMeetings());
                        }
                        else {
                        System.out.println("Pogresan unos");
                        }
                    } catch (IOException e){
                        e.printStackTrace();
                        break;
                        }

                }
                case ("0"):{
                    scanner.close();
                    return;
                } default:{
                    System.out.println("Nije dobro uneto, unesite samo redni broj");
                    break;
                }
            }
        }
    }

    private static void printMeetings(List<Meeting> meetings) {
        for(Meeting m: meetings)
            System.out.println(m.toString());
    }
}
