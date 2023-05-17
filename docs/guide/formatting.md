---
layout: default
title: Ldaptive - formatting
redirect_from: "/docs/guide/formatting/"
---

{% include relative %}

# Reading and Writing LDAP Results

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

