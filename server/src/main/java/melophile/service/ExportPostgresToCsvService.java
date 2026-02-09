package melophile.service;

import melophile.model.PlaylistOwn;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

//code borrowed from https://stuartsplace.com/information-technology/programming/java/java-and-postgresql-exporting-data

@Service
public class ExportPostgresToCsvService {

    public String DATASOURCE_URL;
    public String DATASOURCE_USERNAME;
    public String DATASOURCE_PASSWORD;

    public ExportPostgresToCsvService(@Value("${spring.datasource.url}") String DATASOURCE_URL,
                                      @Value("${spring.datasource.username}") String DATASOURCE_USERNAME,
                                      @Value("${spring.datasource.password}") String DATASOURCE_PASSWORD) {

        this.DATASOURCE_URL = DATASOURCE_URL;
        this.DATASOURCE_USERNAME = DATASOURCE_USERNAME;
        this.DATASOURCE_PASSWORD = DATASOURCE_PASSWORD;
    }

    public void exportSongsTableToCsv() {

        // File path and name
        File filePath = new File("server/src/main/resources/");
        String fileName = filePath.toString() + "\\Songs_Export.csv";

        // Connection variable
        Connection connect = null;

        try {

            // Connect to database
            connect = DriverManager.getConnection(this.DATASOURCE_URL,
                    this.DATASOURCE_USERNAME,
                    this.DATASOURCE_PASSWORD);

        } catch (SQLException e) {

            // Message confirming unsuccessful database connection.
            System.out.println("Database connection unsuccessful.");

            // Stop program execution
            return;

        }

        // Check to see if the file path exists
        if (filePath.isDirectory()) {

            // SQL to select data from the person table.
            String sqlSelect = "SELECT name, artist, external_url, acousticness, danceability, energy, " +
                    "instrumentalness, \"key\", liveness, loudness, \"mode\", popularity, speechiness, " +
                    "tempo, time_signature, valence FROM song ORDER BY id";

            try {

                // Execute query
                Statement statement = connect.createStatement();
                ResultSet results = statement.executeQuery(sqlSelect);

                // Open CSV file
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName));

                // Add table headers to CSV file
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                        .withHeader(results.getMetaData()).withQuoteMode(QuoteMode.ALL));

                // Add data rows to CSV file
                while (results.next()) {

                    csvPrinter.printRecord(
                            results.getString(1),
                            results.getString(2),
                            results.getString(3),
                            results.getFloat(4),
                            results.getFloat(5),
                            results.getFloat(6),
                            results.getFloat(7),
                            results.getInt(8),
                            results.getFloat(9),
                            results.getFloat(10),
                            results.getInt(11),
                            results.getInt(12),
                            results.getFloat(13),
                            results.getFloat(14),
                            results.getInt(15),
                            results.getFloat(16));

                }

                // Close CSV file
                csvPrinter.flush();
                csvPrinter.close();

                // Message stating export successful
                System.out.println("Data export successful. Created file \"Songs_Export.csv\".");

            } catch (SQLException e) {

                // Message stating export unsuccessful
                System.out.println("Data export unsuccessful. Error: " + e.getMessage());

            } catch (IOException e) {

                // Message stating export unsuccessful.
                System.out.println("Data export unsuccessful. Error: " + e.getMessage());

            }

        } else {

            // Display a message stating file path does not exist and exit.
            System.out.println("File path does not exist.");

        }
    }

    public void exportPlaylistToCsv(PlaylistOwn playlistOwn) {

        // File path and name
        File filePath = new File("server/src/main/resources/");
        int id = playlistOwn.getId();
        String fileName = filePath.toString() + "\\Playlist_" + id + "_Export.csv";

        // Connection variable
        Connection connect = null;

        try {

            // Connect to database
            connect = DriverManager.getConnection(this.DATASOURCE_URL,
                    this.DATASOURCE_USERNAME,
                    this.DATASOURCE_PASSWORD);

        } catch (SQLException e) {

            // Message confirming unsuccessful database connection.
            System.out.println("Database connection unsuccessful.");

            // Stop program execution
            return;

        }

        // Check to see if the file path exists
        if (filePath.isDirectory()) {

            // SQL to select data from the person table.
            String sqlSelect = "select name, artist, external_url, acousticness, danceability, energy, " +
                    "instrumentalness, \"key\", liveness, loudness, \"mode\", popularity, speechiness, " +
                    "tempo, time_signature, valence\n" +
                    "from songsinplaylist pl\n" +
                    "inner join song s\n" +
                    "on pl.song_id = s.id\n" +
                    "where pl.playlist_id=" + id + ";";

            try {

                // Execute query
                Statement statement = connect.createStatement();
                ResultSet results = statement.executeQuery(sqlSelect);

                // Open CSV file
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName));

                // Add table headers to CSV file
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                        .withHeader(results.getMetaData()).withQuoteMode(QuoteMode.ALL));

                // Add data rows to CSV file
                while (results.next()) {

                    csvPrinter.printRecord(
                            results.getString(1),
                            results.getString(2),
                            results.getString(3),
                            results.getFloat(4),
                            results.getFloat(5),
                            results.getFloat(6),
                            results.getFloat(7),
                            results.getInt(8),
                            results.getFloat(9),
                            results.getFloat(10),
                            results.getInt(11),
                            results.getInt(12),
                            results.getFloat(13),
                            results.getFloat(14),
                            results.getInt(15),
                            results.getFloat(16));

                }

                // Close CSV file
                csvPrinter.flush();
                csvPrinter.close();

                // Message stating export successful
                System.out.println("Data export successful. Created file \"Playlist_" + id + "_Export.csv\".");

            } catch (SQLException e) {

                // Message stating export unsuccessful
                System.out.println("Data export unsuccessful. Error: " + e.getMessage());

            } catch (IOException e) {

                // Message stating export unsuccessful.
                System.out.println("Data export unsuccessful. Error: " + e.getMessage());

            }

        } else {

            // Display a message stating file path does not exist and exit.
            System.out.println("File path does not exist.");

        }
    }
}
