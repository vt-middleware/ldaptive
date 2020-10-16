---
layout: default
title: Ldaptive - connections
redirect_from: "/docs/guide/connections/"
---

{% include relative %}

# Connections

Connections are created and managed using a ConnectionFactory. Ldaptive provides three implementations of ConnectionFactory: `DefaultConnectionFactory`, `PooledConnectionFactory`, and `SingleConnectionFactory`.

## DefaultConnectionFactory

`DefaultConnectionFactory` opens and closes a connection for every operation it's used to execute. This factory should be used when LDAP operations are infrequent or the cost of managing a connection pool is too high.

{% highlight java %}
{% include source/connections/1.java %}
{% endhighlight %}

## PooledConnectionFactory

`PooledConnectionFactory` maintains a pools of connection for use with an individual operation. See the [pooling guide]({{ relative }}docs/guide/connections/pooling.html) for details on how to use a `PooledConnectionFactory`.

## SingleConnectionFactory
`SingleConnectionFactory` has one connection that is used for operations. Unlike the `DefaultConnectionFactory`, the connection is not closed after each use. This factory opens the connections when `#initialize` is invoked and closed with `#close` is invoked.

## startTLS / LDAPS

When transmitting sensitive data to or from an LDAP it's important to use a secure connection. To use LDAPS, specify that in the scheme:

{% highlight java %}
{% include source/connections/2.java %}
{% endhighlight %}

To execute a StartTLS request immediately after the connection is opened:

{% highlight java %}
{% include source/connections/3.java %}
{% endhighlight %}

In practice it is not advisable to downgrade a TLS connection, after all, you've already done the hard work to establish a TLS connection. In fact, many LDAP servers don't even support the operation. The server will simply close the connection if a stopTLS request is received. Consequently ldaptive doesn't have functions for starting and stopping TLS on an open connection. You must decide whether you wish to use startTLS before the connection is opened.

### Trust Issues

When using SSL or startTLS trust errors are very common. The client must be configured to trust the server and when performing client authentication, the server must be configured to trust the client. This sections deals with how to configure your LDAP client with the proper trust and authentication material.

#### Java cacerts file

You can add either the server certificate or the server certificate's CA to the cacerts file included with your Java installation. This is the simplest solution, but be aware that it impacts the trust of all secure connections made by the JVM.

{% highlight bash %}
keytool -import -file $PATH_TO_CERT -keystore $JAVA_HOME/jre/lib/security/cacerts -alias my_server_cert
{% endhighlight %}

#### Command line options

Java supports command line options for designating both the truststore and keystore to be used for secure connections. Note that this impacts the trust of all secure connections made by the JVM.

{% highlight bash %}
java -Djavax.net.ssl.keyStore=$PATH_TO/my.keystore -Djavax.net.ssl.trustStore=$PATH_TO/my.truststore
{% endhighlight %}

When performing client authentication the JVM will select the first certificate in my.keystore that matches the allowed CAs supplied by the server. 

#### Trust Managers

If you have a implementation of javax.net.ssl.TrustManager it can be added directory to `SslConfig`. Ldaptive provides several implementations which may be helpful:

- AggregateTrustManager - combines multiple trust managers allowing the use of all trust managers or any trust manager to pass validation
- DefaultTrustManager - the default JVM trust managers
- AllowAnyTrustManager - trusts any client or server

Note that if you provide both trust managers and a credential config to the SslConfig, all trust managers will be required.

#### Credential Configuration

Ldaptive includes several classes to make the use of keystores and X509 credentials easier. CredentialConfig implementations support loading key and trust material from both the classpath and the file system.

Use a custom truststore for startTLS connections that is located on the classpath:

{% highlight java %}
{% include source/connections/4.java %}
{% endhighlight %}

Use X509 certificates for both authentication and trust that are located on the file system:

{% highlight java %}
{% include source/connections/5.java %}
{% endhighlight %}

Supported certificate formats include:

- PEM
- DER
- PKCS7

Supported private key formats include:

- PKCS8

### Hostname Validation

By default the [LDAPS endpoint identification algorithm](https://docs.oracle.com/en/java/javase/11/docs/specs/security/standard-names.html) is used for hostname validation. Custom validation can be done by setting a hostname verifier:

{% highlight java %}
{% include source/connections/6.java %}
{% endhighlight %}

## Auto Reconnect

Since LDAP connections are persistent they can be disrupted in a variety of ways: server restarts, miss-behaving load balancers, network blips, etc. Because of the myriad number of ways in which an LDAP connection may suddenly stop working ldaptive provides auto-reconnect functionality. When a disconnect is detected the connection will immediately attempt to reopen.
This example shows a reconnect condition that tries 5 times with a backoff.

{% highlight java %}
{% include source/connections/7.java %}
{% endhighlight %}

## Connection Strategies

Ldaptive provides several different strategies for connecting to multiple hosts with a single connection factory.

Name | Behavior
`ActivePassiveConnectionStrategy` | attempt each URL in the order provided for each connection; the URLs are always tried in the order in which they were provided
`RandomConnectionStrategy` | attempt a random URL from a list of URLs;
`RoundRobinConnectionStrategy` | attempt the next URL in the order provided for each connection; URLs are rotated regardless of connection success or failure
`DnsSrvConnectionStrategy` | queries a DNS server for SRV records and uses those records to construct a list of URLs; When configuring this strategy you must use your DNS server for `ConnectionConfig#ldapUrl` in the form dns://my.server.com.

{% highlight java %}
{% include source/connections/8.java %}
{% endhighlight %}

Note that all `ConnectionConfig` properties apply to every host. You cannot, for instance, configure startTLS on one host and not on another.
