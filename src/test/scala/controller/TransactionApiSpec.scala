package controller

import com.twitter.util.{MockTimer, Time}
import db.Database
import domain.{AccountNotFoundException, Currency, Money, TransactionView}
import external.RateProvider
import io.finch.{Input, Text}
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}
import service.{AccountNumberGenerator, AccountService}

class TransactionApiSpec extends FlatSpec with Matchers with BeforeAndAfterEach {

    private val from = "0000000001"
    private val to = "0000000002"
    private val amount = 10

    private val createTransactionRequest = s"""{"from": "${from}", "to": "${to}", "amount": ${amount}}"""
    private val createNegativeTransactionRequest = s"""{"from": "${from}", "to": "${to}", "amount": -${amount}}"""

    override def afterEach() {
        Database.clearState()
        AccountNumberGenerator.clear()
    }

    behavior of "the transaction endpoint"

    it should "fail to create transaction when account is missing" in {
        assertThrows[AccountNotFoundException] {
            val firstAccount = AccountService.create("John Doe", Some(20000), "RUB")
            Api.createTransaction(Input.post("/transaction")
                .withBody[Text.Plain].apply(createTransactionRequest))
                .awaitValueUnsafe()
        }
    }

    it should "create transaction when arguments are correct" in {
        val mockTimer = new MockTimer()
        Time.withCurrentTimeFrozen { ctrl =>
            mockTimer.tick()
            val transaction: TransactionView = TransactionView(from, to, amount, "RUB", Time.now)
            val firstAccount = AccountService.create("John Doe", Some(20000), "RUB")
            val secondAccount = AccountService.create("Jane Doe", Some(20000), "USD")
            Api.createTransaction(Input.post("/transaction")
                .withBody[Text.Plain].apply(createTransactionRequest))
                .awaitValueUnsafe() shouldBe Some(transaction)
        }
    }

    it should "create transaction and convert amount to currency of receiving account" in {
        val mockTimer = new MockTimer()

        val rate = RateProvider.getRateOn(Currency("RUB"), Currency("USD"), Time.now)
        val convertedAmount = rate * amount
        val accountStartValue = Some(BigDecimal(20000))
        val firstAccount = AccountService.create("John Doe", Some(20000), "RUB")
        val secondAccount = AccountService.create("Jane Doe", Some(20000), "USD")
        Api.createTransaction(Input.post("/transaction")
            .withBody[Text.Plain].apply(createTransactionRequest))
            .awaitValueUnsafe()

        val receiver = AccountService.get(to).get
        assertResult(Money.scale(accountStartValue.get + convertedAmount), "Wrong value after conversion")(receiver.balance)
    }

    it should "fail to create transaction when amount is negative" in {
        assertThrows[IllegalArgumentException] {
            val firstAccount = AccountService.create("John Doe", Some(20000), "RUB")
            val secondAccount = AccountService.create("Jane Doe", Some(20000), "USD")
            Api.createTransaction(Input.post("/transaction")
                .withBody[Text.Plain].apply(createNegativeTransactionRequest))
                .awaitValueUnsafe()
        }
    }
}
