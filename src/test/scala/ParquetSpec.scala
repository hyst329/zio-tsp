package partest

//import zio.{ DefaultRuntime, UIO }
import org.specs2._
//import ParReader

class SimpleSpec extends Specification {

  //val rt = new DefaultRuntime {}

  def is = s2"""

 TSP Parquet should
    read and parse hello.pq $t1    
    
    """

  def t1 = {

    val path = "/tmp/hello.pq"
    true must_== true

  }

  

}
