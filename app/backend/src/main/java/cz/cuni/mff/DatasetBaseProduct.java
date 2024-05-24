package cz.cuni.mff;

import org.bson.BsonType;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonRepresentation;

public interface DatasetBaseProduct {
    @BsonId()
    @BsonRepresentation(BsonType.STRING)
    String id();
    String thumbnail();
}
