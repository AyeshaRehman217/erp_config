package tuf.webscaf.springDocImpl;

import lombok.*;
import org.springframework.data.relational.core.mapping.Column;

import java.util.List;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OfficeDocImpl {

    @Column
    private String country;

    @Column
    private String city;

    @Column
    private String district;

    @Column
    private String address;

    @Column("postal_code")
    private String postalCode;

    @Column
    private String description;

    private List<String> companyUUID;
}
