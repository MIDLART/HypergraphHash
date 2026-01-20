package org.hypergraph_hash.testHypergraphEncryption;

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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hypergraph_hash.symmetric_encryption.enums.EncryptionMode.*;
import static org.hypergraph_hash.symmetric_encryption.enums.PackingMode.*;

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
  @MethodSource("messageKeyInitVectorProvider")
  void testCBCCycle(byte[] message, HomogenousHypergraph key, byte[] initVector) {
    // EXECUTION
    var cryptoSystem = new HypergraphEncryption(key, initVector.length / Byte.SIZE);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, CBC, ZEROS, initVector);

    byte[] decryptedMessage = cryptoContext.decrypt(cryptoContext.encrypt(message));


    // ASSERTION
    assertThat(decryptedMessage).containsExactly(message);
  }

  @ParameterizedTest
  @MethodSource("messageKeyInitVectorProvider")
  void testPCBCCycle(byte[] message, HomogenousHypergraph key, byte[] initVector) {
    // EXECUTION
    var cryptoSystem = new HypergraphEncryption(key, initVector.length / Byte.SIZE);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, PCBC, ZEROS, initVector);

    byte[] decryptedMessage = cryptoContext.decrypt(cryptoContext.encrypt(message));


    // ASSERTION
    assertThat(decryptedMessage).containsExactly(message);
  }

  @ParameterizedTest
  @MethodSource("messageKeyInitVectorProvider")
  void testCFBCycle(byte[] message, HomogenousHypergraph key, byte[] initVector) {
    // EXECUTION
    var cryptoSystem = new HypergraphEncryption(key, initVector.length / Byte.SIZE);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, CFB, ZEROS, initVector);

    byte[] decryptedMessage = cryptoContext.decrypt(cryptoContext.encrypt(message));


    // ASSERTION
    assertThat(decryptedMessage).containsExactly(message);
  }

  @ParameterizedTest
  @MethodSource("messageKeyInitVectorProvider")
  void testOFBCycle(byte[] message, HomogenousHypergraph key, byte[] initVector) {
    // EXECUTION
    var cryptoSystem = new HypergraphEncryption(key, initVector.length / Byte.SIZE);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, OFB, ZEROS, initVector);

    byte[] decryptedMessage = cryptoContext.decrypt(cryptoContext.encrypt(message));


    // ASSERTION
    assertThat(decryptedMessage).containsExactly(message);
  }

  @ParameterizedTest
  @MethodSource("messageKeyInitVectorProvider")
  void testCTRCycle(byte[] message, HomogenousHypergraph key, byte[] initVector) {
    // EXECUTION
    var cryptoSystem = new HypergraphEncryption(key, initVector.length / Byte.SIZE);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, CTR, ZEROS, initVector);

    byte[] decryptedMessage = cryptoContext.decrypt(cryptoContext.encrypt(message));

    // ASSERTION
    assertThat(decryptedMessage).containsExactly(message);
  }

  @ParameterizedTest
  @MethodSource("messageKeyInitVectorDeltaProvider")
  void testRDCycle(byte[] message, HomogenousHypergraph key, byte[] initVector, BigInteger delta) {
    // EXECUTION
    var cryptoSystem = new HypergraphEncryption(key, initVector.length / Byte.SIZE);
    var cryptoContext = new SymmetricAlgorithm(
            cryptoSystem, RD, ZEROS, initVector, delta);

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
    byte[] initVector = initVectorsProvider()[0];

    String in = TEST_DIRECTORY + "/allocator_red_black_tree_tests.cpp";
    String encOut = TEST_DIRECTORY + "/encryptedAllocRBTTests";
    String decOut = TEST_DIRECTORY + "/decryptedAllocRBTTests.cpp";

    // EXECUTION
    var cryptoSystem = new HypergraphEncryption(key, initVector.length / Byte.SIZE);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, PCBC, PKCS7, initVector);

    cryptoContext.encrypt(in, encOut);
    cryptoContext.decrypt(encOut, decOut);

    // ASSERTION
    assertTrue(areFilesEqual(in, decOut));
  }

  @Test
  void testPictureFileCycle() throws IOException {
    // SETUP
    HomogenousHypergraph key = keysProvider()[0];
    byte[] initVector = initVectorsProvider()[0];

    String in = TEST_DIRECTORY + "/picture.jpg";
    String encOut = TEST_DIRECTORY + "/encryptedPicture";
    String decOut = TEST_DIRECTORY + "/decryptedPicture.jpg";

    // EXECUTION
    var cryptoSystem = new HypergraphEncryption(key, initVector.length / Byte.SIZE);
    var cryptoContext = new SymmetricAlgorithm(cryptoSystem, CTR, ANSIX923, initVector);

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

  static byte[][] initVectorsProvider() {
    return new byte[][] {
            {
              (byte) 0xDE, (byte) 0x73, (byte) 0x23, (byte) 0x5A, (byte) 0x5B, (byte) 0x52, (byte) 0x8F, (byte) 0x8B,
            },
            {
              (byte) 0x89, (byte) 0x01, (byte) 0x37, (byte) 0x23, (byte) 0xA0, (byte) 0xB1, (byte) 0x99, (byte) 0xE4,
              (byte) 0xDE, (byte) 0x73, (byte) 0x23, (byte) 0x5A, (byte) 0x5B, (byte) 0x52, (byte) 0x8F, (byte) 0x8B,
            },
            {
              (byte) 0xB3, (byte) 0x58, (byte) 0x98, (byte) 0x6D, (byte) 0xA4, (byte) 0xAB, (byte) 0xD7, (byte) 0x5C,
              (byte) 0x13, (byte) 0x48, (byte) 0x67, (byte) 0x68, (byte) 0x34, (byte) 0x90, (byte) 0x53, (byte) 0xD4,
            },
            {
              (byte) 0x9C, (byte) 0xBD, (byte) 0xE5, (byte) 0x4F, (byte) 0x3F, (byte) 0xC6, (byte) 0x14, (byte) 0x87,
              (byte) 0xF9, (byte) 0xDB, (byte) 0xB2, (byte) 0x46, (byte) 0x14, (byte) 0xEB, (byte) 0xEA, (byte) 0x48,
              (byte) 0x25, (byte) 0xA1, (byte) 0x14, (byte) 0x82, (byte) 0x13, (byte) 0x28, (byte) 0xC2, (byte) 0x06,
              (byte) 0x4D, (byte) 0x0C, (byte) 0x60, (byte) 0x18, (byte) 0x0B, (byte) 0x9D, (byte) 0x77, (byte) 0x37,
            },
            {
              (byte) 0x25, (byte) 0xA1, (byte) 0x14, (byte) 0x82, (byte) 0x13, (byte) 0x28, (byte) 0xC2, (byte) 0x06,
              (byte) 0x4D, (byte) 0x0C, (byte) 0x60, (byte) 0x18, (byte) 0x0B, (byte) 0x9D, (byte) 0x77, (byte) 0x37,
              (byte) 0x89, (byte) 0x01, (byte) 0x37, (byte) 0x23, (byte) 0xA0, (byte) 0xB1, (byte) 0x99, (byte) 0xE4,
              (byte) 0xDE, (byte) 0x73, (byte) 0x23, (byte) 0x5A, (byte) 0x5B, (byte) 0x52, (byte) 0x8F, (byte) 0x8B,
              (byte) 0xB3, (byte) 0x58, (byte) 0x98, (byte) 0x6D, (byte) 0xA4, (byte) 0xAB, (byte) 0xD7, (byte) 0x5C,
              (byte) 0x13, (byte) 0x48, (byte) 0x67, (byte) 0x68, (byte) 0x34, (byte) 0x90, (byte) 0x53, (byte) 0xD4,
              (byte) 0x9C, (byte) 0xBD, (byte) 0xE5, (byte) 0x4F, (byte) 0x3F, (byte) 0xC6, (byte) 0x14, (byte) 0x87,
              (byte) 0xF9, (byte) 0xDB, (byte) 0xB2, (byte) 0x46, (byte) 0x14, (byte) 0xEB, (byte) 0xEA, (byte) 0x48,
            }
    };
  }


  static Stream<Arguments> messageKeyBlockProvider() {
    return Arrays.stream(messagesProvider())
            .flatMap(message -> Arrays.stream(keysProvider())
                    .flatMap(key -> Arrays.stream(smallBlockSizeProvider())
                            .mapToObj(block -> Arguments.of(message, key, block))));
  }

  static Stream<Arguments> messageKeyInitVectorProvider() {
    List<Arguments> args = new ArrayList<>();

    for (byte[] message : messagesProvider()) {
      for (HomogenousHypergraph key : keysProvider()) {
        for (byte[] iv : initVectorsProvider()) {
          args.add(Arguments.of(message, key, iv));
        }
      }
    }

    return args.stream();
  }

  static Stream<Arguments> messageKeyInitVectorDeltaProvider() {
    List<Arguments> args = new ArrayList<>();

    for (byte[] message : messagesProvider()) {
      for (HomogenousHypergraph key : keysProvider()) {
        for (byte[] iv : initVectorsProvider()) {
          for (BigInteger delta : deltasProvider()) {
            args.add(Arguments.of(message, key, iv, delta));
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
