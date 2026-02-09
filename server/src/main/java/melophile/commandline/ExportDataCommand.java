package melophile.commandline;

import melophile.controller.CommandLineController;
import melophile.model.PlaylistOwn;
import melophile.service.ExportPostgresToCsvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Scanner;

@Service
public class ExportDataCommand {

    @Autowired
    DisplayCommand displayCommand;
    @Autowired
    CommandLineController commandLineController;
    @Autowired
    ExportPostgresToCsvService exportPostgresToCsvService;

    private static Scanner sc = new Scanner(System.in);
    private static String[] options = {"Export All Songs to CSV File", "Export a Playlist to CSV File", "Return to Main Menu"};

    public void exportData() {

        int option = 1;

        while (option != options.length) {

            System.out.println("\n< EXPORT DATA >");

            displayCommand.displayOptions(options);

            try {
                option = Integer.parseInt(sc.next());

                switch (option) {
                    case 1: {
                        exportSongsToCsv();
                        break;
                    }
                    case 2: {
                        exportPlaylistToCsv();
                        break;
                    }
                    case 3: {
                        break;
                    }
                    default: {
                        System.out.println("Invalid option - Please enter an integer value between 1 and " + options.length);
                        break;
                    }
                }
            } catch (NumberFormatException exception) {
                System.out.println("Invalid input - Please enter an integer value between 1 and " + options.length);
            } catch (Exception exception) {
                System.out.println(exception);
            }
        }
    }

    private void exportPlaylistToCsv() {

        String input = "";
        while (true) {

            try {

                System.out.println("\n< EXPORT A PLAYLIST TO CSV FILE >");
                System.out.println("Please choose a playlist to export, by entering the corresponding id...");
                List<Integer> playlistIds = displayCommand.displayPlaylistOptions();

                input = sc.next();

                if (input.equals("exit")) {
                    return;
                }

                int option = Integer.parseInt(input);

                //if the entered id corresponds to an existing playlist id, then export that playlist to csv
                if (playlistIds.contains(option)) {

                    //log start time
                    long startTime = System.currentTimeMillis();

                    //business logic to export the playlist to a csv file
                    PlaylistOwn playlistOwn = commandLineController.getPlaylistOwn(option);
                    exportPostgresToCsvService.exportPlaylistToCsv(playlistOwn);
                    System.out.println("The CSV file can be found under server/src/main/resources/...");

                    //log end time, and calculate execution time as the difference between endtime and starttime
                    long endTime = System.currentTimeMillis();
                    System.out.println("Elapsed time in milliseconds: " + (endTime - startTime) + " (or " + ((endTime - startTime) / 1000) + " seconds)");

                    return;

                } else {    //otherwise, prompt the user for a valid playlist id
                    System.out.println("Invalid range - please enter one of the above listed integer values");
                }

            } catch (NumberFormatException exception) {
                System.out.println("Invalid input - Please enter a value corresponding to one of the above options");
            } catch (Exception exception) {
                System.out.println("Error: " + exception);
            }
        }
    }

    private void exportSongsToCsv() {
        try {
            //display informative messages to user regarding export process
            System.out.println("\n< EXPORT ALL SONGS TO CSV FILE >");
            System.out.println("----------------------------------------");

            //log start time
            long startTime = System.currentTimeMillis();

            //business logic to export songs table in the database to CSV file
            exportPostgresToCsvService.exportSongsTableToCsv();
            System.out.println("The CSV file can be found under server/src/main/resources/...");

            //log end time, and calculate execution time as the difference between endtime and starttime
            long endTime = System.currentTimeMillis();
            System.out.println("Elapsed time in milliseconds: " + (endTime - startTime) + " (or " + ((endTime - startTime) / 1000) + " seconds)");

        } catch (Exception exception) {
            System.out.println("Error: " + exception);
        }
    }
}
