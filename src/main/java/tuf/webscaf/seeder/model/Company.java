package tuf.webscaf.seeder.model;


import lombok.*;

import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    UUID uuid;
    String city;
    String company;
    String desc;

}
