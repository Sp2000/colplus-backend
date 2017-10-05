/*
 * Copyright 2014 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.col.api.vocab;

/**
 * Enumeration representing the different nomenclatoral codes found in biology for scientific names.
 * <p/>
 * Nomenclature codes or codes of nomenclature are the various rulebooks that govern biological taxonomic
 * nomenclature, each in their own broad field of organisms.
 * To an end-user who only deals with names of species, with some awareness that species are assignable to
 * families, it may not be noticeable that there is more than one code, but beyond this basic level these are rather
 * different in the way they work.
 *
 * @see <a href="http://en.wikipedia.org/wiki/Nomenclature_codes">Nomenclature codes (Wikipedia)</a>
 */
public enum NomCode {

  BACTERIAL("ICNB", "International Code of Nomenclature of Bacteria", "http://www.ncbi.nlm.nih.gov/books/NBK8808/"),
  BOTANICAL("ICN", "International Code of Nomenclature for algae, fungi, and plants", "http://ibot.sav.sk/icbn/main.htm"),
  CULTIVARS("ICNCP", "International Code of Nomenclature for Cultivated Plants", ""),
  VIRUS("ICVCN", "International Code of Virus Classifications and Nomenclature", "http://talk.ICTVonline.org/"),
  ZOOLOGICAL("ICZN",
      "International Code of Zoological Nomenclature",
      "http://www.nhm.ac.uk/hosted-sites/iczn/code/index.jsp");

  private final String title;
  private final String acronym;
  private final String link;

  NomCode(String acronym, String title, String link) {
    this.acronym = acronym;
    this.link = link;
    this.title = title;
  }

  public String getAcronym() {
    return acronym;
  }

  public String getLink() {
    return link;
  }

  public String getTitle() {
    return title;
  }

}
