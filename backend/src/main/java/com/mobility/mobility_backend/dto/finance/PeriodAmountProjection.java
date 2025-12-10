package com.mobility.mobility_backend.dto.finance;

import java.math.BigDecimal;

public interface PeriodAmountProjection {
    String getPeriod();
    Long getCount();
    BigDecimal getAmount();
}
