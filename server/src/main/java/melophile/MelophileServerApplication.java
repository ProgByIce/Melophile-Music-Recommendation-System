package melophile;

import melophile.commandline.HomeMenuCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class MelophileServerApplication implements CommandLineRunner{

    @Autowired
    HomeMenuCommand homeMenuCommand;

    @Override
    public void run(String... args) {
        homeMenuCommand.homeMenu();
    }

    public static void main(String[] args) {
        SpringApplication.run(MelophileServerApplication.class, args);
    }
}
