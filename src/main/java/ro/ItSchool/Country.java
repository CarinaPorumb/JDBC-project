package ro.ItSchool;

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

    public Country(int country_id, String country_name, String country_capital) {
        this.id = country_id;
        this.name = country_name;
        this.capital = country_capital;
    }
}


