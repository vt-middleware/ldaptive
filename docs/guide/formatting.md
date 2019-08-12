---
layout: default
title: Ldaptive - formatting
redirect_from: "/docs/guide/formatting/"
---

{% include relative %}

# Reading and Writing LDAP Results

Ldaptive provides implementations for formatting LDAP results in LDIF and JSON.

## LDIF

LDIF can be written to any java.io.Writer using an LdifWriter.

{% highlight java %}
{% include source/formatting/1.java %}
{% endhighlight %}

produces:

{% highlight text %}
dn: uid=dfisher,ou=people,dc=ldaptive,dc=org
mail: dfisher@ldaptive.org
{% endhighlight %}

LDIF can be read using any java.io.Reader using an LdifReader.

{% highlight java %}
{% include source/formatting/2.java %}
{% endhighlight %}

## JSON

JSON support is provided in a separate library that uses [GSON](https://github.com/google/gson). This support is provided in a separate library that is available in the _jars_ directory of the [latest download]({{ relative }}download.html).

Or included as a maven dependency:

{% highlight xml %}
<dependencies>
  <dependency>
    <groupId>org.ldaptive</groupId>
    <artifactId>ldaptive-json</artifactId>
    <version>{{ site.version }}</version>
  </dependency>
</dependencies>
{% endhighlight %}

JSON can be written to any java.io.Writer using a JsonWriter.

{% highlight java %}
{% include source/formatting/3.java %}
{% endhighlight %}

produces:

{% highlight json %}
[{"dn":"uid=dfisher,ou=people,dc=ldaptive,dc=org","mail":["dfisher@ldaptive.org"]}]
{% endhighlight %}

JSON can be read using any java.io.Reader using a JsonReader.

{% highlight java %}
{% include source/formatting/4.java %}
{% endhighlight %}
