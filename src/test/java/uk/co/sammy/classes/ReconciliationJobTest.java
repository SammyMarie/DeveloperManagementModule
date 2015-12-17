package uk.co.sammy.classes;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.Times;
import org.mockito.runners.MockitoJUnitRunner;
import uk.co.sammy.dao.FinancialTransactionDAO;
import uk.co.sammy.dao.MembershipDAO;
import uk.co.sammy.dto.MembershipStatusDTO;
import uk.co.sammy.dto.PaymentAdviceDTO;
import uk.co.sammy.dto.TransactionDTO;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class ReconciliationJobTest {

    @Mock
    PayPalFacade paypal;

    @Mock
    FinancialTransactionDAO finance;

    @Mock
    MembershipDAO membersDAO;

    ReconciliationJob job;

    TransactionDTO transactDTO;

    MembershipStatusDTO membershipStats;

    List<TransactionDTO> singleTXs;

    List<TransactionDTO> multipleTXs;

    @Before
    public void setUp() throws Exception {

        MockitoAnnotations.initMocks(this);
        job = new ReconciliationJob(finance, membersDAO, paypal);
        singleTXs = new ArrayList<TransactionDTO>();
        multipleTXs = new ArrayList<TransactionDTO>();
        membershipStats = new MembershipStatusDTO();
        transactDTO = new TransactionDTO();

        membershipStats.setDeductable(.30);

        when(membersDAO.getStatusFor(anyString())).thenReturn(membershipStats);
    }

    @Test
    public void testWithoutA_Transaction() throws Exception {

        assertEquals(0, job.reconcile());
    }

    @Test
    public void testWithA_Transaction() throws Exception {

        singleTXs.add(new TransactionDTO());
        when(finance.retrieveUnsettledTransactions()).thenReturn(singleTXs);
        assertEquals(1, job.reconcile());
    }

    @Test
    public void testWithMany_Transactions() throws Exception {

        singleTXs.add(new TransactionDTO());
        singleTXs.add(new TransactionDTO());
        singleTXs.add(new TransactionDTO());
        when(finance.retrieveUnsettledTransactions()).thenReturn(singleTXs);
        assertEquals(3, job.reconcile());
    }

    @Test
    public void retrieve_developer_details_when_transaction_exists() throws Exception {

        transactDTO.setDeveloperID("DEV001");
        singleTXs.add(transactDTO);
        when(finance.retrieveUnsettledTransactions()).thenReturn(singleTXs);

        assertEquals(1, job.reconcile());
        verify(membersDAO).getStatusFor(anyString());
    }

    @Test
    public void retrieve_member_details_for_existing_Transactions() throws Exception {

        TransactionDTO johnsTransaction = new TransactionDTO();
        String johnsDeveloperID = "john001";

        TransactionDTO bobsTransaction = new TransactionDTO();
        String bobsDeveloperID = "bob999";

        johnsTransaction.setDeveloperID(johnsDeveloperID);
        bobsTransaction.setDeveloperID(bobsDeveloperID);

        multipleTXs.add(johnsTransaction);
        multipleTXs.add(bobsTransaction);

        when(finance.retrieveUnsettledTransactions()).thenReturn(multipleTXs);
        assertEquals(2, job.reconcile());

        ArgumentCaptor<String> argCaptor = ArgumentCaptor.forClass(String.class);
        verify(membersDAO, new Times(2)).getStatusFor(argCaptor.capture());

        List<String> passedValues = argCaptor.getAllValues();

        assertEquals(johnsDeveloperID, passedValues.get(0));
        assertEquals(bobsDeveloperID, passedValues.get(1));
    }

    @Test
    public void send_payble_to_paypal() throws Exception {

        String davidsDeveloperID = "dev001";
        String davidsPayPalID = "david@paypal.co.uk";
        double davidsSuperMarioGamePrice = 100.00;

        singleTXs.add(transactDTO.createTxDTO(davidsDeveloperID, davidsPayPalID, davidsSuperMarioGamePrice));

        when(finance.retrieveUnsettledTransactions()).thenReturn(singleTXs);
        assertEquals(1, job.reconcile());
        verify(paypal).sendAdvice(isA(PaymentAdviceDTO.class));
    }

    @Test
    public void calculates_payable() throws Exception {

        String ronaldosDeveloperID = "ronaldo007";
        String ronaldosPayPalID = "Ronald@realmadrid.com";
        double ronaldosSoccerFee = 100.00;

        singleTXs.add(transactDTO.createTxDTO(ronaldosDeveloperID, ronaldosPayPalID, ronaldosSoccerFee));

        when(finance.retrieveUnsettledTransactions()).thenReturn(singleTXs);
        assertEquals(1, job.reconcile());

        ArgumentCaptor<PaymentAdviceDTO> calculatedAdvice = ArgumentCaptor.forClass(PaymentAdviceDTO.class);
        verify(paypal).sendAdvice(calculatedAdvice.capture());

        assertTrue(70.00 == calculatedAdvice.getValue().getAmount());
    }

    @Test
    public void calculates_payable_with_multiple_Transactions() throws Exception {

        String johnsDeveloperID = "john001";
        String johnsPayPalID = "john@gmail.com";
        double johnsGameFee = 200.00;

        multipleTXs.add(transactDTO.createTxDTO(johnsDeveloperID, johnsPayPalID, johnsGameFee));

        String davesDeveloperID = "dave888";
        String davesPayPalID = "iamdave009@yahoo.co.uk";
        double davesGameFee = 150.00;

        multipleTXs.add(transactDTO.createTxDTO(davesDeveloperID, davesPayPalID, davesGameFee));

        when(finance.retrieveUnsettledTransactions()).thenReturn(multipleTXs);

        when(membersDAO.getStatusFor(eq(johnsDeveloperID))).thenReturn(membershipStats.membership(.15));
        when(membersDAO.getStatusFor(eq(davesDeveloperID))).thenReturn(membershipStats.membership(.10));

        assertEquals(2, job.reconcile());

        ArgumentCaptor<PaymentAdviceDTO> calculatedAdvice = ArgumentCaptor.forClass(PaymentAdviceDTO.class);
        verify(paypal, new Times(2)).sendAdvice(calculatedAdvice.capture());

        assertTrue(170.00 == calculatedAdvice.getAllValues().get(0).getAmount());
        assertTrue(135.00 == calculatedAdvice.getAllValues().get(1).getAmount());
    }

    @Test
    public void calculate_payable_with_multiple_transactions_same_developer() throws Exception {

        String janetsDeveloperID = "janet12567";
        String janetsPayPalID = "janetthejunitguru@gmail.com";
        double fishPondGameFee = 200.00;
        double ticTacToeGameFee = 100.00;

        multipleTXs.add(transactDTO.createTxDTO(janetsDeveloperID, janetsPayPalID, fishPondGameFee));
        multipleTXs.add(transactDTO.createTxDTO(janetsDeveloperID, janetsPayPalID, ticTacToeGameFee));

        when(finance.retrieveUnsettledTransactions()).thenReturn(multipleTXs);

        assertEquals(2, job.reconcile());

        ArgumentCaptor<PaymentAdviceDTO> calculatedAdvice = ArgumentCaptor.forClass(PaymentAdviceDTO.class);
        verify(paypal, new Times(1)).sendAdvice(calculatedAdvice.capture());

        assertTrue(210.00 == calculatedAdvice.getValue().getAmount());
    }
}
