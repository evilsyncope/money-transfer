package service

import java.time.Instant

import com.twitter.util.Time
import db.Database
import domain._

import scala.concurrent.stm._

object AccountService {

    def create(owner: String, initialBalance: Option[BigDecimal], currency: String): Account = {
        val accountNumber = AccountNumberGenerator.next()
        val newAccount =
            Account(
                accountNumber,
                owner,
                Money.scale(initialBalance.getOrElse(0)),
                Currency(currency))

        Database.insert(newAccount)
    }

    def get(number: String): Option[Account] =
        Database.selectAccount(number)

    def transfer(from: String, to: String, amount: BigDecimal, timestamp: Time): Transaction = {
        atomic { implicit tx =>
            Database.selectAccount(from) match {
                case None => throw new AccountNotFoundException(from)
                case Some(fromAcc) =>
                    if (notSufficientFunds(fromAcc, amount)) throw new InsufficientFundsException(from)
                    else Database.selectAccount(to) match {
                        case None => throw new AccountNotFoundException(to)
                        case Some(toAcc) =>
                            val amountScaled = Money.scale(amount)
                            val (convertedAmount, rate) = MoneyConverter.convert(amountScaled, fromAcc.currency, toAcc.currency, timestamp)

                            Database.update(from, fromAcc.copy(balance = Money.scale(fromAcc.balance - amountScaled)))
                            Database.update(to, toAcc.copy(balance = Money.scale(toAcc.balance + convertedAmount)))
                            Database.insert(Transaction(from, to, amountScaled, fromAcc.currency, rate, timestamp))
                }
            }
        }
    }

    private def notSufficientFunds(account: Account, toTransfer: BigDecimal): Boolean =
        Money.scale(account.balance).compare(Money.scale(toTransfer)) < 0
}
