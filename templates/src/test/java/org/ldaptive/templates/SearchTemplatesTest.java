/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.templates;

import org.ldaptive.SearchFilter;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link SearchTemplates}.
 *
 * @author  Middleware Services
 */
public class SearchTemplatesTest
{


  /**
   * Sample template data.
   *
   * @return  template data
   */
  @DataProvider(name = "template-data")
  public Object[][] createTestData()
  {
    // test data for one term query
    final SearchFilter sf11 = new SearchFilter("(|(telephoneNumber={term1})(localPhone={term1}))");
    sf11.setParameter("term1", "fisher");
    sf11.setParameter("initial1", "f");

    final SearchFilter sf12 = new SearchFilter("(|(telephoneNumber=*{term1})(localPhone=*{term1}))");
    sf12.setParameter("term1", "fisher");
    sf12.setParameter("initial1", "f");

    final SearchFilter sf13 = new SearchFilter("(|(givenName={term1})(sn={term1}))");
    sf13.setParameter("term1", "fisher");
    sf13.setParameter("initial1", "f");

    final SearchFilter sf14 = new SearchFilter("(|(givenName={term1}*)(sn={term1}*))");
    sf14.setParameter("term1", "fisher");
    sf14.setParameter("initial1", "f");

    final SearchFilter sf15 = new SearchFilter("(|(givenName=*{term1}*)(sn=*{term1}*))");
    sf15.setParameter("term1", "fisher");
    sf15.setParameter("initial1", "f");

    // test data for two term query
    final SearchFilter sf21 = new SearchFilter("(&(givenName={term1})(sn={term2}))");
    sf21.setParameter("term1", "daniel");
    sf21.setParameter("term2", "fisher");
    sf21.setParameter("initial1", "d");
    sf21.setParameter("initial2", "f");

    final SearchFilter sf22 = new SearchFilter("(cn={term1} {term2})");
    sf22.setParameter("term1", "daniel");
    sf22.setParameter("term2", "fisher");
    sf22.setParameter("initial1", "d");
    sf22.setParameter("initial2", "f");

    final SearchFilter sf23 = new SearchFilter("(&(givenName={term1}*)(sn={term2}*))");
    sf23.setParameter("term1", "daniel");
    sf23.setParameter("term2", "fisher");
    sf23.setParameter("initial1", "d");
    sf23.setParameter("initial2", "f");

    final SearchFilter sf24 = new SearchFilter("(cn={term1}* {term2}*)");
    sf24.setParameter("term1", "daniel");
    sf24.setParameter("term2", "fisher");
    sf24.setParameter("initial1", "d");
    sf24.setParameter("initial2", "f");

    final SearchFilter sf25 = new SearchFilter("(&(givenName=*{term1}*)(sn=*{term2}*))");
    sf25.setParameter("term1", "daniel");
    sf25.setParameter("term2", "fisher");
    sf25.setParameter("initial1", "d");
    sf25.setParameter("initial2", "f");

    final SearchFilter sf26 = new SearchFilter("(cn=*{term1}* *{term2}*)");
    sf26.setParameter("term1", "daniel");
    sf26.setParameter("term2", "fisher");
    sf26.setParameter("initial1", "d");
    sf26.setParameter("initial2", "f");

    final SearchFilter sf27 = new SearchFilter(
      "(|(&(givenName={initial1}*)(sn={term2}))" +
      "(&(middleName={initial1}*)(sn={term2})))");
    sf27.setParameter("term1", "daniel");
    sf27.setParameter("term2", "fisher");
    sf27.setParameter("initial1", "d");
    sf27.setParameter("initial2", "f");

    final SearchFilter sf28 = new SearchFilter("(sn={term2})");
    sf28.setParameter("term1", "daniel");
    sf28.setParameter("term2", "fisher");
    sf28.setParameter("initial1", "d");
    sf28.setParameter("initial2", "f");

    // test data for three term query
    final SearchFilter sf31 = new SearchFilter(
      "(|(&(givenName={term1})(sn={term3}))(&(givenName={term2})(sn={term3})))");
    sf31.setParameter("term1", "daniel");
    sf31.setParameter("term2", "wayne");
    sf31.setParameter("term3", "fisher");
    sf31.setParameter("initial1", "d");
    sf31.setParameter("initial2", "w");
    sf31.setParameter("initial3", "f");

    final SearchFilter sf32 = new SearchFilter("(|(cn={term1} {term2} {term3})(cn={term2} {term1} {term3}))");
    sf32.setParameter("term1", "daniel");
    sf32.setParameter("term2", "wayne");
    sf32.setParameter("term3", "fisher");
    sf32.setParameter("initial1", "d");
    sf32.setParameter("initial2", "w");
    sf32.setParameter("initial3", "f");

    final SearchFilter sf33 = new SearchFilter(
      "(|(&(givenName={term1}*)(sn={term3}*))(&(givenName={term2}*)(sn={term3}*)))");
    sf33.setParameter("term1", "daniel");
    sf33.setParameter("term2", "wayne");
    sf33.setParameter("term3", "fisher");
    sf33.setParameter("initial1", "d");
    sf33.setParameter("initial2", "w");
    sf33.setParameter("initial3", "f");

    final SearchFilter sf34 = new SearchFilter("(|(cn={term1}* {term2}* {term3}*)(cn={term2}* {term1}* {term3}*))");
    sf34.setParameter("term1", "daniel");
    sf34.setParameter("term2", "wayne");
    sf34.setParameter("term3", "fisher");
    sf34.setParameter("initial1", "d");
    sf34.setParameter("initial2", "w");
    sf34.setParameter("initial3", "f");

    final SearchFilter sf35 = new SearchFilter(
      "(|(&(givenName=*{term1}*)(sn=*{term3}*))(&(givenName=*{term2}*)(sn=*{term3}*)))");
    sf35.setParameter("term1", "daniel");
    sf35.setParameter("term2", "wayne");
    sf35.setParameter("term3", "fisher");
    sf35.setParameter("initial1", "d");
    sf35.setParameter("initial2", "w");
    sf35.setParameter("initial3", "f");

    final SearchFilter sf36 = new SearchFilter(
      "(|(cn=*{term1}* *{term2}* *{term3}*)(cn=*{term2}* *{term1}* *{term3}*))");
    sf36.setParameter("term1", "daniel");
    sf36.setParameter("term2", "wayne");
    sf36.setParameter("term3", "fisher");
    sf36.setParameter("initial1", "d");
    sf36.setParameter("initial2", "w");
    sf36.setParameter("initial3", "f");

    final SearchFilter sf37 = new SearchFilter(
      "(|(&(givenName={term1})(middleName={initial2}*)(sn={term3}))" +
        "(&(givenName={term2})(middleName={initial1}*)(sn={term3})))");
    sf37.setParameter("term1", "daniel");
    sf37.setParameter("term2", "wayne");
    sf37.setParameter("term3", "fisher");
    sf37.setParameter("initial1", "d");
    sf37.setParameter("initial2", "w");
    sf37.setParameter("initial3", "f");

    final SearchFilter sf38 = new SearchFilter(
      "(|(&p(givenName={initial1}*)(middlename={initial2}*)(sn={term3}))" +
        "(&(givenName={initial2}*)(middleName={initial1}*)(sn={term3})))");
    sf38.setParameter("term1", "daniel");
    sf38.setParameter("term2", "wayne");
    sf38.setParameter("term3", "fisher");
    sf38.setParameter("initial1", "d");
    sf38.setParameter("initial2", "w");
    sf38.setParameter("initial3", "f");

    final SearchFilter sf39 = new SearchFilter("(sn={term3})");
    sf39.setParameter("term1", "daniel");
    sf39.setParameter("term2", "wayne");
    sf39.setParameter("term3", "fisher");
    sf39.setParameter("initial1", "d");
    sf39.setParameter("initial2", "w");
    sf39.setParameter("initial3", "f");

    // test data for special characters
    final SearchFilter sf41 = new SearchFilter("(cn=*{term1}*)");
    sf41.setParameter("term1", "d\\\\i$h3\\*r");
    sf41.setParameter("initial1", "d");

    return
      new Object[][] {
        {
          new SearchTemplates(sf11.getFilter(), sf12.getFilter(), sf13.getFilter(), sf14.getFilter(), sf15.getFilter()),
          new Query("fisher"),
          new SearchFilter[] {sf11, sf12, sf13, sf14, sf15, },
        },
        {
          new SearchTemplates(
            sf21.getFilter(),
            sf22.getFilter(),
            sf23.getFilter(),
            sf24.getFilter(),
            sf25.getFilter(),
            sf26.getFilter(),
            sf27.getFilter(),
            sf28.getFilter()),
          new Query("daniel fisher"),
          new SearchFilter[] {sf21, sf22, sf23, sf24, sf25, sf26, sf27, sf28, },
        },
        {
          new SearchTemplates(
            sf31.getFilter(),
            sf32.getFilter(),
            sf33.getFilter(),
            sf34.getFilter(),
            sf35.getFilter(),
            sf36.getFilter(),
            sf37.getFilter(),
            sf38.getFilter(),
            sf39.getFilter()),
          new Query("daniel wayne fisher"),
          new SearchFilter[] {sf31, sf32, sf33, sf34, sf35, sf36, sf37, sf38, sf39, },
        },
        {
          new SearchTemplates(sf41.getFilter()),
          new Query("d\\\\i$h3\\*r"),
          new SearchFilter[] {sf41, },
        },
      };
  }


  /**
   * @param  templates  to format
   * @param  query  to apply to the template
   * @param  filters  to compare
   */
  @Test(groups = "templatestest", dataProvider = "template-data")
  public void format(final SearchTemplates templates, final Query query, final SearchFilter[] filters)
  {
    Assert.assertEquals(templates.format(query), filters);
  }
}
