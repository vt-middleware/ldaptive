
package org.ldaptive.beans.generate;

import java.security.cert.Certificate;
import java.util.Calendar;
import java.util.Collection;
import java.util.UUID;
import org.ldaptive.LdapUtils;
import org.ldaptive.beans.Attribute;
import org.ldaptive.beans.Entry;


/**
 * Ldaptive generated bean for objectClass 'inetOrgPerson'
 * 
 */
@Entry(dn = "dn", attributes = {
    @Attribute(name = "customname1", values = "customvalue1"),
    @Attribute(name = "customname2", values = {"customvalue1", "customvalue2"}),
    @Attribute(name = "attributeTypes"),
    @Attribute(name = "audio", binary = true),
    @Attribute(name = "businessCategory"),
    @Attribute(name = "carLicense"),
    @Attribute(name = "cn"),
    @Attribute(name = "createTimestamp"),
    @Attribute(name = "creatorsName"),
    @Attribute(name = "departmentNumber"),
    @Attribute(name = "description"),
    @Attribute(name = "destinationIndicator"),
    @Attribute(name = "displayName"),
    @Attribute(name = "employeeNumber"),
    @Attribute(name = "employeeType"),
    @Attribute(name = "entryDN"),
    @Attribute(name = "entryUUID"),
    @Attribute(name = "facsimileTelephoneNumber"),
    @Attribute(name = "givenName"),
    @Attribute(name = "hasSubordinates"),
    @Attribute(name = "homePhone"),
    @Attribute(name = "homePostalAddress"),
    @Attribute(name = "initials"),
    @Attribute(name = "internationaliSDNNumber"),
    @Attribute(name = "jpegPhoto", binary = true),
    @Attribute(name = "l"),
    @Attribute(name = "labeledURI"),
    @Attribute(name = "ldapSyntaxes"),
    @Attribute(name = "mail"),
    @Attribute(name = "manager"),
    @Attribute(name = "matchingRuleUse"),
    @Attribute(name = "matchingRules"),
    @Attribute(name = "mobile"),
    @Attribute(name = "modifiersName"),
    @Attribute(name = "modifyTimestamp"),
    @Attribute(name = "o"),
    @Attribute(name = "objectClasses"),
    @Attribute(name = "ou"),
    @Attribute(name = "pager"),
    @Attribute(name = "photo"),
    @Attribute(name = "physicalDeliveryOfficeName"),
    @Attribute(name = "postOfficeBox"),
    @Attribute(name = "postalAddress"),
    @Attribute(name = "postalCode"),
    @Attribute(name = "preferredDeliveryMethod"),
    @Attribute(name = "preferredLanguage"),
    @Attribute(name = "pwdAccountLockedTime"),
    @Attribute(name = "pwdChangedTime"),
    @Attribute(name = "pwdFailureTime"),
    @Attribute(name = "pwdGraceUseTime"),
    @Attribute(name = "pwdHistory"),
    @Attribute(name = "pwdPolicySubentry"),
    @Attribute(name = "pwdReset"),
    @Attribute(name = "registeredAddress"),
    @Attribute(name = "roomNumber"),
    @Attribute(name = "secretary"),
    @Attribute(name = "seeAlso"),
    @Attribute(name = "sn"),
    @Attribute(name = "st"),
    @Attribute(name = "street"),
    @Attribute(name = "structuralObjectClass"),
    @Attribute(name = "subschemaSubentry"),
    @Attribute(name = "telephoneNumber"),
    @Attribute(name = "teletexTerminalIdentifier"),
    @Attribute(name = "telexNumber"),
    @Attribute(name = "title"),
    @Attribute(name = "uid"),
    @Attribute(name = "userCertificate"),
    @Attribute(name = "userPKCS12", binary = true),
    @Attribute(name = "userPassword"),
    @Attribute(name = "userSMIMECertificate", binary = true),
    @Attribute(name = "x121Address"),
    @Attribute(name = "x500UniqueIdentifier")
})
public class InetOrgPerson {

    private String dn;
    private Collection<String> attributeTypes;
    private Collection<byte[]> audio;
    private Collection<String> businessCategory;
    private Collection<String> carLicense;
    private Collection<String> cn;
    private Calendar createTimestamp;
    private String creatorsName;
    private Collection<String> departmentNumber;
    private Collection<String> description;
    private Collection<String> destinationIndicator;
    private String displayName;
    private String employeeNumber;
    private Collection<String> employeeType;
    private String entryDN;
    private UUID entryUUID;
    private Collection<String> facsimileTelephoneNumber;
    private Collection<String> givenName;
    private Boolean hasSubordinates;
    private Collection<String> homePhone;
    private Collection<String> homePostalAddress;
    private Collection<String> initials;
    private Collection<Integer> internationaliSDNNumber;
    private Collection<byte[]> jpegPhoto;
    private Collection<String> l;
    private Collection<String> labeledURI;
    private Collection<String> ldapSyntaxes;
    private Collection<String> mail;
    private Collection<String> manager;
    private Collection<String> matchingRuleUse;
    private Collection<String> matchingRules;
    private Collection<String> mobile;
    private String modifiersName;
    private Calendar modifyTimestamp;
    private Collection<String> o;
    private Collection<String> objectClasses;
    private Collection<String> ou;
    private Collection<String> pager;
    private Collection<String> photo;
    private Collection<String> physicalDeliveryOfficeName;
    private Collection<String> postOfficeBox;
    private Collection<String> postalAddress;
    private Collection<String> postalCode;
    private String preferredDeliveryMethod;
    private String preferredLanguage;
    private Calendar pwdAccountLockedTime;
    private Calendar pwdChangedTime;
    private Collection<Calendar> pwdFailureTime;
    private Collection<Calendar> pwdGraceUseTime;
    private Collection<String> pwdHistory;
    private String pwdPolicySubentry;
    private Boolean pwdReset;
    private Collection<String> registeredAddress;
    private Collection<String> roomNumber;
    private Collection<String> secretary;
    private Collection<String> seeAlso;
    private Collection<String> sn;
    private Collection<String> st;
    private Collection<String> street;
    private String structuralObjectClass;
    private String subschemaSubentry;
    private Collection<String> telephoneNumber;
    private Collection<String> teletexTerminalIdentifier;
    private Collection<String> telexNumber;
    private Collection<String> title;
    private Collection<String> uid;
    private Collection<Certificate> userCertificate;
    private Collection<byte[]> userPKCS12;
    private Collection<String> userPassword;
    private Collection<byte[]> userSMIMECertificate;
    private Collection<Integer> x121Address;
    private Collection<String> x500UniqueIdentifier;

    public String getDn() {
        return dn;
    }

    public void setDn(String s) {
        this.dn = s;
    }

    public Collection<String> getAttributeTypes() {
        return attributeTypes;
    }

    public void setAttributeTypes(Collection<String> c) {
        this.attributeTypes = c;
    }

    public Collection<byte[]> getAudio() {
        return audio;
    }

    public void setAudio(Collection<byte[]> c) {
        this.audio = c;
    }

    public Collection<String> getBusinessCategory() {
        return businessCategory;
    }

    public void setBusinessCategory(Collection<String> c) {
        this.businessCategory = c;
    }

    public Collection<String> getCarLicense() {
        return carLicense;
    }

    public void setCarLicense(Collection<String> c) {
        this.carLicense = c;
    }

    public Collection<String> getCn() {
        return cn;
    }

    public void setCn(Collection<String> c) {
        this.cn = c;
    }

    public Calendar getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(Calendar s) {
        this.createTimestamp = s;
    }

    public String getCreatorsName() {
        return creatorsName;
    }

    public void setCreatorsName(String s) {
        this.creatorsName = s;
    }

    public Collection<String> getDepartmentNumber() {
        return departmentNumber;
    }

    public void setDepartmentNumber(Collection<String> c) {
        this.departmentNumber = c;
    }

    public Collection<String> getDescription() {
        return description;
    }

    public void setDescription(Collection<String> c) {
        this.description = c;
    }

    public Collection<String> getDestinationIndicator() {
        return destinationIndicator;
    }

    public void setDestinationIndicator(Collection<String> c) {
        this.destinationIndicator = c;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String s) {
        this.displayName = s;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String s) {
        this.employeeNumber = s;
    }

    public Collection<String> getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(Collection<String> c) {
        this.employeeType = c;
    }

    public String getEntryDN() {
        return entryDN;
    }

    public void setEntryDN(String s) {
        this.entryDN = s;
    }

    public UUID getEntryUUID() {
        return entryUUID;
    }

    public void setEntryUUID(UUID s) {
        this.entryUUID = s;
    }

    public Collection<String> getFacsimileTelephoneNumber() {
        return facsimileTelephoneNumber;
    }

    public void setFacsimileTelephoneNumber(Collection<String> c) {
        this.facsimileTelephoneNumber = c;
    }

    public Collection<String> getGivenName() {
        return givenName;
    }

    public void setGivenName(Collection<String> c) {
        this.givenName = c;
    }

    public Boolean getHasSubordinates() {
        return hasSubordinates;
    }

    public void setHasSubordinates(Boolean s) {
        this.hasSubordinates = s;
    }

    public Collection<String> getHomePhone() {
        return homePhone;
    }

    public void setHomePhone(Collection<String> c) {
        this.homePhone = c;
    }

    public Collection<String> getHomePostalAddress() {
        return homePostalAddress;
    }

    public void setHomePostalAddress(Collection<String> c) {
        this.homePostalAddress = c;
    }

    public Collection<String> getInitials() {
        return initials;
    }

    public void setInitials(Collection<String> c) {
        this.initials = c;
    }

    public Collection<Integer> getInternationaliSDNNumber() {
        return internationaliSDNNumber;
    }

    public void setInternationaliSDNNumber(Collection<Integer> c) {
        this.internationaliSDNNumber = c;
    }

    public Collection<byte[]> getJpegPhoto() {
        return jpegPhoto;
    }

    public void setJpegPhoto(Collection<byte[]> c) {
        this.jpegPhoto = c;
    }

    public Collection<String> getL() {
        return l;
    }

    public void setL(Collection<String> c) {
        this.l = c;
    }

    public Collection<String> getLabeledURI() {
        return labeledURI;
    }

    public void setLabeledURI(Collection<String> c) {
        this.labeledURI = c;
    }

    public Collection<String> getLdapSyntaxes() {
        return ldapSyntaxes;
    }

    public void setLdapSyntaxes(Collection<String> c) {
        this.ldapSyntaxes = c;
    }

    public Collection<String> getMail() {
        return mail;
    }

    public void setMail(Collection<String> c) {
        this.mail = c;
    }

    public Collection<String> getManager() {
        return manager;
    }

    public void setManager(Collection<String> c) {
        this.manager = c;
    }

    public Collection<String> getMatchingRuleUse() {
        return matchingRuleUse;
    }

    public void setMatchingRuleUse(Collection<String> c) {
        this.matchingRuleUse = c;
    }

    public Collection<String> getMatchingRules() {
        return matchingRules;
    }

    public void setMatchingRules(Collection<String> c) {
        this.matchingRules = c;
    }

    public Collection<String> getMobile() {
        return mobile;
    }

    public void setMobile(Collection<String> c) {
        this.mobile = c;
    }

    public String getModifiersName() {
        return modifiersName;
    }

    public void setModifiersName(String s) {
        this.modifiersName = s;
    }

    public Calendar getModifyTimestamp() {
        return modifyTimestamp;
    }

    public void setModifyTimestamp(Calendar s) {
        this.modifyTimestamp = s;
    }

    public Collection<String> getO() {
        return o;
    }

    public void setO(Collection<String> c) {
        this.o = c;
    }

    public Collection<String> getObjectClasses() {
        return objectClasses;
    }

    public void setObjectClasses(Collection<String> c) {
        this.objectClasses = c;
    }

    public Collection<String> getOu() {
        return ou;
    }

    public void setOu(Collection<String> c) {
        this.ou = c;
    }

    public Collection<String> getPager() {
        return pager;
    }

    public void setPager(Collection<String> c) {
        this.pager = c;
    }

    public Collection<String> getPhoto() {
        return photo;
    }

    public void setPhoto(Collection<String> c) {
        this.photo = c;
    }

    public Collection<String> getPhysicalDeliveryOfficeName() {
        return physicalDeliveryOfficeName;
    }

    public void setPhysicalDeliveryOfficeName(Collection<String> c) {
        this.physicalDeliveryOfficeName = c;
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

    public Collection<String> getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(Collection<String> c) {
        this.postalCode = c;
    }

    public String getPreferredDeliveryMethod() {
        return preferredDeliveryMethod;
    }

    public void setPreferredDeliveryMethod(String s) {
        this.preferredDeliveryMethod = s;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String s) {
        this.preferredLanguage = s;
    }

    public Calendar getPwdAccountLockedTime() {
        return pwdAccountLockedTime;
    }

    public void setPwdAccountLockedTime(Calendar s) {
        this.pwdAccountLockedTime = s;
    }

    public Calendar getPwdChangedTime() {
        return pwdChangedTime;
    }

    public void setPwdChangedTime(Calendar s) {
        this.pwdChangedTime = s;
    }

    public Collection<Calendar> getPwdFailureTime() {
        return pwdFailureTime;
    }

    public void setPwdFailureTime(Collection<Calendar> c) {
        this.pwdFailureTime = c;
    }

    public Collection<Calendar> getPwdGraceUseTime() {
        return pwdGraceUseTime;
    }

    public void setPwdGraceUseTime(Collection<Calendar> c) {
        this.pwdGraceUseTime = c;
    }

    public Collection<String> getPwdHistory() {
        return pwdHistory;
    }

    public void setPwdHistory(Collection<String> c) {
        this.pwdHistory = c;
    }

    public String getPwdPolicySubentry() {
        return pwdPolicySubentry;
    }

    public void setPwdPolicySubentry(String s) {
        this.pwdPolicySubentry = s;
    }

    public Boolean getPwdReset() {
        return pwdReset;
    }

    public void setPwdReset(Boolean s) {
        this.pwdReset = s;
    }

    public Collection<String> getRegisteredAddress() {
        return registeredAddress;
    }

    public void setRegisteredAddress(Collection<String> c) {
        this.registeredAddress = c;
    }

    public Collection<String> getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(Collection<String> c) {
        this.roomNumber = c;
    }

    public Collection<String> getSecretary() {
        return secretary;
    }

    public void setSecretary(Collection<String> c) {
        this.secretary = c;
    }

    public Collection<String> getSeeAlso() {
        return seeAlso;
    }

    public void setSeeAlso(Collection<String> c) {
        this.seeAlso = c;
    }

    public Collection<String> getSn() {
        return sn;
    }

    public void setSn(Collection<String> c) {
        this.sn = c;
    }

    public Collection<String> getSt() {
        return st;
    }

    public void setSt(Collection<String> c) {
        this.st = c;
    }

    public Collection<String> getStreet() {
        return street;
    }

    public void setStreet(Collection<String> c) {
        this.street = c;
    }

    public String getStructuralObjectClass() {
        return structuralObjectClass;
    }

    public void setStructuralObjectClass(String s) {
        this.structuralObjectClass = s;
    }

    public String getSubschemaSubentry() {
        return subschemaSubentry;
    }

    public void setSubschemaSubentry(String s) {
        this.subschemaSubentry = s;
    }

    public Collection<String> getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(Collection<String> c) {
        this.telephoneNumber = c;
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

    public Collection<String> getTitle() {
        return title;
    }

    public void setTitle(Collection<String> c) {
        this.title = c;
    }

    public Collection<String> getUid() {
        return uid;
    }

    public void setUid(Collection<String> c) {
        this.uid = c;
    }

    public Collection<Certificate> getUserCertificate() {
        return userCertificate;
    }

    public void setUserCertificate(Collection<Certificate> c) {
        this.userCertificate = c;
    }

    public Collection<byte[]> getUserPKCS12() {
        return userPKCS12;
    }

    public void setUserPKCS12(Collection<byte[]> c) {
        this.userPKCS12 = c;
    }

    public Collection<String> getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(Collection<String> c) {
        this.userPassword = c;
    }

    public Collection<byte[]> getUserSMIMECertificate() {
        return userSMIMECertificate;
    }

    public void setUserSMIMECertificate(Collection<byte[]> c) {
        this.userSMIMECertificate = c;
    }

    public Collection<Integer> getX121Address() {
        return x121Address;
    }

    public void setX121Address(Collection<Integer> c) {
        this.x121Address = c;
    }

    public Collection<String> getX500UniqueIdentifier() {
        return x500UniqueIdentifier;
    }

    public void setX500UniqueIdentifier(Collection<String> c) {
        this.x500UniqueIdentifier = c;
    }

    @Override
    public int hashCode() {
        return LdapUtils.computeHashCode(7919, this.dn, this.attributeTypes, this.audio, this.businessCategory, this.carLicense, this.cn, this.createTimestamp, this.creatorsName, this.departmentNumber, this.description, this.destinationIndicator, this.displayName, this.employeeNumber, this.employeeType, this.entryDN, this.entryUUID, this.facsimileTelephoneNumber, this.givenName, this.hasSubordinates, this.homePhone, this.homePostalAddress, this.initials, this.internationaliSDNNumber, this.jpegPhoto, this.l, this.labeledURI, this.ldapSyntaxes, this.mail, this.manager, this.matchingRuleUse, this.matchingRules, this.mobile, this.modifiersName, this.modifyTimestamp, this.o, this.objectClasses, this.ou, this.pager, this.photo, this.physicalDeliveryOfficeName, this.postOfficeBox, this.postalAddress, this.postalCode, this.preferredDeliveryMethod, this.preferredLanguage, this.pwdAccountLockedTime, this.pwdChangedTime, this.pwdFailureTime, this.pwdGraceUseTime, this.pwdHistory, this.pwdPolicySubentry, this.pwdReset, this.registeredAddress, this.roomNumber, this.secretary, this.seeAlso, this.sn, this.st, this.street, this.structuralObjectClass, this.subschemaSubentry, this.telephoneNumber, this.teletexTerminalIdentifier, this.telexNumber, this.title, this.uid, this.userCertificate, this.userPKCS12, this.userPassword, this.userSMIMECertificate, this.x121Address, this.x500UniqueIdentifier);
    }

    @Override
    public boolean equals(Object o) {
        return LdapUtils.areEqual(this, o);
    }

    @Override
    public String toString() {
        return String.format("[%s@%d::dn=%s, attributeTypes=%s, audio=%s, businessCategory=%s, carLicense=%s, cn=%s, createTimestamp=%s, creatorsName=%s, departmentNumber=%s, description=%s, destinationIndicator=%s, displayName=%s, employeeNumber=%s, employeeType=%s, entryDN=%s, entryUUID=%s, facsimileTelephoneNumber=%s, givenName=%s, hasSubordinates=%s, homePhone=%s, homePostalAddress=%s, initials=%s, internationaliSDNNumber=%s, jpegPhoto=%s, l=%s, labeledURI=%s, ldapSyntaxes=%s, mail=%s, manager=%s, matchingRuleUse=%s, matchingRules=%s, mobile=%s, modifiersName=%s, modifyTimestamp=%s, o=%s, objectClasses=%s, ou=%s, pager=%s, photo=%s, physicalDeliveryOfficeName=%s, postOfficeBox=%s, postalAddress=%s, postalCode=%s, preferredDeliveryMethod=%s, preferredLanguage=%s, pwdAccountLockedTime=%s, pwdChangedTime=%s, pwdFailureTime=%s, pwdGraceUseTime=%s, pwdHistory=%s, pwdPolicySubentry=%s, pwdReset=%s, registeredAddress=%s, roomNumber=%s, secretary=%s, seeAlso=%s, sn=%s, st=%s, street=%s, structuralObjectClass=%s, subschemaSubentry=%s, telephoneNumber=%s, teletexTerminalIdentifier=%s, telexNumber=%s, title=%s, uid=%s, userCertificate=%s, userPKCS12=%s, userPassword=%s, userSMIMECertificate=%s, x121Address=%s, x500UniqueIdentifier=%s]", this.getClass().getName(), this.hashCode(), this.dn, this.attributeTypes, this.audio, this.businessCategory, this.carLicense, this.cn, this.createTimestamp, this.creatorsName, this.departmentNumber, this.description, this.destinationIndicator, this.displayName, this.employeeNumber, this.employeeType, this.entryDN, this.entryUUID, this.facsimileTelephoneNumber, this.givenName, this.hasSubordinates, this.homePhone, this.homePostalAddress, this.initials, this.internationaliSDNNumber, this.jpegPhoto, this.l, this.labeledURI, this.ldapSyntaxes, this.mail, this.manager, this.matchingRuleUse, this.matchingRules, this.mobile, this.modifiersName, this.modifyTimestamp, this.o, this.objectClasses, this.ou, this.pager, this.photo, this.physicalDeliveryOfficeName, this.postOfficeBox, this.postalAddress, this.postalCode, this.preferredDeliveryMethod, this.preferredLanguage, this.pwdAccountLockedTime, this.pwdChangedTime, this.pwdFailureTime, this.pwdGraceUseTime, this.pwdHistory, this.pwdPolicySubentry, this.pwdReset, this.registeredAddress, this.roomNumber, this.secretary, this.seeAlso, this.sn, this.st, this.street, this.structuralObjectClass, this.subschemaSubentry, this.telephoneNumber, this.teletexTerminalIdentifier, this.telexNumber, this.title, this.uid, this.userCertificate, this.userPKCS12, this.userPassword, this.userSMIMECertificate, this.x121Address, this.x500UniqueIdentifier);
    }

}
