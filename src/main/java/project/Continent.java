package project;

import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "countries")
public class Continent {

    private int id;
    private String name;
    private int numberOfStates;

    private Set<Country> countries = new HashSet<>();

    public Continent(int continentId, String continentName, int numberOfStates) {
        this.id = continentId;
        this.name = continentName;
        this.numberOfStates = numberOfStates;
    }

    public void addCountry(Country country) {
        if (country != null) {
            countries.add(country);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Continent continent = (Continent) o;

        return id == continent.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

}