package tuf.webscaf.seeder.model;


import lombok.*;

import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    UUID uuid;
    String city;
    String name;
    String desc;
    String address;
    String company;

}
