/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ext;

import java.util.concurrent.atomic.AtomicInteger;
import org.ldaptive.AbstractTest;
import org.ldaptive.AddOperation;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.DeleteOperation;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.ModifyOperation;
import org.ldaptive.ModifyRequest;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResponse;
import org.ldaptive.TestControl;
import org.ldaptive.handler.RequestHandler;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;
import static org.ldaptive.TestUtils.*;

/**
 * Unit test for {@link MergeOperation}.
 *
 * @author  Middleware Services
 */
public class MergeOperationTest extends AbstractTest
{

  /** Entry created for ldap tests. */
  private static LdapEntry testLdapEntry;


  /**
   * @param  ldifFile  to create.
   *
   * @throws  Exception  On test failure.
   */
  @Parameters("createEntry30")
  @BeforeClass(groups = "merge")
  public void createLdapEntry(final String ldifFile)
    throws Exception
  {
    final String ldif = readFileIntoString(ldifFile);
    testLdapEntry = convertLdifToResult(ldif).getEntry();

    final ConnectionFactory cf = createConnectionFactory();
    assertThat(super.entryExists(cf, testLdapEntry)).isFalse();

    final MergeOperation merge = new MergeOperation(cf);
    merge.execute(new MergeRequest(testLdapEntry));
    assertThat(super.entryExists(cf, testLdapEntry)).isTrue();
  }


  /** @throws  Exception  On test failure. */
  @AfterClass(groups = "merge")
  public void deleteLdapEntry()
    throws Exception
  {
    final ConnectionFactory cf = createConnectionFactory();
    assertThat(super.entryExists(cf, testLdapEntry)).isTrue();

    final MergeOperation merge = new MergeOperation(cf);
    merge.execute(new MergeRequest(testLdapEntry, true));
    assertThat(super.entryExists(cf, testLdapEntry)).isFalse();
    merge.execute(new MergeRequest(testLdapEntry, true));
  }


  /** @throws  Exception  On test failure. */
  @Test(groups = "merge")
  public void merge()
    throws Exception
  {
    final LdapEntry source = new LdapEntry();
    source.setDn(testLdapEntry.getDn());

    final ConnectionFactory cf = createConnectionFactory();
    final AtomicInteger modificationsCount = new AtomicInteger();
    final AtomicInteger addOperationCount = new AtomicInteger();
    final AtomicInteger deleteOperationCount = new AtomicInteger();
    final AtomicInteger modifyOperationCount = new AtomicInteger();
    final MergeOperation merge = new MergeOperation(cf);
    merge.setAddOperation(
      AddOperation.builder()
        .onResult(r -> addOperationCount.getAndIncrement())
        .build());
    merge.setDeleteOperation(
      DeleteOperation.builder()
        .onResult(r -> deleteOperationCount.getAndIncrement())
        .build());
    merge.setModifyOperation(
      ModifyOperation.builder()
        .onRequest((RequestHandler<ModifyRequest>) r -> modificationsCount.getAndAdd(r.getModifications().length))
        .onResult(r -> modifyOperationCount.getAndIncrement())
        .build());
    final MergeRequest request = new MergeRequest(source);
    if (TestControl.isActiveDirectory()) {
      // remove objectClass for comparison testing related to AD
      testLdapEntry.removeAttribute("objectClass");
      source.addAttributes(testLdapEntry.getAttributes());
      // these attributes are single value in AD
      source.addAttributes(new LdapAttribute("givenName", "John"));
      source.addAttributes(new LdapAttribute("initials", "JC"));
      request.setIncludeAttributes("uid");
    } else {
      source.addAttributes(testLdapEntry.getAttributes());

      final LdapAttribute gn = new LdapAttribute("givenName");
      gn.addStringValues(testLdapEntry.getAttribute("givenName").getStringValues());
      gn.addStringValues("John");
      source.addAttributes(gn);

      final LdapAttribute initials = new LdapAttribute("initials");
      initials.addStringValues(testLdapEntry.getAttribute("initials").getStringValues());
      initials.addStringValues("JC");
      source.addAttributes(initials);
      request.setExcludeAttributes("givenName", "initials");
    }
    // no-op, include/exclude should prevent a modify from occurring
    merge.execute(request);
    assertThat(addOperationCount.get()).isEqualTo(0);
    assertThat(deleteOperationCount.get()).isEqualTo(0);
    assertThat(modifyOperationCount.get()).isEqualTo(0);

    final SearchOperation search = new SearchOperation(cf);
    SearchResponse result = search.execute(
      SearchRequest.objectScopeSearchRequest(testLdapEntry.getDn(), testLdapEntry.getAttributeNames()));
    LdapEntryAssert.assertThat(result.getEntry()).isSame(testLdapEntry);

    if (TestControl.isActiveDirectory()) {
      request.setIncludeAttributes("givenName", "initials");
    } else {
      request.setExcludeAttributes((String[]) null);
    }
    // merge givenName and initials changes
    merge.execute(request);
    assertThat(addOperationCount.getAndSet(0)).isEqualTo(0);
    assertThat(deleteOperationCount.getAndSet(0)).isEqualTo(0);
    assertThat(modifyOperationCount.getAndSet(0)).isEqualTo(1);
    assertThat(modificationsCount.getAndSet(0)).isEqualTo(2);
    result = search.execute(SearchRequest.objectScopeSearchRequest(source.getDn(), source.getAttributeNames()));
    LdapEntryAssert.assertThat(result.getEntry()).isSame(source);

    // delete mail attribute
    final LdapAttribute mail = source.getAttribute("mail");
    source.removeAttributes(mail);
    merge.execute(request);
    assertThat(addOperationCount.getAndSet(0)).isEqualTo(0);
    assertThat(deleteOperationCount.getAndSet(0)).isEqualTo(0);
    assertThat(modifyOperationCount.getAndSet(0)).isEqualTo(1);
    assertThat(modificationsCount.getAndSet(0)).isEqualTo(1);
    result = search.execute(SearchRequest.objectScopeSearchRequest(source.getDn(), source.getAttributeNames()));
    LdapEntryAssert.assertThat(result.getEntry()).isSame(source);
    assertThat(result.getEntry().getAttribute("mail")).isNull();

    // add mail attribute
    source.addAttributes(mail);
    merge.execute(request);
    assertThat(addOperationCount.getAndSet(0)).isEqualTo(0);
    assertThat(deleteOperationCount.getAndSet(0)).isEqualTo(0);
    assertThat(modifyOperationCount.getAndSet(0)).isEqualTo(1);
    assertThat(modificationsCount.getAndSet(0)).isEqualTo(1);
    result = search.execute(SearchRequest.objectScopeSearchRequest(source.getDn(), source.getAttributeNames()));
    LdapEntryAssert.assertThat(result.getEntry()).isSame(source);
    assertThat(result.getEntry().getAttribute("mail")).isNotNull();

    // add new mail values using replace
    mail.addStringValues(
      "ccoolidge2@ldaptive.org", "ccoolidge3@ldaptive.org", "ccoolidge4@ldaptive.org", "ccoolidge5@ldaptive.org");
    merge.execute(request);
    assertThat(addOperationCount.getAndSet(0)).isEqualTo(0);
    assertThat(deleteOperationCount.getAndSet(0)).isEqualTo(0);
    assertThat(modifyOperationCount.getAndSet(0)).isEqualTo(1);
    assertThat(modificationsCount.getAndSet(0)).isEqualTo(1);
    result = search.execute(SearchRequest.objectScopeSearchRequest(source.getDn(), source.getAttributeNames()));
    LdapEntryAssert.assertThat(result.getEntry()).isSame(source);
    assertThat(result.getEntry().getAttribute("mail").size()).isEqualTo(5);

    // remove mail values using replace
    mail.removeStringValues("ccoolidge4@ldaptive.org", "ccoolidge5@ldaptive.org");
    merge.execute(request);
    assertThat(addOperationCount.getAndSet(0)).isEqualTo(0);
    assertThat(deleteOperationCount.getAndSet(0)).isEqualTo(0);
    assertThat(modifyOperationCount.getAndSet(0)).isEqualTo(1);
    assertThat(modificationsCount.getAndSet(0)).isEqualTo(1);
    result = search.execute(SearchRequest.objectScopeSearchRequest(source.getDn(), source.getAttributeNames()));
    LdapEntryAssert.assertThat(result.getEntry()).isSame(source);
    assertThat(result.getEntry().getAttribute("mail").size()).isEqualTo(3);

    // add new mail value using add
    request.setUseReplace(false);
    mail.clear();
    mail.addStringValues("ccoolidge@ldaptive.org");
    merge.execute(request);
    assertThat(addOperationCount.getAndSet(0)).isEqualTo(0);
    assertThat(deleteOperationCount.getAndSet(0)).isEqualTo(0);
    assertThat(modifyOperationCount.getAndSet(0)).isEqualTo(1);
    assertThat(modificationsCount.getAndSet(0)).isEqualTo(1);
    result = search.execute(SearchRequest.objectScopeSearchRequest(source.getDn(), source.getAttributeNames()));
    LdapEntryAssert.assertThat(result.getEntry()).isSame(source);
    assertThat(result.getEntry().getAttribute("mail").size()).isEqualTo(1);

    mail.addStringValues(
      "ccoolidge2@ldaptive.org", "ccoolidge3@ldaptive.org", "ccoolidge4@ldaptive.org", "ccoolidge5@ldaptive.org");
    merge.execute(request);
    assertThat(addOperationCount.getAndSet(0)).isEqualTo(0);
    assertThat(deleteOperationCount.getAndSet(0)).isEqualTo(0);
    assertThat(modifyOperationCount.getAndSet(0)).isEqualTo(1);
    assertThat(modificationsCount.getAndSet(0)).isEqualTo(1);
    result = search.execute(SearchRequest.objectScopeSearchRequest(source.getDn(), source.getAttributeNames()));
    LdapEntryAssert.assertThat(result.getEntry()).isSame(source);
    assertThat(result.getEntry().getAttribute("mail").size()).isEqualTo(5);

    // remove mail values using delete
    mail.removeStringValues("ccoolidge4@ldaptive.org", "ccoolidge5@ldaptive.org");
    merge.execute(request);
    assertThat(addOperationCount.getAndSet(0)).isEqualTo(0);
    assertThat(deleteOperationCount.getAndSet(0)).isEqualTo(0);
    assertThat(modifyOperationCount.getAndSet(0)).isEqualTo(1);
    assertThat(modificationsCount.getAndSet(0)).isEqualTo(1);
    result = search.execute(SearchRequest.objectScopeSearchRequest(source.getDn(), source.getAttributeNames()));
    LdapEntryAssert.assertThat(result.getEntry()).isSame(source);
    assertThat(result.getEntry().getAttribute("mail").size()).isEqualTo(3);

    // use batching
    request.setAttributeModificationsHandlers(
      new MergeRequest.MaxSizeAttributeValueHandler(2),
      new MergeRequest.BatchHandler(1));
    mail.addStringValues(
      "ccoolidge4@ldaptive.org", "ccoolidge5@ldaptive.org", "ccoolidge6@ldaptive.org", "ccoolidge7@ldaptive.org",
      "ccoolidge8@ldaptive.org", "ccoolidge9@ldaptive.org", "ccoolidge10@ldaptive.org", "ccoolidge11@ldaptive.org");
    merge.execute(request);
    assertThat(addOperationCount.getAndSet(0)).isEqualTo(0);
    assertThat(deleteOperationCount.getAndSet(0)).isEqualTo(0);
    assertThat(modifyOperationCount.getAndSet(0)).isEqualTo(4);
    assertThat(modificationsCount.getAndSet(0)).isEqualTo(4);
    result = search.execute(SearchRequest.objectScopeSearchRequest(source.getDn(), source.getAttributeNames()));
    LdapEntryAssert.assertThat(result.getEntry()).isSame(source);
    assertThat(result.getEntry().getAttribute("mail").size()).isEqualTo(11);

    request.setAttributeModificationsHandlers(new MergeRequest.MaxSizeAttributeValueHandler(2));
    mail.removeStringValues(
      "ccoolidge4@ldaptive.org", "ccoolidge5@ldaptive.org", "ccoolidge6@ldaptive.org", "ccoolidge7@ldaptive.org",
      "ccoolidge8@ldaptive.org", "ccoolidge9@ldaptive.org", "ccoolidge10@ldaptive.org", "ccoolidge11@ldaptive.org");
    merge.execute(request);
    assertThat(addOperationCount.getAndSet(0)).isEqualTo(0);
    assertThat(deleteOperationCount.getAndSet(0)).isEqualTo(0);
    assertThat(modifyOperationCount.getAndSet(0)).isEqualTo(1);
    assertThat(modificationsCount.getAndSet(0)).isEqualTo(4);
    result = search.execute(SearchRequest.objectScopeSearchRequest(source.getDn(), source.getAttributeNames()));
    LdapEntryAssert.assertThat(result.getEntry()).isSame(source);
    assertThat(result.getEntry().getAttribute("mail").size()).isEqualTo(3);

    request.setAttributeModificationsHandlers(new MergeRequest.BatchHandler(2));
    mail.addStringValues(
      "ccoolidge4@ldaptive.org", "ccoolidge5@ldaptive.org", "ccoolidge6@ldaptive.org", "ccoolidge7@ldaptive.org",
      "ccoolidge8@ldaptive.org", "ccoolidge9@ldaptive.org", "ccoolidge10@ldaptive.org", "ccoolidge11@ldaptive.org");
    merge.execute(request);
    assertThat(addOperationCount.getAndSet(0)).isEqualTo(0);
    assertThat(deleteOperationCount.getAndSet(0)).isEqualTo(0);
    assertThat(modifyOperationCount.getAndSet(0)).isEqualTo(1);
    assertThat(modificationsCount.getAndSet(0)).isEqualTo(1);
    result = search.execute(SearchRequest.objectScopeSearchRequest(source.getDn(), source.getAttributeNames()));
    LdapEntryAssert.assertThat(result.getEntry()).isSame(source);
    assertThat(result.getEntry().getAttribute("mail").size()).isEqualTo(11);

    request.setAttributeModificationsHandlers(
      new MergeRequest.MaxSizeAttributeValueHandler(2),
      new MergeRequest.BatchHandler(10));
    mail.removeStringValues(
      "ccoolidge4@ldaptive.org", "ccoolidge5@ldaptive.org", "ccoolidge6@ldaptive.org", "ccoolidge7@ldaptive.org",
      "ccoolidge8@ldaptive.org", "ccoolidge9@ldaptive.org", "ccoolidge10@ldaptive.org", "ccoolidge11@ldaptive.org");
    merge.execute(request);
    assertThat(addOperationCount.getAndSet(0)).isEqualTo(0);
    assertThat(deleteOperationCount.getAndSet(0)).isEqualTo(0);
    assertThat(modifyOperationCount.getAndSet(0)).isEqualTo(1);
    assertThat(modificationsCount.getAndSet(0)).isEqualTo(4);
    result = search.execute(SearchRequest.objectScopeSearchRequest(source.getDn(), source.getAttributeNames()));
    LdapEntryAssert.assertThat(result.getEntry()).isSame(source);
    assertThat(result.getEntry().getAttribute("mail").size()).isEqualTo(3);

    request.setAttributeModificationsHandlers(
      new MergeRequest.MaxSizeAttributeValueHandler(1),
      new MergeRequest.BatchHandler(10));
    mail.addStringValues(
      "ccoolidge4@ldaptive.org", "ccoolidge5@ldaptive.org", "ccoolidge6@ldaptive.org", "ccoolidge7@ldaptive.org",
      "ccoolidge8@ldaptive.org", "ccoolidge9@ldaptive.org", "ccoolidge10@ldaptive.org", "ccoolidge11@ldaptive.org");
    merge.execute(request);
    assertThat(addOperationCount.getAndSet(0)).isEqualTo(0);
    assertThat(deleteOperationCount.getAndSet(0)).isEqualTo(0);
    assertThat(modifyOperationCount.getAndSet(0)).isEqualTo(1);
    assertThat(modificationsCount.getAndSet(0)).isEqualTo(8);
    result = search.execute(SearchRequest.objectScopeSearchRequest(source.getDn(), source.getAttributeNames()));
    LdapEntryAssert.assertThat(result.getEntry()).isSame(source);
    assertThat(result.getEntry().getAttribute("mail").size()).isEqualTo(11);

    request.setAttributeModificationsHandlers(
      new MergeRequest.MaxSizeAttributeValueHandler(2),
      new MergeRequest.BatchHandler(2));
    mail.removeStringValues(
      "ccoolidge4@ldaptive.org", "ccoolidge5@ldaptive.org", "ccoolidge6@ldaptive.org", "ccoolidge7@ldaptive.org",
      "ccoolidge8@ldaptive.org", "ccoolidge9@ldaptive.org", "ccoolidge10@ldaptive.org", "ccoolidge11@ldaptive.org");
    merge.execute(request);
    assertThat(addOperationCount.getAndSet(0)).isEqualTo(0);
    assertThat(deleteOperationCount.getAndSet(0)).isEqualTo(0);
    assertThat(modifyOperationCount.getAndSet(0)).isEqualTo(2);
    assertThat(modificationsCount.getAndSet(0)).isEqualTo(4);
    result = search.execute(SearchRequest.objectScopeSearchRequest(source.getDn(), source.getAttributeNames()));
    LdapEntryAssert.assertThat(result.getEntry()).isSame(source);
    assertThat(result.getEntry().getAttribute("mail").size()).isEqualTo(3);
  }
}
