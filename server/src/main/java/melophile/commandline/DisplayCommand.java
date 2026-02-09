package melophile.commandline;

import melophile.controller.CommandLineController;
import melophile.model.PlaylistOwn;
import melophile.model.SpotifySongFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DisplayCommand {

    @Autowired
    CommandLineController commandLineController;

    //display a string array of options to the user
    public void displayOptions(String[] options) {
        int i = 1;
        System.out.println("Please choose an option:");
        System.out.println("----------------------------------------");
        for (String option : options) {
            System.out.println("\t" + i + " - " + option);
            i++;
        }
        System.out.println("----------------------------------------");
        System.out.print("Your choice: ");
    }

    public List<Integer> displayPlaylistOptions() {

        //retrieve a list of all playlists in the database, and display them as export options (in the format "id - name")
        List<PlaylistOwn> allPlaylists = commandLineController.getAllPlaylistOwns();
        List<Integer> playlistIds = new ArrayList<>();  //additionally, store a list of all the available playlist ids

        System.out.println("----------------------------------------");
        for (PlaylistOwn playlist : allPlaylists) {
            System.out.println("\t" + playlist.getId() + " - \"" + playlist.getName() + "\" (" + playlist.getSongs().size() + " songs)");
            playlistIds.add(playlist.getId());
        }
        System.out.println("\texit - Return to Previous Menu");
        System.out.println("----------------------------------------");
        System.out.print("Your choice: ");

        //return the list of playlist features for use in input validation
        return playlistIds;
    }

    public List<Integer> displaySongFeatureOptions(){
        //retrieve a list of all spotify song features in the database
        List<SpotifySongFeature> allFeatures = commandLineController.getAllSpotifySongFeatures();
        List<Integer> featureIds = new ArrayList<>();   //additionally, store a list of all the feature ids

        System.out.println("----------------------------------------");
        for (SpotifySongFeature feature : allFeatures) {
            System.out.println("\t" + feature.getId() + " - " + feature.getName() + "");
            featureIds.add(feature.getId());
        }
        System.out.println("\texit - Return to Previous Menu");
        System.out.println("----------------------------------------");
        System.out.print("Your choice: ");

        //return the list of playlist features for use in input validation
        return featureIds;
    }
}
