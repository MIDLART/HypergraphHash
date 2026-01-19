package org.hypergraph_hash;

import org.hypergraph_hash.symmetric_encryption.block.ArrayRead;
import org.hypergraph_hash.symmetric_encryption.block.ReadBlock;
import org.hypergraph_hash.symmetric_encryption.modes.Padding;

import static org.hypergraph_hash.symmetric_encryption.enums.PackingMode.NO;
import static org.hypergraph_hash.utilities.CryptoUtilities.getHashIV;

public abstract class MerkleDamgardConstruction implements CryptographicHash {
  private final int blockSize;
  protected final int hashLength;
  
  protected final BlockTransform blockTransform;
  private final byte[] iv;

  protected MerkleDamgardConstruction(BlockTransform blockTransform, int hashLength) {
    this(blockTransform, hashLength, getHashIV(blockTransform.getBlockSize()));
  }

  protected MerkleDamgardConstruction(BlockTransform blockTransform, int hashLength, byte[] iv) {
    blockSize = blockTransform.getBlockSize();

    if (hashLength <= 0 || hashLength > blockSize) {
      throw new IllegalArgumentException("hash length must be a positive number <= " + (blockSize - 1));
    }

    this.hashLength = hashLength;
    this.blockTransform = blockTransform;

    this.iv = new byte[blockSize];
    System.arraycopy(iv, 0, this.iv, 0, blockSize);
  }

  @Override
  public byte[] hash(byte[] input) {
    if (input == null || input.length == 0) {
      return new byte[blockSize];
    }

    input = padding(input);
    ReadBlock readBlock = new ArrayRead(input, blockSize, new Padding(blockSize, NO)::fill);

    int blockCount = input.length / blockSize;
    byte[] first = readBlock.get(0);

    byte[] hash = compressionFunction(first, iv);

    for (int i = 1; i < blockCount; i++) {
      byte[] m = readBlock.get(i);

      hash = compressionFunction(m, hash);
    }

    return finalisationFunction(hash);
  }

  protected abstract byte[] compressionFunction(byte[] inputBlock, byte[] prevHash);

  protected abstract byte[] finalisationFunction(byte[] input);

  private byte[] padding(byte[] input) {
    int lengthBytes = 8;
    long messageBits = (long) input.length * 8;

    int totalLength = input.length + 1 + lengthBytes;
    while (totalLength % blockSize != 0) {
      totalLength++;
    }

    byte[] res = new byte[totalLength];
    System.arraycopy(input, 0, res, 0, input.length);

    res[input.length] = (byte) 0x80;

    int lengthPos = res.length - lengthBytes;
    for (int i = 0; i < lengthBytes; i++) {
      res[lengthPos + i] = (byte) (messageBits >>> (56 - i * 8));
    }

    return res;
  }

  @Override
  public final int getHashLength() {
    return hashLength;
  }
}
