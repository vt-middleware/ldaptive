---
layout: default
title: Ldaptive - changelog
redirect_from: "/changelog/"
---

# Release Notes

## Version 1.2.3 - 04Oct2017

Issue | Description
:---- | :----------
[ldaptive-127]({{ site.issueURL }}127) | Add CertificateHostnameVerifier property to SslConfig.

## Version 1.2.2 - 21Jul2017

Issue | Description
:---- | :----------
[ldaptive-125]({{ site.issueURL }}125) | Add support for authentication request handling
[ldaptive-123]({{ site.issueURL }}123) | Add support for SessionTrackingControl
[ldaptive-121]({{ site.issueURL }}121) | Fix algorithm handling for CompareAuthenticationHandler

## Version 1.2.1 - 22Nov2016

Issue | Description
:---- | :----------
[ldaptive-119]({{ site.issueURL }}119) | Incorrect DN backslash escaping in JNDI provider
[ldaptive-117]({{ site.issueURL }}117) | Incorrect equals implementation

## Version 1.2.0 - 26Jul2016

Issue | Description
:---- | :----------
[ldaptive-111]({{ site.issueURL }}111) | Add passwordAttribute property to CompareAuthenticationHandler
[ldaptive-107]({{ site.issueURL }}107) | Support resolving Credential property from a resource
[ldaptive-106]({{ site.issueURL }}106) | Fix incorrect logging in AggregateDnResolver
[ldaptive-101]({{ site.issueURL }}101) | Update CaseChangeEntryHandler to support specific attributes
[ldaptive-99]({{ site.issueURL }}99) | Fix potential ClassNotFoundException in JNDI provider with SSLSocketFactory
[ldaptive-96]({{ site.issueURL }}96) | Update LDAPI package, now available in maven central
[ldaptive-89]({{ site.issueURL }}89) | Allow configuration of return attributes on the Authenticator
[ldaptive-75]({{ site.issueURL }}75) | ConnectionStrategy interface moved to the base package
[ldaptive-74]({{ site.issueURL }}74) | ActiveDirectoryAuthenticationHandler updated to use msDS-UserPasswordExpiryTimeComputed
[ldaptive-71]({{ site.issueURL }}71) | Snapshots are published to [maven-repo](https://github.com/vt-middleware/maven-repo)
[ldaptive-64]({{ site.issueURL }}64) | Refactor DnResolver to use User object
[ldaptive-61]({{ site.issueURL }}61) | Refactor JSON package to use GSON
[ldaptive-60]({{ site.issueURL }}60) | Use new java.time API instead of Calendar
[ldaptive-59]({{ site.issueURL }}59) | Use Java 8 base64 implementation
[ldaptive-58]({{ site.issueURL }}58) | Use Duration instead of int or long
[ldaptive-57]({{ site.issueURL }}57) | Add FreeIPA authentication response handler
[ldaptive-56]({{ site.issueURL }}56) | Add support for Spring extensible namespaces to the beans package

## Version 1.1.0 - 09Oct2015

Issue | Description
:---- | :----------
[ldaptive-53]({{ site.issueURL }}53) | Add minimumQueryTermLength to AbstractServletSearchTemplatesExecutor
[ldaptive-52]({{ site.issueURL }}52) | Remove TLSSocketFactory#hostnameVerifier
[ldaptive-50]({{ site.issueURL }}50) | Add support for specific attribute names in DefaultLdapEntryManager
[ldaptive-46]({{ site.issueURL }}46) | EDirectory authentication response handler should support an expiration window
[ldaptive-45]({{ site.issueURL }}45) | Can not set default sort behavior with a JVM switch
[ldaptive-44]({{ site.issueURL }}44) | BindAuthenticationHandler throws for most result codes
[ldaptive-43]({{ site.issueURL }}43) | EDirectoryAuthenticationResponseHandler prefers warning to error
[ldaptive-41]({{ site.issueURL }}41) | PasswordPolicyAuthenticationResponseHandler prefers warning to error
[ldaptive-39]({{ site.issueURL }}39) | Add cipher suites/protocol support to the UnboundIDProvider
[ldaptive-37]({{ site.issueURL }}37) | Make AbstractConnectionPool#isInitialized public
[ldaptive-36]({{ site.issueURL }}36) | JNDI DN Formating
[ldaptive-34]({{ site.issueURL }}34) | DefaultLdapEntryManager find implementation is broken
[ldaptive-33]({{ site.issueURL }}33) | Entry mapper implementations are not generified
[ldaptive-31]({{ site.issueURL }}31) | Schema parsing does not support empty DESC
[ldaptive-29]({{ site.issueURL }}29) | Add resolvedDN to the authentication response
[ldaptive-28]({{ site.issueURL }}28) | Add ldaptive namespace for Spring extensible XML
[ldaptive-25]({{ site.issueURL }}25) | Add ldaptive support for following referrals
[ldaptive-23]({{ site.issueURL }}23) | JNDI only returns first referral URL
[ldaptive-20]({{ site.issueURL }}20) | SpringLdapEntryMapper doesn't map byte array correctly
[ldaptive-19]({{ site.issueURL }}19) | Improve pool log levels
[ldaptive-16]({{ site.issueURL }}16) | Support Java 7 try-with-resources
[ldaptive-14]({{ site.issueURL }}14) | Providers shouldn't detect binary attributes
[ldaptive-7]({{ site.issueURL }}7) | Add entry resolver that leverages the authorization identity control
[ldaptive-5]({{ site.issueURL }}5) | Add support for resolving LDAP servers via SRV DNS records

## Version 1.0.6 - 27Feb2015

Issue | Description
:---- | :----------
[ldaptive-27]({{ site.issueURL }}27) | Webapp should escape HTML in attribute values
[ldaptive-24]({{ site.issueURL }}24) | ThreadLocalTLSSocketFactory not reinitialized by JNDI
[ldaptive-21]({{ site.issueURL }}21) | AggregateDnResolver uses incorrect class in it's AuthenticationHandler
[ldaptive-14]({{ site.issueURL }}14) | Detection of binary attributes
[ldaptive-9]({{ site.issueURL }}9) | Add an entry handler for the AD primary group attribute

## Version 1.0.5 - 24Oct2014

Issue | Description
:---- | :----------
[ldaptive-230]({{ site.gIssueURL }}230) | Add support for the authorization identity control
[ldaptive-229]({{ site.gIssueURL }}229) | BER lengths are decoded as signed 2s complement
[ldaptive-228]({{ site.gIssueURL }}228) | Add support for parsing ASN1 distinguished names
[ldaptive-227]({{ site.gIssueURL }}227) | Default hostname verifier should not parse hostname as a string
[ldaptive-225]({{ site.gIssueURL }}225) | AggregateDnResolver blocks forever
[ldaptive-224]({{ site.gIssueURL }}224) | Refactored SearchEntryResolver to support subtree searches
[ldaptive-223]({{ site.gIssueURL }}223) | Added support for binaryAttributes property in the UnboundID provider

## Version 1.0.4 - 02Jul2014

Issue | Description
:---- | :----------
[ldaptive-219]({{ site.gIssueURL }}219) | fix ClassCastException for SearchReferences with sorted results
[ldaptive-218]({{ site.gIssueURL }}218) | support transcoder in @Attribute annotation
[ldaptive-216]({{ site.gIssueURL }}216) | add name property to connection pools
[ldaptive-215]({{ site.gIssueURL }}215) | refactor ConnectionStrategy from an enum to an interface
[ldaptive-214]({{ site.gIssueURL }}214) | connection pool initialization failures should propagate out
[ldaptive-211]({{ site.gIssueURL }}211) | generate java POJOs from LDAP schema
[ldaptive-206]({{ site.gIssueURL }}206) | add LDAPI support
[ldaptive-160]({{ site.gIssueURL }}160) | add bean persistence support

## Version 1.0.3 - 01Apr2014

Issue | Description
:---- | :----------
[ldaptive-213]({{ site.gIssueURL }}213) | fix JNDI format DN for directories that treat baseDn differently
[ldaptive-212]({{ site.gIssueURL }}212) | aggregrate trust manager should allow any
[ldaptive-210]({{ site.gIssueURL }}210) | add value transcoder for UUID
[ldaptive-209]({{ site.gIssueURL }}209) | LdifReader throws on mixed values
[ldaptive-208]({{ site.gIssueURL }}208) | add schema support classes
[ldaptive-205]({{ site.gIssueURL }}205) | add support for password expiration warnings in Active Directory

## Version 1.0.2 - 22Nov2013

Issue | Description
:---- | :----------
[ldaptive-200]({{ site.gIssueURL }}200) | add failfast property for pool initialization
[ldaptive-199]({{ site.gIssueURL }}199) | provide blocking queue based search executor for memory constrained searching
[ldaptive-197]({{ site.gIssueURL }}197) | allow for selecting a specific alias when using KeyStores in SSL
[ldaptive-196]({{ site.gIssueURL }}196) | provide capacity controls for classes that use blocking queues
[ldaptive-195]({{ site.gIssueURL }}195) | provide an enum for static return attribute values
[ldaptive-194]({{ site.gIssueURL }}194) | expose attribute value encoding methods on SearchFilter
[ldaptive-193]({{ site.gIssueURL }}193) | fix concurrent modification exception in recursive entry handler
[ldaptive-191]({{ site.gIssueURL }}191) | escape user input in FormatDnResolver
[ldaptive-183]({{ site.gIssueURL }}183) | add support for the persitent search request control (draft-ietf-ldapext-psearch-03)
[ldaptive-178]({{ site.gIssueURL }}178) | add support for the proxied authorization control (RFC 4370)
[ldaptive-177]({{ site.gIssueURL }}177) | expose the authentication request in authentication criteria
[ldaptive-176]({{ site.gIssueURL }}176) | provide support for encoding and decoding of Active Directory unicodePwd attribute

## Version 1.0.1 - 01July2013

Issue | Description
:---- | :----------
[ldaptive-172]({{ site.gIssueURL }}172) | allow adding of search entry handlers to search entry resolvers
[ldaptive-171]({{ site.gIssueURL }}171) | search servlet should check for empty query
[ldaptive-170]({{ site.gIssueURL }}170) | add bind passivator implementation
[ldaptive-169]({{ site.gIssueURL }}169) | update unboundid sdk version (2.3.4)
[ldaptive-168]({{ site.gIssueURL }}168) | update apache ldap api version (1.0.0-M18)
[ldaptive-167]({{ site.gIssueURL }}167) | add support for parsing Generalized Time attributes
[ldaptive-166]({{ site.gIssueURL }}166) | update ObjectGuidHandler and ObjectSidHandler to support any attribute in that format
[ldaptive-162]({{ site.gIssueURL }}162) | update unboundid sdk version (2.3.3)
[ldaptive-161]({{ site.gIssueURL }}161) | update apache ldap api version (1.0.0-M17)
[ldaptive-159]({{ site.gIssueURL }}159) | is/set method properties not supported by the property invoker
[ldaptive-157]({{ site.gIssueURL }}157) | property source should not overwrite provider config settings unless data was found
[ldaptive-156]({{ site.gIssueURL }}156) | add ability to create credential config from preexisting credentials
[ldaptive-150]({{ site.gIssueURL }}150) | NullPointerException thrown by BlockingConnectionPool#toString() if invoked before #initialize()
[ldaptive-149]({{ site.gIssueURL }}149) | add support for client side syncing of LDAP entries with a MergeOperation
[ldaptive-148]({{ site.gIssueURL }}148) | add support for creating X509CredentialConfig from a PEM encoded String
[ldaptive-146]({{ site.gIssueURL }}146) | using sorted attributes with multiple binary values causes a ClassCastException

## Version 1.0 - 15March2013
Initial Release
