package org.hypergraph_hash.testHypergraphEncryption;

import org.hypergraph_hash.hypergraph.HomogenousHypergraph;
import org.hypergraph_hash.hypergraph.HyperEdge;
import org.hypergraph_hash.hypergraph.HypergraphEncryption;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class HypergraphEncryptionTest {
  @Test
  void testEncryptionCycle() {
    // SETUP
    var key = HomogenousHypergraph.ofEdges(
            HyperEdge.of(0, 3, 4),
            HyperEdge.of(2, 3, 4),
            HyperEdge.of(1, 2, 3),
            HyperEdge.of(0, 1, 5)
    );
    byte[] message = new byte[] {(byte) 0x27, (byte) 0x00, (byte) 0xFF, (byte) 0x11, (byte) 0xAD, (byte) 0x08};

    // EXECUTION
    var encryptor = new HypergraphEncryption(key, 1);
    byte[] encrypted = encryptor.encryption(message);
    byte[] decrypted = encryptor.decryption(encrypted);

    // ASSERTION
    assertThat(encrypted).isNotEqualTo(message);
    assertThat(decrypted).isEqualTo(message);
  }

  @ParameterizedTest
  @MethodSource("argumentsProvider")
  void testEncryption(HomogenousHypergraph key, int smallBlockSize,
                                    byte[] message, byte[] expectedCipher) {
    // SETUP
    byte[] originalMessage = Arrays.copyOf(message, message.length);

    // EXECUTION
    var encryptor = new HypergraphEncryption(key, smallBlockSize);
    byte[] actualCipher = encryptor.encryption(message);

    // ASSERTION
    assertThat(message).containsExactly(originalMessage);
    assertThat(actualCipher).containsExactly(expectedCipher);
  }

  @ParameterizedTest
  @MethodSource("argumentsProvider")
  void testDecryption(HomogenousHypergraph key, int smallBlockSize,
                                    byte[] expectedMessage, byte[] cipher) {
    // SETUP
    byte[] originalCipher = Arrays.copyOf(cipher, cipher.length);

    // EXECUTION
    var encryptor = new HypergraphEncryption(key, smallBlockSize);
    byte[] actualMessage = encryptor.decryption(cipher);

    // ASSERTION
    assertThat(cipher).containsExactly(originalCipher);
    assertThat(actualMessage).containsExactly(expectedMessage);
  }


  static Stream<Arguments> argumentsProvider() {
    return Stream.of(
            Arguments.of(
                    HomogenousHypergraph.ofEdges(
                            HyperEdge.of(0, 1, 4),
                            HyperEdge.of(0, 1, 5),
                            HyperEdge.of(1, 2, 3),
                            HyperEdge.of(3, 4, 5)
                    ),
                    1,
                    new byte[] {
                            (byte) 0x55, (byte) 0x99, (byte) 0xF0, (byte) 0x0F, (byte) 0xCC, (byte) 0xE5,
                    },
                    new byte[] {
                            (byte) 0xB0, (byte) 0xAA, (byte) 0xD9, (byte) 0x0F, (byte) 0xD6, (byte) 0xFF,
                    }
            ),
            Arguments.of(
                    HomogenousHypergraph.ofEdges(
                            HyperEdge.of(0, 1, 4),
                            HyperEdge.of(0, 1, 5),
                            HyperEdge.of(1, 2, 3),
                            HyperEdge.of(3, 4, 5)
                    ),
                    2,
                    new byte[] {
                            (byte) 0x55, (byte) 0x55, (byte) 0x99, (byte) 0x99, (byte) 0xF0, (byte) 0xF0,
                            (byte) 0x0F, (byte) 0x0F, (byte) 0xCC, (byte) 0xCC, (byte) 0xE5, (byte) 0xE5,
                    },
                    new byte[] {
                            (byte) 0xB0, (byte) 0xB0, (byte) 0xAA, (byte) 0xAA, (byte) 0xD9, (byte) 0xD9,
                            (byte) 0x0F, (byte) 0x0F, (byte) 0xD6, (byte) 0xD6, (byte) 0xFF, (byte) 0xFF,
                    }
            )
    );
  }
}
