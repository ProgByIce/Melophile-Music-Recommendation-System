package melophile.service;

import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.io.IOException;

@Service
public class SpotifyAuthenticationService {

    /*
    * Notes to keep in mind:
    * When a program is run, the first thing that loads in the JVM is private static fields/methods
    * after that, public static,
    * after that, any non-static fields and methods*/

    public String CLIENT_ID;
    public String CLIENT_SECRET;
    private SpotifyApi spotifyApi;
    private ClientCredentialsRequest clientCredentialsRequest;

    //constructor
    public SpotifyAuthenticationService(@Value("${spotify.client-id}") String CLIENT_ID,
                                        @Value("${spotify.client-secret}") String CLIENT_SECRET){

        this.CLIENT_ID=CLIENT_ID;
        this.CLIENT_SECRET=CLIENT_SECRET;

        spotifyApi = new SpotifyApi.Builder()
                .setClientId(CLIENT_ID)
                .setClientSecret(CLIENT_SECRET)
                .build();   //READ INTO BUILDER PATTERN!

        clientCredentialsRequest = spotifyApi.clientCredentials()
                .build();

        setAccessToken();

        //in order to build a spotifyapi object, you need to have initialized clientid and client secret variables
    }

    public String getAccessToken(){
        try {
            ClientCredentials clientCredentials = clientCredentialsRequest.execute();

            // return the access token for further "spotifyApi" object usage
            return clientCredentials.getAccessToken();

        } catch (IOException | SpotifyWebApiException | ParseException e) {
            System.out.println("Error: " + e.getMessage());
            return "";
        }
    }

    public void setAccessToken(){
        String token = this.getAccessToken();
        if (token.equals("")){
            //error handling
        } else{
            spotifyApi.setAccessToken(token);
        }
    }

    public SpotifyApi getSpotifyApi(){
        return this.spotifyApi;
    }

    //Note: to develop - a mechanism for catching an invalid/expired token. If that error is met, generate a new token, and run the process again
}
