---
layout: default_v1
title: Ldaptive - providers
redirect_from: "/v1/docs/guide/providers/"
---

# Providers

Ldaptive does not implement any of the LDAP protocol. Instead, LDAP operations are delegated to what we call a provider. This allows developers and deployers to change the underlying library that provides the LDAP implementation without modifying any code. By default the JNDI provider is used, but a provider can be specified programmatically:

{% highlight java %}
DefaultConnectionFactory.setProvider(new org.ldaptive.provider.unboundid.UnboundIDProvider());
{% endhighlight %}

or it can be specified with a JVM system property:

{% highlight text %}
-Dorg.ldaptive.provider=org.ldaptive.provider.unboundid.UnboundIDProvider
{% endhighlight %}

## Functionality Comparison

| Provider | SSL | startTLS | SASL External | Digest-MD5 | CRAM-MD5 | GSSAPI | Follow Referrals
|:-:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|
| JNDI | <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font>
| JLDAP | <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font> | <font color="#f1c232">✶</font> | <font color="#6aa84f">✓</font> | <font color="#cc0000">✗</font> | <font color="#cc0000">✗</font> | <font color="#6aa84f">✓</font>
| Apache LDAP | <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font> | <font color="#cc0000">✗</font> | <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font> | <font color="#cc0000">✗</font>
| UnboundID | <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font>
| OpenDJ | <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font> | <font color="#6aa84f">✓</font> | <font color="#f1c232">✶</font> | <font color="#f1c232">✶</font> | <font color="#cc0000">✗</font>

{% include provider-support-legend.md %}

## Installation & Configuration

Providers may have custom properties that aren't represented in the ldaptive configuration objects. Those properties are detailed in the following sections. If you don't like the behavior of a provider consider extending it to change it's functionality.

### JNDI Provider

This provider is included in the core library as it has no dependencies outside the Java SE, so it does not need to be installed. The following configuration properties are specific to this provider:

{% highlight java %}
/**
 * Overrides the context environment produced by ldaptive with the supplied environment.
 */
JndiProviderConfig.setEnvironment(Map<String, Object> env)
{% endhighlight %}

{% highlight java %}
/**
 * Sets the hostname verifier to use for startTLS connections.
 */
JndiProviderConfig.setHostnameVerifier(HostnameVerifier verifier)
{% endhighlight %}

{% highlight java %}
/**
 * JNDI may set an LDAP entry's DN to be a URL if the entry was ultimately found at a different baseDN than was searched on,
 * which is often the case when using referrals. This property controls whether to remove that URL from the DN.
 * The default value is true and in most cases this is the desired behavior.
 */
JndiProviderConfig.setRemoveDnUrls(boolean b)
{% endhighlight %}

{% highlight java %}
/**
 * Sets the SSL socket factory to use for SSL and startTLS connections. Setting this property will override any configuration
 * data provided in ConnectionConfig.setSslConfig(SslConfig).
 */
JndiProviderConfig.setSslSocketFactory(SSLSocketFactory sf)
{% endhighlight %}

{% highlight java %}
/**
 * Writes a hexadecimal dump of the incoming and outgoing LDAP ASN.1 BER packets to the supplied OutputStream.
 */
JndiProviderConfig.setTracePackets(OutputStream stream)
{% endhighlight %}

#### Other Links

- [http://www.oracle.com/technetwork/java/jndi/index.html](http://www.oracle.com/technetwork/java/jndi/index.html)
- [http://download.oracle.com/javase/jndi/tutorial/](http://download.oracle.com/javase/jndi/tutorial/)

### JLDAP Provider

This provider uses the JLDAP library created by Novell and often used with their eDirectory product. The following configuration properties are specific to this provider:

{% highlight java %}
/**
 * Overrides the LDAPConstraints produced by ldaptive with the supplied constraints.
 */
JLdapProviderConfig.setLDAPConstraints(LDAPConstraints constraints)
{% endhighlight %}

{% highlight java %}
/**
 * Sets the SSL socket factory to use for SSL and startTLS connections. Setting this property will override any configuration
 * data provided in ConnectionConfig.setSslConfig(SslConfig).
 */
JLdapProviderConfig.setSslSocketFactory(SSLSocketFactory sf)
{% endhighlight %}

#### Other Links 

- [http://www.openldap.org/jldap/](http://www.openldap.org/jldap/)
- [http://www.novell.com/developer/ndk/ldap_classes_for_java.html](http://www.novell.com/developer/ndk/ldap_classes_for_java.html)

### Apache LDAP Provider

This provider uses the Apache LDAP API library created in conjunction with the Apache Directory Project. This library is still under development and should be considered beta software. The following configuration properties are specific to this provider:

{% highlight java %}
/**
 * Sets the LdapConnectionConfig object used by this API. Setting this property will override any configuration
 * data provided in ConnectionConfig.
 */
ApacheLdapProviderConfig.setLdapConnectionConfig(LdapConnectionConfig config)
{% endhighlight %}

#### Other Links

- [http://directory.apache.org/api/](http://directory.apache.org/api/)

### UnboundID Provider

This provider uses the UnboundID LDAP SDK created by UnboundID for use with their directory product. The following configuration properties are specific to this provider:

{% highlight java %}
/**
 * Sets the LDAPConnectionOptions object used by this API. Setting this property will override any configuration
 * data provided in ConnectionConfig.
 */
UnboundIDProviderConfig.setConnectionOptions(LDAPConnectionOptions options)
{% endhighlight %}

{% highlight java %}
/**
 * Sets the SocketFactory to use for LDAP connections.
 */
UnboundIDProviderConfig.setSocketFactory(SocketFactory sf)
{% endhighlight %}

{% highlight java %}
/**
 * Sets the SocketFactory to use for LDAPS and startTLS connections.
 */
UnboundIDProviderConfig.setSSLSocketFactory(SSLSocketFactory sf)
{% endhighlight %}

#### Other Links

- [http://www.unboundid.com/products/ldapsdk/](http://www.unboundid.com/products/ldapsdk/)
- [http://sourceforge.net/projects/ldap-sdk/](http://sourceforge.net/projects/ldap-sdk/)

### OpenDJ Provider

This provider uses the OpenDJ LDAP SDK created in conjunction with the OpenDJ project. This project was forked from the OpenDS project. Instructions on how to install this provider can be found at: http://opendj.forgerock.org/opendj-ldap-sdk/. The following configuration properties are specific to this provider:

{% highlight java %}
/**
 * Sets the LDAPOptions object used by this API. Setting this property will override any configuration
 * data provided in ConnectionConfig.
 */
OpenDJProviderConfig.setOptions(LDAPOptions o)
{% endhighlight %}

