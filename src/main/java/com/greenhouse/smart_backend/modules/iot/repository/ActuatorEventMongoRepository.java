package com.greenhouse.smart_backend.modules.iot.repository;

import com.greenhouse.smart_backend.modules.iot.document.ActuatorEventDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActuatorEventMongoRepository extends MongoRepository<ActuatorEventDocument, String> {

    Optional<ActuatorEventDocument> findFirstByNodeNameAndActuatorNameOrderByTimestampDesc(String nodeName, String actuatorName);

    List<ActuatorEventDocument> findByNodeNameAndActuatorNameAndTimestampBetween(String nodeName, String actuatorName, Instant start, Instant end);
}
