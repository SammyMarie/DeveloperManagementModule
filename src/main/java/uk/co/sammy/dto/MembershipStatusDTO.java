package uk.co.sammy.dto;

public class MembershipStatusDTO {

    private double devDeductable;

    public double getDeductable() {

        return devDeductable;
    }

    public void setDeductable(double deductable) {

        this.devDeductable = deductable;
    }

    public MembershipStatusDTO membership(double deductable) {

        MembershipStatusDTO membershipDTO = new MembershipStatusDTO();

        membershipDTO.setDeductable(deductable);

        return membershipDTO;
    }
}
