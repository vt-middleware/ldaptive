/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.beans.persistence;

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
import org.ldaptive.beans.reflect.DefaultLdapEntryMapper;
import org.ldaptive.transcode.GeneralizedTimeValueTranscoder;
import org.ldaptive.transcode.UUIDValueTranscoder;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link LdapEntryManager} implementations.
 *
 * @author  Middleware Services
 */
public class LdapEntryManagerTest extends AbstractTest
{


  /**
   * Managers to test.
   *
   * @return  ldap entry managers
   *
   * @throws  Exception  On test failure.
   */
  @DataProvider(name = "managers")
  public Object[][] createManagers()
    throws Exception
  {
    final DefaultConnectionFactory cf = new DefaultConnectionFactory(
      TestUtils.readConnectionConfig("classpath:/org/ldaptive/ldap.setup.properties"));
    final BindConnectionInitializer ci =
      (BindConnectionInitializer) cf.getConnectionConfig().getConnectionInitializers()[0];
    final SearchOperation op = new SearchOperation(cf);
    final SearchRequest request = SearchRequest.objectScopeSearchRequest(ci.getBindDn());
    request.setReturnAttributes(ReturnAttributes.ALL.value());
    final LdapEntry entry = op.execute(request).getEntry();

    return
      new Object[][] {
        new Object[] {
          new DefaultLdapEntryManager<>(new DefaultLdapEntryMapper<>(), cf), entry,
        },
      };
  }


  /**
   * @param  manager  to test
   * @param  entry  to compare
   *
   * @throws  Exception  On test failure.
   */
  @Test(groups = "beans-manager", dataProvider = "managers")
  public void find(final LdapEntryManager<InetOrgPerson> manager, final LdapEntry entry)
    throws Exception
  {
    if (TestControl.isActiveDirectory()) {
      return;
    } else {
      final InetOrgPerson emptyPerson = new InetOrgPerson();
      emptyPerson.setDn(entry.getDn());
      final InetOrgPerson person = manager.find(emptyPerson);
      AssertJUnit.assertEquals(
        entry.getAttribute("cn").getStringValues().iterator().next(),
        person.getCn().iterator().next());
      AssertJUnit.assertEquals(
        entry.getAttribute("createTimestamp").getValue((new GeneralizedTimeValueTranscoder()).decoder()),
        person.getCreateTimestamp());
      AssertJUnit.assertEquals(entry.getDn(), person.getDn());
      AssertJUnit.assertEquals(
        entry.getAttribute("entryUUID").getValue((new UUIDValueTranscoder()).decoder()),
        person.getEntryUUID());
    }
  }
}
