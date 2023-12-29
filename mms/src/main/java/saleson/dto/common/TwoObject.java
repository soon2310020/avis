package saleson.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoObject <T1,T2>{
    private T1 left;
    private T2 right;
    public static <S, T> TwoObject<S, T> of(S first, T second) {
        return new TwoObject<>(first, second);
    }
}
