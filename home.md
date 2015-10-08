---
layout: default
title: Ldaptive
permalink: /
---

#LDAP library for Java.
Ldaptive is a simple, extensible Java API for interacting with LDAP servers. It was designed to provide easy LDAP integration for application developers.

##Rationale
Developers need LDAP integration for their products, but the JNDI API is cumbersome and takes a great deal of resources to learn and use. In addition, most applications only require a subset of LDAP functionality: search and authentication.

##Features
* Search result caching
* Connection pooling
* Authentication API with support for password policy
* JAAS modules for authentication and authorization
* SSL/startTLS support with easy configuration of trust and key material
* Input/output of LDIF and DSML version 1
* Command line scripts for operations
* Supported providers:
  * JNDI
  * UnboundID
  * Apache
  * JLdap
  * OpenDJ
* Supported controls (no external dependencies required):
* ManageDsaIT (RFC 3296)
* Paged results (RFC 2696)
* Server side sorting (RFC 2891)
* Password policy (draft-behera-ldap-password-policy-10)

##Quick Start Guide

###Searching
{% highlight java %}
ConnectionFactory cf = new DefaultConnectionFactory("ldap://directory.ldaptive.org");
SearchExecutor executor = new SearchExecutor();
executor.setBaseDn("dc=ldaptive,dc=org");
SearchResult result = executor.search(cf, "(uid=dfisher)").getResult();
LdapEntry entry = result.getEntry();
// do something useful with the entry
{% endhighlight %}

###StartTLS
{% highlight java %}
ConnectionConfig connConfig = new ConnectionConfig("ldap://directory.ldaptive.org");
connConfig.setUseStartTLS(true);
ConnectionFactory cf = new DefaultConnectionFactory(connConfig);
SearchExecutor executor = new SearchExecutor();
executor.setBaseDn("dc=ldaptive,dc=org");
SearchResult result = executor.search(cf, "(uid=*fisher)", "mail", "sn").getResult();
for (LdapEntry entry : result.getEntries()) {
  // do something useful with the entry
}
{% endhighlight %}

###Binding
{% highlight java %}
ConnectionConfig connConfig = new ConnectionConfig("ldap://directory.ldaptive.org");
connConfig.setUseStartTLS(true);
connConfig.setConnectionInitializer(
  new BindConnectionInitializer(
    "cn=manager,ou=people,dc=ldaptive,dc=org", new Credential("manager_password")));
ConnectionFactory cf = new DefaultConnectionFactory(connConfig);
SearchExecutor executor = new SearchExecutor();
executor.setBaseDn("dc=ldaptive,dc=org");
SearchResult result = executor.search(cf, "(uid=*fisher)", "mail", "sn").getResult();
for (LdapEntry entry : result.getEntries()) {
  // do something useful with the entry
}
{% endhighlight %}

These search examples all leverage the SearchExecutor class. For more details on searching and more control over the search operation in general, see the [search operation documentation](docs/guide/operations/search).

###Authentication
{% highlight java %}
ConnectionConfig connConfig = new ConnectionConfig("ldap://directory.ldaptive.org");
connConfig.setUseStartTLS(true);
SearchDnResolver dnResolver = new SearchDnResolver(new DefaultConnectionFactory(connConfig));
dnResolver.setBaseDn("ou=people,dc=ldaptive,dc=org");
dnResolver.setUserFilter("uid={user}");
BindAuthenticationHandler authHandler = new BindAuthenticationHandler(new DefaultConnectionFactory(connConfig));
Authenticator auth = new Authenticator(dnResolver, authHandler);
AuthenticationResponse response = auth.authenticate(new AuthenticationRequest("dfisher", new Credential("password")));
if (response.getResult()) {
  // authentication succeeded
} else {
  // authentication failed
}
{% endhighlight %}

For more details on authentication, see the [authentication documentation](docs/guide/authentication).

