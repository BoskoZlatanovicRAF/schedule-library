package importExport;

import implementation.ScheduleImportExport;
import model.Meeting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static importExport.CSVImportExport.readConfig;

public class PDF_Export extends ScheduleImportExport {
    @Override
    public List<Meeting> importData(String filePath, String configPath) throws IOException {
        return null;
    }

    @Override
    public boolean exportData(String filePath, List<Meeting> meetings) throws IOException {
        // export to PDF using Apache PDFBox
        PDDocument document = new PDDocument();

        try {
            // Add a new blank page to the document
            PDPage page = new PDPage();
            document.addPage(page);

            // Prepare content stream to add content to the page
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Write headers
            List<ConfigMapping> columnMappings = readConfig("C:\\Users\\User\\IdeaProjects\\SK1_master\\src\\main\\resources\\config.txt");
            List<String> headerTitles = new ArrayList<>();
            for(ConfigMapping configMapping : columnMappings) {
                headerTitles.add(configMapping.getCustom());
            }
            headerTitles.remove(0); // Removing the unwanted header as before

            // Start the content stream
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
            contentStream.setLeading(14.5f);
            contentStream.newLineAtOffset(25, 750);

            // Printing headers
            for (String title : headerTitles) {
                contentStream.showText(title + " | ");
            }
            contentStream.newLine();

            // Write data for each meeting
            contentStream.setFont(PDType1Font.HELVETICA, 10);

            for (Meeting meeting : meetings) {
                contentStream.showText(meeting.getTimeStart().toString() + ", ");
                contentStream.showText(meeting.getTimeEnd().toString() + ", ");
                contentStream.showText(meeting.getRoom().getName() + ", ");
                contentStream.showText(meeting.getDayOfWeek().toString() + ", ");

                for (String val : meeting.getAdditionalAttributes().values()) {
                    contentStream.showText(val + ", ");
                }
                for (String val : meeting.getRoom().getFeatures().values()) {
                    contentStream.showText(val + ", ");
                }

                /// how to remove last "," from each line



                contentStream.newLine();
            }

            // End the content stream
            contentStream.endText();
            contentStream.close();

            // Save the document to the specified file
            document.save(filePath);

        } finally {
            // Close the document regardless of success or failure
            if (document != null) {
                document.close();
            }
        }

        return true;
    }

}
