/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.dn;

// CheckStyle:AvoidStaticImport OFF
import java.util.List;
import java.util.Set;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import static org.assertj.core.api.Assertions.*;
import static org.ldaptive.dn.DefaultRDnNormalizer.COMPRESS;
import static org.ldaptive.dn.DefaultRDnNormalizer.LOWERCASE_COMPRESS;
import static org.ldaptive.dn.DefaultRDnNormalizer.LOWERCASE;
// CheckStyle:AvoidStaticImport ON

/**
 * Unit test for {@link RDn}.
 *
 * @author  Middleware Services
 */
public class RDnTest
{


  /**
   * DN test data.
   *
   * @return  test data
   */
  // CheckStyle:MethodLength OFF
  @DataProvider(name = "validRDNs")
  public Object[][] createRDNs()
  {
    final AttributeValueEscaper defaultEscaper = new DefaultAttributeValueEscaper();
    final AttributeValueEscaper minEscaper = new MinimalAttributeValueEscaper();
    return
      new Object[][] {
        new Object[] {
          "UID=jsmith",
          new RDn("UID", "jsmith"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "uid=jsmith",
        },
        new Object[] {
          "  UID = jsmith   ",
          new RDn("UID", "jsmith"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "uid=jsmith",
        },
        new Object[] {
          "  uid  =  john.smith  ",
          new RDn("uid", "john.smith"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "uid=john.smith",
        },
        new Object[] {
          "cn=John     Smith",
          new RDn("cn", "John     Smith"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
          },
          "cn=John     Smith",
        },
        new Object[] {
          "cn=John     Smith",
          new RDn("cn", "John     Smith"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
          },
          "cn=john     smith",
        },
        new Object[] {
          "cn=John     Smith",
          new RDn("cn", "John     Smith"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "cn=John Smith",
        },
        new Object[] {
          "cn=John     Smith",
          new RDn("cn", "John     Smith"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=john smith",
        },
        new Object[] {
          "CN=Jim Smith+UID=jsmith",
          new RDn(new NameValue("CN", "Jim Smith"), new NameValue("UID", "jsmith")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "cn=Jim Smith+uid=jsmith",
        },
        new Object[] {
          "CN=Jim Smith+UID=jsmith",
          new RDn(new NameValue("CN", "Jim Smith"), new NameValue("UID", "jsmith")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=jim smith+uid=jsmith",
        },
        new Object[] {
          "OU=Sales+CN=J.  Smith",
          new RDn(new NameValue("OU", "Sales"), new NameValue("CN", "J.  Smith")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
          },
          "cn=J.  Smith+ou=Sales",
        },
        new Object[] {
          "OU=Sales+CN=J.  Smith",
          new RDn(new NameValue("OU", "Sales"), new NameValue("CN", "J.  Smith")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
          },
          "cn=j.  smith+ou=sales",
        },
        new Object[] {
          "OU=Sales+CN=J.  Smith",
          new RDn(new NameValue("OU", "Sales"), new NameValue("CN", "J.  Smith")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "cn=J. Smith+ou=Sales",
        },
        new Object[] {
          "OU=Sales+CN=J.  Smith",
          new RDn(new NameValue("OU", "Sales"), new NameValue("CN", "J.  Smith")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=j. smith+ou=sales",
        },
        new Object[] {
          "CN=James \"Jim\" Smith\\, III",
          new RDn("CN", "James \"Jim\" Smith, III"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "cn=James \\\"Jim\\\" Smith\\, III",
        },
        new Object[] {
          "CN=James \"Jim\" Smith\\, III",
          new RDn("CN", "James \"Jim\" Smith, III"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=james \\\"jim\\\" smith\\, iii",
        },
        new Object[] {
          "OU=Sales\\; Data\\+Algorithms",
          new RDn("OU", "Sales; Data+Algorithms"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "ou=Sales\\; Data\\+Algorithms",
        },
        new Object[] {
          "OU=Sales\\; Data\\+Algorithms",
          new RDn("OU", "Sales; Data+Algorithms"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "ou=sales\\; data\\+algorithms",
        },
        new Object[] {
          "CN=\\23John Smith\\20",
          new RDn("CN", "#John Smith "),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "cn=\\#John Smith\\ ",
        },
        new Object[] {
          "CN=\\23John Smith\\20",
          new RDn("CN", "#John Smith "),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=\\#john smith\\ ",
        },
        new Object[] {
          "UID=john\\?smith",
          new RDn("UID", "john?smith"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "uid=john?smith",
        },
        new Object[] {
          "UID=john\\GGsmith",
          new RDn("UID", "johnGGsmith"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "uid=johnGGsmith",
        },
        new Object[] {
          "UID=john\\GGsmith",
          new RDn("UID", "johnGGsmith"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "uid=johnggsmith",
        },
        /* valid but cannot be used with DNTest
        new Object[] {
          "UID=john\\",
          new RDN("UID", "johnG"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(escaper, LOWERCASE_NORMALIZER, s -> s_NORMALIZER),
            new DefaultRDnNormalizer(escaper, LOWERCASE_NORMALIZER, COMPRESS_NORMALIZER),
            new DefaultRDnNormalizer(escaper, LOWERCASE_NORMALIZER, LOWERCASE_NORMALIZER),
            new DefaultRDnNormalizer(escaper, LOWERCASE_NORMALIZER, LOWERCASE_COMPRESS_NORMALIZER),
          },
          "uid=john",
        },
        */
        new Object[] {
          "CN=Lu\\C4\\8Di\\C4\\87",
          new RDn("CN", "Lučić"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
          },
          "cn=Lu\\C4\\8Di\\C4\\87",
        },
        new Object[] {
          "CN=Lu\\C4\\8Di\\C4\\87",
          new RDn("CN", "Lučić"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "cn=Lučić",
        },
        new Object[] {
          "CN=Lu\\C4\\8Di\\C4\\87",
          new RDn("CN", "Lučić"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=lu\\C4\\8Di\\C4\\87",
        },
        new Object[] {
          "CN=Lu\\C4\\8Di\\C4\\87",
          new RDn("CN", "Lučić"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=lučić",
        },
        new Object[] {
          "CN=Lu\\C4\\8D\\C4\\8Di\\C4\\87",
          new RDn("CN", "Luččić"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
          },
          "cn=Lu\\C4\\8D\\C4\\8Di\\C4\\87",
        },
        new Object[] {
          "CN=Lu\\C4\\8D\\C4\\8Di\\C4\\87",
          new RDn("CN", "Luččić"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "cn=Luččić",
        },
        new Object[] {
          "CN=Lu\\C4\\8D\\C4\\8Di\\C4\\87",
          new RDn("CN", "Luččić"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=lu\\C4\\8D\\C4\\8Di\\C4\\87",
        },
        new Object[] {
          "CN=Lu\\C4\\8D\\C4\\8Di\\C4\\87",
          new RDn("CN", "Luččić"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=luččić",
        },
        new Object[] {
          "CN=Lu\\C4\\8D\\C4\\8Di\\C4\\87o",
          new RDn("CN", "Luččićo"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
          },
          "cn=Lu\\C4\\8D\\C4\\8Di\\C4\\87o",
        },
        new Object[] {
          "CN=Lu\\C4\\8D\\C4\\8Di\\C4\\87o",
          new RDn("CN", "Luččićo"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "cn=Luččićo",
        },
        new Object[] {
          "CN=Lu\\C4\\8D\\C4\\8Di\\C4\\87o",
          new RDn("CN", "Luččićo"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=lu\\C4\\8D\\C4\\8Di\\C4\\87o",
        },
        new Object[] {
          "CN=Lu\\C4\\8D\\C4\\8Di\\C4\\87o",
          new RDn("CN", "Luččićo"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=luččićo",
        },
        new Object[] {
          "CN=\\C4\\87Lu\\C4\\8D\\C4\\8Di\\C4\\87o",
          new RDn("CN", "ćLuččićo"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
          },
          "cn=\\C4\\87Lu\\C4\\8D\\C4\\8Di\\C4\\87o",
        },
        new Object[] {
          "CN=\\C4\\87Lu\\C4\\8D\\C4\\8Di\\C4\\87o",
          new RDn("CN", "ćLuččićo"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "cn=ćLuččićo",
        },
        new Object[] {
          "CN=\\C4\\87Lu\\C4\\8D\\C4\\8Di\\C4\\87o",
          new RDn("CN", "ćLuččićo"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=\\C4\\87lu\\C4\\8D\\C4\\8Di\\C4\\87o",
        },
        new Object[] {
          "CN=\\C4\\87Lu\\C4\\8D\\C4\\8Di\\C4\\87o",
          new RDn("CN", "ćLuččićo"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=ćluččićo",
        },
        new Object[] {
          "1.3.6.1.4.1.1466.0=#04024869",
          new RDn("1.3.6.1.4.1.1466.0", "Hi"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "1.3.6.1.4.1.1466.0=Hi",
        },
        new Object[] {
          "1.3.6.1.4.1.1466.0=#04024869",
          new RDn("1.3.6.1.4.1.1466.0", "Hi"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "1.3.6.1.4.1.1466.0=hi",
        },
        new Object[] {
          "0.9.2342.19200300.100.1.1=#04066A736D697468",
          new RDn("0.9.2342.19200300.100.1.1", "jsmith"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "0.9.2342.19200300.100.1.1=jsmith",
        },
        new Object[] {
          "1.2.3.4.5.6=foo",
          new RDn("1.2.3.4.5.6", "foo"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "1.2.3.4.5.6=foo",
        },
        new Object[] {
          "givenName=John+sn=Smith",
          new RDn(new NameValue("givenName", "John"), new NameValue("sn", "Smith")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "givenname=John+sn=Smith",
        },
        new Object[] {
          "givenName=John+sn=Smith",
          new RDn(new NameValue("givenName", "John"), new NameValue("sn", "Smith")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "givenname=john+sn=smith",
        },
        new Object[] {
          " sn = Smith + givenName = John ",
          new RDn(new NameValue("givenName", "John"), new NameValue("sn", "Smith")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "givenname=John+sn=Smith",
        },
        new Object[] {
          " sn = Smith + givenName = John ",
          new RDn(new NameValue("givenName", "John"), new NameValue("sn", "Smith")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "givenname=john+sn=smith",
        },
        new Object[] {
          "cn=foo+cn=bar",
          new RDn(new NameValue("cn", "foo"), new NameValue("cn", "bar")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=foo+cn=bar",
        },
        new Object[] {
          "cn=bar+cn=foo",
          new RDn(new NameValue("cn", "bar"), new NameValue("cn", "foo")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=bar+cn=foo",
        },
        new Object[] {
          "cn=foo +cn = bar",
          new RDn(new NameValue("cn", "foo"), new NameValue("cn", "bar")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=foo+cn=bar",
        },
        new Object[] {
          "givenName=John+sn=Smith+cn=John Smith+description=employee",
          new RDn(
            new NameValue("givenName", "John"),
            new NameValue("sn", "Smith"),
            new NameValue("cn", "John Smith"),
            new NameValue("description", "employee")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "cn=John Smith+description=employee+givenname=John+sn=Smith",
        },
        new Object[] {
          "givenName=John+sn=Smith+cn=John Smith+description=employee",
          new RDn(
            new NameValue("givenName", "John"),
            new NameValue("sn", "Smith"),
            new NameValue("cn", "John Smith"),
            new NameValue("description", "employee")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=john smith+description=employee+givenname=john+sn=smith",
        },
        new Object[] {
          " givenName = John + sn = Smith + cn = John Smith + description = foo ",
          new RDn(
            new NameValue("givenName", "John"),
            new NameValue("sn", "Smith"),
            new NameValue("cn", "John Smith"),
            new NameValue("description", "foo")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "cn=John Smith+description=foo+givenname=John+sn=Smith",
        },
        new Object[] {
          " givenName = John + sn = Smith + cn = John Smith + description = foo ",
          new RDn(
            new NameValue("givenName", "John"),
            new NameValue("sn", "Smith"),
            new NameValue("cn", "John Smith"),
            new NameValue("description", "foo")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=john smith+description=foo+givenname=john+sn=smith",
        },
        new Object[] {
          "UID=#040a6a6f686e2e736d697468",
          new RDn("UID", "john.smith"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "uid=john.smith",
        },
        new Object[] {
          "uid=#040A6A6F686E2E736D697468",
          new RDn("uid", "john.smith"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "uid=john.smith",
        },
        new Object[] {
          "UID=#040A4A4F484E2E534D495448",
          new RDn("UID", "JOHN.SMITH"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "uid=JOHN.SMITH",
        },
        new Object[] {
          "UID=#040A4A4F484E2E534D495448",
          new RDn("UID", "JOHN.SMITH"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "uid=john.smith",
        },
        new Object[] {
          "givenName=#04046A6F686E+sn=#0405736D697468",
          new RDn(new NameValue("givenName", "john"), new NameValue("sn", "smith")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "givenname=john+sn=smith",
        },
        new Object[] {
          "  givenName  =  #04046A6F686E  +  sn  =  #0405736D697468  ",
          new RDn(new NameValue("givenName", "john"), new NameValue("sn", "smith")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "givenname=john+sn=smith",
        },
        new Object[] {
          "givenName=john+sn=#0405736D697468",
          new RDn(new NameValue("givenName", "john"), new NameValue("sn", "smith")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "givenname=john+sn=smith",
        },
        new Object[] {
          "givenName=#04046A6F686E+sn=smith",
          new RDn(new NameValue("givenName", "john"), new NameValue("sn", "smith")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "givenname=john+sn=smith",
        },
        new Object[] {
          "uid=\\6A\\6F\\68\\6E\\2E\\73\\6D\\69\\74\\68",
          new RDn("uid", "john.smith"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "uid=john.smith",
        },
        new Object[] {
          "uid=\\4A\\4F\\48\\4E\\2E\\53\\4D\\49\\54\\48",
          new RDn("uid", "JOHN.SMITH"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "uid=JOHN.SMITH",
        },
        new Object[] {
          "uid=\\4A\\4F\\48\\4E\\2E\\53\\4D\\49\\54\\48",
          new RDn("uid", "JOHN.SMITH"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "uid=john.smith",
        },
        new Object[] {
          "givenName=\\4A\\4F\\48\\4E+sn=\\53\\4D\\49\\54\\48",
          new RDn(new NameValue("givenName", "JOHN"), new NameValue("sn", "SMITH")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "givenname=JOHN+sn=SMITH",
        },
        new Object[] {
          "givenName=\\4A\\4F\\48\\4E+sn=\\53\\4D\\49\\54\\48",
          new RDn(new NameValue("givenName", "JOHN"), new NameValue("sn", "SMITH")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "givenname=john+sn=smith",
        },
        new Object[] {
          "uid=j\\4F\\48n.\\73m\\49\\54h",
          new RDn("uid", "jOHn.smITh"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "uid=jOHn.smITh",
        },
        new Object[] {
          "uid=j\\4F\\48n.\\73m\\49\\54h",
          new RDn("uid", "jOHn.smITh"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "uid=john.smith",
        },
        new Object[] {
          "uid=\\\\#\\=\\\"\\+\\,\\,\\;\\<\\>",
          new RDn("uid", "\\#=\"+,,;<>"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "uid=\\\\\\#\\=\\\"\\+\\,\\,\\;\\<\\>",
        },
        new Object[] {
          "uid=\"john.smith\"",
          new RDn("uid", "\"john.smith\""),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "uid=\\\"john.smith\\\"",
        },
        new Object[] {
          "givenName=\"John\"+sn=\"Smith\"",
          new RDn(new NameValue("givenName", "\"John\""), new NameValue("sn", "\"Smith\"")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "givenname=\\\"John\\\"+sn=\\\"Smith\\\"",
        },
        new Object[] {
          "givenName=\"John\"+sn=\"Smith\"",
          new RDn(new NameValue("givenName", "\"John\""), new NameValue("sn", "\"Smith\"")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "givenname=\\\"john\\\"+sn=\\\"smith\\\"",
        },
        new Object[] {
          "cn=\"Smith, John\"+givenName=John+sn=Smith",
          new RDn(
            new NameValue("cn", "\"Smith, John\""), new NameValue("givenName", "John"), new NameValue("sn", "Smith")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "cn=\\\"Smith\\, John\\\"+givenname=John+sn=Smith",
        },
        new Object[] {
          "cn=\"Smith, John\"+givenName=John+sn=Smith",
          new RDn(
            new NameValue("cn", "\"Smith, John\""), new NameValue("givenName", "John"), new NameValue("sn", "Smith")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=\\\"smith\\, john\\\"+givenname=john+sn=smith",
        },
        new Object[] {
          "uid=\"1+1=2\"",
          new RDn("uid", "\"1+1=2\""),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "uid=\\\"1\\+1\\=2\\\"",
        },
        new Object[] {
          "givenName=\"1+1=2\"+sn=\"2+2=4\"",
          new RDn(new NameValue("givenName", "\"1+1=2\""), new NameValue("sn", "\"2+2=4\"")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "givenname=\\\"1\\+1\\=2\\\"+sn=\\\"2\\+2\\=4\\\"",
        },
        new Object[] {
          "cn=Theodore \\\"Ted\\\" Logan\\, III",
          new RDn("cn", "Theodore \"Ted\" Logan, III"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "cn=Theodore \\\"Ted\\\" Logan\\, III",
        },
        new Object[] {
          "cn=Theodore \\\"Ted\\\" Logan\\, III",
          new RDn("cn", "Theodore \"Ted\" Logan, III"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=theodore \\\"ted\\\" logan\\, iii",
        },
        new Object[] {
          "cn=#048180000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f" +
            "303132333435363738393a3b3c3d3e3f404142434445464748494a4b4c4d4e4f505152535455565758595a5b5c5d5e5f60616263" +
            "6465666768696a6b6c6d6e6f707172737475767778797a7b7c7d7e7f",
          new RDn(
            "cn",
            new String(new byte[] {
              0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10,
              0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f, 0x20, 0x21,
              0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x2b, 0x2c, 0x2d, 0x2e, 0x2f, 0x30, 0x31, 0x32,
              0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x3b, 0x3c, 0x3d, 0x3e, 0x3f, 0x40, 0x41, 0x42, 0x43,
              0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e, 0x4f, 0x50, 0x51, 0x52, 0x53, 0x54,
              0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x5b, 0x5c, 0x5d, 0x5e, 0x5f, 0x60, 0x61, 0x62, 0x63, 0x64, 0x65,
              0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x6f, 0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76,
              0x77, 0x78, 0x79, 0x7a, 0x7b, 0x7c, 0x7d, 0x7e, 0x7f,
            })),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
          },
          "cn=\\00\\01\\02\\03\\04\\05\\06\\07\\08\\09\\0A\\0B\\0C\\0D\\0E\\0F\\10\\11\\12\\13\\14\\15\\16\\17\\18" +
            "\\19\\1A\\1B\\1C\\1D\\1E\\1F !\\\"\\#$%&'()*\\+\\,-./0123456789:\\;\\<\\=\\>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "[\\\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\\7F",
        },
        new Object[] {
          "cn=#048180000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f" +
            "303132333435363738393a3b3c3d3e3f404142434445464748494a4b4c4d4e4f505152535455565758595a5b5c5d5e5f60616263" +
            "6465666768696a6b6c6d6e6f707172737475767778797a7b7c7d7e7f",
          new RDn(
            "cn",
            new String(new byte[] {
              0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10,
              0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f, 0x20, 0x21,
              0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x2b, 0x2c, 0x2d, 0x2e, 0x2f, 0x30, 0x31, 0x32,
              0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x3b, 0x3c, 0x3d, 0x3e, 0x3f, 0x40, 0x41, 0x42, 0x43,
              0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e, 0x4f, 0x50, 0x51, 0x52, 0x53, 0x54,
              0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x5b, 0x5c, 0x5d, 0x5e, 0x5f, 0x60, 0x61, 0x62, 0x63, 0x64, 0x65,
              0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x6f, 0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76,
              0x77, 0x78, 0x79, 0x7a, 0x7b, 0x7c, 0x7d, 0x7e, 0x7f,
            })),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=\\00\\01\\02\\03\\04\\05\\06\\07\\08\\09\\0A\\0B\\0C\\0D\\0E\\0F\\10\\11\\12\\13\\14\\15\\16\\17\\18" +
            "\\19\\1A\\1B\\1C\\1D\\1E\\1F !\\\"\\#$%&'()*\\+\\,-./0123456789:\\;\\<\\=\\>?@abcdefghijklmnopqrstuvwxyz" +
            "[\\\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\\7F",
        },
        new Object[] {
          "cn=\\00\\01\\02\\03\\04\\05\\06\\07\\08\\09\\0a\\0b\\0c\\0d\\0e\\0f" +
            "\\10\\11\\12\\13\\14\\15\\16\\17\\18\\19\\1a\\1b\\1c\\1d\\1e\\1f" +
            "\\20\\21\\22\\23\\24\\25\\26\\27\\28\\29\\2a\\2b\\2c\\2d\\2e\\2f" +
            "\\30\\31\\32\\33\\34\\35\\36\\37\\38\\39\\3a\\3b\\3c\\3d\\3e\\3f" +
            "\\40\\41\\42\\43\\44\\45\\46\\47\\48\\49\\4a\\4b\\4c\\4d\\4e\\4f" +
            "\\50\\51\\52\\53\\54\\55\\56\\57\\58\\59\\5a\\5b\\5c\\5d\\5e\\5f" +
            "\\60\\61\\62\\63\\64\\65\\66\\67\\68\\69\\6a\\6b\\6c\\6d\\6e\\6f" +
            "\\70\\71\\72\\73\\74\\75\\76\\77\\78\\79\\7a\\7b\\7c\\7d\\7e\\7f",
          new RDn(
            "cn",
            new String(new byte[] {
              0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10,
              0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f, 0x20, 0x21,
              0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x2b, 0x2c, 0x2d, 0x2e, 0x2f, 0x30, 0x31, 0x32,
              0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x3b, 0x3c, 0x3d, 0x3e, 0x3f, 0x40, 0x41, 0x42, 0x43,
              0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e, 0x4f, 0x50, 0x51, 0x52, 0x53, 0x54,
              0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x5b, 0x5c, 0x5d, 0x5e, 0x5f, 0x60, 0x61, 0x62, 0x63, 0x64, 0x65,
              0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x6f, 0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76,
              0x77, 0x78, 0x79, 0x7a, 0x7b, 0x7c, 0x7d, 0x7e, 0x7f,
            })),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
          },
          "cn=\\00\\01\\02\\03\\04\\05\\06\\07\\08\\09\\0A\\0B\\0C\\0D\\0E\\0F\\10\\11\\12\\13\\14\\15\\16\\17\\18" +
            "\\19\\1A\\1B\\1C\\1D\\1E\\1F !\\\"\\#$%&'()*\\+\\,-./0123456789:\\;\\<\\=\\>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "[\\\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\\7F",
        },
        new Object[] {
          "cn=\\00\\01\\02\\03\\04\\05\\06\\07\\08\\09\\0a\\0b\\0c\\0d\\0e\\0f" +
            "\\10\\11\\12\\13\\14\\15\\16\\17\\18\\19\\1a\\1b\\1c\\1d\\1e\\1f" +
            "\\20\\21\\22\\23\\24\\25\\26\\27\\28\\29\\2a\\2b\\2c\\2d\\2e\\2f" +
            "\\30\\31\\32\\33\\34\\35\\36\\37\\38\\39\\3a\\3b\\3c\\3d\\3e\\3f" +
            "\\40\\41\\42\\43\\44\\45\\46\\47\\48\\49\\4a\\4b\\4c\\4d\\4e\\4f" +
            "\\50\\51\\52\\53\\54\\55\\56\\57\\58\\59\\5a\\5b\\5c\\5d\\5e\\5f" +
            "\\60\\61\\62\\63\\64\\65\\66\\67\\68\\69\\6a\\6b\\6c\\6d\\6e\\6f" +
            "\\70\\71\\72\\73\\74\\75\\76\\77\\78\\79\\7a\\7b\\7c\\7d\\7e\\7f",
          new RDn(
            "cn",
            new String(new byte[] {
              0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10,
              0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f, 0x20, 0x21,
              0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x2b, 0x2c, 0x2d, 0x2e, 0x2f, 0x30, 0x31, 0x32,
              0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x3b, 0x3c, 0x3d, 0x3e, 0x3f, 0x40, 0x41, 0x42, 0x43,
              0x44, 0x45, 0x46, 0x47, 0x48, 0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e, 0x4f, 0x50, 0x51, 0x52, 0x53, 0x54,
              0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x5b, 0x5c, 0x5d, 0x5e, 0x5f, 0x60, 0x61, 0x62, 0x63, 0x64, 0x65,
              0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x6f, 0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76,
              0x77, 0x78, 0x79, 0x7a, 0x7b, 0x7c, 0x7d, 0x7e, 0x7f,
            })),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=\\00\\01\\02\\03\\04\\05\\06\\07\\08\\09\\0A\\0B\\0C\\0D\\0E\\0F\\10\\11\\12\\13\\14\\15\\16\\17\\18" +
            "\\19\\1A\\1B\\1C\\1D\\1E\\1F !\\\"\\#$%&'()*\\+\\,-./0123456789:\\;\\<\\=\\>?@abcdefghijklmnopqrstuvwxyz" +
            "[\\\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\\7F",
        },
        new Object[] {
          "givenName=S\\c3\\b8rin\\c3\\a1+sn=No\\c3\\abl",
          new RDn(new NameValue("givenName", "Søriná"), new NameValue("sn", "Noël")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
          },
          "givenname=S\\C3\\B8rin\\C3\\A1+sn=No\\C3\\ABl",
        },
        new Object[] {
          "givenName=S\\c3\\b8rin\\c3\\a1+sn=No\\c3\\abl",
          new RDn(new NameValue("givenName", "Søriná"), new NameValue("sn", "Noël")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "givenname=Søriná+sn=Noël",
        },
        new Object[] {
          "givenName=S\\c3\\b8rin\\c3\\a1+sn=No\\c3\\abl",
          new RDn(new NameValue("givenName", "Søriná"), new NameValue("sn", "Noël")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "givenname=s\\C3\\B8rin\\C3\\A1+sn=no\\C3\\ABl",
        },
        new Object[] {
          "givenName=S\\c3\\b8rin\\c3\\a1+sn=No\\c3\\abl",
          new RDn(new NameValue("givenName", "Søriná"), new NameValue("sn", "Noël")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "givenname=søriná+sn=noël",
        },
        new Object[] {
          "givenName=S\\C3\\98RIN\\C3\\81+sn=NO\\C3\\8BL",
          new RDn(new NameValue("givenName", "SØRINÁ"), new NameValue("sn", "NOËL")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
          },
          "givenname=S\\C3\\98RIN\\C3\\81+sn=NO\\C3\\8BL",
        },
        new Object[] {
          "givenName=S\\C3\\98RIN\\C3\\81+sn=NO\\C3\\8BL",
          new RDn(new NameValue("givenName", "SØRINÁ"), new NameValue("sn", "NOËL")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "givenname=SØRINÁ+sn=NOËL",
        },
        new Object[] {
          "givenName=S\\C3\\98RIN\\C3\\81+sn=NO\\C3\\8BL",
          new RDn(new NameValue("givenName", "SØRINÁ"), new NameValue("sn", "NOËL")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "givenname=s\\C3\\B8rin\\C3\\A1+sn=no\\C3\\ABl",
        },
        new Object[] {
          "givenName=S\\C3\\98RIN\\C3\\81+sn=NO\\C3\\8BL",
          new RDn(new NameValue("givenName", "SØRINÁ"), new NameValue("sn", "NOËL")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "givenname=søriná+sn=noël",
        },
        new Object[] {
          "uid=",
          new RDn("uid", ""),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "uid=",
        },
        new Object[] {
          " uid = ",
          new RDn("uid", ""),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "uid=",
        },
        new Object[] {
          "uid=1+uid=2",
          new RDn(new NameValue("uid", "1"), new NameValue("uid", "2")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "uid=1+uid=2",
        },
        new Object[] {
          "uid=foo+sn=",
          new RDn(new NameValue("uid", "foo"), new NameValue("sn", "")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "sn=+uid=foo",
        },
        new Object[] {
          "uid=foo+sn=  ",
          new RDn(new NameValue("uid", "foo"), new NameValue("sn", "")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "sn=+uid=foo",
        },
        new Object[] {
          "uid=+sn=",
          new RDn(new NameValue("uid", ""), new NameValue("sn", "")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "sn=+uid=",
        },
        new Object[] {
          "uid=+sn=foo",
          new RDn(new NameValue("uid", ""), new NameValue("sn", "foo")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "sn=foo+uid=",
        },
        new Object[] {
          "cn=foo+sn=bar+givenName=",
          new RDn(new NameValue("cn", "foo"), new NameValue("sn", "bar"), new NameValue("givenName", "")),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=foo+givenname=+sn=bar",
        },
        new Object[] {
          "cn=Runic Letter PERTHO PEORTH \u16C8",
          new RDn("cn", "Runic Letter PERTHO PEORTH ᛈ"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
          },
          "cn=Runic Letter PERTHO PEORTH \\E1\\9B\\88",
        },
        new Object[] {
          "cn=Runic Letter PERTHO PEORTH \u16C8",
          new RDn("cn", "Runic Letter PERTHO PEORTH ᛈ"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "cn=Runic Letter PERTHO PEORTH ᛈ",
        },
        new Object[] {
          "cn=Runic Letter PERTHO PEORTH \u16C8",
          new RDn("cn", "Runic Letter PERTHO PEORTH ᛈ"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=runic letter pertho peorth \\E1\\9B\\88",
        },
        new Object[] {
          "cn=Runic Letter PERTHO PEORTH \u16C8",
          new RDn("cn", "Runic Letter PERTHO PEORTH ᛈ"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=runic letter pertho peorth ᛈ",
        },
        new Object[] {
          "cn=Cuneiform Sign UR4 \uD808\uDF34",
          new RDn("cn", "Cuneiform Sign UR4 \uD808\uDF34"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
          },
          "cn=Cuneiform Sign UR4 \\F0\\92\\8C\\B4",
        },
        new Object[] {
          "cn=Cuneiform Sign UR4 \uD808\uDF34",
          new RDn("cn", "Cuneiform Sign UR4 \uD808\uDF34"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "cn=Cuneiform Sign UR4 \uD808\uDF34",
        },
        new Object[] {
          "cn=Cuneiform Sign UR4 \uD808\uDF34",
          new RDn("cn", "Cuneiform Sign UR4 \uD808\uDF34"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=cuneiform sign ur4 \\F0\\92\\8C\\B4",
        },
        new Object[] {
          "cn=Cuneiform Sign UR4 \uD808\uDF34",
          new RDn("cn", "Cuneiform Sign UR4 \uD808\uDF34"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=cuneiform sign ur4 \uD808\uDF34",
        },
        new Object[] {
          "cn=Smiley Face \uD83D\uDE00",
          new RDn("cn", "Smiley Face \uD83D\uDE00"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
          },
          "cn=Smiley Face \\F0\\9F\\98\\80",
        },
        new Object[] {
          "cn=Smiley Face \uD83D\uDE00",
          new RDn("cn", "Smiley Face \uD83D\uDE00"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "cn=Smiley Face \uD83D\uDE00",
        },
        new Object[] {
          "cn=Smiley Face \uD83D\uDE00",
          new RDn("cn", "Smiley Face \uD83D\uDE00"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=smiley face \\F0\\9F\\98\\80",
        },
        new Object[] {
          "cn=Smiley Face \uD83D\uDE00",
          new RDn("cn", "Smiley Face \uD83D\uDE00"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=smiley face \uD83D\uDE00",
        },
        new Object[] {
          "cn=Oncoming Bus \uD83D\uDE8D",
          new RDn("cn", "Oncoming Bus \uD83D\uDE8D"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
          },
          "cn=Oncoming Bus \\F0\\9F\\9A\\8D",
        },
        new Object[] {
          "cn=Oncoming Bus \uD83D\uDE8D",
          new RDn("cn", "Oncoming Bus \uD83D\uDE8D"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "cn=Oncoming Bus \uD83D\uDE8D",
        },
        new Object[] {
          "cn=Oncoming Bus \uD83D\uDE8D",
          new RDn("cn", "Oncoming Bus \uD83D\uDE8D"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=oncoming bus \\F0\\9F\\9A\\8D",
        },
        new Object[] {
          "cn=Oncoming Bus \uD83D\uDE8D",
          new RDn("cn", "Oncoming Bus \uD83D\uDE8D"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=oncoming bus \uD83D\uDE8D",
        },
        new Object[] {
          "cn=Iceland Flag \uD83C\uDDEE\uD83C\uDDF8",
          new RDn("cn", "Iceland Flag \uD83C\uDDEE\uD83C\uDDF8"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
          },
          "cn=Iceland Flag \\F0\\9F\\87\\AE\\F0\\9F\\87\\B8",
        },
        new Object[] {
          "cn=Iceland Flag \uD83C\uDDEE\uD83C\uDDF8",
          new RDn("cn", "Iceland Flag \uD83C\uDDEE\uD83C\uDDF8"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "cn=Iceland Flag \uD83C\uDDEE\uD83C\uDDF8",
        },
        new Object[] {
          "cn=Iceland Flag \uD83C\uDDEE\uD83C\uDDF8",
          new RDn("cn", "Iceland Flag \uD83C\uDDEE\uD83C\uDDF8"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=iceland flag \\F0\\9F\\87\\AE\\F0\\9F\\87\\B8",
        },
        new Object[] {
          "cn=Iceland Flag \uD83C\uDDEE\uD83C\uDDF8",
          new RDn("cn", "Iceland Flag \uD83C\uDDEE\uD83C\uDDF8"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=iceland flag \uD83C\uDDEE\uD83C\uDDF8",
        },
        new Object[] {
          "cn=Pirate Flag \uD83C\uDFF4\u200D\u2620\uFE0F",
          new RDn("cn", "Pirate Flag \uD83C\uDFF4\u200D\u2620\uFE0F"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, COMPRESS),
          },
          "cn=Pirate Flag \\F0\\9F\\8F\\B4\\E2\\80\\8D\\E2\\98\\A0\\EF\\B8\\8F",
        },
        new Object[] {
          "cn=Pirate Flag \uD83C\uDFF4\u200D\u2620\uFE0F",
          new RDn("cn", "Pirate Flag \uD83C\uDFF4\u200D\u2620\uFE0F"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, s -> s),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, COMPRESS),
          },
          "cn=Pirate Flag \uD83C\uDFF4\u200D\u2620\uFE0F",
        },
        new Object[] {
          "cn=Pirate Flag \uD83C\uDFF4\u200D\u2620\uFE0F",
          new RDn("cn", "Pirate Flag \uD83C\uDFF4\u200D\u2620\uFE0F"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(defaultEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=pirate flag \\F0\\9F\\8F\\B4\\E2\\80\\8D\\E2\\98\\A0\\EF\\B8\\8F",
        },
        new Object[] {
          "cn=Pirate Flag \uD83C\uDFF4\u200D\u2620\uFE0F",
          new RDn("cn", "Pirate Flag \uD83C\uDFF4\u200D\u2620\uFE0F"),
          new RDnNormalizer[] {
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE),
            new DefaultRDnNormalizer(minEscaper, LOWERCASE, LOWERCASE_COMPRESS),
          },
          "cn=pirate flag \uD83C\uDFF4\u200D\u2620\uFE0F",
        },
      };
  }
  // CheckStyle:MethodLength ON

  /**
   * DN test data.
   *
   * @return  test data
   */
  @DataProvider(name = "invalidRDNs")
  public Object[][] createInvalidRDNs()
  {
    return
      new Object[][] {
        new Object[] {"", },
        /* invalid hex */
        new Object[] {"1.1.1=#GG", },
        new Object[] {"1.1.1=#000", },
        new Object[] {"1.1.1=#F", },
        new Object[] {"1.1.1=#", },
        new Object[] {"uid=#746q", },
        new Object[] {"uid=#00", },
        /* invalid escaped */
        new Object[] {"uid=\\74\\6", },
        new Object[] {"uid=\\74\\6q", },
        /* unescaped characters*/
        new Object[] {"UID=jsmith,", },
        new Object[] {"UID=john,smith", },
        new Object[] {"UID=john+smith", },
        new Object[] {"UID=john\\Fsmith", },
        /* missing attribute name */
        new Object[] {"=jsmith", },
        new Object[] {"UID:jsmith", },
        new Object[] {"uid=john+=smith", },
        new Object[] {"uid=+sn", },
        new Object[] {"cn=foo+sn=bar+", },
        new Object[] {"cn=foo+sn=bar+ ", },
        new Object[] {"cn=foo+sn=bar+givenName", },
        new Object[] {"cn=foo+sn=bar+givenName=+", },
        /* missing equals */
        new Object[] {"UID", },
        new Object[] {"not a valid RDN", },
        new Object[] {"cn=foo+sn=bar+givenName=,", },
        new Object[] {"uid=john.smith+", },
        new Object[] {"uid=john.smith,", },
      };
  }


  /**
   * @param  parse  RDN to parse
   * @param  match  to match against parsed RDN
   * @param  normalizers  to normalize DN
   * @param  normalized  string to match
   */
  @Test(dataProvider = "validRDNs")
  public void testParsing(
    final String parse,
    final RDn match,
    final RDnNormalizer[] normalizers,
    final String normalized)
  {
    final RDn rdn = new RDn(parse);
    assertThat(rdn).isEqualTo(match);
    for (RDnNormalizer normalizer : normalizers) {
      assertThat(rdn.format(normalizer)).withFailMessage("Normalizer: %s", normalizer).isEqualTo(normalized);
    }
  }


  /**
   * @param  parse  RDN to parse
   */
  @Test(dataProvider = "invalidRDNs")
  public void testInvalidParsing(final String parse)
  {
    try {
      new RDn(parse);
      fail("Should have thrown IllegalArgumentException for %s", parse);
    } catch (Exception e) {
      assertThat(e).isExactlyInstanceOf(IllegalArgumentException.class);
    }
  }


  @Test
  public void testGetNameValue()
  {
    assertThat(new RDn("gn=foo").getNameValue())
      .isEqualTo(new NameValue("gn", "foo"));
    assertThat(new RDn(new NameValue("gn", "foo")).getNameValue())
      .isEqualTo(new NameValue("gn", "foo"));
    assertThat(new RDn(new NameValue("gn", "foo"), new NameValue("sn", "bar")).getNameValue())
      .isEqualTo(new NameValue("gn", "foo"));
    assertThat(new RDn("gn=foo+sn=bar").getNameValue())
      .isEqualTo(new NameValue("gn", "foo"));
  }


  @Test
  public void testGetNameValues()
  {
    assertThat(new RDn("gn=foo").getNameValues()).isEqualTo(
      Set.of(new NameValue("gn", "foo")));
    assertThat(new RDn(new NameValue("gn", "foo")).getNameValues()).isEqualTo(
      Set.of(new NameValue("gn", "foo")));
    assertThat(new RDn(new NameValue("gn", "foo"), new NameValue("sn", "bar")).getNameValues()).isEqualTo(
      Set.of(new NameValue("gn", "foo"), new NameValue("sn", "bar")));
    assertThat(new RDn("gn=foo+sn=bar").getNameValues()).isEqualTo(
      Set.of(new NameValue("gn", "foo"), new NameValue("sn", "bar")));
  }


  @Test
  public void testGetNames()
  {
    assertThat(new RDn("gn=foo").getNames()).isEqualTo(List.of("gn"));
    assertThat(new RDn("UID=bar+CN=foo").getNames()).isEqualTo(List.of("UID", "CN"));
  }
}
