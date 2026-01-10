package org.hypergraph_hash.testHash;

import org.hypergraph_hash.MerkleDamgardConstruction;
import org.hypergraph_hash.hypergraph.HomogenousHypergraph;
import org.hypergraph_hash.hypergraph.HyperEdge;
import org.hypergraph_hash.hypergraph.transform.HypergraphEncryption;
import org.hypergraph_hash.symmetric_encryption.SymmetricAlgorithm;
import org.hypergraph_hash.symmetric_encryption.enums.EncryptionMode;
import org.hypergraph_hash.symmetric_encryption.enums.PackingMode;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class MerkleDamgardConstructionTest {
  @Test
  void test() {
    // SETUP
    var key = HomogenousHypergraph.ofEdges(
            HyperEdge.of(0, 3, 4),
            HyperEdge.of(2, 3, 4),
            HyperEdge.of(1, 2, 3),
            HyperEdge.of(0, 1, 5)
    );
    byte[] message = new byte[] {
            (byte) 0x27, (byte) 0x01, (byte) 0xFF, (byte) 0x11, (byte) 0xAD, (byte) 0x08,
            (byte) 0x22, (byte) 0x19, (byte) 0xAA, (byte) 0x11, (byte) 0xBD, (byte) 0x65
    };

    // EXECUTION
    var encryptor = new HypergraphEncryption(key, 1);
    var hashAlg = new MerkleDamgardConstruction(encryptor);
    var encryptAlg = new SymmetricAlgorithm(encryptor, EncryptionMode.ECB, PackingMode.ANSIX923);

    byte[] hashed = hashAlg.hash(message);

    byte[] encrypted = encryptAlg.encrypt(message);

    // ASSERTION
    System.out.println(Arrays.toString(hashed));
    System.out.println(Arrays.toString(encrypted));
  }
}
