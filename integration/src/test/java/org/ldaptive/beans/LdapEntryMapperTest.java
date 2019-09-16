/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans;

import org.ldaptive.AbstractTest;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapEntry;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.TestControl;
import org.ldaptive.TestUtils;
import org.ldaptive.beans.generate.InetOrgPerson;
import org.ldaptive.beans.generate.OrganizationalPerson;
import org.ldaptive.beans.reflect.DefaultLdapEntryMapper;
import org.ldaptive.beans.spring.SpringLdapEntryMapper;
import org.ldaptive.transcode.GeneralizedTimeValueTranscoder;
import org.ldaptive.transcode.UUIDValueTranscoder;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link LdapEntryMapper} implementations.
 *
 * @author  Middleware Services
 */
public class LdapEntryMapperTest extends AbstractTest
{


  /**
   * Mappers to test.
   *
   * @return  ldap entry mappers
   *
   * @throws  Exception  On test failure.
   */
  @DataProvider(name = "mappers")
  public Object[][] createMappers()
    throws Exception
  {
    final DefaultConnectionFactory cf = (DefaultConnectionFactory) TestUtils.createSetupConnectionFactory();
    final BindConnectionInitializer ci =
      (BindConnectionInitializer) cf.getConnectionConfig().getConnectionInitializers()[0];
    final SearchOperation op = new SearchOperation(cf);
    final SearchRequest request = SearchRequest.objectScopeSearchRequest(ci.getBindDn());
    request.setReturnAttributes(ReturnAttributes.ALL.value());
    final LdapEntry entry = op.execute(request).getEntry();

    return
      new Object[][] {
        new Object[] {new DefaultLdapEntryMapper(), entry},
        new Object[] {new SpringLdapEntryMapper(), entry},
      };
  }


  /**
   * @param  mapper  to test
   * @param  entry  to compare
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "beans-mapper", dataProvider = "mappers")
  public void mapToObject(final LdapEntryMapper<Object> mapper, final LdapEntry entry)
    throws Exception
  {
    if (TestControl.isActiveDirectory()) {
      final OrganizationalPerson person = new OrganizationalPerson();
      mapper.map(entry, person);
      AssertJUnit.assertEquals(entry.getAttribute("cn").getStringValue(), person.getCn());
      AssertJUnit.assertEquals(entry.getDn(), person.getDn());
      AssertJUnit.assertEquals(entry.getAttribute("countryCode").getStringValue(), person.getCountryCode());
    } else {
      final InetOrgPerson person = new InetOrgPerson();
      mapper.map(entry, person);
      AssertJUnit.assertEquals(
        entry.getAttribute("cn").getStringValues().iterator().next(),
        person.getCn().iterator().next());
      AssertJUnit.assertEquals(
        entry.getAttribute("createTimestamp").getValue((new GeneralizedTimeValueTranscoder()).decoder()),
        person.getCreateTimestamp());
      AssertJUnit.assertEquals(entry.getDn(), person.getDn());
      AssertJUnit.assertEquals(
        entry.getAttribute("entryUUID").getValue(new UUIDValueTranscoder().decoder()),
        person.getEntryUUID());
    }
  }


  /**
   * @param  mapper  to test
   * @param  entry  to compare
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "beans-mapper", dataProvider = "mappers")
  public void mapToLdapEntry(final LdapEntryMapper<Object> mapper, final LdapEntry entry)
    throws Exception
  {
    final LdapEntry mapped = new LdapEntry();
    if (TestControl.isActiveDirectory()) {
      final OrganizationalPerson person = new OrganizationalPerson();
      person.setCn(entry.getAttribute("cn").getStringValue());
      person.setDn(entry.getDn());
      person.setCountryCode(entry.getAttribute("countryCode").getStringValue());
      mapper.map(person, mapped);
      AssertJUnit.assertEquals(person.getCn(), mapped.getAttribute("cn").getStringValue());
      AssertJUnit.assertEquals(person.getDn(), mapped.getDn());
      AssertJUnit.assertEquals(person.getCountryCode(), mapped.getAttribute("countryCode").getStringValue());
      AssertJUnit.assertEquals("customvalue1", mapped.getAttribute("customname1").getStringValue());
    } else {
      final InetOrgPerson person = new InetOrgPerson();
      person.setCn(entry.getAttribute("cn").getStringValues());
      person.setCreateTimestamp(
        entry.getAttribute("createTimestamp").getValue((new GeneralizedTimeValueTranscoder()).decoder()));
      person.setDn(entry.getDn());
      person.setEntryUUID(entry.getAttribute("entryUUID").getValue((new UUIDValueTranscoder()).decoder()));
      mapper.map(person, mapped);
      AssertJUnit.assertEquals(
        person.getCn().iterator().next(),
        mapped.getAttribute("cn").getStringValues().iterator().next());
      AssertJUnit.assertEquals(
        person.getCreateTimestamp(),
        mapped.getAttribute("createTimestamp").getValue((new GeneralizedTimeValueTranscoder()).decoder()));
      AssertJUnit.assertEquals(person.getDn(), mapped.getDn());
      AssertJUnit.assertEquals(
        person.getEntryUUID(),
        mapped.getAttribute("entryUUID").getValue((new UUIDValueTranscoder()).decoder()));
      AssertJUnit.assertEquals("customvalue1", mapped.getAttribute("customname1").getStringValue());
    }
  }
}
