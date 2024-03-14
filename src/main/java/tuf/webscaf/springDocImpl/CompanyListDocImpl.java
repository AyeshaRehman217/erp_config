package tuf.webscaf.springDocImpl;

import lombok.*;

import java.util.List;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CompanyListDocImpl {
    List<String> companyUUID;
}
