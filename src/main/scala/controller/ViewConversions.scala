package controller

import com.twitter.util.Time
import domain._
import io.circe.{Encoder, Json}

import scala.language.implicitConversions

object ViewConversions {

    implicit val encodeJsonError: Encoder[Exception] = Encoder.instance(e =>
        Json.obj("error" -> Option(e.getMessage).fold(Json.Null)(Json.fromString)))

    implicit def account2View(x: Account): AccountView =
        AccountView(x.number, x.owner, Money.scaleView(x.balance), x.currency.code)

    implicit def transaction2View(x: Transaction): TransactionView =
        TransactionView(x.from, x.to, Money.scaleView(x.amount), x.currency.code, x.timestamp)

    implicit def transactionList2View(xs: List[Transaction]): List[TransactionView] =
        xs.map(x => TransactionView(x.from, x.to, Money.scaleView(x.amount), x.currency.code, x.timestamp))

    implicit final val encodeTime: Encoder[Time] = Encoder.instance(time => Json.fromString(time.toString))
}
