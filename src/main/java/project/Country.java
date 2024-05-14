package project;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Country {

    private int id;
    private String name;
    private String capital;

    private Continent continent;

        public Country(int countryId, String countryName, String countryCapital) {
            this.id = countryId;
            this.name = countryName;
            this.capital = countryCapital;
        }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Country country = (Country) o;

        return id == country.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

}