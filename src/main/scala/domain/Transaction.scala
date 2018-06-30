package domain

import com.twitter.util.Time

case class Transaction(from: String, to: String, amount: BigDecimal, currency: Currency, conversionRate: BigDecimal,
                       timestamp: Time, id: Long = 0) {
}
case class TransactionView(from: String, to: String, amount: BigDecimal, currency: String, timestamp: Time) {
}
case class TransactionRequest(from: String, to: String, amount: BigDecimal) {
}
