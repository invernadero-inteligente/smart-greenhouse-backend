package com.greenhouse.smart_backend.modules.iot.repository;

import com.greenhouse.smart_backend.modules.iot.document.SensorReadingDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SensorReadingMongoRepository extends MongoRepository<SensorReadingDocument, String> {
    
    Optional<SensorReadingDocument> findFirstByNodeNameAndVariableNameOrderByTimestampDesc(String nodeName, String variableName);
    
    List<SensorReadingDocument> findByNodeNameAndVariableNameAndTimestampBetween(String nodeName, String variableName, Instant start, Instant end);
}
