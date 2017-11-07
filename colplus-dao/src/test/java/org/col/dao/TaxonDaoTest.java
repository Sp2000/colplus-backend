package org.col.dao;

import static org.col.dao.DaoTestUtil.DATASET1;
import static org.col.dao.DaoTestUtil.newTaxon;
import static org.col.dao.DaoTestUtil.newVernacularName;

import org.col.api.Taxon;
import org.col.api.TaxonInfo;
import org.col.db.mapper.TaxonMapper;
import org.col.db.mapper.VernacularNameMapper;
import org.junit.Test;

public class TaxonDaoTest extends DaoTestBase {

	@Test
	public void testInfo() throws Exception {
		Taxon taxon = newTaxon("test-taxon");
		mapper(TaxonMapper.class).create(taxon);
		int i = taxon.getKey();
		int j = DATASET1.getKey();
		mapper(VernacularNameMapper.class).create(newVernacularName("cat"), i, j);
		mapper(VernacularNameMapper.class).create(newVernacularName("mouse"), i, j);
		mapper(VernacularNameMapper.class).create(newVernacularName("dog"), i, j);
		session().commit();
		TaxonDao dao = new TaxonDao(session());
		TaxonInfo info = dao.getTaxonInfo(DATASET1.getKey(), taxon.getId());
	}

}
