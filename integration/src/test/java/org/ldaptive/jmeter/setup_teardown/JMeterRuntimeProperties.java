/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.jmeter.setup_teardown;

import java.lang.reflect.Field;
import java.util.Properties;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.ldaptive.LdapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Properties that can be manipulated for JMeter test executions.
 *
 * @author Middleware Services
 */
public final class JMeterRuntimeProperties
{

  /** Logger instance */
  private static final Logger LOGGER = LoggerFactory.getLogger(JMeterRuntimeProperties.class);

  /** LDAP URL used for JMeter tests */
  private String ldapUrl;

  /** Base DN */
  private String baseDn;

  /** DN for Bind operations*/
  private String bindDn;

  /** Credential for testing Authentication */
  private String bindCredential;

  /** Filter for Search operation tests */
  private String searchFilter;

  /** Filter for Authentication operation tests */
  private String bindFilter;

  /** {@link org.ldaptive.auth.User} identifier for Authentication operation tests */
  private String authRequestId;

  /** Initial pool size for {@link org.ldaptive.PooledConnectionFactory} */
  private int minPoolSize;

  /** Maximum pool size for {@link org.ldaptive.PooledConnectionFactory} */
  private int maxPoolSize;

  /** {@link org.ldaptive.ConnectionConfig} autoReconnect */
  private boolean autoReconnect;

  /** {@link org.ldaptive.ConnectionConfig} useStartTls */
  private boolean useStartTls;

  /** Properties that are defined at ldaptive runtime. */
  private Properties ldaptiveProps;

  /**
   * Properties that are either set in the JMeter TestPlan or that have been provided via command
   * line arguments prefixed with -J flag.
   */
  private JavaSamplerContext samplerContext;

  /**
   * Private constructor.
   * Instantiation must occur using {@link #JMeterRuntimeProperties(Properties, JavaSamplerContext)}
   */
  private JMeterRuntimeProperties() {}


  /**
   * Default constructor.
   * Initialize all properties that are used by JMeter tests at startup
   *
   * @param props   Properties that are defined at ldaptive (via docker-compose or command line args) runtime.
   * @param context current JMeter context
   *
   * @throws LdapException if a property value cannot be read
   */
  public JMeterRuntimeProperties(final Properties props, final JavaSamplerContext context)
    throws LdapException
  {
    ldaptiveProps = props;
    samplerContext = context;
    bindFilter = getPropValue("AUTH_FILTER", null);
    authRequestId = getPropValue("AUTH_REQUEST_ID", null);
    baseDn = getPropValue("BASE_DN", "org.ldaptive.baseDn");
    bindCredential = getPropValue("BIND_CREDENTIAL", "org.ldaptive.bindCredential");
    bindDn = getPropValue("BIND_DN", "org.ldaptive.bindDn");
    autoReconnect = Boolean.parseBoolean(getPropValue("CONNECTION_AUTO_RECONNECT", null));
    useStartTls = Boolean.parseBoolean(getPropValue("CONNECTION_USE_START_TLS", "org.ldaptive.useStartTLS"));
    ldapUrl = getPropValue("LDAP_URL", "org.ldaptive.ldapUrl");
    maxPoolSize = Integer.parseInt(getPropValue("MAX_POOL_SIZE", null));
    minPoolSize = Integer.parseInt(getPropValue("MIN_POOL_SIZE", null));
    searchFilter = getPropValue("SEARCH_FILTER", null);
  }


  public String ldapUrl()
  {
    return ldapUrl;
  }


  public String baseDn()
  {
    return baseDn;
  }


  public String bindDn()
  {
    return bindDn;
  }


  public String bindCredential()
  {
    return bindCredential;
  }


  public String searchFilter()
  {
    return searchFilter;
  }


  public String bindFilter()
  {
    return bindFilter;
  }


  public String authRequestId()
  {
    return authRequestId;
  }


  public int minPoolSize()
  {
    return minPoolSize;
  }


  public int maxPoolSize()
  {
    return maxPoolSize;
  }


  public boolean autoReconnect()
  {
    return autoReconnect;
  }


  public boolean useStartTls()
  {
    return useStartTls;
  }


  /**
   * Get property from either the samplerContext or ldaptiveProperties (props defined in ldap.properties file).
   *
   * @param jmeterVarName    name defined in the TestPlan jmx file
   * @param ldaptivePropName name defined in ldap.properties file
   * @return property value
   * @throws LdapException when the property is not found in either location. This should never happen and
   *                       indicates there was an issue with wiring up property sources or the test plan.
   */
  private String getPropValue(final String jmeterVarName, final String ldaptivePropName)
    throws LdapException
  {
    final String jmeterVar = samplerContext.getJMeterVariables().get(jmeterVarName);
        /*
           The jmx file for these tests use the JMeter Property function for passing variables around. By default
           this will return 1 if there isn't a default value provided in the jmx. Any props that don't have default
           values should be give the value 'undefined'.
           Any properties that need to be overridden should be done so via command line args with -J flag or under
           <propertiesUser> in the plugin.
        */
    if (jmeterVar != null && !"undefined".equals(jmeterVar)) {
      return jmeterVar;
    }
    if (ldaptivePropName != null) {
      final String ldaptiveProp = ldaptiveProps.getProperty(ldaptivePropName);
      if (ldaptiveProp != null) {
        return ldaptiveProp;
      }
    }
    throw new LdapException(
      String.format("Expected either %s or %s property at runtime but neither were found",
        jmeterVarName,
        ldaptivePropName == null ? "N/A" : ldaptivePropName
      ));
  }

  @Override
  public String toString()
  {
    final StringBuilder details = new StringBuilder("Runtime Properties");
    details.append(System.lineSeparator()).append("{").append(System.lineSeparator());
    for (Field field : this.getClass().getDeclaredFields()) {
      details.append("\t");
      try {
        details.append(field.getName()).append(" : ");
        details.append(field.get(this));
      } catch (IllegalAccessException ex) {
        LOGGER.error("Exception occurred while getting field data", ex);
      }
      details.append(System.lineSeparator());
    }
    details.append("}");
    return details.toString();
  }
}
