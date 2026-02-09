package melophile.commandline;

import melophile.controller.CommandLineController;
import melophile.model.PlaylistOwn;
import melophile.model.Song;
import melophile.service.ImportSpotifyDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Service;

import java.util.Scanner;

@Service
public class ImportSpotifyDataCommand {

    @Autowired
    ImportSpotifyDataService importSpotifyDataService;
    @Autowired
    CommandLineController commandLineController;
    @Autowired
    DisplayCommand displayCommand;

    private static Scanner sc = new Scanner(System.in);
    private static String[] options = {"Import a Song", "Import a Playlist", "Return to Main Menu"};

    public void importData() {

        int option = 1;

        while (option != options.length) {

            System.out.println("\n< IMPORT DATA FROM SPOTIFY >");
            displayCommand.displayOptions(options);

            try {
                option = Integer.parseInt(sc.next());

                switch (option) {
                    case 1: {
                        importSong();
                        break;
                    }
                    case 2: {
                        importPlaylist();
                        break;
                    }
                    case 3: {
                        return;
                    }
                    default: {
                        System.out.println("Invalid option - Please enter an integer value between 1 and " + options.length);
                        break;
                    }
                }

            } catch (NumberFormatException exception) {
                System.out.println("Invalid input - Please enter an integer value between 1 and " + options.length);
            } catch (Exception exception) {
                System.out.println("Error: " + exception);
            }
        }
    }

    private void importSong() {

        String input = "";

        while (true) {
            try {

                System.out.println("\n< IMPORT SPOTIFY TRACK>");
                System.out.println("----------------------------------------");
                System.out.print("Please enter the Spotify URL of the track, or \"exit\" to cancel: ");

                input = sc.next();

                if(input.equals("exit")){
                    return;
                }

                //log start time
                long startTime = System.currentTimeMillis();

                //business logic to import song into database
                String trackId = importSpotifyDataService.extractIdFromUrl(input);
                Song imported = importSpotifyDataService.convertSpotifyTrackToSong(trackId);
                commandLineController.saveSongInDatabase(imported);

                //log end time, and calculate execution time as the difference between endtime and starttime
                long endTime = System.currentTimeMillis();
                System.out.println("Elapsed time in milliseconds: " + (endTime-startTime) + " (or " + ((endTime-startTime)/1000) + " seconds)");

                return;

            } catch (InvalidDataAccessApiUsageException | NullPointerException exception) {
                System.out.println("Invalid Spotify URL - Please enter a valid Spotify object's URL");
            } catch (Exception exception) {
                System.out.println("Error: " + exception);
            }
        }
    }

    private void importPlaylist() {

        String input = "";

        while (true) {
            try{

                System.out.println("\n< IMPORT SPOTIFY PLAYLIST>");
                System.out.println("----------------------------------------");
                System.out.print("Please enter the Spotify URL of the (PUBLIC) playlist, or \"exit\" to cancel: ");

                input = sc.next();

                if(input.equals("exit")){
                    return;
                }

                //log start time
                long startTime = System.currentTimeMillis();

                //business logic to import playlist into database
                String playlistId = importSpotifyDataService.extractIdFromUrl(input);
                PlaylistOwn imported = importSpotifyDataService.convertSpotifyPlaylistToPlaylistOwn(playlistId);
                imported.setName("Imported - " + imported.getName());
                commandLineController.savePlaylistOwnSongsInDatabase(imported);
                commandLineController.savePlaylistOwnInDatabase(imported);

                //log end time, and calculate execution time as the difference between endtime and starttime
                long endTime = System.currentTimeMillis();
                System.out.println("Elapsed time in milliseconds: " + (endTime-startTime) + " (or " + ((endTime-startTime)/1000) + " seconds)");

                return;

            } catch (InvalidDataAccessApiUsageException | NullPointerException exception) {
                System.out.println("Invalid Spotify URL - Please enter a valid Spotify object's URL");
            } catch (Exception exception) {
                System.out.println("Error: " + exception);
            }
        }
    }
}
