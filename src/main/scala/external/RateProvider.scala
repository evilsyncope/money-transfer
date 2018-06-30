package external

import com.twitter.util.Time
import domain.Currency

/**
  * Fake implementation of external conversion rate provider.
  */
object RateProvider {

    def getRateOn(from: Currency, to: Currency, date: Time): BigDecimal =
        (from.code.toLowerCase, to.code.toLowerCase) match {
            case ("rub", "usd") => BigDecimal(1) / BigDecimal(60)
            case ("usd", "rub") => BigDecimal(60)
            case (_, _) => BigDecimal(1)
        }
}
