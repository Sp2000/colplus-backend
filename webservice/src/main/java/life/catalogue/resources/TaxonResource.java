package life.catalogue.resources;

import com.codahale.metrics.annotation.Timed;
import life.catalogue.api.exception.NotFoundException;
import life.catalogue.api.model.*;
import life.catalogue.dao.TaxonDao;
import life.catalogue.db.mapper.NameUsageMapper;
import life.catalogue.db.mapper.TaxonMapper;
import life.catalogue.db.mapper.VerbatimSourceMapper;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.util.List;

@Path("/dataset/{key}/taxon")
@Produces(MediaType.APPLICATION_JSON)
@SuppressWarnings("static-method")
public class TaxonResource extends AbstractDatasetScopedResource<String, Taxon, TaxonResource.TaxonSearchRequest> {
  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(TaxonResource.class);
  private final TaxonDao dao;
  
  public TaxonResource(TaxonDao dao) {
    super(Taxon.class, dao);
    this.dao = dao;
  }

  public static class TaxonSearchRequest {
    @QueryParam("root")
    boolean root;
  }

  @Override
  ResultPage<Taxon> searchImpl(int datasetKey, TaxonSearchRequest req, Page page) {
    return req.root ? dao.listRoot(datasetKey, page) : dao.list(datasetKey, page);
  }

  @GET
  @Override
  @Path("{id}")
  public Taxon get(@PathParam("key") int datasetKey, @PathParam("id") String id) {
    var key = new DSIDValue<>(datasetKey, id);
    Taxon obj = dao.get(key);
    if (obj == null) {
      // try with a synonym and issue a redirect
      try (SqlSession session = dao.getFactory().openSession()) {
        SimpleName syn = session.getMapper(NameUsageMapper.class).getSimple(key);
        if (syn != null) {
          ResourceUtils.redirect(URI.create("/dataset/"+datasetKey+"/taxon/" + syn.getParent()));
        } else {
          throw NotFoundException.notFound(Taxon.class, key);
        }
      }
    }
    return obj;
  }

  @GET
  @Path("{id}/children")
  public ResultPage<Taxon> children(@PathParam("key") int datasetKey, @PathParam("id") String id, @Valid @BeanParam Page page) {
    return dao.getChildren(DSID.of(datasetKey, id), page);
  }
  
  @GET
  @Path("{id}/synonyms")
  public Synonymy synonyms(@PathParam("key") int datasetKey, @PathParam("id") String id) {
    return dao.getSynonymy(datasetKey, id);
  }
  
  @GET
  @Path("{id}/classification")
  public List<Taxon> classification(@PathParam("key") int datasetKey, @PathParam("id") String id, @Context SqlSession session) {
    return session.getMapper(TaxonMapper.class).classification(DSID.of(datasetKey, id));
  }
  
  @GET
  @Timed
  @Path("{id}/info")
  public TaxonInfo info(@PathParam("key") int datasetKey, @PathParam("id") String id) {
    TaxonInfo info = dao.getTaxonInfo(datasetKey, id);
    if (info == null) {
      throw NotFoundException.notFound(Taxon.class, datasetKey, id);
    }
    return info;
  }

  @GET
  @Path("{id}/source")
  public VerbatimSource source(@PathParam("key") int datasetKey, @PathParam("id") String id, @Context SqlSession session) {
    return session.getMapper(VerbatimSourceMapper.class).get(DSID.of(datasetKey, id));
  }
}
