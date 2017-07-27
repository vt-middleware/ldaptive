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

  /** User identifier. */
  private User user;

  /** User credential. */
  private Credential credential;

  /** User attributes to return. */
  private String[] retAttrs = ReturnAttributes.NONE.value();

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
    setUser(id);
    setCredential(c);
  }


  /**
   * Creates a new authentication request.
   *
   * @param  u  that identifies the user
   * @param  c  credential to authenticate the user
   */
  public AuthenticationRequest(final User u, final Credential c)
  {
    setUserEx(u);
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
    setUser(id);
    setCredential(c);
    setReturnAttributes(attrs);
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
    setUserEx(u);
    setCredential(c);
    setReturnAttributes(attrs);
  }


  /**
   * Returns the user.
   *
   * @return  user identifier
   */
  public String getUser()
  {
    return user != null ? user.getIdentifier() : null;
  }


  /**
   * Returns the user.
   *
   * @return  user identifier
   */
  public User getUserEx()
  {
    return user;
  }


  /**
   * Sets the user.
   *
   * @param  id  of the user to authenticate
   */
  public void setUser(final String id)
  {
    user = new User(id);
  }


  /**
   * Sets the user.
   *
   * @param  u  user to authenticate
   */
  public void setUserEx(final User u)
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
    return retAttrs;
  }


  /**
   * Sets the return attributes.
   *
   * @param  attrs  return attributes
   */
  public void setReturnAttributes(final String... attrs)
  {
    retAttrs = ReturnAttributes.parse(attrs);
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
   * @param  c  controls to set
   */
  public void setControls(final RequestControl... c)
  {
    controls = c;
  }


  /**
   * Returns an authentication request initialized with the supplied request.
   *
   * @param  request  authentication request to read properties from
   *
   * @return  authentication request
   */
  public static AuthenticationRequest newAuthenticationRequest(final AuthenticationRequest request)
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
    return
      String.format(
        "[%s@%d::user=%s, retAttrs=%s, controls=%s]",
        getClass().getName(),
        hashCode(),
        user,
        Arrays.toString(retAttrs),
        Arrays.toString(controls));
  }
}
