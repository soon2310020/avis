package saleson.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThreeObject<T1,T2,T3> {
    private T1 left;
    private T2 center;
    private T3 right;
}
