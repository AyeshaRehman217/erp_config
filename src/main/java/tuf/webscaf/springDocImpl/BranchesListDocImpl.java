package tuf.webscaf.springDocImpl;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BranchesListDocImpl {
    //list of branch Ids
    private List<UUID> branch;
}
