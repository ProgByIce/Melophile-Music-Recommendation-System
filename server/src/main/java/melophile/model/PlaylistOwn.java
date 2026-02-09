package melophile.model;

import jakarta.persistence.*;

import java.util.Set;

@Entity
@Table(name = "playlist")
public class PlaylistOwn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "songsinplaylist",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id"))
    private Set<Song> songs;

    //constructors
    public PlaylistOwn() {
    }

    //getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Song> getSongs() {
        return songs;
    }

    //setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSongs(Set<Song> songs) {
        this.songs = songs;
    }
}
