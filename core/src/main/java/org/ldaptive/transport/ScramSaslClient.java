/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.transport;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.ldaptive.BindResponse;
import org.ldaptive.LdapException;
import org.ldaptive.LdapUtils;
import org.ldaptive.ResultCode;
import org.ldaptive.sasl.Mechanism;
import org.ldaptive.sasl.SaslBindRequest;
import org.ldaptive.sasl.SaslClient;
import org.ldaptive.sasl.ScramBindRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScramSaslClient implements SaslClient<ScramBindRequest>
{

  /** Logger for this class. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ScramSaslClient.class);


  /**
   * Performs a SCRAM SASL bind.
   *
   * @param  conn  to perform the bind on
   * @param  request  SASL request to perform
   *
   * @return  final result of the bind process
   *
   * @throws  LdapException  if an error occurs
   */
  public BindResponse bind(final TransportConnection conn, final ScramBindRequest request)
    throws LdapException
  {
    final ClientFirstMessage clientFirstMessage = new ClientFirstMessage(request.getUsername(), request.getNonce());

    final BindResponse serverFirstResult = conn.operation(
      new SaslBindRequest(
        request.getMechanism().mechanism(), clientFirstMessage.encode().getBytes(StandardCharsets.UTF_8))).execute();

    if (serverFirstResult.getResultCode() != ResultCode.SASL_BIND_IN_PROGRESS) {
      if (serverFirstResult.isSuccess()) {
        throw new IllegalStateException(
          "Unexpected success result from SCRAM SASL bind: " + serverFirstResult.getResultCode());
      }
      LOGGER.warn("Unexpected server result {}", serverFirstResult);
      return serverFirstResult;
    }

    final ClientFinalMessage clientFinalMessage = new ClientFinalMessage(
      request.getMechanism(),
      request.getPassword(),
      clientFirstMessage,
      new ServerFirstMessage(clientFirstMessage, serverFirstResult));

    final BindResponse serverFinalResult = conn.operation(
      new SaslBindRequest(
        request.getMechanism().mechanism(), clientFinalMessage.encode().getBytes(StandardCharsets.UTF_8))).execute();

    final ServerFinalMessage serverFinalMessage = new ServerFinalMessage(
      request.getMechanism(),
      clientFinalMessage,
      serverFinalResult);

    if (!serverFinalResult.isSuccess() && serverFinalMessage.isVerified()) {
      throw new IllegalStateException("Verified server message but result was not a success");
    } else if (serverFinalResult.isSuccess() && !serverFinalMessage.isVerified()) {
      throw new IllegalStateException("Received success from server but message could not be verified");
    }
    return serverFinalResult;
  }


  /** Properties associated with the client first message. */
  static class ClientFirstMessage
  {

    /** GS2 header for no channel binding. */
    private static final String GS2_NO_CHANNEL_BINDING = "n,,";

    /** Default nonce size. */
    private static final int DEFAULT_NONCE_SIZE = 16;

    /** Username to authenticate. */
    private final String clientUsername;

    /** Protocol nonce. */
    private final String clientNonce;

    /** Message produced from the username and nonce. */
    private final String message;


    /**
     * Creates a new client first message. If nonce is null a random is created for this client.
     *
     * @param  username  to authenticate
     * @param  nonce  to supply to the server or null
     */
    ClientFirstMessage(final String username, final byte[] nonce)
    {
      clientUsername = username;
      if (nonce == null) {
        final SecureRandom random = new SecureRandom();
        final byte[] b = new byte[DEFAULT_NONCE_SIZE];
        random.nextBytes(b);
        clientNonce = LdapUtils.base64Encode(b);
      } else {
        clientNonce = LdapUtils.base64Encode(nonce);
      }
      message = "n=".concat(clientUsername).concat(",").concat("r=").concat(clientNonce);
    }


    public String getNonce()
    {
      return clientNonce;
    }


    public String getMessage()
    {
      return message;
    }


    /**
     * Encodes this message to send to the server. This methods prepends the message with a GS2 header indicating that
     * no channel binding is supported.
     *
     * @return  encoded message
     */
    public String encode()
    {
      return GS2_NO_CHANNEL_BINDING.concat(message);
    }
  }


  /** Properties associated with the final client message. */
  static class ClientFinalMessage
  {

    /** GS2 header for no channel binding. */
    private static final String GS2_NO_CHANNEL_BINDING = LdapUtils.base64Encode("n,,");

    /** 4-octet encoding of the integer 1. */
    private static final byte[] INTEGER_ONE = {0x00, 0x00, 0x00, 0x01, };

    /** Bytes for the client key hmac. */
    private static final byte[] CLIENT_KEY_INIT = "Client Key".getBytes(StandardCharsets.UTF_8);

    /** Scram SASL mechanism. */
    private final Mechanism mechanism;

    /** Channel binding attribute plus the combined nonce. */
    private final String withoutProof;

    /** Client first message plus the server first message plus the withoutProof string. */
    private final String message;

    /** Computed password using the server salt and iterations. */
    private final byte[] saltedPassword;


    /**
     * Creates a new client final message.
     *
     * @param  mech  scram mechanism
     * @param  password  to authenticate the user with
     * @param  clientFirstMessage  first message sent to the server
     * @param  serverFirstMessage  first response from the server
     */
    ClientFinalMessage(
      final Mechanism mech,
      final String password,
      final ClientFirstMessage clientFirstMessage,
      final ServerFirstMessage serverFirstMessage)
    {
      mechanism = mech;
      saltedPassword = createSaltedPassword(
        mechanism.properties()[1],
        password,
        serverFirstMessage.getSalt(),
        serverFirstMessage.getIterations());

      withoutProof = "c=".concat(GS2_NO_CHANNEL_BINDING).concat(",")
        .concat("r=").concat(serverFirstMessage.getCombinedNonce());

      message = clientFirstMessage.getMessage().concat(",")
        .concat(serverFirstMessage.getMessage()).concat(",")
        .concat(withoutProof);
    }


    public byte[] getSaltedPassword()
    {
      return saltedPassword;
    }


    public String getMessage()
    {
      return message;
    }


    /**
     * Encodes this message to send to the server. Concatenation of the message without proof and the proof.
     *
     * @return  encoded message
     */
    public String encode()
    {
      final byte[] clientKey = createMac(mechanism.properties()[1], saltedPassword).doFinal(CLIENT_KEY_INIT);
      final byte[] storedKey = createDigest(mechanism.properties()[0], clientKey);

      final byte[] clientSignature =
        createMac(mechanism.properties()[1], storedKey).doFinal(message.getBytes(StandardCharsets.UTF_8));

      final byte[] clientProof = new byte[clientKey.length];
      for (int i = 0; i < clientProof.length; i++) {
        clientProof[i] = (byte) (clientKey[i] ^ clientSignature[i]);
      }

      return withoutProof.concat(",p=").concat(LdapUtils.base64Encode(clientProof));
    }


    /**
     * Computes a salted password.
     *
     * @param  algorithm  of the MAC
     * @param  password  to seed the MAC with
     * @param  salt  for the MAC
     * @param  iterations  of the MAC
     *
     * @return  salted password
     */
    private static byte[] createSaltedPassword(
      final String algorithm,
      final String password,
      final byte[] salt,
      final int iterations)
    {
      // create an HMAC using the UTF-8 password
      final Mac mac = createMac(algorithm, password.getBytes(StandardCharsets.UTF_8));

      // Per the RFC, seed the salt with the bytes of integer 1
      byte[] bytes = Arrays.copyOf(salt, salt.length + INTEGER_ONE.length);
      System.arraycopy(INTEGER_ONE, 0, bytes, salt.length, INTEGER_ONE.length);

      // first iteration is the MAC of the salt and integer 1
      bytes = mac.doFinal(bytes);

      // remaining iterations create the MAC of the previous MAC and XOR that result with the previous MAC
      final byte[] xor = bytes;
      for (int i = 1; i < iterations; i++) {
        final byte[] macResult = mac.doFinal(bytes);
        for (int j = 0; j < macResult.length; j++) {
          xor[j] ^= macResult[j];
        }
        bytes = macResult;
      }
      return xor;
    }
  }


  /** Properties associated with the first server response. */
  static class ServerFirstMessage
  {
    /** Minimum number of iterations we will allow. */
    private static final int MINIMUM_ITERATION_COUNT = 4096;

    /** The server SASL credentials. */
    private final String message;

    /** Nonce parsed from the SASL credentials. */
    private final String combinedNonce;

    /** Salt parsed from the SASL credentials. */
    private final byte[] salt;

    /** Iterations parsed from the SASL credentials. */
    private final int iterations;


    /**
     * Creates a new server first message.
     *
     * @param  clientFirstMessage  first message sent to the server
     * @param  result  response to the first message
     */
    ServerFirstMessage(final ClientFirstMessage clientFirstMessage, final BindResponse result)
    {
      if (result.getServerSaslCreds() == null || result.getServerSaslCreds().length == 0) {
        throw new IllegalArgumentException("Bind response missing server SASL credentials");
      }

      message = new String(result.getServerSaslCreds(), StandardCharsets.UTF_8);
      final Map<String, String> attributes = Stream.of(message.split(","))
        .map(s -> s.split("=", 2)).collect(Collectors.toMap(attr -> attr[0], attr -> attr[1]));

      final String r = attributes.get("r");
      if (r == null) {
        throw new IllegalArgumentException("Invalid SASL credentials, missing server nonce");
      }
      if (!r.startsWith(clientFirstMessage.getNonce())) {
        throw new IllegalArgumentException("Invalid SASL credentials, missing client nonce");
      }
      combinedNonce = r;

      final String s = attributes.get("s");
      if (s == null) {
        throw new IllegalArgumentException("Invalid SASL credentials, missing server salt");
      }
      salt = LdapUtils.base64Decode(s);

      final String i = attributes.get("i");
      iterations = Integer.parseInt(i);
      if (iterations < MINIMUM_ITERATION_COUNT) {
        throw new IllegalArgumentException("Invalid SASL credentials, iterations minimum value is 4096");
      }
    }


    public String getMessage()
    {
      return message;
    }


    public String getCombinedNonce()
    {
      return combinedNonce;
    }


    public byte[] getSalt()
    {
      return salt;
    }


    public int getIterations()
    {
      return iterations;
    }
  }


  /** Verifies the final server message. */
  static class ServerFinalMessage
  {

    /** Bytes for the server key hmac. */
    private static final byte[] SERVER_KEY_INIT = "Server Key".getBytes(StandardCharsets.UTF_8);

    /** Server SASL credentials. */
    private final String message;

    /** Whether the server message was successfully verified. */
    private final boolean verified;


    /**
     * Creates a new server final message.
     *
     * @param  mech  scram mechanism
     * @param  clientFinalMessage  final message sent to the server
     * @param  result  response to the final message
     */
    ServerFinalMessage(
      final Mechanism mech,
      final ClientFinalMessage clientFinalMessage,
      final BindResponse result)
    {
      if (result.getServerSaslCreds() == null || result.getServerSaslCreds().length == 0) {
        throw new IllegalArgumentException("Bind response missing server SASL credentials");
      }

      message = new String(result.getServerSaslCreds(), StandardCharsets.UTF_8);
      final Map<String, String> attributes = Stream.of(message.split(","))
        .map(s -> s.split("=", 2)).collect(Collectors.toMap(attr -> attr[0], attr -> attr[1]));

      final String e = attributes.get("e");
      if (e != null) {
        LOGGER.warn("SASL bind server final message included error: {}", e);
      }

      if (result.getResultCode() != ResultCode.SUCCESS) {
        verified = false;
      } else {
        final String serverSignature = attributes.get("v");
        if (serverSignature == null) {
          throw new IllegalArgumentException("Invalid SASL credentials, missing server verification");
        }

        // compare the server signature in the message to what we expect
        final byte[] serverKey =
          createMac(mech.properties()[1], clientFinalMessage.getSaltedPassword()).doFinal(SERVER_KEY_INIT);
        final String expectedServerSignature = LdapUtils.base64Encode(
          createMac(
            mech.properties()[1], serverKey).doFinal(clientFinalMessage.getMessage().getBytes(StandardCharsets.UTF_8)));
        if (!expectedServerSignature.equals(serverSignature)) {
          throw new IllegalArgumentException("Invalid SASL credentials, incorrect server verification");
        }
        verified = true;
      }
    }


    /**
     * Returns whether the server final message was successfully verified.
     *
     * @return  whether the server message was verified.
     */
    public boolean isVerified()
    {
      return verified;
    }
  }


  /**
   * Creates a new MAC using the supplied algorithm and key.
   *
   * @param  algorithm  of the MAC
   * @param  key  to seed the MAC
   *
   * @return  new mac
   */
  private static Mac createMac(final String algorithm, final byte[] key)
  {
    try {
      final Mac mac = Mac.getInstance(algorithm);
      mac.init(new SecretKeySpec(key, algorithm));
      return mac;
    } catch (Exception e) {
      throw new IllegalStateException("Could not create MAC", e);
    }
  }


  /**
   * Digests the supplied data using the supplied algorithm.
   *
   * @param  algorithm  of the digest
   * @param  data  to digest
   *
   * @return  digested data
   */
  private static byte[] createDigest(final String algorithm, final byte[] data)
  {
    try {
      return MessageDigest.getInstance(algorithm).digest(data);
    } catch (Exception e) {
      throw new IllegalStateException("Could not create digest", e);
    }
  }
}
