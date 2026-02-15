package org.hypergraph_hash.test_hypergraph_encryption;

import org.hypergraph_hash.symmetric_encryption.SymmetricAlgorithm;
import org.hypergraph_hash.hypergraph.HomogenousHypergraph;
import org.hypergraph_hash.hypergraph.HyperEdge;
import org.hypergraph_hash.hypergraph.transform.HypergraphEncryption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hypergraph_hash.utilities.CryptoUtilities.getHashIV;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hypergraph_hash.symmetric_encryption.enums.EncryptionMode.*;
import static org.hypergraph_hash.symmetric_encryption.enums.PaddingMode.*;

class HypergraphEncryptionContextTest {
  private static final String TEST_DIRECTORY = "src/test/resources/HypergraphEncryptionTest";

  @BeforeAll
  static void initTestClass() throws IOException {
    Path path = Path.of(TEST_DIRECTORY);
    if (Files.notExists(path)) {
      Files.createDirectory(path);
    }
  }

  // region --- Test of cipher modes via both encryption and decryption ---

  @ParameterizedTest
  @MethodSource("messageKeyBlockProvider")
  void testECBCycle(byte[] message, HomogenousHypergraph key, int smallBlockSize) {
    // EXECUTION
    var cryptoSystem = new HypergraphEncryption(key, smallBlockSize);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, ECB, ZEROS);

    byte[] decryptedMessage = cryptoContext.decrypt(cryptoContext.encrypt(message));

    // ASSERTION
    assertThat(decryptedMessage).containsExactly(message);
  }

  @ParameterizedTest
  @MethodSource("messageKeyBlockProvider")
  void testCBCCycle(byte[] message, HomogenousHypergraph key, int smallBlockSize) {
    // EXECUTION
    var cryptoSystem = new HypergraphEncryption(key, smallBlockSize);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, CBC, ZEROS);

    byte[] decryptedMessage = cryptoContext.decrypt(cryptoContext.encrypt(message));


    // ASSERTION
    assertThat(decryptedMessage).containsExactly(message);
  }

  @ParameterizedTest
  @MethodSource("messageKeyBlockProvider")
  void testPCBCCycle(byte[] message, HomogenousHypergraph key, int smallBlockSize) {
    // EXECUTION
    var cryptoSystem = new HypergraphEncryption(key, smallBlockSize);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, PCBC, ZEROS);

    byte[] decryptedMessage = cryptoContext.decrypt(cryptoContext.encrypt(message));


    // ASSERTION
    assertThat(decryptedMessage).containsExactly(message);
  }

  @ParameterizedTest
  @MethodSource("messageKeyBlockProvider")
  void testCFBCycle(byte[] message, HomogenousHypergraph key, int smallBlockSize) {
    // EXECUTION
    var cryptoSystem = new HypergraphEncryption(key, smallBlockSize);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, CFB, ZEROS);

    byte[] decryptedMessage = cryptoContext.decrypt(cryptoContext.encrypt(message));


    // ASSERTION
    assertThat(decryptedMessage).containsExactly(message);
  }

  @ParameterizedTest
  @MethodSource("messageKeyBlockProvider")
  void testOFBCycle(byte[] message, HomogenousHypergraph key, int smallBlockSize) {
    // EXECUTION
    var cryptoSystem = new HypergraphEncryption(key, smallBlockSize);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, OFB, ZEROS);

    byte[] decryptedMessage = cryptoContext.decrypt(cryptoContext.encrypt(message));


    // ASSERTION
    assertThat(decryptedMessage).containsExactly(message);
  }

  @ParameterizedTest
  @MethodSource("messageKeyBlockProvider")
  void testCTRCycle(byte[] message, HomogenousHypergraph key, int smallBlockSize) {
    // EXECUTION
    var cryptoSystem = new HypergraphEncryption(key, smallBlockSize);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, CTR, ZEROS);

    byte[] decryptedMessage = cryptoContext.decrypt(cryptoContext.encrypt(message));

    // ASSERTION
    assertThat(decryptedMessage).containsExactly(message);
  }

  @ParameterizedTest
  @MethodSource("messageKeyDeltaProvider")
  void testRDCycle(byte[] message, HomogenousHypergraph key, int smallBlockSize, BigInteger delta) {
    // EXECUTION
    var cryptoSystem = new HypergraphEncryption(key, smallBlockSize);
    var cryptoContext = new SymmetricAlgorithm(
            cryptoSystem, RD, ZEROS, getHashIV(smallBlockSize * key.getVerticesCount()), delta);

    byte[] decryptedMessage = cryptoContext.decrypt(cryptoContext.encrypt(message));


    // ASSERTION
    assertThat(decryptedMessage).containsExactly(message);
  }

  // endregion

  // region --- Test file encryption and decryption ---

  @Test
  void testTextFileCycle() throws IOException {
    // SETUP
    HomogenousHypergraph key = keysProvider()[0];

    String in = TEST_DIRECTORY + "/allocator_red_black_tree_tests.cpp";
    String encOut = TEST_DIRECTORY + "/encryptedAllocRBTTests";
    String decOut = TEST_DIRECTORY + "/decryptedAllocRBTTests.cpp";

    // EXECUTION
    var cryptoSystem = new HypergraphEncryption(key, 1);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, PCBC, PKCS7);

    cryptoContext.encrypt(in, encOut);
    cryptoContext.decrypt(encOut, decOut);

    // ASSERTION
    assertTrue(areFilesEqual(in, decOut));
  }

  @Test
  void testPictureFileCycle() throws IOException {
    // SETUP
    HomogenousHypergraph key = keysProvider()[0];

    String in = TEST_DIRECTORY + "/picture.jpg";
    String encOut = TEST_DIRECTORY + "/encryptedPicture";
    String decOut = TEST_DIRECTORY + "/decryptedPicture.jpg";

    // EXECUTION
    var cryptoSystem = new HypergraphEncryption(key, 1);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, CTR, ANSIX923);

    cryptoContext.encrypt(in, encOut);
    cryptoContext.decrypt(encOut, decOut);

    // ASSERTION
    assertTrue(areFilesEqual(in, decOut));
  }

  // endregion

  // region --- Data providers ---

  static byte[][] messagesProvider() {
    return new byte[][] {
            {
              (byte) 0xFD, (byte) 0xCE, (byte) 0xA5, (byte) 0x8D,
              (byte) 0x37, (byte) 0x0B, (byte) 0x42, (byte) 0x50,
            },
            {
              (byte) 0x57, (byte) 0x30, (byte) 0x4A, (byte) 0xF5, (byte) 0x44, (byte) 0xA9, (byte) 0x55, (byte) 0x70,
              (byte) 0x4D, (byte) 0xA5, (byte) 0x39, (byte) 0x2F, (byte) 0xEF, (byte) 0x67, (byte) 0x12, (byte) 0x33,
              (byte) 0x7D, (byte) 0xF4, (byte) 0x42, (byte) 0xED, (byte) 0x32, (byte) 0xB4, (byte) 0x50, (byte) 0x84,
              (byte) 0x8D, (byte) 0x8D, (byte) 0xE6, (byte) 0x73, (byte) 0x69, (byte) 0x5F, (byte) 0x6F, (byte) 0x91,
              (byte) 0x5E, (byte) 0x60, (byte) 0x55, (byte) 0x73, (byte) 0xC0, (byte) 0xF2, (byte) 0x99, (byte) 0xA5,
              (byte) 0xFE, (byte) 0xE6, (byte) 0x35, (byte) 0x17, (byte) 0x99, (byte) 0x59, (byte) 0x98, (byte) 0x54,
              (byte) 0xD4, (byte) 0x1B, (byte) 0xE8, (byte) 0x98, (byte) 0x21, (byte) 0xA4, (byte) 0xBE, (byte) 0x9B,
              (byte) 0xEE, (byte) 0xC5, (byte) 0xBC, (byte) 0x7D, (byte) 0x43, (byte) 0x6B, (byte) 0x4B, (byte) 0xD6,
              (byte) 0x17, (byte) 0xF3, (byte) 0x67, (byte) 0xF3, (byte) 0xF7, (byte) 0x11, (byte) 0x53, (byte) 0x7B,
              (byte) 0xB3, (byte) 0x65, (byte) 0x3D, (byte) 0xCD, (byte) 0xC5, (byte) 0xB4, (byte) 0xB0, (byte) 0xDD,
              (byte) 0xA8, (byte) 0x69, (byte) 0xBC, (byte) 0xD8, (byte) 0xBD, (byte) 0x4E, (byte) 0x64, (byte) 0xC2,
              (byte) 0x00, (byte) 0x75, (byte) 0x0D, (byte) 0x20, (byte) 0x91, (byte) 0x4C, (byte) 0xC4, (byte) 0x89,
              (byte) 0x8D, (byte) 0x7F, (byte) 0x44, (byte) 0x1B, (byte) 0xB3, (byte) 0x5A, (byte) 0x90, (byte) 0xF3,
              (byte) 0xEB, (byte) 0xBD, (byte) 0xDD, (byte) 0xE3, (byte) 0x91, (byte) 0xE0, (byte) 0x64, (byte) 0xC3,
              (byte) 0x44, (byte) 0xE4, (byte) 0x17, (byte) 0x2A, (byte) 0xAC, (byte) 0xC5, (byte) 0xBA, (byte) 0x09,
              (byte) 0xB7, (byte) 0xBA, (byte) 0xD4, (byte) 0xC7, (byte) 0x94, (byte) 0x7F, (byte) 0xEE, (byte) 0x6C,
            },
            {
              (byte) 0x59, (byte) 0x19, (byte) 0x40, (byte) 0x73, (byte) 0xAC, (byte) 0x50, (byte) 0xBE, (byte) 0xEE,
              (byte) 0x7C, (byte) 0x24
            },
            {
              (byte) 0xF5, (byte) 0x01, (byte) 0x77, (byte) 0x6E, (byte) 0x14, (byte) 0x41, (byte) 0x2C, (byte) 0x74,
              (byte) 0xE4, (byte) 0x47, (byte) 0xA0, (byte) 0x01, (byte) 0x1C, (byte) 0x96, (byte) 0x6C, (byte) 0x4A,
              (byte) 0x97, (byte) 0xBF, (byte) 0x78, (byte) 0xAA, (byte) 0x72, (byte) 0x06, (byte) 0x82, (byte) 0xAE,
              (byte) 0x15, (byte) 0x9E, (byte) 0xAD, (byte) 0x2C, (byte) 0x3F, (byte) 0xF7, (byte) 0x46, (byte) 0x71,
              (byte) 0x5E, (byte) 0x33, (byte) 0x85, (byte) 0xF8, (byte) 0x5A, (byte) 0x44, (byte) 0xFC, (byte) 0x71,
              (byte) 0x04, (byte) 0xDF, (byte) 0x50, (byte) 0x3C, (byte) 0x82, (byte) 0x23, (byte) 0x58, (byte) 0xCC,
              (byte) 0x69, (byte) 0xBC, (byte) 0xFD, (byte) 0xF4, (byte) 0xA3, (byte) 0xEA, (byte) 0x93, (byte) 0xA8,
              (byte) 0x5A, (byte) 0xCD, (byte) 0x07, (byte) 0xBA, (byte) 0xAA, (byte) 0xD1, (byte) 0xDA, (byte) 0xCD,
              (byte) 0x16, (byte) 0x89, (byte) 0x01, (byte) 0x6C, (byte) 0x9A, (byte) 0x28, (byte) 0x2D, (byte) 0x61,
              (byte) 0x17, (byte) 0x09, (byte) 0x1F, (byte) 0xE7, (byte) 0x02, (byte) 0x1E, (byte) 0xAB, (byte) 0xD0,
              (byte) 0x22, (byte) 0x49, (byte) 0x8D, (byte) 0x29, (byte) 0x0D, (byte) 0xBA, (byte) 0x5C, (byte) 0xD2,
              (byte) 0x0A, (byte) 0x35, (byte) 0xDD, (byte) 0xDB, (byte) 0xC8, (byte) 0x38, (byte) 0xFA, (byte) 0x89,
              (byte) 0x86, (byte) 0xA8, (byte) 0x5C, (byte) 0x6C, (byte) 0xF3, (byte) 0x5A, (byte) 0xA8, (byte) 0x5E,
              (byte) 0x08, (byte) 0x43, (byte) 0x19
            }
    };
  }

  static HomogenousHypergraph[] keysProvider() {
    return new HomogenousHypergraph[] {
            HomogenousHypergraph.ofEdges(
                    HyperEdge.of(0, 1, 4),
                    HyperEdge.of(0, 1, 5),
                    HyperEdge.of(1, 2, 3),
                    HyperEdge.of(3, 4, 5)
            ),
            HomogenousHypergraph.ofEdges(
                    HyperEdge.of(0, 3, 4),
                    HyperEdge.of(2, 3, 4),
                    HyperEdge.of(1, 2, 3),
                    HyperEdge.of(0, 1, 5)
            ),
            HomogenousHypergraph.ofEdges(
                    HyperEdge.of(0, 1, 7),
                    HyperEdge.of(1, 6, 5),
                    HyperEdge.of(2, 0, 3),
                    HyperEdge.of(3, 7, 5),
                    HyperEdge.of(4, 5, 6),
                    HyperEdge.of(5, 0, 1),
                    HyperEdge.of(6, 2, 3),
                    HyperEdge.of(7, 4, 2)
            ),
            HomogenousHypergraph.ofEdges(
                    HyperEdge.of(0, 1, 7),
                    HyperEdge.of(1, 8, 5),
                    HyperEdge.of(2, 10, 3),
                    HyperEdge.of(3, 7, 31),
                    HyperEdge.of(4, 5, 6),
                    HyperEdge.of(5, 25, 1),
                    HyperEdge.of(6, 2, 9),
                    HyperEdge.of(7, 4, 2),
                    HyperEdge.of(8, 22, 1),
                    HyperEdge.of(9, 6, 8),
                    HyperEdge.of(10, 7, 9),
                    HyperEdge.of(11, 0, 15),
                    HyperEdge.of(12, 13, 2),
                    HyperEdge.of(13, 11, 14),
                    HyperEdge.of(14, 22, 3),
                    HyperEdge.of(15, 5, 9),
                    HyperEdge.of(16, 10, 22),
                    HyperEdge.of(17, 12, 19),
                    HyperEdge.of(18, 3, 25),
                    HyperEdge.of(19, 16, 28),
                    HyperEdge.of(20, 7, 31),
                    HyperEdge.of(21, 9, 14),
                    HyperEdge.of(22, 18, 27),
                    HyperEdge.of(23, 4, 30),
                    HyperEdge.of(24, 11, 26),
                    HyperEdge.of(25, 8, 21),
                    HyperEdge.of(26, 15, 29),
                    HyperEdge.of(27, 6, 20),
                    HyperEdge.of(28, 13, 23),
                    HyperEdge.of(29, 17, 24),
                    HyperEdge.of(30, 0, 31),
                    HyperEdge.of(31, 19, 25)
            )
    };
  }

  static int[] smallBlockSizeProvider() {
    return new int[] {
            1, 2, 3, 8, 10, 16, 50, 100, 128
    };
  }

  static BigInteger[] deltasProvider() {
    return new BigInteger[] {
            new BigInteger("0CB803D5AB15D23E", 16),
            new BigInteger("8837E85E8F4DD256", 16),
            new BigInteger("ECA2FA29F1AB2AF0", 16),
            new BigInteger("FFFFFFFFFFFFFFFF", 16),
            new BigInteger("F000000000000000", 16),
    };
  }

  static Stream<Arguments> messageKeyBlockProvider() {
    return Arrays.stream(messagesProvider())
            .flatMap(message -> Arrays.stream(keysProvider())
                    .flatMap(key -> Arrays.stream(smallBlockSizeProvider())
                            .mapToObj(block -> Arguments.of(message, key, block))));
  }

  static Stream<Arguments> messageKeyDeltaProvider() {
    List<Arguments> args = new ArrayList<>();

    for (byte[] message : messagesProvider()) {
      for (HomogenousHypergraph key : keysProvider()) {
        for (int smallBlockSize : smallBlockSizeProvider()) {
          for (BigInteger delta : deltasProvider()) {
            args.add(Arguments.of(message, key, smallBlockSize, delta));
          }
        }
      }
    }

    return args.stream();
  }

  // endregion

  // region -- Utility --

  boolean areFilesEqual(String path1, String path2) throws IOException {
    try (FileChannel fileChannel1 = FileChannel.open(Path.of(path1), StandardOpenOption.READ);
         FileChannel fileChannel2 = FileChannel.open(Path.of(path2), StandardOpenOption.READ)) {
      if (fileChannel1.size() != fileChannel2.size()) {
        return false;
      }

      int blockSize = 1 << 16;
      byte[] arr1 = new byte[blockSize];
      byte[] arr2 = new byte[blockSize];
      
      for (long i = 0; i < fileChannel1.size(); i += blockSize) {
        fileChannel1.read(ByteBuffer.wrap(arr1));
        fileChannel2.read(ByteBuffer.wrap(arr2));
        
        if (!Arrays.equals(arr1, arr2)) {
          return false;
        }
      }

      return true;
    }
  }

  // endregion
}
