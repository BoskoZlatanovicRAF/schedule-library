package importExport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import implementation.ScheduleImportExport;
import model.Meeting;
import model.Room;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONImportExport extends ScheduleImportExport {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    @Override
    public List<Meeting> importData(String filePath, String configPath) throws IOException {
        List<Meeting> meetings = new ArrayList<>();
        Gson gson = new Gson();

        // Read and parse config.json
        Reader configReader = new FileReader(configPath);
        Map<String, Map<String, String>> config = gson.fromJson(configReader, new TypeToken<Map<String, Map<String, String>>>(){}.getType());
        configReader.close();
        Map<String, String> mandatoryFields = config.get("mandatoryFields");

        // Read and parse schedule.json
        Reader scheduleReader = new FileReader(filePath);
        List<Map<String, Object>> scheduleList = gson.fromJson(scheduleReader, new TypeToken<List<Map<String, Object>>>(){}.getType());
        scheduleReader.close();

        for (Map<String, Object> scheduleItem : scheduleList) {
            String startTime = (String)scheduleItem.get(mandatoryFields.get("start"));
            String endTime = (String) scheduleItem.get(mandatoryFields.get("end"));
            LocalDateTime timeStart = null;
            LocalDateTime timeEnd = null;
            if ( startTime.length() < 8){
                LocalDate canoncal = LocalDate.of(1000, 1, 1);
                LocalTime localStartDate = LocalTime.parse(startTime);
                timeStart= LocalDateTime.of(canoncal, localStartDate);
                LocalTime localEndTime = LocalTime.parse(endTime);
                timeEnd= LocalDateTime.of(canoncal, localEndTime);

            }
            else {
                timeStart = LocalDateTime.parse((String) scheduleItem.get(mandatoryFields.get("start")), formatter);
                timeEnd = LocalDateTime.parse((String) scheduleItem.get(mandatoryFields.get("end")), formatter);
            }

            Room room = new Room((String) scheduleItem.get(mandatoryFields.get("place")));
            String  str = mandatoryFields.get("DayOfWeek").toString();
            String val = (String) scheduleItem.get(str);
            DayOfWeek dayOfWeek = DayOfWeek.valueOf(val.toUpperCase());
            HashMap<String, String> additionalAttributes = new HashMap<>();
            for (Map.Entry<String, Object> entry : scheduleItem.entrySet()) {
                if (!mandatoryFields.containsValue(entry.getKey())) {
                    additionalAttributes.put(entry.getKey(), entry.getValue().toString());
                }
                if (mandatoryFields.get("room").equals(entry.getKey())) {
                    room.getFeatures().put(entry.getKey(), entry.getValue().toString());
                }
            }

            Meeting meeting =  new Meeting(timeStart, timeEnd, room, additionalAttributes);
            meeting.setDayOfWeek(dayOfWeek);
            meetings.add(meeting);

        }

        return meetings;

    }


    @Override
    public boolean exportData(String filepath, List<Meeting> meetings) throws IOException{

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonArray meetingsArray = new JsonArray();

        for (Meeting meeting : meetings) {
            JsonObject meetingJson = new JsonObject();
            //meetingJson.addProperty("room", meeting.getRoom().getName()); // Assuming there's a method to get the room name
            meetingJson.addProperty("start", meeting.getTimeStart().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            meetingJson.addProperty("end", meeting.getTimeEnd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            meetingJson.addProperty("DayOfWeek", meeting.getDayOfWeek().toString());
            JsonObject additionalAttributes = new JsonObject();

            for (Map.Entry<String, String> entry : meeting.getAdditionalAttributes().entrySet()) {
                additionalAttributes.addProperty(entry.getKey(), entry.getValue());
            }
            meetingJson.add("additional_attributes", additionalAttributes);

            JsonObject roomFeatures = new JsonObject();
            for (Map.Entry<String, String> entry : meeting.getRoom().getFeatures().entrySet()) {
                roomFeatures.addProperty(entry.getKey(), entry.getValue());
            }

            meetingJson.addProperty("room_name", meeting.getRoom().getName());

            // If roomFeatures is already a JsonObject, add it directly:
            meetingJson.add("room_features", roomFeatures);
            meetingsArray.add(meetingJson);
        }
        String json = gson.toJson(meetingsArray);

        try (FileWriter fileWriter = new FileWriter(filepath)) {
            fileWriter.write(json);
        }

        return true;
    }


}
