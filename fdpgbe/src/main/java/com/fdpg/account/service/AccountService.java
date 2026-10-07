package com.fdpg.account.service;

import com.fdpg.account.dto.AccountResponse;
import com.fdpg.auth.entity.User;
import com.fdpg.common.response.ApiResponse;

public interface AccountService {

    /**
     * Creates a new account for the given user with ₹5000 initial balance.
     * Idempotent — if account already exists, returns existing.
     */
    AccountResponse createAccountForUser(User user);

    /**
     * Returns the account for the authenticated user.
     */
    ApiResponse<AccountResponse> getMyAccount(String username);

    /**
     * Ensures all existing users without accounts get one.
     * Called on application startup.
     */
    void ensureAccountsForAllUsers();
}
