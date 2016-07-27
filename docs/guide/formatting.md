---
layout: default
title: Ldaptive - formatting
redirect_from: "/docs/guide/formatting/"
---

{% include relative %}

# Reading and Writing LDAP Results

Ldaptive provides implementations for formatting LDAP results in LDIF, DSML version 1, and JSON.

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

## DSML

DSML can be written to any java.io.Writer using a Dsmlv1Writer.

{% highlight java %}
{% include source/formatting/3.java %}
{% endhighlight %}

produces:

{% highlight xml %}
<?xml version="1.0" encoding="UTF-8"?>
<dsml:dsml xmlns:dsml="http://www.dsml.org/DSML">
  <dsml:directory-entries>
    <dsml:entry dn="uid=dfisher,ou=people,dc=ldaptive,dc=org">
      <dsml:attr name="mail">
        <dsml:value>dfisher@ldaptive.org</dsml:value>
      </dsml:attr>
    </dsml:entry>
  </dsml:directory-entries>
</dsml:dsml>
{% endhighlight %}

DSML can be read using any java.io.Reader using a Dsmlv1Reader.

{% highlight java %}
{% include source/formatting/4.java %}
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
<dependencies>
{% endhighlight %}

JSON can be written to any java.io.Writer using a JsonWriter.

{% highlight java %}
{% include source/formatting/5.java %}
{% endhighlight %}

produces:

{% highlight json %}
[{"dn":"uid=dfisher,ou=people,dc=ldaptive,dc=org","mail":["dfisher@ldaptive.org"]}]
{% endhighlight %}

JSON can be read using any java.io.Reader using a JsonReader.

{% highlight java %}
{% include source/formatting/6.java %}
{% endhighlight %}

## Sorting

To control sorting when reading an LDAP result the sort behavior can be supplied to the reader:

{% highlight java %}
{% include source/formatting/7.java %}
{% endhighlight %}

Sort behavior can also be controlled by setting a JVM System property:

{% highlight text %}
-Dorg.ldaptive.sortBehavior=ORDERED
{% endhighlight %}
