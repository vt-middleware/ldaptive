/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive;

import java.util.Arrays;
import org.ldaptive.control.RequestControl;
import org.ldaptive.sasl.CramMD5BindRequest;
import org.ldaptive.sasl.DigestMD5BindRequest;
import org.ldaptive.sasl.DigestMD5Config;
import org.ldaptive.sasl.ExternalBindRequest;
import org.ldaptive.sasl.GssApiBindRequest;
import org.ldaptive.sasl.GssApiConfig;
import org.ldaptive.sasl.SaslBindRequest;
import org.ldaptive.sasl.SaslConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initializes a connection by performing a bind operation. Useful if you need all connections to bind as the same
 * principal.
 *
 * @author  Middleware Services
 */
public class BindConnectionInitializer implements ConnectionInitializer
{

  /** Logger for this class. */
  protected final Logger logger = LoggerFactory.getLogger(getClass());

  /** DN to bind as before performing operations. */
  private String bindDn;

  /** Credential for the bind DN. */
  private Credential bindCredential;

  /** Configuration for bind SASL authentication. */
  private SaslConfig bindSaslConfig;

  /** Bind controls. */
  private RequestControl[] bindControls;


  /** Default constructor. */
  public BindConnectionInitializer() {}


  /**
   * Creates a new bind connection initializer.
   *
   * @param  dn  bind dn
   * @param  credential  bind credential
   */
  public BindConnectionInitializer(final String dn, final String credential)
  {
    setBindDn(dn);
    setBindCredential(new Credential(credential));
  }


  /**
   * Creates a new bind connection initializer.
   *
   * @param  dn  bind dn
   * @param  credential  bind credential
   */
  public BindConnectionInitializer(final String dn, final Credential credential)
  {
    setBindDn(dn);
    setBindCredential(credential);
  }


  /**
   * Returns the bind DN.
   *
   * @return  DN to bind as
   */
  public String getBindDn()
  {
    return bindDn;
  }


  /**
   * Sets the bind DN to authenticate as before performing operations.
   *
   * @param  dn  to bind as
   */
  public void setBindDn(final String dn)
  {
    logger.trace("setting bindDn: {}", dn);
    bindDn = dn;
  }


  /**
   * Returns the credential used with the bind DN.
   *
   * @return  bind DN credential
   */
  public Credential getBindCredential()
  {
    return bindCredential;
  }


  /**
   * Sets the credential of the bind DN.
   *
   * @param  credential  to use with bind DN
   */
  public void setBindCredential(final Credential credential)
  {
    logger.trace("setting bindCredential: <suppressed>");
    bindCredential = credential;
  }


  /**
   * Returns the bind sasl config.
   *
   * @return  sasl config
   */
  public SaslConfig getBindSaslConfig()
  {
    return bindSaslConfig;
  }


  /**
   * Sets the bind sasl config.
   *
   * @param  config  sasl config
   */
  public void setBindSaslConfig(final SaslConfig config)
  {
    logger.trace("setting bindSaslConfig: {}", config);
    bindSaslConfig = config;
  }


  /**
   * Returns the bind controls.
   *
   * @return  controls
   */
  public RequestControl[] getBindControls()
  {
    return bindControls;
  }


  /**
   * Sets the bind controls.
   *
   * @param  cntrls  controls to set
   */
  public void setBindControls(final RequestControl... cntrls)
  {
    logger.trace("setting bindControls: {}", Arrays.toString(cntrls));
    bindControls = cntrls;
  }


  @Override
  public Result initialize(final Connection c)
    throws LdapException
  {
    final Result result;
    if (bindSaslConfig != null) {
      switch(bindSaslConfig.getMechanism()) {
      case EXTERNAL:
        result = c.operation(SaslBindRequest.builder()
          .mechanism(ExternalBindRequest.MECHANISM)
          .credentials(bindSaslConfig.getAuthorizationId() != null ? bindSaslConfig.getAuthorizationId() : "")
          .controls().build()).execute();
        break;
      case DIGEST_MD5:
        result = c.operation(new DigestMD5BindRequest(
          bindDn,
          bindSaslConfig.getAuthorizationId(),
          bindCredential != null ? bindCredential.getString() : null,
          bindSaslConfig instanceof DigestMD5Config ? bindSaslConfig.getRealm() : null,
          bindSaslConfig instanceof DigestMD5Config ? bindSaslConfig.getQualityOfProtection() : null));
        break;
      case CRAM_MD5:
        result = c.operation(new CramMD5BindRequest(
          bindDn,
          bindCredential != null ? bindCredential.getString() : null));
        break;
      case GSSAPI:
        result = c.operation(new GssApiBindRequest(
          bindDn,
          bindSaslConfig.getAuthorizationId(),
          bindCredential != null ? bindCredential.getString() : null,
          bindSaslConfig instanceof GssApiConfig ? bindSaslConfig.getRealm() : null,
          bindSaslConfig instanceof GssApiConfig ? bindSaslConfig.getQualityOfProtection() : null));
        break;
      default:
        throw new IllegalStateException("Unknown SASL mechanism: " + bindSaslConfig.getMechanism());
      }
    } else if (bindDn == null && bindCredential == null) {
      result = c.operation(AnonymousBindRequest.builder()
        .controls(bindControls).build()).execute();
    } else {
      result = c.operation(SimpleBindRequest.builder()
        .dn(bindDn)
        .password(bindCredential.getString())
        .controls(bindControls).build()).execute();
    }
    return result;
  }


  /**
   * Returns whether this connection initializer contains any configuration data.
   *
   * @return  whether all properties are null
   */
  public boolean isEmpty()
  {
    return bindDn == null && bindCredential == null && bindSaslConfig == null && bindControls == null;
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("bindDn=").append(bindDn).append(", ")
      .append("bindSaslConfig=").append(bindSaslConfig).append(", ")
      .append("bindControls=").append(Arrays.toString(bindControls)).append("]").toString();
  }


  /**
   * Creates a builder for this class.
   *
   * @return  new builder
   */
  public static Builder builder()
  {
    return new Builder();
  }


  // CheckStyle:OFF
  public static class Builder
  {


    private final BindConnectionInitializer object = new BindConnectionInitializer();


    protected Builder() {}


    public Builder dn(final String dn)
    {
      object.setBindDn(dn);
      return this;
    }


    public Builder credential(final Credential credential)
    {
      object.setBindCredential(credential);
      return this;
    }


    public Builder credential(final String credential)
    {
      object.setBindCredential(new Credential(credential));
      return this;
    }


    public Builder credential(final byte[] credential)
    {
      object.setBindCredential(new Credential(credential));
      return this;
    }


    public Builder saslConfig(final SaslConfig config)
    {
      object.setBindSaslConfig(config);
      return this;
    }


    public Builder controls(final RequestControl... controls)
    {
      object.setBindControls(controls);
      return this;
    }


    public BindConnectionInitializer build()
    {
      return object;
    }
  }
  // CheckStyle:ON
}
