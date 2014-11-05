/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link ObjectClass}.
 *
 * @author  Middleware Services
 * @version  $Revision: 3005 $ $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
 */
public class ObjectClassTest
{


  /**
   * Test data for object class.
   *
   * @return  object class and string definition
   */
  @DataProvider(name = "definitions")
  public Object[][] createDefinitions()
  {
    return
      new Object[][] {
        new Object[] {
          new ObjectClass(
            "2.5.6.0",
            null,
            null,
            false,
            null,
            null,
            null,
            null,
            null),
          "( 2.5.6.0 )",
        },
        new Object[] {
          new ObjectClass(
            "2.5.6.0",
            new String[] {"top"},
            null,
            false,
            null,
            null,
            null,
            null,
            null),
          "( 2.5.6.0 NAME 'top' )",
        },
        new Object[] {
          new ObjectClass(
            "2.5.6.0",
            new String[] {"top"},
            "top of the superclass chain",
            false,
            null,
            null,
            null,
            null,
            null),
          "( 2.5.6.0 NAME 'top' DESC 'top of the superclass chain' )",
        },
        new Object[] {
          new ObjectClass(
            "2.5.6.0",
            new String[] {"top"},
            "top of the superclass chain",
            false,
            null,
            ObjectClassType.ABSTRACT,
            null,
            null,
            null),
          "( 2.5.6.0 NAME 'top' DESC 'top of the superclass chain' ABSTRACT )",
        },
        new Object[] {
          new ObjectClass(
            "2.5.6.0",
            new String[] {"top"},
            "top of the superclass chain",
            false,
            null,
            ObjectClassType.ABSTRACT,
            new String[] {"objectClass"},
            null,
            null),
          "( 2.5.6.0 NAME 'top' DESC 'top of the superclass chain' ABSTRACT " +
            "MUST objectClass )",
        },
        new Object[] {
          new ObjectClass(
            "1.3.6.1.4.1.1466.101.120.111",
            new String[] {"extensibleObject"},
            "RFC4512: extensible object",
            false,
            new String[] {"top"},
            ObjectClassType.AUXILIARY,
            null,
            null,
            null),
          "( 1.3.6.1.4.1.1466.101.120.111 NAME 'extensibleObject' " +
            "DESC 'RFC4512: extensible object' SUP top AUXILIARY )",
        },
        new Object[] {
          new ObjectClass(
            "2.5.6.1",
            new String[] {"alias"},
            "RFC4512: an alias",
            false,
            new String[] {"top"},
            ObjectClassType.STRUCTURAL,
            new String[] {"aliasedObjectName"},
            null,
            null),
          "( 2.5.6.1 NAME 'alias' DESC 'RFC4512: an alias' SUP top " +
            "STRUCTURAL MUST aliasedObjectName )",
        },
        new Object[] {
          new ObjectClass(
            "1.3.6.1.4.1.4203.1.4.1",
            new String[] {"OpenLDAProotDSE", "LDAProotDSE"},
            "OpenLDAP Root DSE object",
            false,
            new String[] {"top"},
            ObjectClassType.STRUCTURAL,
            null,
            new String[] {"cn"},
            null),
          "( 1.3.6.1.4.1.4203.1.4.1 NAME ( 'OpenLDAProotDSE' 'LDAProotDSE' ) " +
            "DESC 'OpenLDAP Root DSE object' SUP top STRUCTURAL MAY cn )",
        },
        new Object[] {
          new ObjectClass(
            "2.5.17.0",
            new String[] {"subentry"},
            "RFC3672: subentry",
            false,
            new String[] {"top"},
            ObjectClassType.STRUCTURAL,
            new String[] {"cn", "subtreeSpecification"},
            null,
            null),
          "( 2.5.17.0 NAME 'subentry' DESC 'RFC3672: subentry' SUP top " +
            "STRUCTURAL MUST ( cn $ subtreeSpecification ) )",
        },
        new Object[] {
          new ObjectClass(
            "2.5.6.5",
            new String[] {"organizationalUnit"},
            "RFC2256: an organizational unit",
            false,
            new String[] {"top"},
            ObjectClassType.STRUCTURAL,
            new String[] {"ou"},
            new String[] {
              "userPassword",
              "searchGuide",
              "seeAlso",
              "businessCategory",
              "x121Address",
              "registeredAddress",
              "destinationIndicator",
              "preferredDeliveryMethod",
              "telexNumber",
              "teletexTerminalIdentifier",
              "telephoneNumber",
              "internationaliSDNNumber",
              "facsimileTelephoneNumber",
              "street",
              "postOfficeBox",
              "postalCode",
              "postalAddress",
              "physicalDeliveryOfficeName",
              "st",
              "l",
              "description",
            },
            null),
          "( 2.5.6.5 NAME 'organizationalUnit' " +
            "DESC 'RFC2256: an organizational unit' SUP top STRUCTURAL " +
            "MUST ou MAY ( userPassword $ searchGuide $ seeAlso $ " +
            "businessCategory $ x121Address $ registeredAddress $ " +
            "destinationIndicator $ preferredDeliveryMethod $ telexNumber $ " +
            "teletexTerminalIdentifier $ telephoneNumber $ " +
            "internationaliSDNNumber $ facsimileTelephoneNumber $ street $ " +
            "postOfficeBox $ postalCode $ postalAddress $ " +
            "physicalDeliveryOfficeName $ st $ l $ description ) )",
        },
      };
  }


  /**
   * @param  objectClass  to compare
   * @param  definition  to parse
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"schema"},
    dataProvider = "definitions"
  )
  public void parse(final ObjectClass objectClass, final String definition)
    throws Exception
  {
    final ObjectClass parsed = ObjectClass.parse(definition);
    Assert.assertEquals(objectClass, parsed);
    Assert.assertEquals(definition, parsed.format());
    Assert.assertEquals(objectClass.format(), parsed.format());
  }
}
