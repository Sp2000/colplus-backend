package org.col.db.mapper;

import java.util.List;
import javax.annotation.Nullable;

import org.apache.ibatis.annotations.Param;
import org.col.api.model.Page;
import org.col.api.model.SpeciesEstimate;
import org.gbif.nameparser.api.Rank;

public interface EstimateMapper extends GlobalCRUDMapper<SpeciesEstimate> {
  
  SpeciesEstimate getById(@Param("id") String id);
  
  /**
   * List all estimates that cannot be linked to subject taxa in the catalogue
   */
  List<SpeciesEstimate> broken();
  
  int count(@Nullable @Param("rank") Rank rank
      , @Nullable @Param("min") Integer min
      , @Nullable @Param("max") Integer max);
  
  List<SpeciesEstimate> search(@Nullable @Param("rank") Rank rank
      , @Nullable @Param("min") Integer min
      , @Nullable @Param("max") Integer max,
                  @Param("page") Page page);
  
}
