---
layout: default
title: Ldaptive - search
redirect_from: "/docs/guide/operations/search/"
---

# Search Operation

The primary operation of LDAP servers. Provides the ability to retrieve multiple entries containing variable attribute sets using a defined query syntax. The syntax for LDAP filters is defined in [RFC 2254](http://www.ietf.org/rfc/rfc2254.txt).

Searches for entries matching: _(&(givenName=daniel)(sn=fisher))_ over the _dc=ldaptive,dc=org_ DIT and returns entries containing just the mail and displayName attributes.

{% highlight java %}
{% include source/operations/search/1.java %}
{% endhighlight %}

### SearchRequest Properties

The following properties can be configured on a per request basis:

Name | Default Value | Description
baseDn | "" | DN to search; An empty value searches the rootDSE;
filter | null | LDAP filter to execute
returnAttributes | ALL_USER | names of attributes to include in the search result
searchScope | SUBTREE | scope of the search; Valid values include: OBJECT, ONELEVEL, SUBTREE
timeLimit | 0 | length of time that a search operation should execute; A value of 0 means execute indefinitely; When the time limit arrives result will contain any result returned up to that point
sizeLimit | 0 | maximum number of entries to include in the search result; A value of 0 means includes all entries
derefAliases | NEVER | how aliases are dereferences; Valid values include: NEVER, SEARCHING, FINDING, ALWAYS
typesOnly | false | whether to return attribute types only, no values
binaryAttributes | null | attribute names that should be considered binary regardless of how they are stored

## Search Filters

The SearchFilter object provides support for both positional and named parameters. Values provided as parameters are escaped according to [RFC 2254](http://www.ietf.org/rfc/rfc2254.txt).

#### Positional

{% highlight java %}
{% include source/operations/search/2.java %}
{% endhighlight %}

#### Named

{% highlight java %}
{% include source/operations/search/3.java %}
{% endhighlight %}

In this manner applications can define custom, readable filters for their users and then set the parameters accordingly.

## Search Result Order

The result data stored in the SearchResponse, LdapEntry, and LdapAttribute objects are ordered as they are returned in the LDAP response. If you need to sort this data, static methods are available which sort elements naturally:

{% highlight java %}
{% include source/operations/search/4.java %}
{% endhighlight %}

Search results can be sorted automatically by setting the following JVM switch:

{% highlight bash %}
-Dorg.ldaptive.sortSearchResults=true
{% endhighlight %}
