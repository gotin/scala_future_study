import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise, Await}
import scala.concurrent.duration._
import scala.util.{Failure, Random, Success}

object FutureSample extends App {
  val random = new Random()
  val MAX = 3000

  def waitRandomWithFuture(name: String): Future[Int] = {
    def waitRandom(name:String): Int = {
      val waitMilliSec = random.nextInt(MAX)
      if(waitMilliSec < 500) throw new RuntimeException(s"${name} waitMilliSec is ${waitMilliSec}")
      Thread.sleep(waitMilliSec)
      waitMilliSec
    }

    Future {
      waitRandom(name)
    }
  }

  val future1 = waitRandomWithFuture("one")
  val future2 = waitRandomWithFuture("two")

  val result = for { // type of result will be Future[Int]
    one <- future1
    two <- future2
  } yield {
    one + two // one + two will become a value of result (in future)
    // type of Future#value is Option[Either[Throwable, T]] by the way.
  }
  result onComplete {
    case Success(r) => println(s"Success! ${r}")
    case Failure(t) => println(s"Failure: ${t.getMessage}")
  }
  // Thread.sleep(MAX+1000)
  // println(result)
  println(result.value) // will be None 
  Await.ready(result, (MAX+1000).millisecond)
}
