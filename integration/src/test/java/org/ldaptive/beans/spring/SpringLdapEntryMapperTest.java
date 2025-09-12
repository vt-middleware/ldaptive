/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.spring;

import org.ldaptive.AbstractTest;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapEntry;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.TestControl;
import org.ldaptive.beans.LdapEntryMapper;
import org.ldaptive.beans.generate.InetOrgPerson;
import org.ldaptive.beans.generate.OrganizationalPerson;
import org.ldaptive.transcode.GeneralizedTimeValueTranscoder;
import org.ldaptive.transcode.UUIDValueTranscoder;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;
import static org.ldaptive.TestUtils.*;

/**
 * Unit test for {@link SpringLdapEntryMapper} implementations.
 *
 * @author  Middleware Services
 */
public class SpringLdapEntryMapperTest extends AbstractTest
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
    final DefaultConnectionFactory cf = (DefaultConnectionFactory) createSetupConnectionFactory();
    final BindConnectionInitializer ci =
      (BindConnectionInitializer) cf.getConnectionConfig().getConnectionInitializers()[0];
    final SearchOperation op = new SearchOperation(cf);
    final SearchRequest request = SearchRequest.objectScopeSearchRequest(ci.getBindDn());
    request.setReturnAttributes(ReturnAttributes.ALL.value());
    final LdapEntry entry = op.execute(request).getEntry();

    try {
      return
        new Object[][] {
          new Object[] {new SpringLdapEntryMapper<>(), entry},
        };
    } catch (NoClassDefFoundError e) {
      // ignore this test if running under java < 17
      assertThat(e).isNotNull();
    }
    return new Object[][] {};
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
      assertThat(person.getCn()).isEqualTo(entry.getAttribute("cn").getStringValue());
      assertThat(person.getDn()).isEqualTo(entry.getDn());
      assertThat(person.getCountryCode()).isEqualTo(entry.getAttribute("countryCode").getStringValue());
    } else {
      final InetOrgPerson person = new InetOrgPerson();
      mapper.map(entry, person);
      assertThat(person.getCn().iterator().next())
        .isEqualTo(entry.getAttribute("cn").getStringValues().iterator().next());
      assertThat(person.getCreateTimestamp())
        .isEqualTo(entry.getAttribute("createTimestamp").getValue((new GeneralizedTimeValueTranscoder()).decoder()));
      assertThat(person.getDn()).isEqualTo(entry.getDn());
      assertThat(person.getEntryUUID())
        .isEqualTo(entry.getAttribute("entryUUID").getValue(new UUIDValueTranscoder().decoder()));
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
      assertThat(person.getCn()).isEqualTo(mapped.getAttribute("cn").getStringValue());
      assertThat(person.getDn()).isEqualTo(mapped.getDn());
      assertThat(person.getCountryCode()).isEqualTo(mapped.getAttribute("countryCode").getStringValue());
      assertThat("customvalue1").isEqualTo(mapped.getAttribute("customname1").getStringValue());
    } else {
      final InetOrgPerson person = new InetOrgPerson();
      person.setCn(entry.getAttribute("cn").getStringValues());
      person.setCreateTimestamp(
        entry.getAttribute("createTimestamp").getValue((new GeneralizedTimeValueTranscoder()).decoder()));
      person.setDn(entry.getDn());
      person.setEntryUUID(entry.getAttribute("entryUUID").getValue((new UUIDValueTranscoder()).decoder()));
      mapper.map(person, mapped);
      assertThat(person.getCn().iterator().next())
        .isEqualTo(mapped.getAttribute("cn").getStringValues().iterator().next());
      assertThat(person.getCreateTimestamp())
        .isEqualTo(mapped.getAttribute("createTimestamp").getValue((new GeneralizedTimeValueTranscoder()).decoder()));
      assertThat(person.getDn()).isEqualTo(mapped.getDn());
      assertThat(person.getEntryUUID())
        .isEqualTo(mapped.getAttribute("entryUUID").getValue((new UUIDValueTranscoder()).decoder()));
      assertThat("customvalue1").isEqualTo(mapped.getAttribute("customname1").getStringValue());
    }
  }
}
