package service

import db.Database
import domain.{Transaction}

object TransactionService {

    def getAllForAccount(accountNumber: String): List[Transaction] =
        Database.selectTransactions(accountNumber)
}
