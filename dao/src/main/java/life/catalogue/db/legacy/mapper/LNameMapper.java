package life.catalogue.db.legacy.mapper;

import life.catalogue.db.legacy.model.LName;
import life.catalogue.db.legacy.model.LSpeciesName;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LNameMapper {

  LSpeciesName get(@Param("datasetKey") int datasetKey, @Param("id") String id);

  int count(@Param("datasetKey") int datasetKey,
            @Param("prefix") boolean prefix,
            @Param("name") String name);

  List<LName> search(@Param("datasetKey") int datasetKey,
                     @Param("prefix") boolean prefix,
                     @Param("name") String name,
                     @Param("start") int start,
                     @Param("limit") int limit);

}
