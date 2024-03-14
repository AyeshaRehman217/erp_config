package tuf.webscaf.springDocImpl;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ExistingCompanyListDocImpl {
    //list of company Ids
    private List<UUID> companyUUID;
}
