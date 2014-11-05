/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

import java.nio.ByteBuffer;
import org.ldaptive.LdapUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit test for {@link DN}.
 *
 * @author  Middleware Services
 */
public class DNTest
{


  /**
   * DN test data.
   *
   * @return  test data
   *
   * @throws  Exception  On test failure.
   */
  // CheckStyle:MethodLength OFF
  @DataProvider(name = "dns")
  public Object[][] createData()
    throws Exception
  {
    return
      new Object[][] {
        // CN=www.ldaptive.org, DC=vt, DC=edu
        // BER 30:4A:31:19:30:17:06:03:55:04:03:13:10:77:77:77:2E:6C:64:61:70:74
        //     69:76:65:2E:6F:72:67:31:18:30:16:06:0A:09:92:26:89:93:F2:2C:64:01
        //     19:16:08:6C:64:61:70:74:69:76:65:31:13:30:11:06:0A:09:92:26:89:93
        //     F2:2C:64:01:19:16:03:6F:72:67:
        new Object[] {
          new byte[] {
            (byte) 0x30, (byte) 0x4A, (byte) 0x31, (byte) 0x19, (byte) 0x30,
            (byte) 0x17, (byte) 0x06, (byte) 0x03, (byte) 0x55, (byte) 0x04,
            (byte) 0x03, (byte) 0x13, (byte) 0x10, (byte) 0x77, (byte) 0x77,
            (byte) 0x77, (byte) 0x2E, (byte) 0x6C, (byte) 0x64, (byte) 0x61,
            (byte) 0x70, (byte) 0x74, (byte) 0x69, (byte) 0x76, (byte) 0x65,
            (byte) 0x2E, (byte) 0x6F, (byte) 0x72, (byte) 0x67, (byte) 0x31,
            (byte) 0x18, (byte) 0x30, (byte) 0x16, (byte) 0x06, (byte) 0x0A,
            (byte) 0x09, (byte) 0x92, (byte) 0x26, (byte) 0x89, (byte) 0x93,
            (byte) 0xF2, (byte) 0x2C, (byte) 0x64, (byte) 0x01, (byte) 0x19,
            (byte) 0x16, (byte) 0x08, (byte) 0x6C, (byte) 0x64, (byte) 0x61,
            (byte) 0x70, (byte) 0x74, (byte) 0x69, (byte) 0x76, (byte) 0x65,
            (byte) 0x31, (byte) 0x13, (byte) 0x30, (byte) 0x11, (byte) 0x06,
            (byte) 0x0A, (byte) 0x09, (byte) 0x92, (byte) 0x26, (byte) 0x89,
            (byte) 0x93, (byte) 0xF2, (byte) 0x2C, (byte) 0x64, (byte) 0x01,
            (byte) 0x19, (byte) 0x16, (byte) 0x03, (byte) 0x6F, (byte) 0x72,
            (byte) 0x67,
          },
          new DN(
            new RDN(
              new AttributeValueAssertion(
                "2.5.4.3",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.PRINTSTR,
                  "www.ldaptive.org".getBytes("UTF-8")))),
            new RDN(
              new AttributeValueAssertion(
                "0.9.2342.19200300.100.1.25",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.IA5STR,
                  "ldaptive".getBytes("UTF-8")))),
            new RDN(
              new AttributeValueAssertion(
                "0.9.2342.19200300.100.1.25",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.IA5STR,
                  "org".getBytes("UTF-8"))))),
        },
        // DC=org, DC=ldaptive, UID=7 + CN=www.ldaptive.org
        // BER 30:5B:31:2A:30:0F:06:0A:09:92:26:89:93:F2:2C:64:01:01:13:01:37:30
        //     17:06:03:55:04:03:13:10:77:77:77:2E:6C:64:61:70:74:69:76:65:2E:6F
        //     72:67:31:18:30:16:06:0A:09:92:26:89:93:F2:2C:64:01:19:16:08:6C:64
        //     61:70:74:69:76:65:31:13:30:11:06:0A:09:92:26:89:93:F2:2C:64:01:19
        //     16:03:6F:72:67:
        new Object[] {
          new byte[] {
            (byte) 0x30, (byte) 0x5B, (byte) 0x31, (byte) 0x2A, (byte) 0x30,
            (byte) 0x0F, (byte) 0x06, (byte) 0x0A, (byte) 0x09, (byte) 0x92,
            (byte) 0x26, (byte) 0x89, (byte) 0x93, (byte) 0xF2, (byte) 0x2C,
            (byte) 0x64, (byte) 0x01, (byte) 0x01, (byte) 0x13, (byte) 0x01,
            (byte) 0x37, (byte) 0x30, (byte) 0x17, (byte) 0x06, (byte) 0x03,
            (byte) 0x55, (byte) 0x04, (byte) 0x03, (byte) 0x13, (byte) 0x10,
            (byte) 0x77, (byte) 0x77, (byte) 0x77, (byte) 0x2E, (byte) 0x6C,
            (byte) 0x64, (byte) 0x61, (byte) 0x70, (byte) 0x74, (byte) 0x69,
            (byte) 0x76, (byte) 0x65, (byte) 0x2E, (byte) 0x6F, (byte) 0x72,
            (byte) 0x67, (byte) 0x31, (byte) 0x18, (byte) 0x30, (byte) 0x16,
            (byte) 0x06, (byte) 0x0A, (byte) 0x09, (byte) 0x92, (byte) 0x26,
            (byte) 0x89, (byte) 0x93, (byte) 0xF2, (byte) 0x2C, (byte) 0x64,
            (byte) 0x01, (byte) 0x19, (byte) 0x16, (byte) 0x08, (byte) 0x6C,
            (byte) 0x64, (byte) 0x61, (byte) 0x70, (byte) 0x74, (byte) 0x69,
            (byte) 0x76, (byte) 0x65, (byte) 0x31, (byte) 0x13, (byte) 0x30,
            (byte) 0x11, (byte) 0x06, (byte) 0x0A, (byte) 0x09, (byte) 0x92,
            (byte) 0x26, (byte) 0x89, (byte) 0x93, (byte) 0xF2, (byte) 0x2C,
            (byte) 0x64, (byte) 0x01, (byte) 0x19, (byte) 0x16, (byte) 0x03,
            (byte) 0x6F, (byte) 0x72, (byte) 0x67,
          },
          new DN(
            new RDN(
              new AttributeValueAssertion(
                "0.9.2342.19200300.100.1.1",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.PRINTSTR,
                  "7".getBytes("UTF-8"))),
              new AttributeValueAssertion(
                "2.5.4.3",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.PRINTSTR,
                  "www.ldaptive.org".getBytes("UTF-8")))),
            new RDN(
              new AttributeValueAssertion(
                "0.9.2342.19200300.100.1.25",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.IA5STR,
                  "ldaptive".getBytes("UTF-8")))),
            new RDN(
              new AttributeValueAssertion(
                "0.9.2342.19200300.100.1.25",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.IA5STR,
                  "org".getBytes("UTF-8"))))),
        },
        // O="CN=www.apache.org, foo", CN=www.example.org
        // BER 30:3B:31:18:30:16:06:03:55:04:03:13:0F:77:77:77:2E:65:78:61:6D:70
        //     6C:65:2E:6F:72:67:31:1F:30:1D:06:03:55:04:0A:13:16:43:4E:3D:77:77
        //     77:2E:61:70:61:63:68:65:2E:6F:72:67:2C:20:66:6F:6F:
        new Object[] {
          new byte[]{
            (byte) 0x30, (byte) 0x3B, (byte) 0x31, (byte) 0x18, (byte) 0x30,
            (byte) 0x16, (byte) 0x06, (byte) 0x03, (byte) 0x55, (byte) 0x04,
            (byte) 0x03, (byte) 0x13, (byte) 0x0F, (byte) 0x77, (byte) 0x77,
            (byte) 0x77, (byte) 0x2E, (byte) 0x65, (byte) 0x78, (byte) 0x61,
            (byte) 0x6D, (byte) 0x70, (byte) 0x6C, (byte) 0x65, (byte) 0x2E,
            (byte) 0x6F, (byte) 0x72, (byte) 0x67, (byte) 0x31, (byte) 0x1F,
            (byte) 0x30, (byte) 0x1D, (byte) 0x06, (byte) 0x03, (byte) 0x55,
            (byte) 0x04, (byte) 0x0A, (byte) 0x13, (byte) 0x16, (byte) 0x43,
            (byte) 0x4E, (byte) 0x3D, (byte) 0x77, (byte) 0x77, (byte) 0x77,
            (byte) 0x2E, (byte) 0x61, (byte) 0x70, (byte) 0x61, (byte) 0x63,
            (byte) 0x68, (byte) 0x65, (byte) 0x2E, (byte) 0x6F, (byte) 0x72,
            (byte) 0x67, (byte) 0x2C, (byte) 0x20, (byte) 0x66, (byte) 0x6F,
            (byte) 0x6F,
          },
          new DN(
            new RDN(
              new AttributeValueAssertion(
                "2.5.4.3",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.PRINTSTR,
                  "www.example.org".getBytes("UTF-8")))),
            new RDN(
              new AttributeValueAssertion(
                "2.5.4.10",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.PRINTSTR,
                  "CN=www.apache.org, foo".getBytes("UTF-8"))))),
        },
        // O="foo, CN=www.apache.org,", CN=www.example.org
        // BER 30:3C:31:18:30:16:06:03:55:04:03:13:0F:77:77:77:2E:65:78:61:6D:70
        //     6C:65:2E:6F:72:67:31:20:30:1E:06:03:55:04:0A:13:17:66:6F:6F:2C:20
        //     43:4E:3D:77:77:77:2E:61:70:61:63:68:65:2E:6F:72:67:2C:
        new Object[] {
          new byte[]{
            (byte) 0x30, (byte) 0x3C, (byte) 0x31, (byte) 0x18, (byte) 0x30,
            (byte) 0x16, (byte) 0x06, (byte) 0x03, (byte) 0x55, (byte) 0x04,
            (byte) 0x03, (byte) 0x13, (byte) 0x0F, (byte) 0x77, (byte) 0x77,
            (byte) 0x77, (byte) 0x2E, (byte) 0x65, (byte) 0x78, (byte) 0x61,
            (byte) 0x6D, (byte) 0x70, (byte) 0x6C, (byte) 0x65, (byte) 0x2E,
            (byte) 0x6F, (byte) 0x72, (byte) 0x67, (byte) 0x31, (byte) 0x20,
            (byte) 0x30, (byte) 0x1E, (byte) 0x06, (byte) 0x03, (byte) 0x55,
            (byte) 0x04, (byte) 0x0A, (byte) 0x13, (byte) 0x17, (byte) 0x66,
            (byte) 0x6F, (byte) 0x6F, (byte) 0x2C, (byte) 0x20, (byte) 0x43,
            (byte) 0x4E, (byte) 0x3D, (byte) 0x77, (byte) 0x77, (byte) 0x77,
            (byte) 0x2E, (byte) 0x61, (byte) 0x70, (byte) 0x61, (byte) 0x63,
            (byte) 0x68, (byte) 0x65, (byte) 0x2E, (byte) 0x6F, (byte) 0x72,
            (byte) 0x67, (byte) 0x2C,
          },
          new DN(
            new RDN(
              new AttributeValueAssertion(
                "2.5.4.3",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.PRINTSTR,
                  "www.example.org".getBytes("UTF-8")))),
            new RDN(
              new AttributeValueAssertion(
                "2.5.4.10",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.PRINTSTR,
                  "foo, CN=www.apache.org,".getBytes("UTF-8"))))),
        },
        // CN=login.live.com, OU=Passport, O=Microsoft Corporation,
        // STREET=One Microsoft Way, L=Redmond, ST=Washington,
        // OID.2.5.4.17=98052, C=US, SERIALNUMBER=600413485,
        // OID.2.5.4.15="V1.0, Clause 5.(b)",
        // OID.1.3.6.1.4.1.311.60.2.1.2=Washington,
        // OID.1.3.6.1.4.1.311.60.2.1.3=US
        // BER 30:84:00:00:01:0F:31:13:30:11:06:0B:2B:06:01:04:01:82:37:3C:02:01
        //     03:13:02:55:53:31:1B:30:19:06:0B:2B:06:01:04:01:82:37:3C:02:01:02
        //     13:0A:57:61:73:68:69:6E:67:74:6F:6E:31:1B:30:19:06:03:55:04:0F:13
        //     12:56:31:2E:30:2C:20:43:6C:61:75:73:65:20:35:2E:28:62:29:31:12:30
        //     10:06:03:55:04:05:13:09:36:30:30:34:31:33:34:38:35:31:0B:30:09:06
        //     03:55:04:06:13:02:55:53:31:0E:30:0C:06:03:55:04:11:14:05:39:38:30
        //     35:32:31:13:30:11:06:03:55:04:08:13:0A:57:61:73:68:69:6E:67:74:6F
        //     6E:31:10:30:0E:06:03:55:04:07:14:07:52:65:64:6D:6F:6E:64:31:1A:30
        //     18:06:03:55:04:09:14:11:4F:6E:65:20:4D:69:63:72:6F:73:6F:66:74:20
        //     57:61:79:31:1E:30:1C:06:03:55:04:0A:14:15:4D:69:63:72:6F:73:6F:66
        //     74:20:43:6F:72:70:6F:72:61:74:69:6F:6E:31:11:30:0F:06:03:55:04:0B
        //     14:08:50:61:73:73:70:6F:72:74:31:17:30:15:06:03:55:04:03:14:0E:6C
        //     6F:67:69:6E:2E:6C:69:76:65:2E:63:6F:6D:
        new Object[] {
          LdapUtils.base64Decode(
            "MIQAAAEPMRMwEQYLKwYBBAGCNzwCAQMTAlVTMRswGQYLKwYBBAGCNzwCAQITCldh" +
            "c2hpbmd0b24xGzAZBgNVBA8TElYxLjAsIENsYXVzZSA1LihiKTESMBAGA1UEBRMJ" +
            "NjAwNDEzNDg1MQswCQYDVQQGEwJVUzEOMAwGA1UEERQFOTgwNTIxEzARBgNVBAgT" +
            "Cldhc2hpbmd0b24xEDAOBgNVBAcUB1JlZG1vbmQxGjAYBgNVBAkUEU9uZSBNaWNy" +
            "b3NvZnQgV2F5MR4wHAYDVQQKFBVNaWNyb3NvZnQgQ29ycG9yYXRpb24xETAPBgNV" +
            "BAsUCFBhc3Nwb3J0MRcwFQYDVQQDFA5sb2dpbi5saXZlLmNvbQ=="),
          new DN(
            new RDN(
              new AttributeValueAssertion(
                "1.3.6.1.4.1.311.60.2.1.3",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.PRINTSTR,
                  "US".getBytes("UTF-8")))),
            new RDN(
              new AttributeValueAssertion(
                "1.3.6.1.4.1.311.60.2.1.2",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.PRINTSTR,
                  "Washington".getBytes("UTF-8")))),
            new RDN(
              new AttributeValueAssertion(
                "2.5.4.15",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.PRINTSTR,
                  "V1.0, Clause 5.(b)".getBytes("UTF-8")))),
            new RDN(
              new AttributeValueAssertion(
                "2.5.4.5",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.PRINTSTR,
                  "600413485".getBytes("UTF-8")))),
            new RDN(
              new AttributeValueAssertion(
                "2.5.4.6",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.PRINTSTR,
                  "US".getBytes("UTF-8")))),
            new RDN(
              new AttributeValueAssertion(
                "2.5.4.17",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.T61STR,
                  "98052".getBytes("UTF-8")))),
            new RDN(
              new AttributeValueAssertion(
                "2.5.4.8",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.PRINTSTR,
                  "Washington".getBytes("UTF-8")))),
            new RDN(
              new AttributeValueAssertion(
                "2.5.4.7",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.T61STR,
                  "Redmond".getBytes("UTF-8")))),
            new RDN(
              new AttributeValueAssertion(
                "2.5.4.9",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.T61STR,
                  "One Microsoft Way".getBytes("UTF-8")))),
            new RDN(
              new AttributeValueAssertion(
                "2.5.4.10",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.T61STR,
                  "Microsoft Corporation".getBytes("UTF-8")))),
            new RDN(
              new AttributeValueAssertion(
                "2.5.4.11",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.T61STR,
                  "Passport".getBytes("UTF-8")))),
            new RDN(
              new AttributeValueAssertion(
                "2.5.4.3",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.T61STR,
                  "login.live.com".getBytes("UTF-8"))))),
        },
        // C=US, DC=edu, DC=vt, ST=Virginia, L=Blacksburg,
        // O=Virginia Polytechnic Institute and State University,
        // OU=Middleware-Client, OU=SETI, SERIALNUMBER=1248110657961,
        // CN=glider.cc.vt.edu
        // BER 30:84:00:00:00:FA:31:19:30:17:06:03:55:04:03:0C:10:67:6C:69:64:65
        //     72:2E:63:63:2E:76:74:2E:65:64:75:31:16:30:14:06:03:55:04:05:13:0D
        //     31:32:34:38:31:31:30:36:35:37:39:36:31:31:0D:30:0B:06:03:55:04:0B
        //     0C:04:53:45:54:49:31:1A:30:18:06:03:55:04:0B:0C:11:4D:69:64:64:6C
        //     65:77:61:72:65:2D:43:6C:69:65:6E:74:31:3C:30:3A:06:03:55:04:0A:0C
        //     33:56:69:72:67:69:6E:69:61:20:50:6F:6C:79:74:65:63:68:6E:69:63:20
        //     49:6E:73:74:69:74:75:74:65:20:61:6E:64:20:53:74:61:74:65:20:55:6E
        //     69:76:65:72:73:69:74:79:31:13:30:11:06:03:55:04:07:0C:0A:42:6C:61
        //     63:6B:73:62:75:72:67:31:11:30:0F:06:03:55:04:08:0C:08:56:69:72:67
        //     69:6E:69:61:31:12:30:10:06:0A:09:92:26:89:93:F2:2C:64:01:19:16:02
        //     76:74:31:13:30:11:06:0A:09:92:26:89:93:F2:2C:64:01:19:16:03:65:64
        //     75:31:0B:30:09:06:03:55:04:06:13:02:55:53:
        new Object[] {
          LdapUtils.base64Decode(
            "MIQAAAD6MRkwFwYDVQQDDBBnbGlkZXIuY2MudnQuZWR1MRYwFAYDVQQFEw0xMjQ4" +
            "MTEwNjU3OTYxMQ0wCwYDVQQLDARTRVRJMRowGAYDVQQLDBFNaWRkbGV3YXJlLUNs" +
            "aWVudDE8MDoGA1UECgwzVmlyZ2luaWEgUG9seXRlY2huaWMgSW5zdGl0dXRlIGFu" +
            "ZCBTdGF0ZSBVbml2ZXJzaXR5MRMwEQYDVQQHDApCbGFja3NidXJnMREwDwYDVQQI" +
            "DAhWaXJnaW5pYTESMBAGCgmSJomT8ixkARkWAnZ0MRMwEQYKCZImiZPyLGQBGRYD" +
            "ZWR1MQswCQYDVQQGEwJVUw=="),
          new DN(
            new RDN(
              new AttributeValueAssertion(
                "2.5.4.3",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.UTF8STR,
                  "glider.cc.vt.edu".getBytes("UTF-8")))),
            new RDN(
              new AttributeValueAssertion(
                "2.5.4.5",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.PRINTSTR,
                  "1248110657961".getBytes("UTF-8")))),
            new RDN(
              new AttributeValueAssertion(
                "2.5.4.11",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.UTF8STR,
                  "SETI".getBytes("UTF-8")))),
            new RDN(
              new AttributeValueAssertion(
                "2.5.4.11",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.UTF8STR,
                  "Middleware-Client".getBytes("UTF-8")))),
            new RDN(
              new AttributeValueAssertion(
                "2.5.4.10",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.UTF8STR,
                  "Virginia Polytechnic Institute and State University"
                    .getBytes("UTF-8")))),
            new RDN(
              new AttributeValueAssertion(
                "2.5.4.7",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.UTF8STR,
                  "Blacksburg".getBytes("UTF-8")))),
            new RDN(
              new AttributeValueAssertion(
                "2.5.4.8",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.UTF8STR,
                  "Virginia".getBytes("UTF-8")))),
            new RDN(
              new AttributeValueAssertion(
                "0.9.2342.19200300.100.1.25",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.IA5STR,
                  "vt".getBytes("UTF-8")))),
            new RDN(
              new AttributeValueAssertion(
                "0.9.2342.19200300.100.1.25",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.IA5STR,
                  "edu".getBytes("UTF-8")))),
            new RDN(
              new AttributeValueAssertion(
                "2.5.4.6",
                new AttributeValueAssertion.Value(
                  UniversalDERTag.PRINTSTR,
                  "US".getBytes("UTF-8"))))),
        },
      };
  }
  // CheckStyle:MethodLength ON


  /**
   * @param  bytes  to decode.
   * @param  expected  to compare.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"asn1"},
    dataProvider = "dns"
  )
  public void decode(
    final byte[] bytes,
    final DN expected)
    throws Exception
  {
    Assert.assertEquals(DN.decode(ByteBuffer.wrap(bytes)), expected);
  }


  /**
   * @param  expected  bytes to compare.
   * @param  sequence  to encode.
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"asn1"},
    dataProvider = "dns"
  )
  public void encode(final byte[] expected, final DN sequence)
    throws Exception
  {
    Assert.assertEquals(sequence.encode(), expected);
  }
}
