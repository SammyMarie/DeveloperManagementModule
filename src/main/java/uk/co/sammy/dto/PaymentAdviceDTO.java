package uk.co.sammy.dto;

public class PaymentAdviceDTO {

    private final double amount;
    private final String targetPayPalID;
    private final String desc;

    public PaymentAdviceDTO(double amount, String targetPayPalID, String desc) {

        this.amount = amount;
        this.targetPayPalID = targetPayPalID;
        this.desc = desc;
    }

    public double getAmount() {

        return amount;
    }

    public String getTargetPayPalID() {

        return targetPayPalID;
    }

    public String getDesc() {

        return desc;
    }


}
