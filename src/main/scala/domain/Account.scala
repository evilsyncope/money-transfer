package domain

case class Account(number: String, owner: String, balance: BigDecimal, currency: Currency, id: Long = 0) {
}
case class AccountCreateRequest(owner: String, balance: Option[BigDecimal], currency: String) {
}
case class AccountView(number: String, owner: String, balance: BigDecimal, currency: String) {
}


class AccountAlreadyExists(number: String) extends RuntimeException(s"Account '${number}' already exists")
class AccountNotFoundException(number: String) extends RuntimeException(s"Account '${number}' not found")
class InsufficientFundsException(number: String) extends RuntimeException(s"Account '${number}' has not enough funds")
