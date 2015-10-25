---
layout: default
title: Ldaptive - value transcoders
redirect_from: "/docs/guide/operations/search/valuetranscoders/"
---

# Value Transcoders

In cases where LDAP attribute values represent concrete Java objects, a ValueTranscoder can be used to convert those types. The ValueTranscoder interface looks like:

{% highlight java %}
public interface ValueTranscoder<T>
{
  T decodeStringValue(String value);

  T decodeBinaryValue(byte[] value);

  String encodeStringValue(T value);

  byte[] encodeBinaryValue(T value);

  Class<T> getType();
}
{% endhighlight %}

Ldaptive provides the following value transcoder implementations:

## CertificateValueTranscoder

Provides the ability to read a PEM encoded certificate from an LDAP attribute in order to create a _java.security.cert.Certificate_ object. Conversely, a _java.security.cert.Certificate_ can also be written to the directory as a PEM encoded certificate.

{% highlight java %}
{% include source/operations/search/valuetranscoders/1.java %}
{% endhighlight %}

## GeneralizedTimeValueTranscoder

Provides the ability to read a generalized time from an LDAP attribute in order to create a _java.util.Calendar_ object. Conversely, a _java.util.Calendar_ can also be written to the directory in generalized time format.

{% highlight java %}
{% include source/operations/search/valuetranscoders/2.java %}
{% endhighlight %}

