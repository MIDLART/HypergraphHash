package org.hypergraph_hash;

import org.hypergraph_hash.symmetric_encryption.block.ArrayRead;
import org.hypergraph_hash.symmetric_encryption.block.ReadBlock;
import org.hypergraph_hash.symmetric_encryption.modes.Packing;

import static org.hypergraph_hash.operations.BitOperations.xor;
import static org.hypergraph_hash.symmetric_encryption.enums.PackingMode.PKCS7;

//TODO abstract?
public class MerkleDamgardConstruction implements CryptographicHash {
  private final int hashLength; //TODO финальное сжатие
  
  private final BlockTransform blockTransform;
  private final byte[] iv;

  public MerkleDamgardConstruction(BlockTransform blockTransform) {
    hashLength = blockTransform.getBlockSize();
    this.blockTransform = blockTransform;

    this.iv = new byte[hashLength];
    initIV();
  }

  public MerkleDamgardConstruction(BlockTransform blockTransform,
                                      byte[] iv) {
    hashLength = blockTransform.getBlockSize();
    this.blockTransform = blockTransform;

    this.iv = new byte[hashLength];
    System.arraycopy(iv, 0, this.iv, 0, hashLength);
  }

  @Override
  public byte[] hash(byte[] input) {
    ReadBlock readBlock = new ArrayRead(input, hashLength, new Packing(hashLength, PKCS7)::fill); //TODO специальный packing mode

    int blockCount = getBlockCount(input.length);
    byte[] first = readBlock.get(0);

    byte[] hash = compressionFunction(first, iv);

    for (int i = 1; i < blockCount; i++) {
      byte[] m = readBlock.get(i);

      hash = compressionFunction(m, hash);
    }

    return finalisationFunction(hash);
  }

  @Override
  public final int getHashLength() {
    return hashLength;
  }

  @Override
  public String getAlgorithmName() {
    return "MerkleDamgardConstruction";
  }

  protected byte[] compressionFunction(byte[] inputBlock, byte[] prevHash) {
    return xor(blockTransform.encryption(inputBlock), prevHash);
  }

  protected byte[] finalisationFunction(byte[] input) {
    return input;
  }

  private int getBlockCount(long length) {
    return (int) (length / hashLength + 1);
  }

  private void initIV() {
    for (int i = 0; i < hashLength; i++) {
      iv[i] = (byte) i;
    }
  }
}
