package implementation;

import model.Meeting;

import java.io.IOException;
import java.util.List;

public abstract class ScheduleImportExport {

    public abstract List<Meeting> importData(String filePath, String configPath) throws IOException;

    public abstract boolean exportData(String filePath, List<Meeting> meetings) throws IOException;

}
