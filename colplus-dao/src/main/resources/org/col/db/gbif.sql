-- all datasets from https://github.com/gbif/checklistbank/blob/master/checklistbank-nub/nub-sources.tsv
-- excluding CoL, the GBIF patches and entire organisation or installations which we add below as lists of datasets
-- nom codes: 0=BACTERIAL, 1=BOTANICAL, 2=CULTIVARS, 3=VIRUS, 4=ZOOLOGICAL

INSERT INTO dataset (gbif_key, origin, import_frequency, code, data_access, title) VALUES
    ('00e791be-36ae-40ee-8165-0b2cb0b8c84f', 0, 7, null, 'https://github.com/mdoering/famous-organism/archive/master.zip', 'Species named after famous people'),
    ('046bbc50-cae2-47ff-aa43-729fbf53f7c5', 0, 7, 1, 'http://rs.gbif.org/datasets/protected/ipni.zip', 'International Plant Names Index'),
    ('0938172b-2086-439c-a1dd-c21cb0109ed5', 0, 7, null, 'http://www.irmng.org/export/IRMNG_genera_DwCA.zip', 'The Interim Register of Marine and Nonmarine Genera'),
    ('0e61f8fe-7d25-4f81-ada7-d970bbb2c6d6', 0, 7, null, 'http://ipt.gbif.fr/archive.do?r=taxref-test', 'TAXREF'),
    ('1c1f2cfc-8370-414f-9202-9f00ccf51413', 0, 7, 1, 'http://rs.gbif.org/datasets/protected/euro_med.zip', 'Euro+Med PlantBase data sample'),
    ('1ec61203-14fa-4fbd-8ee5-a4a80257b45a', 0, 7, null, 'http://ipt.taibif.tw/archive.do?r=taibnet_com_all', 'The National Checklist of Taiwan'),
    ('2d59e5db-57ad-41ff-97d6-11f5fb264527', 0, 7, null, 'http://www.marinespecies.org/dwca/WoRMS_DwC-A.zip', 'World Register of Marine Species'),
    ('3f8a1297-3259-4700-91fc-acc4170b27ce', 0, 7, 1, 'http://data.canadensys.net/ipt/archive.do?r=vascan', 'Database of Vascular Plants of Canada (VASCAN)'),
    ('47f16512-bf31-410f-b272-d151c996b2f6', 0, 7, 4, 'http://rs.gbif.org/datasets/clements.zip', 'The Clements Checklist'),
    ('4dd32523-a3a3-43b7-84df-4cda02f15cf7', 0, 7, null, 'http://api.biodiversitydata.nl/v2/taxon/dwca/getDataSet/nsr', 'Checklist Dutch Species Register - Nederlands Soortenregister'),
    ('52a423d2-0486-4e77-bcee-6350d708d6ff', 0, 7, 0, 'http://rs.gbif.org/datasets/dsmz.zip', 'Prokaryotic Nomenclature Up-to-date'),
    ('5c7bf05c-2890-48e8-9b65-a6060cb75d6d', 0, 7, 4, 'http://ipt.zin.ru:8080/ipt/archive.do?r=zin_megophryidae_bufonidae', 'Catalogue of the type specimens of Bufonidae and Megophryidae (Amphibia: Anura) from research collections of the Zoological Institute,'),
    ('65c9103f-2fbf-414b-9b0b-e47ca96c5df2', 0, 7, 4, 'http://ipt.biodiversity.be/archive.do?r=afromoths', 'Afromoths, online database of Afrotropical moth species (Lepidoptera)'),
    ('66dd0960-2d7d-46ee-a491-87b9adcfe7b1', 0, 7, 1, 'http://rs.gbif.org/datasets/grin_archive.zip', 'GRIN Taxonomy'),
    ('672aca30-f1b5-43d3-8a2b-c1606125fa1b', 0, 7, 4, 'http://rs.gbif.org/datasets/msw3.zip', 'Mammal Species of the World'),
    ('6cfd67d6-4f9b-400b-8549-1933ac27936f', 0, 7, null, 'http://api.gbif.org/v1/occurrence/download/request/dwca-type-specimen-checklist.zip', 'GBIF Type Specimen Names'),
    ('7a9bccd4-32fc-420e-a73b-352b92267571', 0, 7, 4, 'http://data.canadensys.net/ipt/archive.do?r=coleoptera-ca-ak', 'Checklist of Beetles (Coleoptera) of Canada and Alaska. Second Edition.'),
    ('7ea21580-4f06-469d-995b-3f713fdcc37c', 0, 7, 1, 'https://github.com/gbif/algae/archive/master.zip', 'GBIF Algae Classification'),
    ('80b4b440-eaca-4860-aadf-d0dfdd3e856e', 0, 30, 4, 'https://github.com/gbif/iczn-lists/archive/master.zip', 'Official Lists and Indexes of Names in Zoology'),
    ('8d431c96-9e2f-4249-8b0a-d875e3273908', 0, 7, 4, 'http://ipt.zin.ru:8080/ipt/archive.do?r=zin_cosmopterigidae', 'Catalogue of the type specimens of Cosmopterigidae (Lepidoptera: Gelechioidea) from research collections of the Zoological Institute, R'),
    ('8dc469b3-8e61-4f6f-b9db-c70dbbc8858c', 0, 7, null, 'https://raw.githubusercontent.com/mdoering/ion-taxonomic-hierarchy/master/classification.tsv', 'ION Taxonomic Hierarchy'),
    ('90d9e8a6-0ce1-472d-b682-3451095dbc5a', 0, 30, 4, 'http://rs.gbif.org/datasets/protected/fauna_europaea.zip', 'Fauna Europaea'),
    ('96dfd141-7bca-4f82-9325-4420d24e0793', 0, 7, 4, 'http://plazi.cs.umb.edu/GgServer/dwca/49CC45D6B497E6D97BDDF3C0D38289E2.zip', 'Spinnengids'),
    ('9ca92552-f23a-41a8-a140-01abaa31c931', 0, 7, null, 'http://rs.gbif.org/datasets/itis.zip', 'Integrated Taxonomic Information System (ITIS)'),
    ('a43ec6d8-7b8a-4868-ad74-56b824c75698', 0, 7, null, 'http://ipt.gbif.pt/ipt/archive.do?r=uac_checklist_madeira', 'A list of the terrestrial fungi, flora and fauna of Madeira and Selvagens archipelagos'),
    ('a6c6cead-b5ce-4a4e-8cf5-1542ba708dec', 0, 7, null, 'https://data.gbif.no/ipt/archive.do?r=artsnavn', 'Artsnavnebasen'),
    ('aacd816d-662c-49d2-ad1a-97e66e2a2908', 0, 7, 1, 'http://ipt.jbrj.gov.br/jbrj/archive.do?r=lista_especies_flora_brasil', 'Brazilian Flora 2020 project - Projeto Flora do Brasil 2020'),
    ('b267ac9b-6516-458e-bea7-7643842187f7', 0, 7, 4, 'http://ipt.zin.ru:8080/ipt/archive.do?r=zin_polycestinae', 'Catalogue of the type specimens of Polycestinae (Coleoptera: Buprestidae) from research collections of the Zoological Institute, Russia'),
    ('bd25fbf7-278f-41d6-bc17-9f08f2632f70', 0, 7, 4, 'http://ipt.biodiversity.be/archive.do?r=mrac_fruitfly_checklist', 'True Fruit Flies (Diptera, Tephritidae) of the Afrotropical Region'),
    ('bf3db7c9-5e5d-4fd0-bd5b-94539eaf9598', 0, 30, 1, 'http://rs.gbif.org/datasets/index_fungorum.zip', 'Index Fungorum'),
    ('c33ce2f2-c3cc-43a5-a380-fe4526d63650', 0, 7, null, 'http://rs.gbif.org/datasets/pbdb.zip', 'The Paleobiology Database'),
    ('c696e5ee-9088-4d11-bdae-ab88daffab78', 0, 7, 4, 'http://rs.gbif.org/datasets/ioc.zip', 'IOC World Bird List, v8.1'),
    ('c8227bb4-4143-443f-8cb2-51f9576aff14', 0, 7, 4, 'http://zoobank.org:8080/ipt/archive.do?r=zoobank', 'ZooBank'),
    ('d8fb1600-d636-4b35-aa0d-d4f292c1b424', 0, 7, 4, 'http://rs.gbif.org/datasets/protected/fauna_europaea-lepidoptera.zip', 'Fauna Europaea - Lepidoptera'),
    ('d9a4eedb-e985-4456-ad46-3df8472e00e8', 0, 7, 1, 'https://zenodo.org/record/1194673/files/dwca.zip', 'The Plant List with literature'),
    ('da38f103-4410-43d1-b716-ea6b1b92bbac', 0, 7, 4, 'http://ipt.saiab.ac.za/archive.do?r=catalogueofafrotropicalbees', 'Catalogue of Afrotropical Bees'),
    ('de8934f4-a136-481c-a87a-b0b202b80a31', 0, 7, null, 'http://www.gbif.se/ipt/archive.do?r=test', 'Dyntaxa. Svensk taxonomisk databas'),
    ('ded724e7-3fde-49c5-bfa3-03b4045c4c5f', 0, 7, 1, 'http://wp5.e-taxonomy.eu/download/data/dwca/cichorieae.zip', 'International Cichorieae Network (ICN): Cichorieae Portal'),
    ('e01b0cbb-a10a-420c-b5f3-a3b20cc266ad', 0, 7, 3, 'http://rs.gbif.org/datasets/ictv.zip', 'ICTV Master Species List'),
    ('e1c9e885-9d8c-45b5-9f7d-b710ac2b303b', 0, 7, null, 'http://ipt.taibif.tw/archive.do?r=taibnet_endemic', 'Endemic species in Taiwan'),
    ('e402255a-aed1-4701-9b96-14368e1b5d6b', 0, 7, 4, 'http://ctap.inhs.uiuc.edu/dmitriev/DwCArchive.zip', '3i - Typhlocybinae Database'),
    ('e768b669-5f12-42b3-9bc7-ede76e4436fa', 0, 7, 4, 'http://plazi.cs.umb.edu/GgServer/dwca/61134126326DC5BE0901E529D48F9481.zip', 'Carabodes cephalotes'),
    ('f43069fe-38c1-43e3-8293-37583dcf5547', 0, 7, 1, 'https://svampe.databasen.org/dwc/DMS_Fun_taxa.zip', 'Danish Mycological Society - Checklist of Fungi'),
    ('56c83fd9-533b-4b77-a67a-cf521816866e', 0, 7, 4, 'http://ipt.pensoft.net/archive.do?r=tenebrionidae_north_america', 'Catalogue of Tenebrionidae (Coleoptera) of North America');

-- Species Files
-- select d.key, e.url, d.title from dataset d join dataset_endpoint de on de.dataset_key=d.key JOIN endpoint e on e.key=de.endpoint_key and e.type='DWC_ARCHIVE' where deleted is null and d.publishing_organization_key='47a779a6-a230-4edd-b787-19c3d2c80ab5';
INSERT INTO dataset (gbif_key, origin, import_frequency, code, data_access, title) VALUES
    ('3e812f13-bd5f-46b6-9bae-710766be526d', 0, 7, 4, 'http://IPT.speciesfile.org:8080/archive.do?r=blattodea', 'Cockroach Species File'),
    ('af66d4cf-0fd2-434b-9334-9806a5efa6f7', 0, 7, 4, 'http://IPT.speciesfile.org:8080/archive.do?r=orthoptera', 'Orthoptera Species File'),
    ('214c3109-d37a-40f8-9c24-5b6e59915394', 0, 7, 4, 'http://IPT.speciesfile.org:8080/archive.do?r=aphid', 'Aphid Species File'),
    ('db93cee5-60d1-4e16-a69e-83dd7080a55e', 0, 7, 4, 'http://IPT.speciesfile.org:8080/archive.do?r=coleorrhyncha', 'Coleorrhyncha Species File'),
    ('c5b58f9d-54b3-4ecf-933e-a9f785a54924', 0, 7, 4, 'http://IPT.speciesfile.org:8080/archive.do?r=mantophasmatodea', 'Mantophasmatodea Species File'),
    ('490cff72-0094-4c5c-8a63-7b1f97de92bb', 0, 7, 4, 'http://IPT.speciesfile.org:8080/archive.do?r=chrysididae', 'Chrysididae Species File'),
    ('598678e4-323c-49dc-8eb7-3a96ac72d472', 0, 7, 4, 'http://IPT.speciesfile.org:8080/archive.do?r=phasmida', 'Phasmida Species File'),
    ('ca694ebf-2b52-4c67-b2e1-0f423149401c', 0, 7, 4, 'http://IPT.speciesfile.org:8080/archive.do?r=lygaeoidea', 'Lygaeoidea Species File'),
    ('203863b6-2adb-43f8-b648-959bcbaec091', 0, 7, 4, 'http://IPT.speciesfile.org:8080/archive.do?r=embioptera', 'Embioptera Species File'),
    ('bc1d816e-97fe-40b3-92eb-ff48fbf8bf08', 0, 7, 4, 'http://IPT.speciesfile.org:8080/archive.do?r=grylloblattodea', 'Grylloblattodea Species File'),
    ('6fb9265c-2d98-40ad-989d-7b6bf4820519', 0, 7, 4, 'http://IPT.speciesfile.org:8080/archive.do?r=psocodea', 'Psocodea Species File'),
    ('fbab0194-b923-49d4-9848-1b3279a6673e', 0, 7, 4, 'http://IPT.speciesfile.org:8080/archive.do?r=plecoptera', 'Plecoptera Species File'),
    ('99948a8b-63b2-41bf-9d10-6e007e967789', 0, 7, 4, 'http://IPT.speciesfile.org:8080/archive.do?r=mantodea', 'Mantodea Species File'),
    ('71e3fb9b-34ed-43d9-97d2-a829918828b2', 0, 7, 4, 'http://IPT.speciesfile.org:8080/archive.do?r=dermaptera', 'Dermaptera Species File'),
    ('e0a61544-c923-4fd7-8ed1-9b692655cf6b', 0, 7, 4, 'http://IPT.speciesfile.org:8080/archive.do?r=coreoidea', 'Coreoidea Species File'),
    ('f5f4d316-e9d2-419b-bd2e-79fefcb3652f', 0, 7, 4, 'http://IPT.speciesfile.org:8080/archive.do?r=zoraptera', 'Zoraptera Species File');
-- DiversityTaxonNames Server
-- select d.key, e.url, d.title from dataset d join dataset_endpoint de on de.dataset_key=d.key JOIN endpoint e on e.key=de.endpoint_key and e.type='DWC_ARCHIVE' where deleted is null and d.installation_key='1a540a73-a60d-4816-bff6-b1ac74b598c9';
INSERT INTO dataset (gbif_key, origin, import_frequency, code, data_access, title) VALUES
    ('f5c60e9e-5b76-43b7-aa14-bbc3fa23b7d5', 0, 7, null, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Fossils/1154/dwc',   'Taxon list of Jurassic Pisces of the Tethys Palaeo-Environment compiled at the SNSB-JME'),
    ('abf8d32d-849f-451e-8226-066e9901f608', 0, 7, 1, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Plants/853/dwc',     'Taxon list of hornworts from Germany compiled in the context of the GBOL project'),
    ('a17fce87-988c-4913-945c-008588c875d1', 0, 7, 4, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Vertebrata/927/dwc', 'Taxon list of Pisces and Cyclostoma from Germany compiled in the context of the GBOL project'),
    ('463893f8-d1e6-4f02-99f1-467ce47c90d0', 0, 7, 1, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Plants/851/dwc',     'Taxon list of Pteridophyta from Germany compiled in the context of the GBOL project'),
    ('e20af983-3d85-4627-b8a5-35c9125c24fc', 0, 7, 4, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Animalia/1137/dwc',  'Taxon list of Pauropoda from Germany compiled in the context of the GBOL project'),
    ('90b8e466-8ef2-440f-9b38-8f283d3174cc', 0, 7, 4, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Animalia/704/dwc',   'Taxon list of Diplopoda from Germany in the context of the GBOL project'),
    ('d027759f-84bc-4dfc-a5ea-b17a50793451', 0, 7, 1, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Plants/1129/dwc',    'Taxon list of vascular plants from Bavaria, Germany compiled in the context of the BFL project'),
    ('88f4e35a-bdf8-4aa2-9a1b-56401d4eed15', 0, 7, null, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_TaxaVaria/1144/dwc', 'Taxon list of animals with German names (worldwide) compiled at the SMNS'),
    ('c791e14a-3e86-40e7-af95-c1663c922c59', 0, 7, 1, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Plants/849/dwc',     'Taxon list of mosses from Germany compiled in the context of the GBOL project'),
    ('4d36790e-4ba1-4a84-86db-5e0c8ca7956e', 0, 7, 1, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Plants/852/dwc',     'Taxon list of liverworts from Germany compiled in the context of the GBOL project'),
    ('155b33d2-84b1-4a31-9287-9d9e900bc6c8', 0, 7, 1, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Fungi/1140/dwc',     'Taxon list of fungi and fungal-like organisms from Germany compiled by the DGfM'),
    ('aa66719c-5738-441e-bdc5-46c9306196fa', 0, 7, 4, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Animalia/705/dwc',   'Taxon list of Pseudoscorpiones from Germany compiled in the context of the GBOL project'),
    ('a389964a-2ea5-45af-89b2-e96dc644a2ea', 0, 7, 4, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Animalia/714/dwc',   'Taxon list of Tardigrada from Germany compiled in the context of the GBOL project'),
    ('a1b129e5-b226-44cd-a7d0-e3981e643a06', 0, 7, 4, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Animalia/1138/dwc',  'Taxon list of Symphyla from Germany compiled in the context of the GBOL project'),
    ('5ffee3be-accc-4745-9d4c-22da64cf3225', 0, 7, 4, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Insecta/874/dwc',    'Taxon list of Microcoryphia (Archaeognatha) from Germany compiled in the context of the GBOL project'),
    ('7573b209-1f65-4e90-9026-a003c8dcd3a3', 0, 7, 4, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Insecta/876/dwc',    'Taxon list of Diplura from Germany compiled in the context of the GBOL project'),
    ('3b347a21-6a68-417a-b44d-a552984fdc84', 0, 7, 4, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Insecta/854/dwc',    'Taxon list of Auchenorrhyncha from Germany compiled in the context of the GBOL project'),
    ('7b1d0ad2-67be-47f3-9d7f-bb4e62c89f37', 0, 7, 4, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Insecta/860/dwc',    'Taxon list of Megaloptera from Germany compiled in the context of the GBOL project'),
    ('59c9d60f-9d81-442a-b22c-7a6130b14771', 0, 7, 4, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Insecta/877/dwc',    'Taxon list of Protura from Germany compiled in the context of the GBOL project'),
    ('9c1b2b20-7d83-44e1-8aab-828859239d82', 0, 7, 4, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Insecta/859/dwc',    'Taxon list of Pscoptera from Germany compiled in the context of the GBOL project'),
    ('21db5658-1621-4a7d-b284-3ec1dca446e7', 0, 7, 4, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Insecta/872/dwc',    'Taxon list of Siphonaptera from Germany compiled in the context of the GBOL project'),
    ('a0f2130b-b848-4acc-a597-6bc93ca4b597', 0, 7, 4, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Insecta/861/dwc',    'Taxon list of Raphidioptera from Germany compiled in the context of the GBOL project'),
    ('6c4baae4-b74c-4f1c-bd19-5dbf14f57789', 0, 7, 4, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Insecta/879/dwc',    'Taxon list of Sternorrhyncha from Germany compiled in the context of the GBOL project'),
    ('59bf75f2-fffc-4de1-b759-10e1da66e144', 0, 7, 4, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Insecta/873/dwc',    'Taxon list of Strepsiptera from Germany compiled in the context of the GBOL project'),
    ('b84223da-ccb6-40dc-8949-06ab4b71ae85', 0, 7, 4, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Insecta/870/dwc',    'Taxon list of Trichoptera from Germany compiled in the context of the GBOL project'),
    ('a53c6aa0-338f-451d-966f-eb336d2145b6', 0, 7, 4, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Insecta/862/dwc',    'Taxon list of Thysanoptera from Germany compiled in the context of the GBOL project'),
    ('a288dc1e-48dd-40cd-8a4f-cb928fe050e5', 0, 7, 4, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Insecta/871/dwc',    'Taxon list of Zygentoma from Germany compiled in the context of the GBOL project'),
    ('b3272c2c-78cc-4d3e-94eb-a54774dd9461', 0, 7, 4, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Insecta/864/dwc',    'Taxon list of Dermaptera from Germany compiled in the context of the GBOL project'),
    ('0b366d90-bd5e-4e7a-ba06-295813334125', 0, 7, 4, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Insecta/867/dwc',    'Taxon list of Hymenoptera from Germany compiled in the context of the GBOL project'),
    ('2204aafd-c27c-4ebb-a5a1-2614679cb215', 0, 7, 4, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Insecta/865/dwc',    'Taxon list of Dictyoptera from Germany compiled in the context of the GBOL project'),
    ('d2c9167d-cd03-4589-9323-f36fdbf14be4', 0, 7, 4, 'http://services.snsb.info/DTNtaxonlists/rest/v0.1/lists/DiversityTaxonNames_Insecta/381/dwc',    'Taxon list of Orthoptera (Grashoppers) from Germany compiled at the SNSB');

-- Plazi is excluded as its too large and changes constantly. Use the GBIF sync feature instead!
-- non backbone lists
INSERT INTO dataset (gbif_key, origin, import_frequency, data_access, title) VALUES
    ('8067e0a2-a26d-4831-8a1e-21b9118a299c', 0, 7, 'https://github.com/mdoering/ggi-families/archive/master.zip', 'Families of Living Organisms (FALO)'),
    ('fa8ab13c-52ed-4754-b838-aeff74c79718', 0, 7, 'https://github.com/Sp2000/dwca-apg/archive/master.zip', 'Angiosperm phylogeny classification of flowering plants (APG IV)');


UPDATE dataset set catalogue=1,
    data_format=0
    WHERE gbif_key is not NULL;