package com.mobility.mobility_backend.dto.finance;

import java.math.BigDecimal;

public class OutstandingPointDTO {

    private String period;
    private long count;
    private BigDecimal amount;

    public OutstandingPointDTO() {
    }

    public OutstandingPointDTO(String period, long count, BigDecimal amount) {
        this.period = period;
        this.count = count;
        this.amount = amount;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
