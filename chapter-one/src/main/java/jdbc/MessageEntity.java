package jdbc;

import lombok.*;

@ToString
@EqualsAndHashCode
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MessageEntity {
    private Long id;
    private String text;
}
