package partest

import zio.{ DefaultRuntime }
import org.specs2._
import ParquetReader._

class SimpleSpec extends Specification with DefaultRuntime {

  //val rt   = new DefaultRuntime {}
  //val path = "/tmp/huge.pq"
  val path = "/tmp/hello.pq"

  def is = s2"""

 TSP Parquet should
    read and print rows from file               $t1    
    read and print cols from file               $t2   
    
    """

  def t1 = {

    val rows =
      for {
        frame <- Reader.getFrame(path)
        size  = frame.getRowSize
        data  <- Reader.getRows(frame)
      } yield (size, data)

    val out = rows map (println)
    unsafeRun(out)

    true must_== true
  }

  def t2 = {
    val cols =
      for {
        frame <- Reader.getFrame(path)
        size  = frame.getColSize
        data  <- Reader.getCols(frame)
      } yield (size, data)

    val out = cols map (println)
    unsafeRun(out)

    true must_== true
  }

}
