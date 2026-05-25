package com.greenhouse.smart_backend.modules.iot.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Document(collection = "sensor_readings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorReadingDocument {

    @Id
    private String id;

    @Field("node_name")
    private String nodeName;

    @Field("variable_name")
    private String variableName;

    @Field("value")
    private String value;

    @Field("unit")
    private String unit;

    @Field("timestamp")
    private Instant timestamp;
}
