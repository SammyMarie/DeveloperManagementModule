package uk.co.sammy.classes;

import uk.co.sammy.dto.PaymentAdviceDTO;

public interface PayPalFacade {

    void sendAdvice(PaymentAdviceDTO a);
}
