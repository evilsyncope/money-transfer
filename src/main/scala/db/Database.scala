package db

import java.util.concurrent.atomic.AtomicLong

import scala.concurrent.stm._
import domain.{Account, AccountAlreadyExists, AccountNotFoundException, Transaction}

import scala.concurrent.stm.TMap

/**
 * Simple implementation of transactional in-memory storage based on STM.
 */
object Database {

    private val accountStorage = TMap.empty[String, Account]
    private val transactionIdx = TMap.empty[String, List[Transaction]]
    private val accountIdSequence: AtomicLong = new AtomicLong()
    private val txIdSequence: AtomicLong = new AtomicLong()

    def selectAccount(accountNumber: String): Option[Account] = atomic { implicit txn =>
        accountStorage.get(accountNumber)
    }

    def selectTransactions(accountNumber: String): List[Transaction] = atomic { implicit txn =>
            transactionIdx.get(accountNumber) match {
                case Some(txs) => txs
                case None => List()
            }
    }

    def insert(account: Account): Account = atomic { implicit txn =>
        accountStorage.put(account.number, account.copy(id = accountIdSequence.incrementAndGet())) match {
            case Some(_) => throw new AccountAlreadyExists(account.number)
            case None => account
        }
    }

    def insert(transaction: Transaction): Transaction = {
        val persisted = transaction.copy(id = txIdSequence.incrementAndGet())
        atomic { implicit txn =>

            transactionIdx += persisted.from -> (persisted :: transactionIdx.getOrElse(persisted.from, List()))
            transactionIdx += persisted.to -> (persisted :: transactionIdx.getOrElse(persisted.to, List()))

            persisted
        }
    }

    def update(accountNumber: String, account: Account): Unit = atomic { implicit txn =>
        if (accountStorage.put(accountNumber, account).isEmpty)
            throw new AccountNotFoundException(accountNumber)
    }

    /**
      *
      * Clears everything. Only for tests.
      */
    def clearState() = {
        atomic { implicit txn =>
            for {v <- accountStorage} accountStorage -= v._1
            for {v <- transactionIdx} transactionIdx -= v._1
            accountIdSequence.set(0)
            txIdSequence.set(0)
        }
    }
}
