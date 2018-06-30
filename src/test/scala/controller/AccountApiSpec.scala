package controller

import domain.{AccountNotFoundException, AccountView, Money}
import io.finch.{Input, Text}
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}
import service.{AccountNumberGenerator, AccountService}
import ViewConversions._
import db.Database
import io.finch.circe._

class AccountApiSpec extends FlatSpec with Matchers with BeforeAndAfterEach {

    private val owner = "John Doe"
    private val balance = Money.scaleView(BigDecimal(100))
    private val negativeBalance = Money.scaleView(BigDecimal(-100))
    private val currency = "RUB"
    private val number = "0000000001"

    private val createAccountRequest = s"""{"owner": "${owner}", "balance": ${balance}, "currency": "${currency}"}"""
    private val wrongAccountRequest = s"""{"owner": "${owner}", "balance": ${negativeBalance}, "currency": "${currency}"}"""

    override def afterEach() {
        Database.clearState()
        AccountNumberGenerator.clear()
    }

    behavior of "the account endpoint"

    it should "get 'Account not found' when it not exists" in {
        assertThrows[AccountNotFoundException] {
            Api.getAccount(Input.get(s"/account/${number}")).awaitValueUnsafe()
        }
    }

    it should "return account view if it's created" in {
        val accountView: AccountView = AccountService.create("John Doe", Some(20000), "RUB")
        Api.getAccount(Input.get(s"/account/${number}")).awaitValueUnsafe() shouldBe Some(accountView)
    }

    it should "create account with valid params" in {
        val accountView = AccountView(number, owner, balance, currency)
        Api.createAccount(Input.post("/account")
            .withBody[Text.Plain].apply(createAccountRequest))
            .awaitValueUnsafe() shouldBe Some(accountView)
    }

    it should "fail to create account with wrong params" in {
        assertThrows[IllegalArgumentException] {
            Api.createAccount(Input.post("/account")
                .withBody[Text.Plain].apply(wrongAccountRequest))
                .awaitValueUnsafe()
        }
    }
}
