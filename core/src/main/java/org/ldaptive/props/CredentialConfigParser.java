/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.props;

import java.util.regex.Matcher;

/**
 * Parses the configuration data associated with credential configs. The format of the property string should be like:
 *
 * <pre>
   KeyStoreCredentialConfig
     {{trustStore=file:/tmp/my.truststore}{trustStoreType=JKS}}
 * </pre>
 *
 * <p>or</p>
 *
 * <pre>
   {{trustCertificates=file:/tmp/my.crt}}
 * </pre>
 *
 * @author  Middleware Services
 */
public class CredentialConfigParser extends PropertyValueParser
{

  /** Credential config class found in the config. */
  protected static final String DEFAULT_CREDENTIAL_CONFIG_CLASS = "org.ldaptive.ssl.X509CredentialConfig";


  /**
   * Creates a new credential config parser.
   *
   * @param  config  containing configuration data
   */
  public CredentialConfigParser(final String config)
  {
    final Matcher credentialOnlyMatcher = CONFIG_PATTERN.matcher(config);
    final Matcher paramsOnlyMatcher = PARAMS_ONLY_CONFIG_PATTERN.matcher(config);
    if (credentialOnlyMatcher.matches()) {
      initialize(credentialOnlyMatcher.group(1).trim(), credentialOnlyMatcher.group(2).trim());
    } else if (paramsOnlyMatcher.matches()) {
      initialize(DEFAULT_CREDENTIAL_CONFIG_CLASS, paramsOnlyMatcher.group(1).trim());
    }
  }


  /**
   * Returns whether the supplied configuration data contains a credential config.
   *
   * @param  config  containing configuration data
   *
   * @return  whether the supplied configuration data contains a credential config
   */
  public static boolean isCredentialConfig(final String config)
  {
    return isConfig(config) || isParamsOnlyConfig(config);
  }
}
