---
layout: default
title: Ldaptive - jaas
redirect_from: "/docs/guide/jaas/"
---

# JAAS Login Modules

Ldaptive provides several login modules for authentication and authorization against an LDAP. Each module accepts properties that correspond to the setters on objects in the ldaptive code base. If you are looking to set a specific configuration option that is available as a setter, the chances are that it will be accepted on the module. Any unknown options will be passed to the provider as a generic property. Both the [authentication](docs/guide/authentication.md) and the [connection](docs/guide/connections.md) documentation is useful for understanding JAAS configuration options.

## LdapLoginModule

A JAAS module which authenticates against an LDAP.

### Configuration

Authenticate a user using the 'uid' attribute and populate the user's principals with the values of the 'eduPersonAffiliation' attribute.

{% highlight text %}
ldaptive {
  org.ldaptive.jaas.LdapLoginModule required
    ldapUrl="ldap://ldaptive.directory.org:389"
    baseDn="ou=people,dc=ldaptive,dc=org"
    useStartTLS="true"
    userFilter="(uid={user})"
    userRoleAttribute="eduPersonAffiliation";
};
{% endhighlight %}

Performs the same function as the previous, but uses the bindDn and bindCredential to lookup to the user's DN. In addition, the configured certificate will be used to trust the LDAP server.

{% highlight text %}
ldaptive {
  org.ldaptive.jaas.LdapLoginModule required
    ldapUrl="ldap://directory.ldaptive.org:389"
    baseDn="ou=people,dc=ldaptive,dc=org"
    bindDn="cn=priviledged_user,ou=services,dc=vt,dc=edu"
    bindCredential="notarealpassword"
    useStartTLS="true"
    credentialConfig="{trustCertificates=file:/path/to/certs.pem}"
    userFilter="(uid={user})"
    userRoleAttribute="eduPersonAffiliation";
};
{% endhighlight %}

### Module Options

Name | Description
userRoleAttribute | An attribute(s) that exists on the user entry. The value(s) of these attributes will be added as roles for this user. Comma delimited for multiple attributes. By default no attributes are returned as roles. If all attributes should be assigned as role data, set this property to '*'.
useFirstPass | Whether the login name and password should be retrieved from shared state rather than the callback handler. A login failure with this login name results in a LoginException.
tryFirstPass | Whether the login name and password should be retrieved from shared state rather than the callback handler. A login failure with this login name results in the callback handler begin invoked for a new login name and password.
storePass | Whether the login name, login dn, and login password should be stored in shared state after a successful login. Existing values are overwritten.
clearPass | Whether the login name, login dn, and login password should be removed from shared state after a successful login.
setLdapPrincipal | Whether the login name should be stored in the LdapPrincipal class. Default value is true.
setLdapDnPrincipal | Whether the LDAP entry DN should be stored in the LdapDnPrincipal class. Default value is false.
setLdapCredential | Whether the login password should be stored in the LdapCredential class. Default value is true.
defaultRole | Role(s) to set if login succeeds on this module. Comma delimited for multiple values. Default value is empty.
principalGroupName | Name of the Group to place the principal(s) in if login succeeds on this module. Default value is empty.
roleGroupName | Name of the Group to place the roles in if login succeeds on this module. Default value is empty.

### Pooling

It is often desirable to use a pool of LDAP connections for both DN resolution and authentication. The stateless nature of JAAS modules makes this very difficult. Ldaptive provides a default AuthenticationFactory that supports caching to solve this problem.


{% highlight text %}
ldaptive {
  org.ldaptive.jaas.LdapLoginModule required
    ldapUrl="ldap://directory.ldaptive.org:389"
    baseDn="ou=people,dc=ldaptive,dc=org"
    bindDn="cn=priviledged_user,ou=services,dc=vt,dc=edu"
    bindCredential="notarealpassword"
    useStartTLS="true"
    userFilter="(uid={user})"
    userRoleAttribute="eduPersonAffiliation"
    dnResolver="org.ldaptive.auth.PooledSearchDnResolver"
    authenticationHandler="org.ldaptive.auth.PooledBindAuthenticationHandler"
    cacheId="ldaptive-pooled";
};
{% endhighlight %}

Note that both the dnResolver and the authenticationHandler will use a pool of connections in this example. The cacheId option is required for this configuration to work. Without it, the pools will be recreated for every request. It is the responsibility of the application to properly tear down the pools. This can be done by invoking: `PropertiesAuthenticatorFactory#close()`.

### Custom Configuration

It may be necessary to use a more powerful configuration mechanism than what can be achieved with JAAS options. For those cases the `AuthenticatorFactory` interface exists to allow developers a greater level of control for how objects are wired together. For instance, an AuthenticatorFactory could be written to leverage [Spring IOC](http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/beans.html):

This allows you to break free of the restrictive format imposed by the JAAS config. A sample implementation that uses Spring can be seen [here](https://github.com/vt-middleware/ldaptive/blob/master/integration/src/test/java/org/ldaptive/jaas/SpringAuthenticatorFactory.java).

## LdapRoleAuthorizationModule

A JAAS module which provides authorization data from an LDAP. This module is meant to be stacked with a module that has performed authentication.

### Configuration

Authenticate a user using the 'uid' attribute and populate the user's principals with the values of the 'eduPersonAffiliation' attribute. Populate the user's principals with value of the 'uugid' attribute found on any entries in the 'ou=groups,dc=ldaptive,dc=org' branch which contain a member attribute value that includes this user's DN.

{% highlight text %}
ldaptive {
  org.ldaptive.jaas.LdapLoginModule required
    storePass="true"
    ldapUrl="ldap://directory.ldaptive.org:389"
    baseDn="ou=people,dc=ldaptive,dc=org"
    useStartTLS="true"
    userFilter="(uid={user})"
    userRoleAttribute="eduPersonAffiliation";
  org.ldaptive.jaas.LdapRoleAuthorizationModule required
    useFirstPass="true"
    ldapUrl="ldap://directory.ldaptive.org:389"
    baseDn="ou=groups,dc=vt,dc=edu"
    roleFilter="(member={dn})"
    roleAttribute="uugid";
};
{% endhighlight %}

### Module Options

Name | Description
roleFilter | An LDAP search filter where {dn} is replaced with the user dn and {user} is replaced with the user. This is used to find roles for the user.
roleAttribute | An attribute(s) that exists on any role entries found with the roleFilter. The value(s) of these attributes will be added as roles for this user. Comma delimited for multiple attributes. By default no attributes are returned as roles. If all attributes should be assigned as role data, set this property to '*'.
noResultsIsError | Whether an exception should be thrown if no roles are found. Default value is false.
useFirstPass | Whether the login name and password should be retrieved from shared state rather than the callback handler. A login failure with this login name results in a LoginException.
tryFirstPass | Whether the login name and password should be retrieved from shared state rather than the callback handler. A login failure with this login name results in the callback handler begin invoked for a new login name and password.
storePass | Whether the login name, login dn, and login password should be stored in shared state after a successful login. Existing values are overwritten.
clearPass | Whether the login name, login dn, and login password should be removed from shared state after a successful login.
setLdapPrincipal | Whether the login name should be stored in the LdapPrincipal class. Default value is false.
setLdapDnPrincipal | Whether the LDAP entry DN should be stored in the LdapDnPrincipal class. Default value is false.
setLdapCredential | Whether the login password should be stored in the LdapCredential class. Default value is false.
defaultRole | Role(s) to set if login succeeds on this module. Comma delimited for multiple values. Default value is empty.
principalGroupName | Name of the Group to place the principal(s) in if login succeeds on this module. Default value is empty.
roleGroupName | Name of the Group to place the roles in if login succeeds on this module. Default value is empty.

## LdapDnAuthorizationModule

A JAAS module which injects the login name, login dn, and/or login password into shared state. This module is meant to be stacked with a module that has performed authentication.

### Configuration

Uses Kerberos for authentication and then injects LDAP data into the subject. Note that the LdapDnAuthorizationModule is stacked before the LdapRoleAuthorizationModule so that the DN can be leveraged in the roleFilter query.

{% highlight text %}
ldaptive {
  com.sun.security.auth.module.Krb5LoginModule required
    storePass="true"
    debug="true";
  org.ldaptive.jaas.LdapDnAuthorizationModule required
    useFirstPass="true"
    storePass="true"
    ldapUrl="ldap://directory.ldaptive.org:389"
    baseDn="ou=people,dc=ldaptive,dc=org"
    useStartTLS="true"
    userFilter="(uid={user})";
  org.ldaptive.jaas.LdapRoleAuthorizationModule required
    useFirstPass="true"
    ldapUrl="ldap://directory.ldaptive.org:389"
    baseDn="ou=groups,dc=vt,dc=edu"
    roleFilter="(member={dn})"
    roleAttribute="uugid";
};
{% endhighlight %}

### Module Options

Name | Description
noResultsIsError | Whether an exception should be thrown if no DN is found. Default value is false.
useFirstPass | Whether the login name and password should be retrieved from shared state rather than the callback handler. A login failure with this login name results in a LoginException.
tryFirstPass | Whether the login name and password should be retrieved from shared state rather than the callback handler. A login failure with this login name results in the callback handler begin invoked for a new login name and password.
storePass | Whether the login name, login dn, and login password should be stored in shared state after a successful login. Existing values are overwritten.
clearPass | Whether the login name, login dn, and login password should be removed from shared state after a successful login.
setLdapPrincipal | Whether the login name should be stored in the LdapPrincipal class. Default value is false.
setLdapDnPrincipal | Whether the LDAP entry DN should be stored in the LdapDnPrincipal class. Default value is false.
setLdapCredential | Whether the login password should be stored in the LdapCredential class. Default value is false.
defaultRole | Role(s) to set if login succeeds on this module. Comma delimited for multiple values. Default value is empty.
principalGroupName | Name of the Group to place the principal(s) in if login succeeds on this module. Default value is empty.
roleGroupName | Name of the Group to place the roles in if login succeeds on this module. Default value is empty.

