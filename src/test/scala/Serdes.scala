package nettest

import org.specs2._

import zio.{ Chunk, DefaultRuntime }

import ParquetPkg._
import ParquetReader._

import zio.serdes._

class SerdesSpec extends Specification with DefaultRuntime {

  val path = "/tmp/hello.pq"

  val rows: Chunk[TypeData] =
    for {
      frame <- Reader.getFrame(path)
      data  <- Reader.getRows(frame)
    } yield data

  def is = s2"""

  Serdes should      
    serialize parquet                         $parSerdes

    """

  def parSerdes = {

    // Read parquet data
    val path = "/tmp/hello.pq"

    val rows: Chunk[TypeData] =
      for {
        frame <- Reader.getFrame(path)
        data  <- Reader.getRows(frame)
      } yield data

    val arr: BArr            = Array(1, 2, 3)
    val bytes: Chunk[Byte]   = Serdes[Chunk, Chunk].serialize[TypeData](rows)
    val out: Chunk[TypeData] = Serdes[Chunk, Chunk].deserialize[TypeData](bytes)

    out === Chunk.fromArray(arr)

  }
}
