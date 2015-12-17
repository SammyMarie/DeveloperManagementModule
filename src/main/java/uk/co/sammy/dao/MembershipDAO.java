package uk.co.sammy.dao;

import uk.co.sammy.dto.MembershipStatusDTO;

public interface MembershipDAO {

    MembershipStatusDTO getStatusFor(String id);
}
