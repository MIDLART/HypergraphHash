package org.hypergraph_hash.modes;

import org.hypergraph_hash.SymmetricEncryption;
import org.hypergraph_hash.block.ReadBlock;
import org.hypergraph_hash.block.WriteBlock;
import org.hypergraph_hash.enums.EncryptOrDecrypt;
import org.hypergraph_hash.enums.EncryptionMode;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

import static org.hypergraph_hash.enums.EncryptOrDecrypt.ENCRYPT;
import static org.hypergraph_hash.operations.BitOperations.xor;

public class EncryptionModes {
  private final SymmetricEncryption symmetricEncryption;
  private final EncryptionMode encryptionMode;

  private final int blockSize;
  private final byte[] iv;

  private final BigInteger rd;
  private final BigInteger rdMax;

  public EncryptionModes(SymmetricEncryption symmetricEncryption,
                         EncryptionMode encryptionMode,
                         byte[] iv,
                         BigInteger delta) {
    this.symmetricEncryption = symmetricEncryption;
    this.encryptionMode = encryptionMode;
    blockSize = symmetricEncryption.getBlockSize();
    rd = delta;

    this.iv = new byte[blockSize];
    System.arraycopy(iv, 0, this.iv, 0, blockSize);

    rdMax = BigInteger.valueOf(2).pow(8 * blockSize);
  }

  public EncryptionModes(SymmetricEncryption symmetricEncryption,
                         EncryptionMode encryptionMode) {
    this.symmetricEncryption = symmetricEncryption;
    this.encryptionMode = encryptionMode;
    blockSize = symmetricEncryption.getBlockSize();
    rd = BigInteger.ONE;

    this.iv = new byte[blockSize];
    new SecureRandom().nextBytes(iv);

    rdMax = BigInteger.valueOf(2).pow(8 * blockSize);
  }

  public void encryptionMode(ReadBlock readBlock, WriteBlock writeBlock, int blockCount,
                              EncryptOrDecrypt encryptOrDecrypt) {
    switch (encryptionMode) {
      case ECB  -> ECB(readBlock, writeBlock, blockCount, encryptOrDecrypt);
      case CBC  -> CBC(readBlock, writeBlock, blockCount, encryptOrDecrypt);
      case CFB  -> CFB(readBlock, writeBlock, blockCount, encryptOrDecrypt);
      case CTR  -> CTR(readBlock, writeBlock, blockCount);
      case OFB  -> OFB(readBlock, writeBlock, blockCount);
      case PCBC -> PCBC(readBlock, writeBlock, blockCount, encryptOrDecrypt);
      case RD   -> RandomDelta(readBlock, writeBlock, blockCount);
    }
  }

  private void ECB(ReadBlock readBlock, WriteBlock writeBlock, int blockCount,
                   EncryptOrDecrypt encryptOrDecrypt) {
    UnaryOperator<byte[]> function = encryptOrDecrypt == ENCRYPT ?
            symmetricEncryption::encryption : symmetricEncryption::decryption;

    IntStream.range(0, blockCount).parallel().forEach(i -> {
      byte[] buffer = readBlock.get(i);
      writeBlock.put(i, function.apply(buffer));
    });
  }

  private void CBC(ReadBlock readBlock, WriteBlock writeBlock, int blockCount,
                   EncryptOrDecrypt encryptOrDecrypt) {
    UnaryOperator<byte[]> function;

    byte[] first = readBlock.get(0);

    if (encryptOrDecrypt == ENCRYPT) {
      function = symmetricEncryption::encryption;

      byte[] c = function.apply(xor(first, iv));
      writeBlock.put(0, c);

      for (int i = 1; i < blockCount; i++) {
        byte[] m = readBlock.get(i);
        c = function.apply(xor(m, c));

        writeBlock.put(i, c);
      }
    } else {
      function = symmetricEncryption::decryption;

      writeBlock.put(0, xor(function.apply(first), iv));

      IntStream.range(1, blockCount).parallel().forEach(i -> {
        byte[] c = readBlock.get(i);
        byte[] prevC = readBlock.get(i - 1);

        writeBlock.put(i, xor(function.apply(c), prevC));
      });
    }
  }

  private void OFB(ReadBlock readBlock, WriteBlock writeBlock, int blockCount) {
    UnaryOperator<byte[]> function = symmetricEncryption::encryption;

    byte[] E = iv;

    for (int i = 0; i < blockCount; i++) {
      byte[] buffer = readBlock.get(i);
      E = function.apply(E);

      writeBlock.put(i, xor(E, buffer));
    }
  }

  private void CFB(ReadBlock readBlock, WriteBlock writeBlock, int blockCount,
                   EncryptOrDecrypt encryptOrDecrypt) {
    UnaryOperator<byte[]> function = symmetricEncryption::encryption;

    byte[] first = readBlock.get(0);

    if (encryptOrDecrypt == ENCRYPT) {
      byte[] c = xor(function.apply(iv), first);
      writeBlock.put(0, c);

      for (int i = 1; i < blockCount; i++) {

        byte[] m = readBlock.get(i);
        c = xor(function.apply(c), m);

        writeBlock.put(i, c);
      }
    } else {
      writeBlock.put(0, xor(function.apply(iv), first));

      IntStream.range(1, blockCount).parallel().forEach(i -> {
        byte[] c = readBlock.get(i);
        byte[] prevC = readBlock.get(i - 1);

        writeBlock.put(i, xor(function.apply(prevC), c));
      });
    }
  }

  private void PCBC(ReadBlock readBlock, WriteBlock writeBlock, int blockCount,
                    EncryptOrDecrypt encryptOrDecrypt) {
    UnaryOperator<byte[]> function;

    byte[] first = readBlock.get(0);

    if (encryptOrDecrypt == ENCRYPT) {
      function = symmetricEncryption::encryption;

      byte[] c = function.apply(xor(first, iv));
      writeBlock.put(0, c);

      for (int i = 1; i < blockCount; i++) {
        byte[] m = readBlock.get(i);
        byte[] prevM = readBlock.get(i - 1);
        c = function.apply(xor(xor(m, c), prevM));

        writeBlock.put(i, c);
      }
    } else {
      function = symmetricEncryption::decryption;

      byte[] m = xor(function.apply(first), iv);
      writeBlock.put(0, m);

      for (int i = 1; i < blockCount; i++) {
        byte[] c = readBlock.get(i);
        byte[] prevC = readBlock.get(i - 1);
        m = xor(xor(function.apply(c), prevC), m);

        writeBlock.put(i, m);
      }
    }
  }

  private void CTR(ReadBlock readBlock, WriteBlock writeBlock, int blockCount) {
    UnaryOperator<byte[]> function = symmetricEncryption::encryption;
    BigInteger count = new BigInteger(iv);

    IntStream.range(0, blockCount).parallel().forEach(i -> {
      byte[] buffer = readBlock.get(i);
      writeBlock.put(i, xor(buffer, function.apply(counter(count, BigInteger.valueOf(i)))));
    });
  }

  private void RandomDelta(ReadBlock readBlock, WriteBlock writeBlock, int blockCount) {
    UnaryOperator<byte[]> function = symmetricEncryption::encryption;
    BigInteger count = new BigInteger(iv);

    IntStream.range(0, blockCount).parallel().forEach(i -> {
      byte[] buffer = readBlock.get(i);
      writeBlock.put(i, xor(buffer, function.apply(counter(count, modularMultiply(rd, i)))));
    });
  }

  private byte[] counter(BigInteger num, BigInteger n) {
    byte[] res = new byte[blockSize];

    byte[] sum = num.add(n).toByteArray();

    int index = blockSize - 1;
    int sumIndex = sum.length - 1;

    for (; sumIndex >= 0 && index >= 0; sumIndex--, index--) {
      res[index] = sum[sumIndex];
    }

    return res;
  }

  private BigInteger modularMultiply(BigInteger rd, int i) {
    BigInteger b = BigInteger.valueOf(i);

    return rd.multiply(b).mod(rdMax);
  }
}
