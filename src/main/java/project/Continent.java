package project;

import lombok.*;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Continent {

    private int id;
    private String name;
    private int state;

    private Country country;

    public Continent(int continent_id, String continent_name, int states) {
        this.id = continent_id;
        this.name = continent_name;
        this.state = states;
    }
}