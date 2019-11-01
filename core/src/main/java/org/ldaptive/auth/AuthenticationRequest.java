/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.auth;

import java.util.Arrays;
import org.ldaptive.Credential;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.control.RequestControl;

/**
 * Contains the data required to perform an ldap authentication.
 *
 * @author  Middleware Services
 */
public class AuthenticationRequest
{

  /** User. */
  private User user;

  /** User credential. */
  private Credential credential;

  /** User attributes to return. */
  private String[] returnAttributes = ReturnAttributes.NONE.value();

  /** Request controls. */
  private RequestControl[] controls;


  /** Default constructor. */
  public AuthenticationRequest() {}


  /**
   * Creates a new authentication request.
   *
   * @param  id  that identifies the user
   * @param  c  credential to authenticate the user
   */
  public AuthenticationRequest(final String id, final Credential c)
  {
    setUser(new User(id));
    setCredential(c);
  }


  /**
   * Creates a new authentication request.
   *
   * @param  id  that identifies the user
   * @param  c  credential to authenticate the user
   * @param  attrs  attributes to return
   */
  public AuthenticationRequest(final String id, final Credential c, final String... attrs)
  {
    setUser(new User(id));
    setCredential(c);
    setReturnAttributes(attrs);
  }


  /**
   * Creates a new authentication request.
   *
   * @param  u  that identifies the user
   * @param  c  credential to authenticate the user
   */
  public AuthenticationRequest(final User u, final Credential c)
  {
    setUser(u);
    setCredential(c);
  }


  /**
   * Creates a new authentication request.
   *
   * @param  u  that identifies the user
   * @param  c  credential to authenticate the user
   * @param  attrs  attributes to return
   */
  public AuthenticationRequest(final User u, final Credential c, final String... attrs)
  {
    setUser(u);
    setCredential(c);
    setReturnAttributes(attrs);
  }


  /**
   * Returns the user.
   *
   * @return  user identifier
   */
  public User getUser()
  {
    return user;
  }


  /**
   * Sets the user.
   *
   * @param  u  user
   */
  public void setUser(final User u)
  {
    user = u;
  }


  /**
   * Returns the credential.
   *
   * @return  user credential
   */
  public Credential getCredential()
  {
    return credential;
  }


  /**
   * Sets the credential.
   *
   * @param  c  user credential
   */
  public void setCredential(final Credential c)
  {
    credential = c;
  }


  /**
   * Returns the return attributes.
   *
   * @return  attributes to return
   */
  public String[] getReturnAttributes()
  {
    return returnAttributes;
  }


  /**
   * Sets the return attributes.
   *
   * @param  attrs  return attributes
   */
  public void setReturnAttributes(final String... attrs)
  {
    returnAttributes = ReturnAttributes.parse(attrs);
  }


  /**
   * Returns the controls.
   *
   * @return  controls
   */
  public RequestControl[] getControls()
  {
    return controls;
  }


  /**
   * Sets the controls.
   *
   * @param  cntrls  controls to set
   */
  public void setControls(final RequestControl... cntrls)
  {
    controls = cntrls;
  }


  /**
   * Returns an authentication request initialized with the supplied request.
   *
   * @param  request  authentication request to read properties from
   *
   * @return  authentication request
   */
  public static AuthenticationRequest copy(final AuthenticationRequest request)
  {
    final AuthenticationRequest r = new AuthenticationRequest();
    r.setUser(request.getUser());
    r.setCredential(request.getCredential());
    r.setReturnAttributes(request.getReturnAttributes());
    r.setControls(request.getControls());
    return r;
  }


  @Override
  public String toString()
  {
    return new StringBuilder("[").append(
      getClass().getName()).append("@").append(hashCode()).append("::")
      .append("user=").append(user).append(", ")
      .append("returnAttributes=").append(Arrays.toString(returnAttributes)).append(", ")
      .append("controls=").append(Arrays.toString(controls)).append("]").toString();
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


  /** Authentication request builder. */
  public static class Builder
  {

    /** Authentication request to build. */
    private final AuthenticationRequest object = new AuthenticationRequest();


    /**
     * Default constructor.
     */
    protected Builder() {}


    /**
     * Sets the user id.
     *
     * @param  id  user id
     *
     * @return  this builder
     */
    public Builder id(final String id)
    {
      object.setUser(new User(id));
      return this;
    }


    /**
     * Sets the user credential.
     *
     * @param  credential  user credential
     *
     * @return  this builder
     */
    public Builder credential(final Credential credential)
    {
      object.setCredential(credential);
      return this;
    }


    /**
     * Sets the user credential.
     *
     * @param  credential  user credential
     *
     * @return  this builder
     */
    public Builder credential(final String credential)
    {
      object.setCredential(new Credential(credential));
      return this;
    }


    /**
     * Sets the user credential.
     *
     * @param  credential  user credential
     *
     * @return  this builder
     */
    public Builder credential(final char[] credential)
    {
      object.setCredential(new Credential(credential));
      return this;
    }


    /**
     * Sets the user credential.
     *
     * @param  credential  user credential
     *
     * @return  this builder
     */
    public Builder credential(final byte[] credential)
    {
      object.setCredential(new Credential(credential));
      return this;
    }


    /**
     * Sets the user.
     *
     * @param  user  to authenticate
     *
     * @return  this builder
     */
    public Builder user(final User user)
    {
      object.setUser(user);
      return this;
    }


    /**
     * Sets the return attributes.
     *
     * @param  attributes  return attributes
     *
     * @return  this builder
     */
    public Builder returnAttributes(final String... attributes)
    {
      object.setReturnAttributes(attributes);
      return this;
    }


    /**
     * Sets the request controls.
     *
     * @param  controls  request controls
     *
     * @return  this builder
     */
    public Builder controls(final RequestControl... controls)
    {
      object.setControls(controls);
      return this;
    }


    /**
     * Returns the authentication request.
     *
     * @return  authentication request
     */
    public AuthenticationRequest build()
    {
      return object;
    }
  }
}
