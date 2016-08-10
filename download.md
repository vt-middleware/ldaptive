---
layout: default
title: Ldaptive - download
redirect_from: "/download/"
---

# Latest distribution
Download version {{ site.version }} which includes source code and classes in zip or tar.gz format

* [ldaptive-{{ site.version }}-dist.tar.gz](downloads/{{ site.version }}/ldaptive-{{ site.version }}-dist.tar.gz)   [[PGP](downloads/{{ site.version }}/ldaptive-{{ site.version }}-dist.tar.gz.asc)]
* [ldaptive-{{ site.version }}-dist.zip](downloads/{{ site.version }}/ldaptive-{{ site.version }}-dist.zip)   [[PGP](downloads/{{ site.version }}/ldaptive-{{ site.version }}-dist.zip.asc)]

Individual artifacts are available in the [Maven Central Repository](http://repo1.maven.org/maven2/org/ldaptive/). If you would like to use this project in your maven build, include the following in your pom.xml:
{% highlight xml %}
<dependencies>
  <dependency>
    <groupId>org.ldaptive</groupId>
    <artifactId>ldaptive</artifactId>
    <version>{{ site.version }}</version>
  </dependency>
<dependencies>
{% endhighlight %}

## Providers
By default the core library uses JNDI for LDAP operations. You can replace JNDI with a provider of your choice. See the [provider documentation](docs/guide/providers.html).

### UnboundID
{% highlight xml %}
<dependencies>
  <dependency>
    <groupId>org.ldaptive</groupId>
    <artifactId>ldaptive-unboundid</artifactId>
    <version>{{ site.version }}</version>
  </dependency>
<dependencies>
{% endhighlight %}

### Apache
{% highlight xml %}
<dependencies>
  <dependency>
    <groupId>org.ldaptive</groupId>
    <artifactId>ldaptive-apache</artifactId>
    <version>{{ site.version }}</version>
  </dependency>
<dependencies>
{% endhighlight %}

### JLDAP
{% highlight xml %}
<dependencies>
  <dependency>
    <groupId>org.ldaptive</groupId>
    <artifactId>ldaptive-jldap</artifactId>
    <version>{{ site.version }}</version>
  </dependency>
<dependencies>
{% endhighlight %}

### OpenDJ
This provider has not released a library to maven central and therefore must be installed locally to be used in maven builds.

## Additional Libraries

### JSON Support
{% highlight xml %}
<dependencies>
  <dependency>
    <groupId>org.ldaptive</groupId>
    <artifactId>ldaptive-json</artifactId>
    <version>{{ site.version }}</version>
  </dependency>
<dependencies>
{% endhighlight %}
