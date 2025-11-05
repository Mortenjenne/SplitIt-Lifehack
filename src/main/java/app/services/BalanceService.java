package app.services;

import app.dto.UserBalanceDTO;
import app.entities.Settlement;
import app.exceptions.DatabaseException;

import java.util.List;

public interface BalanceService {
        public List<UserBalanceDTO>  getGroupBalances(int groupId) throws DatabaseException;
        public List<Settlement> getSettlements(int groupId) throws DatabaseException;
    }


