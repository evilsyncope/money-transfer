package service

import com.twitter.util.Time
import domain.{Currency, Money}
import external.RateProvider

object MoneyConverter {

    /**
      * @return tuple of converted sum and conversion rate
      */
    def convert(sum: BigDecimal, from: Currency, to: Currency, onDate: Time) = {
        val rate = RateProvider.getRateOn(from, to, onDate)
        (sum * rate, rate)
    }

}
