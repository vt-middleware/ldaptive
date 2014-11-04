/*
  $Id: LdapEntryMapperTest.java 2931 2014-03-26 14:10:52Z dfisher $

  Copyright (C) 2003-2010 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 2931 $
  Updated: $Date: 2014-03-26 10:10:52 -0400 (Wed, 26 Mar 2014) $
*/
package org.ldaptive.beans;

import org.ldaptive.AbstractTest;
import org.ldaptive.BindConnectionInitializer;
import org.ldaptive.Connection;
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
import org.ldaptive.io.GeneralizedTimeValueTranscoder;
import org.ldaptive.io.UUIDValueTranscoder;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link LdapEntryMapper} implementations.
 *
 * @author  Middleware Services
 * @version  $Revision: 2931 $ $Date: 2014-03-26 10:10:52 -0400 (Wed, 26 Mar 2014) $
 */
public class LdapEntryMapperTest extends AbstractTest
{


  /**
   * Mappers to test.
   *
   * @return  ldap entry mappers
   */
  @DataProvider(name = "mappers")
  public Object[][] createMappers()
    throws Exception
  {
    LdapEntry entry = null;
    final Connection conn = TestUtils.createSetupConnection();
    try {
      conn.open();
      final BindConnectionInitializer ci =
        (BindConnectionInitializer)
          conn.getConnectionConfig().getConnectionInitializer();
      final SearchOperation op = new SearchOperation(conn);
      final SearchRequest request = SearchRequest.newObjectScopeSearchRequest(
        ci.getBindDn());
      request.setReturnAttributes(ReturnAttributes.ALL.value());
      entry = op.execute(request).getResult().getEntry();
    } finally {
      conn.close();
    }

    return new Object[][] {
      new Object[] {new DefaultLdapEntryMapper(), entry},
      new Object[] {new SpringLdapEntryMapper(), entry},
    };
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"beans-mapper"}, dataProvider = "mappers")
  public void mapToObject(final LdapEntryMapper<Object> mapper, final LdapEntry entry)
    throws Exception
  {
    if (TestControl.isActiveDirectory()) {
      final OrganizationalPerson person = new OrganizationalPerson();
      mapper.map(entry, person);
      AssertJUnit.assertEquals(
        entry.getAttribute("cn").getStringValue(),
        person.getCn());
      AssertJUnit.assertEquals(entry.getDn(), person.getDn());
      AssertJUnit.assertEquals(
        entry.getAttribute("countryCode").getStringValue(),
        person.getCountryCode());
    } else {
      final InetOrgPerson person = new InetOrgPerson();
      mapper.map(entry, person);
      AssertJUnit.assertEquals(
        entry.getAttribute("cn").getStringValues().iterator().next(),
        person.getCn().iterator().next());
      AssertJUnit.assertEquals(
        entry.getAttribute(
          "createTimestamp").getValue(new GeneralizedTimeValueTranscoder()),
        person.getCreateTimestamp());
      AssertJUnit.assertEquals(entry.getDn(), person.getDn());
      AssertJUnit.assertEquals(
        entry.getAttribute("entryUUID").getValue(new UUIDValueTranscoder()),
        person.getEntryUUID());
    }
  }


  /**
   * @throws  Exception  On test failure.
   */
  @Test(groups = {"beans-mapper"}, dataProvider = "mappers")
  public void mapToLdapEntry(
    final LdapEntryMapper<Object> mapper,
    final LdapEntry entry)
    throws Exception
  {
    final LdapEntry mapped = new LdapEntry();
    if (TestControl.isActiveDirectory()) {
      final OrganizationalPerson person = new OrganizationalPerson();
      person.setCn(entry.getAttribute("cn").getStringValue());
      person.setDn(entry.getDn());
      person.setCountryCode(entry.getAttribute("countryCode").getStringValue());
      mapper.map(person, mapped);
      AssertJUnit.assertEquals(
        person.getCn(),
        mapped.getAttribute("cn").getStringValue());
      AssertJUnit.assertEquals(person.getDn(), mapped.getDn());
      AssertJUnit.assertEquals(
        person.getCountryCode(),
        mapped.getAttribute("countryCode").getStringValue());
      AssertJUnit.assertEquals(
        "customvalue1",
        mapped.getAttribute("customname1").getStringValue());
    } else {
      final InetOrgPerson person = new InetOrgPerson();
      person.setCn(entry.getAttribute("cn").getStringValues());
      person.setCreateTimestamp(entry.getAttribute("createTimestamp").getValue(
        new GeneralizedTimeValueTranscoder()));
      person.setDn(entry.getDn());
      person.setEntryUUID(
        entry.getAttribute("entryUUID").getValue(new UUIDValueTranscoder()));
      mapper.map(person, mapped);
      AssertJUnit.assertEquals(
        person.getCn().iterator().next(),
        mapped.getAttribute("cn").getStringValues().iterator().next());
      AssertJUnit.assertEquals(
        person.getCreateTimestamp(),
        mapped.getAttribute(
          "createTimestamp").getValue(new GeneralizedTimeValueTranscoder()));
      AssertJUnit.assertEquals(person.getDn(), mapped.getDn());
      AssertJUnit.assertEquals(
        person.getEntryUUID(),
        mapped.getAttribute("entryUUID").getValue(new UUIDValueTranscoder()));
      AssertJUnit.assertEquals(
        "customvalue1",
        mapped.getAttribute("customname1").getStringValue());
    }
  }
}
