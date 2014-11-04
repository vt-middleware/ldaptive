/*
  $Id: SerializableTest.java 3005 2014-07-02 14:20:47Z dfisher $

  Copyright (C) 2003-2014 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 3005 $
  Updated: $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
*/
package org.ldaptive;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.ldaptive.jaas.LdapCredential;
import org.ldaptive.jaas.LdapDnPrincipal;
import org.ldaptive.jaas.LdapGroup;
import org.ldaptive.jaas.LdapPrincipal;
import org.ldaptive.jaas.LdapRole;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test that objects that implement {@link Serializable} can be serialized.
 *
 * @author  Middleware Services
 * @version  $Revision: 3005 $ $Date: 2014-07-02 10:20:47 -0400 (Wed, 02 Jul 2014) $
 */
public class SerializableTest
{


  /**
   * Serializable test data.
   *
   * @return  serializable objects
   */
  @DataProvider(name = "objects")
  public Object[][] createSerializable()
  {
    final LdapAttribute attr1 = new LdapAttribute(
      "string-name",
      "string-value1",
      "string-value2");
    final LdapAttribute attr2 = new LdapAttribute(
      "binary-name",
      new byte[] {0x00, 0x01, },
      new byte[] {0x02, 0x03, });
    final LdapEntry entry1 = new LdapEntry("entry-dn1", attr1, attr2);
    final LdapEntry entry2 = new LdapEntry("entry-dn2", attr1, attr2);
    final SearchResult result = new SearchResult(entry1, entry2);

    final LdapPrincipal prin = new LdapPrincipal("principal-name", entry1);
    final LdapDnPrincipal dnPrin = new LdapDnPrincipal(
      "dn-principal-name",
      entry2);
    final LdapRole role = new LdapRole("role-name");
    final LdapCredential cred = new LdapCredential("credential");
    final LdapGroup group = new LdapGroup("principal-group-name");
    group.addMember(prin);
    group.addMember(dnPrin);
    group.addMember(role);

    return
      new Object[][] {
        new Object[] {attr1, },
        new Object[] {attr2, },
        new Object[] {entry1, },
        new Object[] {entry2, },
        new Object[] {result, },
        new Object[] {prin, },
        new Object[] {role, },
        new Object[] {cred, },
        new Object[] {group, },
      };
  }


  /**
   * @param  s  serializable object to test
   *
   * @throws  Exception  On test failure.
   */
  @Test(
    groups = {"serialize"},
    dataProvider = "objects"
  )
  public void testSerialize(final Serializable s)
    throws Exception
  {
    Assert.assertEquals(deserialize(serialize(s)), s);
  }


  /**
   * Serializes the supplied object.
   *
   * @param  s  to serialize
   *
   * @return  serialized object
   *
   * @throws  Exception  if object cannot be serialized
   */
  private byte[] serialize(final Serializable s)
    throws Exception
  {
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
      oos.writeObject(s);
      return bos.toByteArray();
    } finally {
      bos.close();
    }
  }


  /**
   * Deserializes the supplied byte array.
   *
   * @param  b  to deserialize
   *
   * @return  deserialized object
   *
   * @throws  Exception  if object cannot be deserialized
   */
  private Object deserialize(final byte[] b)
    throws Exception
  {
    try (ObjectInputStream is = new ObjectInputStream(
      new ByteArrayInputStream(b))) {
      return is.readObject();
    }
  }
}
