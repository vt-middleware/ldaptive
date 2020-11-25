---
layout: default
title: Ldaptive - templates
redirect_from: "/docs/guide/templates/"
---

{% include relative %}

# Templates

The templates project provides a framework for accepting some number of words (or terms) and producing search results from an LDAP. The most common use case being free form search input, like one would expect for a search engine.

A `SearchTemplatesOperation` is configured with an ordered number of `SearchTemplates`. The first `SearchTemplates` object is expected to format search filters for one term queries. The second `SearchTemplates` object is expected to format search filters for two term queries, and so forth. The appropriate `SearchTemplates` is selected based on the `Query` that is provided to the operation and a search is executed for each of the filters in that templates. A single `SearchResponse` containing all results is returned. In this fashion a set of search terms can be transformed into LDAP search results.

Some named parameters are defined by the templates in order to write search filters:

- {term1} is replaced with the first query term
- {initial1} is replaced with the first letter of the first query term
- {term2} is replaced with the second query term
- {initial2} is replaced with the first letter of the second query term
- ...
- {termN} is replaced with the Nth query term
- {initialN} is replaced with the first letter of the Nth query term

{% highlight java %}
{% include source/templates/1.java %}
{% endhighlight %}

Templates support is provided in a separate library that is available in the _jars_ directory of the [latest download]({{ relative }}download.html).

Or included as a maven dependency:

{% highlight xml %}
 <dependencies>
   <dependency>
     <groupId>org.ldaptive</groupId>
     <artifactId>ldaptive-templates</artifactId>
     <version>{{ site.version }}</version>
   </dependency>
</dependencies>
{% endhighlight %}

