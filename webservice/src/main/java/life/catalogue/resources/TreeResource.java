package life.catalogue.resources;

import io.dropwizard.auth.Auth;
import life.catalogue.api.model.*;
import life.catalogue.api.util.ObjectUtils;
import life.catalogue.api.vocab.Datasets;
import life.catalogue.dao.TaxonDao;
import life.catalogue.dao.TreeDao;
import life.catalogue.dw.auth.Roles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/dataset/{datasetKey}/tree")
@Produces(MediaType.APPLICATION_JSON)
@SuppressWarnings("static-method")
public class TreeResource {
  private static final int DEFAULT_PAGE_SIZE = 100;

  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(TreeResource.class);
  private final TaxonDao dao;
  private final TreeDao tree;
  
  public TreeResource(TaxonDao dao, TreeDao tree) {
    this.dao = dao;
    this.tree = tree;
  }

  private static Page page(Integer limit, Integer offset){
    Page p = new Page(ObjectUtils.coalesce(offset, 0), DEFAULT_PAGE_SIZE);
    // we set the limit here and not in the constructor to bypass the max argument exception
    // we want to allow higher limits than 1000 for the tree API only !!!
    p.setLimit(ObjectUtils.coalesce(limit, DEFAULT_PAGE_SIZE));
    return p;
  }

  @GET
  public ResultPage<TreeNode> root(@PathParam("datasetKey") int datasetKey,
                                   @QueryParam("catalogueKey") @DefaultValue(Datasets.DRAFT_COL+"") int catalogueKey,
                                   @QueryParam("type") TreeNode.Type type,
                                   @QueryParam("limit") Integer limit,
                                   @QueryParam("offset") Integer offset) {
    return tree.root(datasetKey, catalogueKey, type, page(limit, offset));
  }
  
  @GET
  @Path("{id}")
  public List<TreeNode> classification(@PathParam("datasetKey") int datasetKey,
                                @PathParam("id") String id,
                                @QueryParam("catalogueKey") @DefaultValue(Datasets.DRAFT_COL+"") int catalogueKey,
                                @QueryParam("insertPlaceholder") boolean placeholder,
                                @QueryParam("type") TreeNode.Type type) {
    return tree.classification(DSID.of(datasetKey, id), catalogueKey, placeholder, type);
  }

  @DELETE
  @Path("{id}")
  @RolesAllowed({Roles.ADMIN, Roles.EDITOR})
  public void deleteRecursively(@PathParam("datasetKey") int datasetKey,
                                @PathParam("id") String id,
                                @Auth User user) {
    dao.deleteRecursively(DSID.of(datasetKey, id), user);
  }
  
  @GET
  @Path("{id}/children")
  public ResultPage<TreeNode> children(@PathParam("datasetKey") int datasetKey,
                                       @PathParam("id") String id,
                                       @QueryParam("catalogueKey") @DefaultValue(Datasets.DRAFT_COL+"") int catalogueKey,
                                       @QueryParam("insertPlaceholder") boolean placeholder,
                                       @QueryParam("type") TreeNode.Type type,
                                       @QueryParam("limit") Integer limit,
                                       @QueryParam("offset") Integer offset) {
    return tree.children(DSID.of(datasetKey, id), catalogueKey, placeholder, type, page(limit, offset));
  }
}
