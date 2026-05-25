package com.greenhouse.smart_backend.modules.iot.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Document(collection = "actuator_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActuatorEventDocument {

    @Id
    private String id;

    @Field("node_name")
    private String nodeName;

    @Field("actuator_name")
    private String actuatorName;

    @Field("action")
    private String action;

    @Field("timestamp")
    private Instant timestamp;
}
