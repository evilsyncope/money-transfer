# money-transfer
Simple implementation of money transfering system using Scala, Finch and STM.

To build fat jar: `sbt assembly`.

To run it: `java -jar target/scala-2.12/MoneyTransfer-assembly-1.0.jar`

Api server starts on port `8081`. 
Endpoints are: 
* GET [localhost:8081/account/0000000001] - fetch account info
* GET [localhost:8081/account/0000000001/transactions] - fetch account transactions list
* POST [localhost:8081/account] - create account. Request body is JSON like `{"owner": "John Doe", "balance": 10000, "currency": "RUB"}`
* POST [localhost:8081/transaction] - transfer funds between accounts. Request body is JSON like `{"from": "0000000002", "to": "0000000001", "amount": 10.01}`
