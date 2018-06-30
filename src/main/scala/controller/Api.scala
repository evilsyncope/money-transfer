package controller

import java.time.Instant

import com.twitter.util.Time
import com.typesafe.scalalogging.LazyLogging
import domain._
import io.circe.generic.auto._
import io.finch.circe._
import service.{AccountService, TransactionService}
import controller.ViewConversions._
import org.apache.commons.lang3.StringUtils

object Api extends LazyLogging {
    import io.finch._, io.finch.syntax._

    val getAccount: Endpoint[AccountView] = get("account" :: path[String]) {
        number: String =>
            logger.debug(s"Fetching account for number ${number}")
            AccountService.get(number) match {
                case Some(acc) =>
                    logger.debug(s"Fetched account for number ${number}")
                    Ok[AccountView](acc)
                case None => NotFound(new AccountNotFoundException(number))
            }
    }

    val getAccountTransactions: Endpoint[List[TransactionView]] = get("account" :: path[String] :: "transactions") {
        number: String =>
            logger.debug(s"Fetching transactions for account number $number")
            val transactions = TransactionService.getAllForAccount(number)
            logger.debug(s"Fetched transactions for account number $number")
            Ok[List[TransactionView]](transactions)
    }


    val createAccount: Endpoint[AccountView] = post("account" :: jsonBody[AccountCreateRequest]) {
        request: AccountCreateRequest =>
            validate(request)
            logger.info(s"Creating account for owner: ${request.owner}, balance: ${request.balance}" +
                s", currency: ${request.currency}")
            val account = AccountService.create(request.owner, request.balance, request.currency)
            logger.debug(s"Created account for owner: ${account.owner}")
            Ok[AccountView](account)
    } handle {
        case e: Exception => logger.error("Error on account creation", e); BadRequest(e)
    }

    val createTransaction: Endpoint[TransactionView] = post("transaction" :: jsonBody[TransactionRequest]) {
        request: TransactionRequest =>
            validate(request)
            logger.info(s"Transferring ${request.amount} from account=${request.from} to account=${request.to}")
            val transaction = AccountService.transfer(
                request.from,
                request.to,
                request.amount,
                Time.now)
            logger.info(s"Successfully transferred ${transaction.amount} ${transaction.currency.code} " +
                s"from account=${transaction.from} to account=${transaction.to}")
            Ok[TransactionView](transaction)
    } handle {
        case e: Exception => logger.error("Error on transfer", e); BadRequest(e)
    }

    private def validate(request: AccountCreateRequest) = {
        if (StringUtils.isBlank(request.owner))
            throw new IllegalArgumentException("'Owner' should not be blank")
        else if (request.balance.isDefined && request.balance.get < 0)
            throw new IllegalArgumentException("Initial balance should be positive")

        request.currency.toLowerCase match {
            case "rub" | "usd" => ()
            case _ => throw new IllegalArgumentException("Only 'RUB' and 'USD' currency codes are supported")
        }
    }

    private def validate(request: TransactionRequest) =
        if (StringUtils.isBlank(request.from))
            throw new IllegalArgumentException("'From' should not be blank")
        else if (StringUtils.isBlank(request.to))
            throw new IllegalArgumentException("'To' should not be blank")
        else if (request.amount < 0)
            throw new IllegalArgumentException("'Amount' should be positive")
}
