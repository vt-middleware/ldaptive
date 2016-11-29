---
layout: default
title: Ldaptive - account state
redirect_from: "/docs/guide/authentication/accountstate/"
---

# Account State

No standard for exposing account state data has been universally adopted by LDAP vendors. This leaves clients with vendor specific solutions that typically fall into the following categories:

- use a request/response control
- read directory attributes
- parse custom error messages/exceptions

The AuthenticationResponseHandler can be leveraged to solve this type of problem by populating the AccountState object of the AuthenticationResponse. AccountState contains Warning and Error types that are common to the most popular policy implementations. AccountState contains the following properties:

#### Warnings

expiration | date this account will expire
loginsRemaining | number of logins allowed until the account will start failing

#### Errors

code | integer code for this error
message | text for this error

Ldaptive provides several implementations for well-known directories:

## Password Policy

This request/response control is defined in the following draft: [http://tools.ietf.org/html/draft-behera-ldap-password-policy-10](http://tools.ietf.org/html/draft-behera-ldap-password-policy-10) and is most commonly used with OpenLDAP.

{% highlight java %}
{% include source/authentication/accountstate/1.java %}
{% endhighlight %}

## Active Directory

Active Directory returns account state as part of the ldap result message when a bind fails (error 49). Warnings are supported by leveraging either the 'msDS-UserPasswordExpiryTimeComputed' or 'pwdLastSet' attributes. A list of common bind errors can be found at [http://ldapwiki.willeke.com/wiki/Common%20Active%20Directory%20Bind%20Errors](http://ldapwiki.willeke.com/wiki/Common%20Active%20Directory%20Bind%20Errors)

{% highlight java %}
{% include source/authentication/accountstate/2.java %}
{% endhighlight %}

If this handler is assigned an expirationPeriod, then the 'pwdLastSet' attribute will cause the handler to emit a warning for the pwdLastSet value plus the expiration amount. The scope of that warning can be further narrowed by providing a warningPeriod. By default if the 'msDS-UserPasswordExpiryTimeComputed' attribute is found, expirationPeriod is ignored.

See [Reading User Account Password Attributes](http://technet.microsoft.com/en-us/library/ee198831.aspx).

## eDirectory

eDirectory uses a combination of result messages and attributes to convey account state. In order to parse warnings the required attributes must be requested from the Authenticator. See [http://support.novell.com/docs/Tids/Solutions/10067240.html](http://support.novell.com/docs/Tids/Solutions/10067240.html) for more discussion and an explanation of error codes.

{% highlight java %}
{% include source/authentication/accountstate/3.java %}
{% endhighlight %}

If this handler is assigned a warningPeriod, this handler will only emit warnings during that window before password expiration. Otherwise, a warning is always emitted if the 'passwordExpirationTime' attribute is found.

## FreeIPA

FreeIPA also uses a combination of result messages and attributes to convey account state.

{% highlight java %}
{% include source/authentication/accountstate/4.java %}
{% endhighlight %}

