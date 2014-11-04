
package org.ldaptive.beans.generate;

import java.util.Collection;
import org.ldaptive.LdapUtils;
import org.ldaptive.beans.Attribute;
import org.ldaptive.beans.Entry;


/**
 * Ldaptive generated bean for objectClass 'organizationalPerson'
 * 
 */
@Entry(dn = "dn", attributes = {
    @Attribute(name = "customname1", values = "customvalue1"),
    @Attribute(name = "customname2", values = {"customvalue1", "customvalue2"}),
    @Attribute(name = "bridgeheadTransportList"),
    @Attribute(name = "businessCategory"),
    @Attribute(name = "c"),
    @Attribute(name = "cn"),
    @Attribute(name = "co"),
    @Attribute(name = "countryCode"),
    @Attribute(name = "dNSHostName"),
    @Attribute(name = "dSHeuristics"),
    @Attribute(name = "defaultGroup"),
    @Attribute(name = "description"),
    @Attribute(name = "desktopProfile"),
    @Attribute(name = "destinationIndicator"),
    @Attribute(name = "facsimileTelephoneNumber"),
    @Attribute(name = "gPLink"),
    @Attribute(name = "gPOptions"),
    @Attribute(name = "garbageCollPeriod"),
    @Attribute(name = "homePostalAddress"),
    @Attribute(name = "houseIdentifier"),
    @Attribute(name = "internationalISDNNumber"),
    @Attribute(name = "l"),
    @Attribute(name = "location"),
    @Attribute(name = "mSMQInterval1"),
    @Attribute(name = "mSMQInterval2"),
    @Attribute(name = "mSMQNt4Stub"),
    @Attribute(name = "mSMQSiteForeign"),
    @Attribute(name = "mSMQSiteID"),
    @Attribute(name = "mailAddress"),
    @Attribute(name = "managedBy"),
    @Attribute(name = "msCOM-UserPartitionSetLink", property = "msCOMUserPartitionSetLink"),
    @Attribute(name = "msDS-AllowedDNSSuffixes", property = "msDSAllowedDNSSuffixes"),
    @Attribute(name = "msDS-AzApplicationData", property = "msDSAzApplicationData"),
    @Attribute(name = "msDS-AzApplicationName", property = "msDSAzApplicationName"),
    @Attribute(name = "msDS-AzApplicationVersion", property = "msDSAzApplicationVersion"),
    @Attribute(name = "msDS-AzClassId", property = "msDSAzClassId"),
    @Attribute(name = "msDS-AzDomainTimeout", property = "msDSAzDomainTimeout"),
    @Attribute(name = "msDS-AzGenerateAudits", property = "msDSAzGenerateAudits"),
    @Attribute(name = "msDS-AzGenericData", property = "msDSAzGenericData"),
    @Attribute(name = "msDS-AzMajorVersion", property = "msDSAzMajorVersion"),
    @Attribute(name = "msDS-AzMinorVersion", property = "msDSAzMinorVersion"),
    @Attribute(name = "msDS-AzObjectGuid", property = "msDSAzObjectGuid"),
    @Attribute(name = "msDS-AzScopeName", property = "msDSAzScopeName"),
    @Attribute(name = "msDS-AzScriptEngineCacheMax", property = "msDSAzScriptEngineCacheMax"),
    @Attribute(name = "msDS-AzScriptTimeout", property = "msDSAzScriptTimeout"),
    @Attribute(name = "msDS-Behavior-Version", property = "msDSBehaviorVersion"),
    @Attribute(name = "msDS-BridgeHeadServersUsed", property = "msDSBridgeHeadServersUsed"),
    @Attribute(name = "msDS-DeletedObjectLifetime", property = "msDSDeletedObjectLifetime"),
    @Attribute(name = "msDS-EnabledFeature", property = "msDSEnabledFeature"),
    @Attribute(name = "msDS-HABSeniorityIndex", property = "msDSHABSeniorityIndex"),
    @Attribute(name = "msDS-IsUserCachableAtRodc", property = "msDSIsUserCachableAtRodc"),
    @Attribute(name = "msDS-ObjectReference", property = "msDSObjectReference"),
    @Attribute(name = "msDS-Other-Settings", property = "msDSOtherSettings"),
    @Attribute(name = "msDS-PhoneticCompanyName", property = "msDSPhoneticCompanyName"),
    @Attribute(name = "msDS-PhoneticDepartment", property = "msDSPhoneticDepartment"),
    @Attribute(name = "msDS-PhoneticDisplayName", property = "msDSPhoneticDisplayName"),
    @Attribute(name = "msDS-PhoneticFirstName", property = "msDSPhoneticFirstName"),
    @Attribute(name = "msDS-PhoneticLastName", property = "msDSPhoneticLastName"),
    @Attribute(name = "msDS-SiteName", property = "msDSSiteName"),
    @Attribute(name = "msDS-USNLastSyncSuccess", property = "msDSUSNLastSyncSuccess"),
    @Attribute(name = "msDS-isGC", property = "msDSisGC"),
    @Attribute(name = "msDS-isRODC", property = "msDSisRODC"),
    @Attribute(name = "msExchHouseIdentifier"),
    @Attribute(name = "notificationList"),
    @Attribute(name = "o"),
    @Attribute(name = "ou"),
    @Attribute(name = "physicalDeliveryOfficeName"),
    @Attribute(name = "physicalLocationObject"),
    @Attribute(name = "postOfficeBox"),
    @Attribute(name = "postalAddress"),
    @Attribute(name = "postalCode"),
    @Attribute(name = "preferredDeliveryMethod"),
    @Attribute(name = "registeredAddress"),
    @Attribute(name = "replTopologyStayOfExecution"),
    @Attribute(name = "sPNMappings"),
    @Attribute(name = "searchGuide"),
    @Attribute(name = "seeAlso"),
    @Attribute(name = "serialNumber"),
    @Attribute(name = "serverReference"),
    @Attribute(name = "siteObject"),
    @Attribute(name = "st"),
    @Attribute(name = "street"),
    @Attribute(name = "telephoneNumber"),
    @Attribute(name = "teletexTerminalIdentifier"),
    @Attribute(name = "telexNumber"),
    @Attribute(name = "thumbnailLogo"),
    @Attribute(name = "tombstoneLifetime"),
    @Attribute(name = "uPNSuffixes"),
    @Attribute(name = "userPassword"),
    @Attribute(name = "x121Address")
})
public class OrganizationalPerson {

    private String dn;
    private Collection<String> bridgeheadTransportList;
    private Collection<String> businessCategory;
    private String c;
    private String cn;
    private String co;
    private String countryCode;
    private String dNSHostName;
    private String dSHeuristics;
    private String defaultGroup;
    private Collection<String> description;
    private String desktopProfile;
    private Collection<String> destinationIndicator;
    private String facsimileTelephoneNumber;
    private String gPLink;
    private String gPOptions;
    private String garbageCollPeriod;
    private String homePostalAddress;
    private Collection<String> houseIdentifier;
    private Collection<String> internationalISDNNumber;
    private String l;
    private String location;
    private String mSMQInterval1;
    private String mSMQInterval2;
    private Collection<String> mSMQNt4Stub;
    private Collection<String> mSMQSiteForeign;
    private String mSMQSiteID;
    private String mailAddress;
    private String managedBy;
    private String msCOMUserPartitionSetLink;
    private Collection<String> msDSAllowedDNSSuffixes;
    private String msDSAzApplicationData;
    private String msDSAzApplicationName;
    private String msDSAzApplicationVersion;
    private String msDSAzClassId;
    private String msDSAzDomainTimeout;
    private String msDSAzGenerateAudits;
    private String msDSAzGenericData;
    private String msDSAzMajorVersion;
    private String msDSAzMinorVersion;
    private String msDSAzObjectGuid;
    private String msDSAzScopeName;
    private String msDSAzScriptEngineCacheMax;
    private String msDSAzScriptTimeout;
    private String msDSBehaviorVersion;
    private Collection<String> msDSBridgeHeadServersUsed;
    private String msDSDeletedObjectLifetime;
    private Collection<String> msDSEnabledFeature;
    private String msDSHABSeniorityIndex;
    private String msDSIsUserCachableAtRodc;
    private Collection<String> msDSObjectReference;
    private Collection<String> msDSOtherSettings;
    private String msDSPhoneticCompanyName;
    private String msDSPhoneticDepartment;
    private String msDSPhoneticDisplayName;
    private String msDSPhoneticFirstName;
    private String msDSPhoneticLastName;
    private String msDSSiteName;
    private String msDSUSNLastSyncSuccess;
    private String msDSisGC;
    private String msDSisRODC;
    private String msExchHouseIdentifier;
    private String notificationList;
    private Collection<String> o;
    private Collection<String> ou;
    private String physicalDeliveryOfficeName;
    private String physicalLocationObject;
    private Collection<String> postOfficeBox;
    private Collection<String> postalAddress;
    private String postalCode;
    private Collection<String> preferredDeliveryMethod;
    private Collection<String> registeredAddress;
    private String replTopologyStayOfExecution;
    private Collection<String> sPNMappings;
    private Collection<String> searchGuide;
    private Collection<String> seeAlso;
    private Collection<String> serialNumber;
    private String serverReference;
    private String siteObject;
    private String st;
    private String street;
    private String telephoneNumber;
    private Collection<String> teletexTerminalIdentifier;
    private Collection<String> telexNumber;
    private String thumbnailLogo;
    private String tombstoneLifetime;
    private Collection<String> uPNSuffixes;
    private Collection<String> userPassword;
    private Collection<String> x121Address;

    public String getDn() {
        return dn;
    }

    public void setDn(String s) {
        this.dn = s;
    }

    public Collection<String> getBridgeheadTransportList() {
        return bridgeheadTransportList;
    }

    public void setBridgeheadTransportList(Collection<String> c) {
        this.bridgeheadTransportList = c;
    }

    public Collection<String> getBusinessCategory() {
        return businessCategory;
    }

    public void setBusinessCategory(Collection<String> c) {
        this.businessCategory = c;
    }

    public String getC() {
        return c;
    }

    public void setC(String s) {
        this.c = s;
    }

    public String getCn() {
        return cn;
    }

    public void setCn(String s) {
        this.cn = s;
    }

    public String getCo() {
        return co;
    }

    public void setCo(String s) {
        this.co = s;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String s) {
        this.countryCode = s;
    }

    public String getDNSHostName() {
        return dNSHostName;
    }

    public void setDNSHostName(String s) {
        this.dNSHostName = s;
    }

    public String getDSHeuristics() {
        return dSHeuristics;
    }

    public void setDSHeuristics(String s) {
        this.dSHeuristics = s;
    }

    public String getDefaultGroup() {
        return defaultGroup;
    }

    public void setDefaultGroup(String s) {
        this.defaultGroup = s;
    }

    public Collection<String> getDescription() {
        return description;
    }

    public void setDescription(Collection<String> c) {
        this.description = c;
    }

    public String getDesktopProfile() {
        return desktopProfile;
    }

    public void setDesktopProfile(String s) {
        this.desktopProfile = s;
    }

    public Collection<String> getDestinationIndicator() {
        return destinationIndicator;
    }

    public void setDestinationIndicator(Collection<String> c) {
        this.destinationIndicator = c;
    }

    public String getFacsimileTelephoneNumber() {
        return facsimileTelephoneNumber;
    }

    public void setFacsimileTelephoneNumber(String s) {
        this.facsimileTelephoneNumber = s;
    }

    public String getGPLink() {
        return gPLink;
    }

    public void setGPLink(String s) {
        this.gPLink = s;
    }

    public String getGPOptions() {
        return gPOptions;
    }

    public void setGPOptions(String s) {
        this.gPOptions = s;
    }

    public String getGarbageCollPeriod() {
        return garbageCollPeriod;
    }

    public void setGarbageCollPeriod(String s) {
        this.garbageCollPeriod = s;
    }

    public String getHomePostalAddress() {
        return homePostalAddress;
    }

    public void setHomePostalAddress(String s) {
        this.homePostalAddress = s;
    }

    public Collection<String> getHouseIdentifier() {
        return houseIdentifier;
    }

    public void setHouseIdentifier(Collection<String> c) {
        this.houseIdentifier = c;
    }

    public Collection<String> getInternationalISDNNumber() {
        return internationalISDNNumber;
    }

    public void setInternationalISDNNumber(Collection<String> c) {
        this.internationalISDNNumber = c;
    }

    public String getL() {
        return l;
    }

    public void setL(String s) {
        this.l = s;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String s) {
        this.location = s;
    }

    public String getMSMQInterval1() {
        return mSMQInterval1;
    }

    public void setMSMQInterval1(String s) {
        this.mSMQInterval1 = s;
    }

    public String getMSMQInterval2() {
        return mSMQInterval2;
    }

    public void setMSMQInterval2(String s) {
        this.mSMQInterval2 = s;
    }

    public Collection<String> getMSMQNt4Stub() {
        return mSMQNt4Stub;
    }

    public void setMSMQNt4Stub(Collection<String> c) {
        this.mSMQNt4Stub = c;
    }

    public Collection<String> getMSMQSiteForeign() {
        return mSMQSiteForeign;
    }

    public void setMSMQSiteForeign(Collection<String> c) {
        this.mSMQSiteForeign = c;
    }

    public String getMSMQSiteID() {
        return mSMQSiteID;
    }

    public void setMSMQSiteID(String s) {
        this.mSMQSiteID = s;
    }

    public String getMailAddress() {
        return mailAddress;
    }

    public void setMailAddress(String s) {
        this.mailAddress = s;
    }

    public String getManagedBy() {
        return managedBy;
    }

    public void setManagedBy(String s) {
        this.managedBy = s;
    }

    public String getMsCOMUserPartitionSetLink() {
        return msCOMUserPartitionSetLink;
    }

    public void setMsCOMUserPartitionSetLink(String s) {
        this.msCOMUserPartitionSetLink = s;
    }

    public Collection<String> getMsDSAllowedDNSSuffixes() {
        return msDSAllowedDNSSuffixes;
    }

    public void setMsDSAllowedDNSSuffixes(Collection<String> c) {
        this.msDSAllowedDNSSuffixes = c;
    }

    public String getMsDSAzApplicationData() {
        return msDSAzApplicationData;
    }

    public void setMsDSAzApplicationData(String s) {
        this.msDSAzApplicationData = s;
    }

    public String getMsDSAzApplicationName() {
        return msDSAzApplicationName;
    }

    public void setMsDSAzApplicationName(String s) {
        this.msDSAzApplicationName = s;
    }

    public String getMsDSAzApplicationVersion() {
        return msDSAzApplicationVersion;
    }

    public void setMsDSAzApplicationVersion(String s) {
        this.msDSAzApplicationVersion = s;
    }

    public String getMsDSAzClassId() {
        return msDSAzClassId;
    }

    public void setMsDSAzClassId(String s) {
        this.msDSAzClassId = s;
    }

    public String getMsDSAzDomainTimeout() {
        return msDSAzDomainTimeout;
    }

    public void setMsDSAzDomainTimeout(String s) {
        this.msDSAzDomainTimeout = s;
    }

    public String getMsDSAzGenerateAudits() {
        return msDSAzGenerateAudits;
    }

    public void setMsDSAzGenerateAudits(String s) {
        this.msDSAzGenerateAudits = s;
    }

    public String getMsDSAzGenericData() {
        return msDSAzGenericData;
    }

    public void setMsDSAzGenericData(String s) {
        this.msDSAzGenericData = s;
    }

    public String getMsDSAzMajorVersion() {
        return msDSAzMajorVersion;
    }

    public void setMsDSAzMajorVersion(String s) {
        this.msDSAzMajorVersion = s;
    }

    public String getMsDSAzMinorVersion() {
        return msDSAzMinorVersion;
    }

    public void setMsDSAzMinorVersion(String s) {
        this.msDSAzMinorVersion = s;
    }

    public String getMsDSAzObjectGuid() {
        return msDSAzObjectGuid;
    }

    public void setMsDSAzObjectGuid(String s) {
        this.msDSAzObjectGuid = s;
    }

    public String getMsDSAzScopeName() {
        return msDSAzScopeName;
    }

    public void setMsDSAzScopeName(String s) {
        this.msDSAzScopeName = s;
    }

    public String getMsDSAzScriptEngineCacheMax() {
        return msDSAzScriptEngineCacheMax;
    }

    public void setMsDSAzScriptEngineCacheMax(String s) {
        this.msDSAzScriptEngineCacheMax = s;
    }

    public String getMsDSAzScriptTimeout() {
        return msDSAzScriptTimeout;
    }

    public void setMsDSAzScriptTimeout(String s) {
        this.msDSAzScriptTimeout = s;
    }

    public String getMsDSBehaviorVersion() {
        return msDSBehaviorVersion;
    }

    public void setMsDSBehaviorVersion(String s) {
        this.msDSBehaviorVersion = s;
    }

    public Collection<String> getMsDSBridgeHeadServersUsed() {
        return msDSBridgeHeadServersUsed;
    }

    public void setMsDSBridgeHeadServersUsed(Collection<String> c) {
        this.msDSBridgeHeadServersUsed = c;
    }

    public String getMsDSDeletedObjectLifetime() {
        return msDSDeletedObjectLifetime;
    }

    public void setMsDSDeletedObjectLifetime(String s) {
        this.msDSDeletedObjectLifetime = s;
    }

    public Collection<String> getMsDSEnabledFeature() {
        return msDSEnabledFeature;
    }

    public void setMsDSEnabledFeature(Collection<String> c) {
        this.msDSEnabledFeature = c;
    }

    public String getMsDSHABSeniorityIndex() {
        return msDSHABSeniorityIndex;
    }

    public void setMsDSHABSeniorityIndex(String s) {
        this.msDSHABSeniorityIndex = s;
    }

    public String getMsDSIsUserCachableAtRodc() {
        return msDSIsUserCachableAtRodc;
    }

    public void setMsDSIsUserCachableAtRodc(String s) {
        this.msDSIsUserCachableAtRodc = s;
    }

    public Collection<String> getMsDSObjectReference() {
        return msDSObjectReference;
    }

    public void setMsDSObjectReference(Collection<String> c) {
        this.msDSObjectReference = c;
    }

    public Collection<String> getMsDSOtherSettings() {
        return msDSOtherSettings;
    }

    public void setMsDSOtherSettings(Collection<String> c) {
        this.msDSOtherSettings = c;
    }

    public String getMsDSPhoneticCompanyName() {
        return msDSPhoneticCompanyName;
    }

    public void setMsDSPhoneticCompanyName(String s) {
        this.msDSPhoneticCompanyName = s;
    }

    public String getMsDSPhoneticDepartment() {
        return msDSPhoneticDepartment;
    }

    public void setMsDSPhoneticDepartment(String s) {
        this.msDSPhoneticDepartment = s;
    }

    public String getMsDSPhoneticDisplayName() {
        return msDSPhoneticDisplayName;
    }

    public void setMsDSPhoneticDisplayName(String s) {
        this.msDSPhoneticDisplayName = s;
    }

    public String getMsDSPhoneticFirstName() {
        return msDSPhoneticFirstName;
    }

    public void setMsDSPhoneticFirstName(String s) {
        this.msDSPhoneticFirstName = s;
    }

    public String getMsDSPhoneticLastName() {
        return msDSPhoneticLastName;
    }

    public void setMsDSPhoneticLastName(String s) {
        this.msDSPhoneticLastName = s;
    }

    public String getMsDSSiteName() {
        return msDSSiteName;
    }

    public void setMsDSSiteName(String s) {
        this.msDSSiteName = s;
    }

    public String getMsDSUSNLastSyncSuccess() {
        return msDSUSNLastSyncSuccess;
    }

    public void setMsDSUSNLastSyncSuccess(String s) {
        this.msDSUSNLastSyncSuccess = s;
    }

    public String getMsDSisGC() {
        return msDSisGC;
    }

    public void setMsDSisGC(String s) {
        this.msDSisGC = s;
    }

    public String getMsDSisRODC() {
        return msDSisRODC;
    }

    public void setMsDSisRODC(String s) {
        this.msDSisRODC = s;
    }

    public String getMsExchHouseIdentifier() {
        return msExchHouseIdentifier;
    }

    public void setMsExchHouseIdentifier(String s) {
        this.msExchHouseIdentifier = s;
    }

    public String getNotificationList() {
        return notificationList;
    }

    public void setNotificationList(String s) {
        this.notificationList = s;
    }

    public Collection<String> getO() {
        return o;
    }

    public void setO(Collection<String> c) {
        this.o = c;
    }

    public Collection<String> getOu() {
        return ou;
    }

    public void setOu(Collection<String> c) {
        this.ou = c;
    }

    public String getPhysicalDeliveryOfficeName() {
        return physicalDeliveryOfficeName;
    }

    public void setPhysicalDeliveryOfficeName(String s) {
        this.physicalDeliveryOfficeName = s;
    }

    public String getPhysicalLocationObject() {
        return physicalLocationObject;
    }

    public void setPhysicalLocationObject(String s) {
        this.physicalLocationObject = s;
    }

    public Collection<String> getPostOfficeBox() {
        return postOfficeBox;
    }

    public void setPostOfficeBox(Collection<String> c) {
        this.postOfficeBox = c;
    }

    public Collection<String> getPostalAddress() {
        return postalAddress;
    }

    public void setPostalAddress(Collection<String> c) {
        this.postalAddress = c;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String s) {
        this.postalCode = s;
    }

    public Collection<String> getPreferredDeliveryMethod() {
        return preferredDeliveryMethod;
    }

    public void setPreferredDeliveryMethod(Collection<String> c) {
        this.preferredDeliveryMethod = c;
    }

    public Collection<String> getRegisteredAddress() {
        return registeredAddress;
    }

    public void setRegisteredAddress(Collection<String> c) {
        this.registeredAddress = c;
    }

    public String getReplTopologyStayOfExecution() {
        return replTopologyStayOfExecution;
    }

    public void setReplTopologyStayOfExecution(String s) {
        this.replTopologyStayOfExecution = s;
    }

    public Collection<String> getSPNMappings() {
        return sPNMappings;
    }

    public void setSPNMappings(Collection<String> c) {
        this.sPNMappings = c;
    }

    public Collection<String> getSearchGuide() {
        return searchGuide;
    }

    public void setSearchGuide(Collection<String> c) {
        this.searchGuide = c;
    }

    public Collection<String> getSeeAlso() {
        return seeAlso;
    }

    public void setSeeAlso(Collection<String> c) {
        this.seeAlso = c;
    }

    public Collection<String> getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(Collection<String> c) {
        this.serialNumber = c;
    }

    public String getServerReference() {
        return serverReference;
    }

    public void setServerReference(String s) {
        this.serverReference = s;
    }

    public String getSiteObject() {
        return siteObject;
    }

    public void setSiteObject(String s) {
        this.siteObject = s;
    }

    public String getSt() {
        return st;
    }

    public void setSt(String s) {
        this.st = s;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String s) {
        this.street = s;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String s) {
        this.telephoneNumber = s;
    }

    public Collection<String> getTeletexTerminalIdentifier() {
        return teletexTerminalIdentifier;
    }

    public void setTeletexTerminalIdentifier(Collection<String> c) {
        this.teletexTerminalIdentifier = c;
    }

    public Collection<String> getTelexNumber() {
        return telexNumber;
    }

    public void setTelexNumber(Collection<String> c) {
        this.telexNumber = c;
    }

    public String getThumbnailLogo() {
        return thumbnailLogo;
    }

    public void setThumbnailLogo(String s) {
        this.thumbnailLogo = s;
    }

    public String getTombstoneLifetime() {
        return tombstoneLifetime;
    }

    public void setTombstoneLifetime(String s) {
        this.tombstoneLifetime = s;
    }

    public Collection<String> getUPNSuffixes() {
        return uPNSuffixes;
    }

    public void setUPNSuffixes(Collection<String> c) {
        this.uPNSuffixes = c;
    }

    public Collection<String> getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(Collection<String> c) {
        this.userPassword = c;
    }

    public Collection<String> getX121Address() {
        return x121Address;
    }

    public void setX121Address(Collection<String> c) {
        this.x121Address = c;
    }

    @Override
    public int hashCode() {
        return LdapUtils.computeHashCode(7919, this.dn, this.bridgeheadTransportList, this.businessCategory, this.c, this.cn, this.co, this.countryCode, this.dNSHostName, this.dSHeuristics, this.defaultGroup, this.description, this.desktopProfile, this.destinationIndicator, this.facsimileTelephoneNumber, this.gPLink, this.gPOptions, this.garbageCollPeriod, this.homePostalAddress, this.houseIdentifier, this.internationalISDNNumber, this.l, this.location, this.mSMQInterval1, this.mSMQInterval2, this.mSMQNt4Stub, this.mSMQSiteForeign, this.mSMQSiteID, this.mailAddress, this.managedBy, this.msCOMUserPartitionSetLink, this.msDSAllowedDNSSuffixes, this.msDSAzApplicationData, this.msDSAzApplicationName, this.msDSAzApplicationVersion, this.msDSAzClassId, this.msDSAzDomainTimeout, this.msDSAzGenerateAudits, this.msDSAzGenericData, this.msDSAzMajorVersion, this.msDSAzMinorVersion, this.msDSAzObjectGuid, this.msDSAzScopeName, this.msDSAzScriptEngineCacheMax, this.msDSAzScriptTimeout, this.msDSBehaviorVersion, this.msDSBridgeHeadServersUsed, this.msDSDeletedObjectLifetime, this.msDSEnabledFeature, this.msDSHABSeniorityIndex, this.msDSIsUserCachableAtRodc, this.msDSObjectReference, this.msDSOtherSettings, this.msDSPhoneticCompanyName, this.msDSPhoneticDepartment, this.msDSPhoneticDisplayName, this.msDSPhoneticFirstName, this.msDSPhoneticLastName, this.msDSSiteName, this.msDSUSNLastSyncSuccess, this.msDSisGC, this.msDSisRODC, this.msExchHouseIdentifier, this.notificationList, this.o, this.ou, this.physicalDeliveryOfficeName, this.physicalLocationObject, this.postOfficeBox, this.postalAddress, this.postalCode, this.preferredDeliveryMethod, this.registeredAddress, this.replTopologyStayOfExecution, this.sPNMappings, this.searchGuide, this.seeAlso, this.serialNumber, this.serverReference, this.siteObject, this.st, this.street, this.telephoneNumber, this.teletexTerminalIdentifier, this.telexNumber, this.thumbnailLogo, this.tombstoneLifetime, this.uPNSuffixes, this.userPassword, this.x121Address);
    }

    @Override
    public boolean equals(Object o) {
        return LdapUtils.areEqual(this, o);
    }

    @Override
    public String toString() {
        return String.format("[%s@%d::dn=%s, bridgeheadTransportList=%s, businessCategory=%s, c=%s, cn=%s, co=%s, countryCode=%s, dNSHostName=%s, dSHeuristics=%s, defaultGroup=%s, description=%s, desktopProfile=%s, destinationIndicator=%s, facsimileTelephoneNumber=%s, gPLink=%s, gPOptions=%s, garbageCollPeriod=%s, homePostalAddress=%s, houseIdentifier=%s, internationalISDNNumber=%s, l=%s, location=%s, mSMQInterval1=%s, mSMQInterval2=%s, mSMQNt4Stub=%s, mSMQSiteForeign=%s, mSMQSiteID=%s, mailAddress=%s, managedBy=%s, msCOMUserPartitionSetLink=%s, msDSAllowedDNSSuffixes=%s, msDSAzApplicationData=%s, msDSAzApplicationName=%s, msDSAzApplicationVersion=%s, msDSAzClassId=%s, msDSAzDomainTimeout=%s, msDSAzGenerateAudits=%s, msDSAzGenericData=%s, msDSAzMajorVersion=%s, msDSAzMinorVersion=%s, msDSAzObjectGuid=%s, msDSAzScopeName=%s, msDSAzScriptEngineCacheMax=%s, msDSAzScriptTimeout=%s, msDSBehaviorVersion=%s, msDSBridgeHeadServersUsed=%s, msDSDeletedObjectLifetime=%s, msDSEnabledFeature=%s, msDSHABSeniorityIndex=%s, msDSIsUserCachableAtRodc=%s, msDSObjectReference=%s, msDSOtherSettings=%s, msDSPhoneticCompanyName=%s, msDSPhoneticDepartment=%s, msDSPhoneticDisplayName=%s, msDSPhoneticFirstName=%s, msDSPhoneticLastName=%s, msDSSiteName=%s, msDSUSNLastSyncSuccess=%s, msDSisGC=%s, msDSisRODC=%s, msExchHouseIdentifier=%s, notificationList=%s, o=%s, ou=%s, physicalDeliveryOfficeName=%s, physicalLocationObject=%s, postOfficeBox=%s, postalAddress=%s, postalCode=%s, preferredDeliveryMethod=%s, registeredAddress=%s, replTopologyStayOfExecution=%s, sPNMappings=%s, searchGuide=%s, seeAlso=%s, serialNumber=%s, serverReference=%s, siteObject=%s, st=%s, street=%s, telephoneNumber=%s, teletexTerminalIdentifier=%s, telexNumber=%s, thumbnailLogo=%s, tombstoneLifetime=%s, uPNSuffixes=%s, userPassword=%s, x121Address=%s]", this.getClass().getName(), this.hashCode(), this.dn, this.bridgeheadTransportList, this.businessCategory, this.c, this.cn, this.co, this.countryCode, this.dNSHostName, this.dSHeuristics, this.defaultGroup, this.description, this.desktopProfile, this.destinationIndicator, this.facsimileTelephoneNumber, this.gPLink, this.gPOptions, this.garbageCollPeriod, this.homePostalAddress, this.houseIdentifier, this.internationalISDNNumber, this.l, this.location, this.mSMQInterval1, this.mSMQInterval2, this.mSMQNt4Stub, this.mSMQSiteForeign, this.mSMQSiteID, this.mailAddress, this.managedBy, this.msCOMUserPartitionSetLink, this.msDSAllowedDNSSuffixes, this.msDSAzApplicationData, this.msDSAzApplicationName, this.msDSAzApplicationVersion, this.msDSAzClassId, this.msDSAzDomainTimeout, this.msDSAzGenerateAudits, this.msDSAzGenericData, this.msDSAzMajorVersion, this.msDSAzMinorVersion, this.msDSAzObjectGuid, this.msDSAzScopeName, this.msDSAzScriptEngineCacheMax, this.msDSAzScriptTimeout, this.msDSBehaviorVersion, this.msDSBridgeHeadServersUsed, this.msDSDeletedObjectLifetime, this.msDSEnabledFeature, this.msDSHABSeniorityIndex, this.msDSIsUserCachableAtRodc, this.msDSObjectReference, this.msDSOtherSettings, this.msDSPhoneticCompanyName, this.msDSPhoneticDepartment, this.msDSPhoneticDisplayName, this.msDSPhoneticFirstName, this.msDSPhoneticLastName, this.msDSSiteName, this.msDSUSNLastSyncSuccess, this.msDSisGC, this.msDSisRODC, this.msExchHouseIdentifier, this.notificationList, this.o, this.ou, this.physicalDeliveryOfficeName, this.physicalLocationObject, this.postOfficeBox, this.postalAddress, this.postalCode, this.preferredDeliveryMethod, this.registeredAddress, this.replTopologyStayOfExecution, this.sPNMappings, this.searchGuide, this.seeAlso, this.serialNumber, this.serverReference, this.siteObject, this.st, this.street, this.telephoneNumber, this.teletexTerminalIdentifier, this.telexNumber, this.thumbnailLogo, this.tombstoneLifetime, this.uPNSuffixes, this.userPassword, this.x121Address);
    }

}
