package domain

object Money {
    def scale(v: BigDecimal) =
        v.setScale(4, BigDecimal.RoundingMode.HALF_EVEN)

    def scaleView(v: BigDecimal) =
        v.setScale(2, BigDecimal.RoundingMode.HALF_UP)
}
