---
layout: default_v1
title: Ldaptive - active directory
redirect_from: "/v1/docs/guide/ad/"
---

# Active Directory

Active Directory provides an LDAPv3 compliant interface for performing operations and Ldaptive can be used as a client.

## Controls

The following controls have concrete implementations in Ldaptive. A matrix of control to supported provider is shown here:

|                    | JNDI | JLDAP | Apache LDAP | UnboundID | OpenDJ
|--------------------|:----:|:-----:|:-----------:|:---------:|:-----:
|DirSync             | <font color="#cc0000">✗</font>    | <font color="#6aa84f">✓</font>     | <font color="#cc0000">✗</font>           | <font color="#6aa84f">✓</font>         | <font color="#cc0000">✗</font>
|ExtendedDn          | <font color="#6aa84f">✓</font>    | <font color="#6aa84f">✓</font>     | <font color="#cc0000">✗</font>           | <font color="#6aa84f">✓</font>         | <font color="#6aa84f">✓</font>
|ForceUpdate         | <font color="#6aa84f">✓</font>    | <font color="#6aa84f">✓</font>     | <font color="#cc0000">✗</font>           | <font color="#6aa84f">✓</font>         | <font color="#6aa84f">✓</font>
|GetStats            | <font color="#6aa84f">✓</font>    | <font color="#6aa84f">✓</font>     | <font color="#cc0000">✗</font>           | <font color="#6aa84f">✓</font>         | <font color="#6aa84f">✓</font>
|LazyCommit          | <font color="#6aa84f">✓</font>    | <font color="#6aa84f">✓</font>     | <font color="#cc0000">✗</font>           | <font color="#6aa84f">✓</font>         | <font color="#6aa84f">✓</font>
|Notification        | <font color="#cc0000">✗</font>    | <font color="#6aa84f">✓</font>     | <font color="#cc0000">✗</font>           | <font color="#6aa84f">✓</font>         | <font color="#6aa84f">✓</font>
|PermissiveModify    | <font color="#6aa84f">✓</font>    | <font color="#6aa84f">✓</font>     | <font color="#cc0000">✗</font>           | <font color="#6aa84f">✓</font>         | <font color="#6aa84f">✓</font>
|RangeRetrievalNoerr | <font color="#6aa84f">✓</font>    | <font color="#6aa84f">✓</font>     | <font color="#cc0000">✗</font>           | <font color="#6aa84f">✓</font>         | <font color="#6aa84f">✓</font>
|SearchOptions       | <font color="#6aa84f">✓</font>    | <font color="#6aa84f">✓</font>     | <font color="#cc0000">✗</font>           | <font color="#6aa84f">✓</font>         | <font color="#6aa84f">✓</font>
|ShowDeactivatedLink | <font color="#6aa84f">✓</font>    | <font color="#6aa84f">✓</font>     | <font color="#cc0000">✗</font>           | <font color="#6aa84f">✓</font>         | <font color="#6aa84f">✓</font>
|ShowDeleted         | <font color="#6aa84f">✓</font>    | <font color="#6aa84f">✓</font>     | <font color="#cc0000">✗</font>           | <font color="#6aa84f">✓</font>         | <font color="#6aa84f">✓</font>
|ShowRecycled        | <font color="#6aa84f">✓</font>    | <font color="#6aa84f">✓</font>     | <font color="#cc0000">✗</font>           | <font color="#6aa84f">✓</font>         | <font color="#6aa84f">✓</font>
|VerifyName          | <font color="#6aa84f">✓</font>    | <font color="#6aa84f">✓</font>     | <font color="#cc0000">✗</font>           | <font color="#6aa84f">✓</font>         | <font color="#6aa84f">✓</font>

{% include provider-support-legend.md %}

## Global Catalog

The Global Catalog enables searching for Active Directory objects in any domain in the forest without the need for subordinate referrals. Because the Global Catalog contains only a subset of the attributes of an object, using it is viable only if the attributes requested for the search results are stored in the Global Catalog. (Note the GC is accessible on port 3268/3269, not the standard LDAP ports of 389/636.)

## Binary Attributes

Some attributes in the Active Directory may be binary and need to be declared as such when they are retrieved. To work around this issue you can invoke: `SearchRequest.setBinaryAttributes(new String[] {"objectSid", "objectGUID"})`. This will allow you to properly retrieve these attributes as byte arrays. If you prefer to use these attributes in their string forms, ldaptive provides two handlers that will automatically convert these values for you:

{% highlight java %}
{% include source_v1/ad/1.java %}
{% endhighlight %}

Using these search entry handlers will return values like:

{% highlight text %}
dn: uid=dfisher,ou=people,dc=ldaptive,dc=org
objectGUID: {B1DB3CCA-72BD-4F31-9EBF-C70CD44BDA32}
objectSid: S-1-5-21-1051162837-3568060411-1686669321-1105
{% endhighlight %}

## JNDI Provider

### Referrals

JNDI provides three ways in which to handle LDAP referrals: 'ignore', 'follow', and 'throw'. Ldaptive does not expose the 'ignore' option as this sends the ManageDsaIT control. Since Active Directory does not support this control, you will avoid `javax.naming.PartialResultException: Unprocessed Continuation Reference(s)` exceptions. By default ldaptive sets the 'throw' option and exposes any referrals in `SearchResult#getReferences()`.

If you are confident that all the referrals returned by the Active Directory can be followed then `SearchRequest#setReferralHandler(new SearchReferralHandler())` can be invoked. Note that referrals often contain hostnames other than the server that is being searched. The authentication credentials for the original connection must be valid for any hosts supplied by the referrals. In addition, these hostnames must be DNS resolvable and reachable in order for the search to be successful. `javax.naming.CommunicationException` and `java.net.UnknownHostException` are commonly encountered when following referrals.

Note that Active Directory will typically use referrals for any search bases that access domain DNS objects at the root level. e.g. dc=mydomain,dc=org.

#### Useful Links

- [Referrals in JNDI](http://docs.oracle.com/javase/jndi/tutorial/ldap/referral/jndi.html)
- [Manually Following Referrals](http://docs.oracle.com/javase/jndi/tutorial/ldap/referral/throw.html)

