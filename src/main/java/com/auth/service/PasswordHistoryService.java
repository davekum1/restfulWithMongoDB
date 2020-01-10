package com.auth.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.auth.repository.PasswordHistoryRepository;
import com.auth.entity.PasswordHistory;
import com.auth.entity.User;


@Service
@Transactional
public class PasswordHistoryService {
	 	@Autowired
	    private PasswordHistoryRepository passwordHistoryRepository;

	    public Page<PasswordHistory> findAllByUuid(Pageable pageable, UUID uuid) {
	        return this.passwordHistoryRepository.findAllByUuid(pageable, uuid);
	    }

	    /**
	     * Save a new entry in the PasswordHistory table.
	     *
	     * @param user The User this new entry will be associated with.
	     */
	    public PasswordHistory createNewPasswordHistory(User user) {
	        PasswordHistory passwordHistory = new PasswordHistory();
	        passwordHistory.setDateAdded(LocalDateTime.now());
	        passwordHistory.setPasswordHash(user.getPasswordHash());
	        passwordHistory.setUuid(user.getUuid());

	        return this.passwordHistoryRepository.save(passwordHistory);
	    }

	    /**
	     * Delete the old password history entries for a User.
	     *
	     * @param user The User's whose PasswordHistory will be deleted
	     * @param passwordHistoryReuse The application configured value for passwordRequirements.passwordHistoryReuse
	     */
	    public void deleteUserPasswordHistory(User user, int passwordHistoryReuse) {
	        // Set up the max # of results to return (limit it to 10,000) at a time
	        PageRequest pageRequest = new PageRequest(0, PasswordHistory.MAX_DELETIONS_AT_ONCE, Sort.Direction.DESC, "dateAdded");
	        // Ensure the new password meets all the validation rules
	        Page<PasswordHistory> pageOne = this.passwordHistoryRepository.findAllByUuid(pageRequest, user.getUuid());
	        List<PasswordHistory> passwordHistories = pageOne.getContent();

	        List<PasswordHistory> objectsToDelete = new ArrayList<PasswordHistory>();
	        for (int i = passwordHistoryReuse; i < passwordHistories.size(); i++) {
	            objectsToDelete.add(passwordHistories.get(i));
	        }
	        if (objectsToDelete.size() > 0) {
	            this.passwordHistoryRepository.deleteInBatch(objectsToDelete);
	        }
	    }
}
