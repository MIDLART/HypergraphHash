package org.hypergraph_hash.symmetric_encryption;

import org.hypergraph_hash.symmetric_encryption.block.*;
import org.hypergraph_hash.symmetric_encryption.enums.EncryptOrDecrypt;
import org.hypergraph_hash.symmetric_encryption.enums.EncryptionMode;
import org.hypergraph_hash.symmetric_encryption.enums.PaddingMode;
import org.hypergraph_hash.symmetric_encryption.modes.EncryptionModes;
import org.hypergraph_hash.symmetric_encryption.modes.Padding;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hypergraph_hash.operations.ArrayOperations.listToArray;
import static org.hypergraph_hash.symmetric_encryption.enums.EncryptOrDecrypt.*;
import static org.hypergraph_hash.utilities.Validation.EMPTY_INPUT;
import static org.hypergraph_hash.utilities.Validation.validateNotZero;

public class SymmetricAlgorithm {
  private final int blockSize;

  private final EncryptionModes encryptionModes;
  private final Padding padding;

  public SymmetricAlgorithm(SymmetricEncryption symmetricEncryption,
                            EncryptionMode encryptionMode,
                            PaddingMode paddingMode) {
    blockSize = symmetricEncryption.getBlockSize();

    encryptionModes = new EncryptionModes(symmetricEncryption, encryptionMode);
    padding = new Padding(blockSize, paddingMode);
  }

  public SymmetricAlgorithm(SymmetricEncryption symmetricEncryption,
                            EncryptionMode encryptionMode,
                            PaddingMode paddingMode,
                            byte[] iv) {
    this(symmetricEncryption, encryptionMode, paddingMode, iv, BigInteger.ONE);
  }

  public SymmetricAlgorithm(SymmetricEncryption symmetricEncryption,
                            EncryptionMode encryptionMode,
                            PaddingMode paddingMode,
                            byte[] iv,
                            BigInteger delta) {
    blockSize = symmetricEncryption.getBlockSize();

    encryptionModes = new EncryptionModes(symmetricEncryption, encryptionMode, iv, delta);
    padding = new Padding(blockSize, paddingMode);
  }
  
  public byte[] encrypt(byte[] input) {
    int inputLength = input.length;

    byte[] output = new byte[getBlockCount(inputLength, ENCRYPT) * blockSize];

    encrypt(input, output);

    return output;
  }

  public void encrypt(byte[] input, byte[] output) {
    int inputLength = input.length;

    validateNotZero(inputLength, EMPTY_INPUT);

    if (output.length < getBlockCount(inputLength, ENCRYPT) * blockSize) {
      throw new IllegalArgumentException("Output is too small");
    }

    ReadBlock readBlock = new ArrayRead(input, blockSize, padding::fill);
    WriteBlock writeBlock = new ArrayWrite(output, blockSize);

    encryptionModes.encryptionMode(readBlock, writeBlock, getBlockCount(inputLength, ENCRYPT), ENCRYPT);
  }

  public void encrypt(String inputFile, String outputFile) {
    fileErrorCheck(inputFile, outputFile);
    long inputLength = new File(inputFile).length();

    ReadBlock readBlock = new FileRead(inputFile, blockSize, padding::fill);
    WriteBlock writeBlock = new FileWrite(outputFile, blockSize);

    encryptionModes.encryptionMode(readBlock, writeBlock, getBlockCount(inputLength, ENCRYPT), ENCRYPT);
  }

  public void decrypt(byte[] input, byte[] output) {
    int inputLength = input.length;

    validateNotZero(inputLength, EMPTY_INPUT);

    if (inputLength != output.length) {
      throw new IllegalArgumentException("Invalid output length");
    }

    ReadBlock readBlock = new ArrayRead(input, blockSize);
    WriteBlock writeBlock = new ArrayWrite(output, blockSize, padding::unpack);

    encryptionModes.encryptionMode(readBlock, writeBlock, getBlockCount(inputLength, DECRYPT), DECRYPT);
  }

  public byte[] decrypt(byte[] input) {
    int inputLength = input.length;

    validateNotZero(inputLength, EMPTY_INPUT);

    List<byte[]> outputList =
            Collections.synchronizedList(
            new ArrayList<>(
                    Collections.nCopies(getBlockCount(inputLength, DECRYPT), new byte[0])));

    ReadBlock readBlock = new ArrayRead(input, blockSize);
    WriteBlock writeBlock = new ListWrite(outputList, blockSize, inputLength, padding::unpack);

    encryptionModes.encryptionMode(readBlock, writeBlock, getBlockCount(inputLength, DECRYPT), DECRYPT);

    return listToArray(outputList);
  }

  public void decrypt(String inputFile, String outputFile) {
    fileErrorCheck(inputFile, outputFile);
    long inputLength = new File(inputFile).length();

    ReadBlock readBlock = new FileRead(inputFile, blockSize);
    WriteBlock writeBlock = new FileWrite(outputFile, blockSize, inputLength, padding::unpack);

    encryptionModes.encryptionMode(readBlock, writeBlock, getBlockCount(inputLength, DECRYPT), DECRYPT);
  }

  private void fileErrorCheck(String inputFile, String outputFile) {
    if (inputFile == null || inputFile.isEmpty() || !(new File(inputFile).exists())) {
      throw new IllegalArgumentException("Input file not found ");
    }

    if (outputFile == null || outputFile.isEmpty()) {
      throw new IllegalArgumentException("Output file name is empty");
    }

    if (inputFile.equals(outputFile)) {
      throw new IllegalArgumentException("Input and output files cannot be the same");
    }
  }

  private int getBlockCount(long length, EncryptOrDecrypt encryptOrDecrypt) {
    int blockCount = (int) (length / blockSize);
    if (encryptOrDecrypt == ENCRYPT && padding.getMode() != PaddingMode.NO) {
      blockCount++;
    }

    return blockCount;
  }
}
