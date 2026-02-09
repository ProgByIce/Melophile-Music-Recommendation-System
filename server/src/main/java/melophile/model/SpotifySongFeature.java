package melophile.model;

import jakarta.persistence.*;

@Entity
@Table (name="spotifysongfeature")
public class SpotifySongFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String name;

    @Column (name = "datatype")
    private String dataType;

    @Column
    private int minimum;

    @Column
    private int maximum;

    @Column
    private boolean normalized;

    //constructors
    public SpotifySongFeature() {
    }

    //getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDataType() {
        return dataType;
    }

    public int getMinimum() {
        return minimum;
    }

    public int getMaximum() {
        return maximum;
    }

    public boolean isNormalized() {
        return normalized;
    }

    //setters
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public void setMinimum(int minimum) {
        this.minimum = minimum;
    }

    public void setMaximum(int maximum) {
        this.maximum = maximum;
    }

    public void setNormalized(boolean normalized) {
        this.normalized = normalized;
    }
}
