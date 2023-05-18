package org.example;

import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonRepresentation;

public interface MongoDocument {
    @BsonId()
    @BsonRepresentation(BsonType.STRING)
    String id();
}
