package tuf.webscaf.springDocImpl;

import lombok.*;

import java.util.List;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CompaniesListDocImpl {
    //list of company Ids
    private List<Long> company;
}
