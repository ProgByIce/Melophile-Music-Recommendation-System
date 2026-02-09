package melophile.commandline;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Scanner;

@Service
public class HomeMenuCommand {

    @Autowired
    ImportSpotifyDataCommand spotifyDataCommand;
    @Autowired
    DisplayCommand displayCommand;
    @Autowired
    ExportDataCommand exportDataCommand;
    @Autowired
    GeneratePlaylistCommand generatePlaylistCommand;
    @Autowired
    EnhancePlaylistCommand enhancePlaylistCommand;

    private Scanner sc = new Scanner(System.in);
    private String[] options = {"Import Data from Spotify", "Export Data to CSV", "Generate Playlist", "Enhance Existing Playlist", "Exit"};

    public void homeMenu() {

        System.out.println("< Welcome to MELOPHILE >");
        int option = 1;

        while (option != options.length) {

            System.out.println("\n< HOME MENU >");
            displayCommand.displayOptions(options);

            try {
                option = Integer.parseInt(sc.next());   //get input like this instead of option=s.nextInt(), because nextInt() does not consume invalid tokens => will throw the program into an infinite loop

                switch (option) {
                    case 1: {
                        spotifyDataCommand.importData();
                        break;
                    }
                    case 2: {
                        exportDataCommand.exportData();
                        break;
                    }
                    case 3: {
                        generatePlaylistCommand.generatePlaylist();
                        break;
                    }
                    case 4: {
                        enhancePlaylistCommand.enhancePlaylist();
                        break;
                    }
                    case 5: {
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

        System.out.println("< Thank you for using MELOPHILE >");
    }
}
