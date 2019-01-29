package de.neshanjo.rcswitch.server.data;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString(exclude = "secretKey")
public class SqsConfig {

    @NonNull
    private String accessKey;
    @NonNull
    private String secretKey;
    @NonNull
    private String queueUrl;

}
