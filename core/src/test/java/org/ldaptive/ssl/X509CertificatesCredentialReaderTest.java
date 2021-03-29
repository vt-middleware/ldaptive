/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.ssl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.cert.X509Certificate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.ldaptive.LdapUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit test for {@link X509CertificatesCredentialReader}.
 *
 * @author  Misagh Moayyed
 */
public class X509CertificatesCredentialReaderTest
{

  /** Test cert. */
  private static final String MW_CERT =
    "MIIDhzCCAvCgAwIBAgIJAPpeFAkJP5xgMA0GCSqGSIb3DQEBBQUAMIGKMQswCQYD" +
      "VQQGEwJVUzERMA8GA1UECBMIVmlyZ2luaWExEzARBgNVBAcTCkJsYWNrc2J1cmcx" +
      "FjAUBgNVBAoTDVZpcmdpbmlhIFRlY2gxEzARBgNVBAsTCk1pZGRsZXdhcmUxJjAk" +
      "BgNVBAMTHWxkYXAtdGVzdC0xLm1pZGRsZXdhcmUudnQuZWR1MB4XDTExMDkyNjE2" +
      "NDczOFoXDTIxMDkyMzE2NDczOFowgYoxCzAJBgNVBAYTAlVTMREwDwYDVQQIEwhW" +
      "aXJnaW5pYTETMBEGA1UEBxMKQmxhY2tzYnVyZzEWMBQGA1UEChMNVmlyZ2luaWEg" +
      "VGVjaDETMBEGA1UECxMKTWlkZGxld2FyZTEmMCQGA1UEAxMdbGRhcC10ZXN0LTEu" +
      "bWlkZGxld2FyZS52dC5lZHUwgZ8wDQYJKoZIhvcNAQEBBQADgY0AMIGJAoGBAJWf" +
      "/vBsfFn6sQo57IHrBzMlPARpDI1DJeqH7zl2UeVzeiZDjGiU4ETSjEsvvQRzLfXZ" +
      "IgJEr1IEAzjCX8wKF4svrmkPK3KN6JvdlknM7Thw5p0NzAh2Bq1R1h7+bUvQJGep" +
      "aizNM0od/mKrJnOnUCWEgcpG91mWg8b1PphGobeNAgMBAAGjgfIwge8wHQYDVR0O" +
      "BBYEFMT2Hkcp6JFq242hWfdMOeT3/hZ1MIG/BgNVHSMEgbcwgbSAFMT2Hkcp6JFq" +
      "242hWfdMOeT3/hZ1oYGQpIGNMIGKMQswCQYDVQQGEwJVUzERMA8GA1UECBMIVmly" +
      "Z2luaWExEzARBgNVBAcTCkJsYWNrc2J1cmcxFjAUBgNVBAoTDVZpcmdpbmlhIFRl" +
      "Y2gxEzARBgNVBAsTCk1pZGRsZXdhcmUxJjAkBgNVBAMTHWxkYXAtdGVzdC0xLm1p" +
      "ZGRsZXdhcmUudnQuZWR1ggkA+l4UCQk/nGAwDAYDVR0TBAUwAwEB/zANBgkqhkiG" +
      "9w0BAQUFAAOBgQBe0bV5iZyPupNh2zmdH7opuwldz1sxlkRdUQhKSlYsOqgAKDvS" +
      "DypmR4mqntAULTFGZIdcQ1W8HJcnRc8KuPfNatAV8A9OqMbtDLnmfWkl33JPiDUd" +
      "fIKCXuG4dZ6nn3RbjlKhXzHYADmJzdQNIC3M9eDQBEYmMy8+mV+ErVebBg==";


  @Test
  public void verifyMultipleCertificatesSeparated() throws Exception
  {
    final File cert1 = File.createTempFile("cert1", ".pem");
    try (OutputStream writer = new FileOutputStream(cert1)) {
      writer.write(LdapUtils.base64Decode(MW_CERT));
      writer.flush();
    }
    final File cert2 = File.createTempFile("cert2", ".pem");
    try (OutputStream writer = new FileOutputStream(cert2)) {
      writer.write(LdapUtils.base64Decode(MW_CERT));
      writer.flush();
    }
    final X509CertificatesCredentialReader reader = new X509CertificatesCredentialReader();
    final String paths = Stream.of(cert1, cert2)
      .map(file -> "file:" + file.getAbsolutePath())
      .collect(Collectors.joining(","));
    final X509Certificate[] certificates = reader.read(paths);
    Assert.assertEquals(2, certificates.length);
  }
}
