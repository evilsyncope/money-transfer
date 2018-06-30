import com.twitter.finagle.Http
import com.twitter.util.Await
import io.finch.circe._
import io.circe.generic.auto._
import controller.Api._
import controller.ViewConversions.{encodeJsonError, encodeTime}

import scala.language.implicitConversions

object Main {

    def main(args: Array[String]): Unit  = {
        val endpoints = getAccount :+: getAccountTransactions :+: createAccount :+: createTransaction
        val server = Http.serve(":8081", endpoints.toService)

        Await.ready(server)
    }
}
