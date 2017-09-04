import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise, Await}
import scala.concurrent.duration._
import scala.util.{Failure, Random, Success}

class CallbackSomething {
  val random = new Random()

  def doSomething(onSuccess: Int => Unit, onFailure: Throwable => Unit): Unit = {
    val i = random.nextInt(10)
    if(i < 5) onSuccess(i) else onFailure(new RuntimeException(i.toString))
  }
}

class FutureSomething {
  val callbackSomething = new CallbackSomething

  def doSomething(): Future[Int] = {
    val promise = Promise[Int] 
    callbackSomething.doSomething(i => promise.success(i), t => promise.failure(t))
    promise.future
  }

}

object CallbackFuture extends App {
  val futureSomething = new FutureSomething
  val iFuture = futureSomething.doSomething()
  val jFuture = futureSomething.doSomething()
  val iplusj = for {
    i <- iFuture
    j <- jFuture
  } yield i+j
  val result = Await.result(iplusj, Duration.Inf)
  println(result)
}

/*
 * callback関数を指定して動かすようなAPIの処理系がある場合に、
 * そこに渡す関数内でPromise#success, Promise#failureを実行するように設定しておくと、
 * そのcallback処理を複数実行して後から同期後に処理結果に基づく処理をしたい場合に、
 * （threadのjoin処理するような処理）
 * callback関数に渡したpromiseオブジェクトに対してPromise#futureで
 * futureを受け取るようにしておけば、
 * for式で各futureの評価結果を受けてから動作する処理を書くことができる。
 * という例。
 * めっちゃわかりづらいように見えるけど、
 * 要するにFutureを意識した処理系じゃなくても、promiseを介してfuture化してしまえば
 * for式を使った読みやすい書き方ができるよ、ということ。
 */

/*
 * じゃぁfailureが入る場合、普通はexceptionがthrowされるような場合に、
 * そのexception内容に応じて例外処理をしたい場合はどうすればいいかというと、
 *  https://gist.github.com/rirakkumya/2382341
 *  ↑ここに綺麗な解法がある。
 */
