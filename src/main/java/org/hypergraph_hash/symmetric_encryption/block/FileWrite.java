package org.hypergraph_hash.symmetric_encryption.block;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.function.Function;
import java.util.function.UnaryOperator;

@Slf4j
public class FileWrite implements WriteBlock {
  private final String fileName;
  private final int blockSize;
  private Function<byte[], byte[]> unpackingFunction = null;
  private long fileSize = 0;

  public FileWrite(String fileName, int blockSize) {
    this.fileName = fileName;
    this.blockSize = blockSize;
  }

  public FileWrite(String fileName, int blockSize, long fileSize, UnaryOperator<byte[]> unpackingFunction) {
    this.fileName = fileName;
    this.blockSize = blockSize;
    this.unpackingFunction = unpackingFunction;
    this.fileSize = fileSize;
  }

  @Override
  public void put(int index, byte[] block) {
    index *= blockSize;

    try (FileChannel output = FileChannel.open(
            Paths.get(fileName),
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE)) {

      output.position(index);

      int written;
      if (unpackingFunction != null && index + blockSize >= fileSize) {
        written = output.write(ByteBuffer.wrap(unpackingFunction.apply(block)));
      } else {
        written = output.write(ByteBuffer.wrap(block));
      }

      if (written != block.length) {
        log.warn("Incomplete block entry in a file");
      }
    } catch (IOException e) {
      throw new IllegalStateException("Failed to write to file: " + fileName, e);
    }
  }
}
