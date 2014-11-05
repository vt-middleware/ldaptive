/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.schema;

import java.util.Arrays;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Unit tests for {@link AttributeType}.
 *
 * @author  Middleware Services
 * @version  $Revision: 3005 $ $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
 */
public class AttributeTypeTest
{


  /**
   * Test data for attribute type.
   *
   * @return  attribute type and string definition
   */
  @DataProvider(name = "definitions")
  public Object[][] createDefinitions()
  {
    final Extensions ext = new Extensions();
    ext.addExtension("X-NDS_NAME", Arrays.asList("Aliased Object Name"));
    ext.addExtension("X-NDS_NONREMOVABLE", Arrays.asList("1"));
    ext.addExtension("X-NDS_FILTERED_REQUIRED", Arrays.asList("1"));

    return
      new Object[][] {
        new Object[] {
          new AttributeType(
            "2.5.4.0",
            null,
            null,
            false,
            null,
            null,
            null,
            null,
            null,
            false,
            false,
            false,
            null,
            null),
          "( 2.5.4.0 )",
        },
        new Object[] {
          new AttributeType(
            "2.5.4.0",
            null,
            null,
            false,
            null,
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.38",
            false,
            false,
            false,
            null,
            null),
          "( 2.5.4.0 SYNTAX 1.3.6.1.4.1.1466.115.121.1.38 )",
        },
        new Object[] {
          new AttributeType(
            "2.5.4.0",
            new String[] {"objectClass"},
            null,
            false,
            null,
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.38",
            false,
            false,
            false,
            null,
            null),
          "( 2.5.4.0 NAME 'objectClass' SYNTAX 1.3.6.1.4.1.1466.115.121.1.38 )",
        },
        new Object[] {
          new AttributeType(
            "2.5.4.0",
            null,
            "RFC4512: object classes of the entity",
            false,
            null,
            null,
            null,
            null,
            null,
            false,
            false,
            false,
            null,
            null),
          "( 2.5.4.0 DESC 'RFC4512: object classes of the entity' )",
        },
        new Object[] {
          new AttributeType(
            "2.5.4.0",
            new String[] {"objectClass"},
            null,
            false,
            null,
            "objectIdentifierMatch",
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.38",
            false,
            false,
            false,
            null,
            null),
          "( 2.5.4.0 NAME 'objectClass' " +
            "EQUALITY objectIdentifierMatch " +
            "SYNTAX 1.3.6.1.4.1.1466.115.121.1.38 )",
        },
        new Object[] {
          new AttributeType(
            "2.5.4.0",
            new String[] {"objectClass"},
            "RFC4512: object classes of the entity",
            false,
            null,
            "objectIdentifierMatch",
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.38",
            false,
            false,
            false,
            null,
            null),
          "( 2.5.4.0 NAME 'objectClass' " +
            "DESC 'RFC4512: object classes of the entity' " +
            "EQUALITY objectIdentifierMatch " +
            "SYNTAX 1.3.6.1.4.1.1466.115.121.1.38 )",
        },
        new Object[] {
          new AttributeType(
            "2.5.21.9",
            new String[] {"structuralObjectClass"},
            "RFC4512: structural object class of entry",
            false,
            null,
            "objectIdentifierMatch",
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.38",
            true,
            false,
            true,
            AttributeUsage.DIRECTORY_OPERATION,
            null),
          "( 2.5.21.9 NAME 'structuralObjectClass' " +
            "DESC 'RFC4512: structural object class of entry' " +
            "EQUALITY objectIdentifierMatch " +
            "SYNTAX 1.3.6.1.4.1.1466.115.121.1.38 SINGLE-VALUE " +
            "NO-USER-MODIFICATION USAGE directoryOperation )",
        },
        new Object[] {
          new AttributeType(
            "2.5.18.1",
            new String[] {"createTimestamp"},
            "RFC4512: time which object was created",
            false,
            null,
            "generalizedTimeMatch",
            "generalizedTimeOrderingMatch",
            null,
            "1.3.6.1.4.1.1466.115.121.1.24",
            true,
            false,
            true,
            AttributeUsage.DIRECTORY_OPERATION,
            null),
          "( 2.5.18.1 NAME 'createTimestamp' " +
            "DESC 'RFC4512: time which object was created' " +
            "EQUALITY generalizedTimeMatch " +
            "ORDERING generalizedTimeOrderingMatch " +
            "SYNTAX 1.3.6.1.4.1.1466.115.121.1.24 SINGLE-VALUE " +
            "NO-USER-MODIFICATION USAGE directoryOperation )",
        },
        new Object[] {
          new AttributeType(
            "2.5.4.1",
            new String[] {"aliasedObjectName", "aliasedEntryName"},
            null,
            false,
            null,
            null,
            null,
            null,
            null,
            false,
            false,
            false,
            null,
            null),
          "( 2.5.4.1 NAME ( 'aliasedObjectName' 'aliasedEntryName' ) )",
        },
        new Object[] {
          new AttributeType(
            "2.5.4.1",
            new String[] {"aliasedObjectName", "aliasedEntryName"},
            "RFC4512: name of aliased object",
            false,
            null,
            "distinguishedNameMatch",
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.12",
            true,
            false,
            false,
            null,
            null),
          "( 2.5.4.1 NAME ( 'aliasedObjectName' 'aliasedEntryName' ) " +
            "DESC 'RFC4512: name of aliased object' " +
            "EQUALITY distinguishedNameMatch " +
            "SYNTAX 1.3.6.1.4.1.1466.115.121.1.12 SINGLE-VALUE )",
        },
        new Object[] {
          new AttributeType(
            "0.9.2342.19200300.100.1.1",
            new String[] {"uid"},
            null,
            false,
            null,
            "caseIgnoreMatch",
            null,
            "caseIgnoreSubstringsMatch",
            "1.3.6.1.4.1.1466.115.121.1.15{256}",
            false,
            false,
            false,
            null,
            new Extensions("X-ORIGIN", Arrays.asList("RFC 1274"))),
          "( 0.9.2342.19200300.100.1.1 NAME 'uid' EQUALITY caseIgnoreMatch " +
            "SUBSTR caseIgnoreSubstringsMatch " +
            "SYNTAX 1.3.6.1.4.1.1466.115.121.1.15{256} X-ORIGIN 'RFC 1274' )",
        },
        new Object[] {
          new AttributeType(
            "2.5.4.1",
            new String[] {"aliasedObjectName"},
            null,
            false,
            null,
            null,
            null,
            null,
            "1.3.6.1.4.1.1466.115.121.1.12",
            true,
            false,
            false,
            null,
            ext),
          "( 2.5.4.1 NAME 'aliasedObjectName' " +
            "SYNTAX 1.3.6.1.4.1.1466.115.121.1.12 SINGLE-VALUE " +
            "X-NDS_NAME 'Aliased Object Name' X-NDS_NONREMOVABLE '1' " +
            "X-NDS_FILTERED_REQUIRED '1' )",
        },
      };
  }


  /**
   * @param  type  to compare
   * @param  definition  to parse
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"schema"},
    dataProvider = "definitions"
  )
  public void parse(final AttributeType type, final String definition)
    throws Exception
  {
    final AttributeType parsed = AttributeType.parse(definition);
    Assert.assertEquals(type, parsed);
    Assert.assertEquals(definition, parsed.format());
    Assert.assertEquals(type.format(), parsed.format());
  }
}
