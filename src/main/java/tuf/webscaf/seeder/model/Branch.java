package tuf.webscaf.seeder.model;


import lombok.*;

import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Branch {

    UUID uuid;
    String city;
    String branch;
    String desc;
    String address;
    String company;

}
