package uk.co.sammy.dao;

import uk.co.sammy.dto.TransactionDTO;

import java.util.List;


public interface FinancialTransactionDAO {

    List<TransactionDTO> retrieveUnsettledTransactions();
}
