---
layout: default
title: Ldaptive - active directory
redirect_from: "/docs/guide/ad/"
---

# Active Directory

Active Directory provides an LDAPv3 compliant interface for performing operations and Ldaptive can be used as a client.

## Controls

The following controls have concrete implementations in Ldaptive.

- DirSync
- ExtendedDn
- ForceUpdate
- GetStats
- LazyCommit
- Notification
- PermissiveModify
- RangeRetrievalNoerr
- SearchOptions
- ShowDeactivatedLink
- ShowDeleted
- ShowRecycled
- VerifyName

## Global Catalog

The Global Catalog enables searching for Active Directory objects in any domain in the forest without the need for subordinate referrals. Because the Global Catalog contains only a subset of the attributes of an object, using it is viable only if the attributes requested for the search results are stored in the Global Catalog. (Note the GC is accessible on port 3268/3269, not the standard LDAP ports of 389/636.)

## Binary Attributes

Some attributes in the Active Directory may be binary and need to be declared as such when they are retrieved. To work around this issue you can invoke: `SearchRequest.setBinaryAttributes(new String[] {"objectSid", "objectGUID"})`. This will allow you to properly retrieve these attributes as byte arrays. If you prefer to use these attributes in their string forms, ldaptive provides two handlers that will automatically convert these values for you:

{% highlight java %}
{% include source/ad/1.java %}
{% endhighlight %}

Using these search entry handlers will return values like:

{% highlight text %}
dn: uid=dfisher,ou=people,dc=ldaptive,dc=org
objectGUID: {B1DB3CCA-72BD-4F31-9EBF-C70CD44BDA32}
objectSid: S-1-5-21-1051162837-3568060411-1686669321-1105
{% endhighlight %}

## Referrals

If you are confident that all the referrals returned by the Active Directory can be followed then `SearchOperation#setSearchResultHandlers(new FollowSearchReferralHandler(new DefaultReferralConnectionFactory(adConnectionConfig)))` can be used. Note that referrals often contain hostnames other than the server that is being searched. If you are using the same `ConnectionConfig`, the authentication credentials must be valid for any hosts supplied by the referrals. In addition, these hostnames must be DNS resolvable and reachable in order for the search to be successful.

Note that Active Directory will typically use referrals for any search bases that access domain DNS objects at the root level. e.g. dc=mydomain,dc=org.
