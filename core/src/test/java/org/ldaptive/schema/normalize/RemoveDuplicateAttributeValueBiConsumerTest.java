/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema.normalize;

import java.util.List;
import org.ldaptive.LdapAttribute;
import org.ldaptive.schema.AttributeType;
import org.ldaptive.schema.Schema;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Unit test for {@link RemoveDuplicateAttributeValueBiConsumer}.
 *
 * @author  Middleware Services
 */
public class RemoveDuplicateAttributeValueBiConsumerTest
{

  /** Consumer to test. */
  private static final RemoveDuplicateAttributeValueBiConsumer CONSUMER = new RemoveDuplicateAttributeValueBiConsumer();

  /**
   * Consumer test data.
   *
   * @return  consumer test data
   */
  @DataProvider(name = "schemaAndAttributes")
  public Object[][] schemaAndAttributes()
  {
    final AttributeType c = new AttributeType("2.5.4.6");
    c.setNames("c");
    c.setEqualityMatchingRule("caseIgnoreMatch");
    c.setSyntaxOID("1.3.6.1.4.1.1466.115.121.1.15");
    c.setSingleValued(true);
    final AttributeType cn = new AttributeType("2.5.4.3");
    cn.setNames("cn");
    cn.setEqualityMatchingRule("caseIgnoreMatch");
    cn.setSyntaxOID("1.3.6.1.4.1.1466.115.121.1.15");
    cn.setSingleValued(false);
    final AttributeType givenName = new AttributeType("2.5.4.42");
    givenName.setNames("givenName");
    givenName.setEqualityMatchingRule("caseIgnoreMatch");
    givenName.setSyntaxOID("1.3.6.1.4.1.1466.115.121.1.15");
    givenName.setSingleValued(false);
    final AttributeType postalAddress = new AttributeType("2.5.4.16");
    postalAddress.setNames("postalAddress");
    postalAddress.setEqualityMatchingRule("caseIgnoreListMatch");
    postalAddress.setSyntaxOID("1.3.6.1.4.1.1466.115.121.1.41");
    postalAddress.setSingleValued(true);
    final AttributeType sn = new AttributeType("2.5.4.4");
    sn.setNames("sn");
    sn.setEqualityMatchingRule("caseIgnoreMatch");
    sn.setSyntaxOID("1.3.6.1.4.1.1466.115.121.1.15");
    sn.setSingleValued(false);
    final AttributeType title = new AttributeType("2.5.4.12");
    title.setNames("title");
    title.setEqualityMatchingRule("caseExactMatch");
    title.setSyntaxOID("1.3.6.1.4.1.1466.115.121.1.15");
    title.setSingleValued(false);

    final Schema schema = new Schema();
    schema.setAttributeTypes(List.of(c, cn, givenName, postalAddress, sn, title));
    schema.freeze();
    return
      new Object[][] {
        new Object[] {
          schema,
          LdapAttribute.builder().name("givenName").values("Bob", " bob ").build(),
          LdapAttribute.builder().name("givenName").values("Bob").build(),
        },
        new Object[] {
          schema,
          LdapAttribute.builder().name("sn").values(" Robinson", "Robinson", "Robinson  ").build(),
          LdapAttribute.builder().name("sn").values(" Robinson").build(),
        },
        new Object[] {
          schema,
          LdapAttribute.builder().name("title").values("Manager", " Manager ", " manager ").build(),
          LdapAttribute.builder().name("title").values("Manager", " manager ").build(),
        },
      };
  }


  @Test(dataProvider = "schemaAndAttributes")
  public void accept(final Schema schema, final LdapAttribute actual, final LdapAttribute expected)
  {
    CONSUMER.accept(schema, actual);
    assertThat(actual).isEqualTo(expected);
  }
}
