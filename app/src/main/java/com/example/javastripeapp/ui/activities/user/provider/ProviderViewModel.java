package com.example.javastripeapp.ui.activities.user.provider;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.javastripeapp.data.models.address.Address;
import com.example.javastripeapp.data.models.user.AccountType;
import com.example.javastripeapp.data.models.user.User;
import com.example.javastripeapp.data.repos.StripeProviderRepo;
import com.example.javastripeapp.ui.activities.user.common.BaseProfileViewModel;
import com.example.javastripeapp.utils.TaskUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.List;

public class ProviderViewModel extends BaseProfileViewModel {
    private static final String TAG = "ProviderViewModel";
    private final StripeProviderRepo providerRepo = new StripeProviderRepo();
    private final MutableLiveData<OnboardingStatus> _onboardingStatus = new MutableLiveData<>();
    public LiveData<OnboardingStatus> onboardingStatus = _onboardingStatus;
    private User currentUser;

    @Override
    public Task<User> retrieveUser() {
        return super.retrieveUser().continueWithTask(task -> {
            if (!task.isSuccessful()) {
                return TaskUtils.forTaskException(task, "Failed to retrieve current user");
            }
            currentUser = task.getResult();
            return Tasks.forResult(currentUser);
        });
    }

    @Override
    public Task<List<Address>> fetchUserAddresses(String userId) {
        return super.fetchUserAddresses(userId);
    }

    @Override
    public void signOutUser() {
        super.signOutUser();
    }

    public Task<OnboardingStatus> checkOnboardingStatus() {
        if (!currentUser.getAccountType().equals(AccountType.PROVIDER.toString())) {
            return TaskUtils.forIllegalStateException("User account type is not PROVIDER");
        }
        if (currentUser.getStripeAccountId() == null || currentUser.getStripeAccountId().isEmpty()) {
            return Tasks.forResult(OnboardingStatus.NOT_STARTED);
        }
        return providerRepo.isAccountFullyOnboarded(currentUser.getStripeAccountId()).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                String errorMsg = TaskUtils.extractErrorMessage(task, "Failed to retrieve account status");
                Log.e(TAG, errorMsg);
                return Tasks.forResult(OnboardingStatus.ERROR);
            }
            boolean isFullyOnboarded = task.getResult();

            OnboardingStatus status = isFullyOnboarded ? OnboardingStatus.FULLY_ONBOARDED : OnboardingStatus.ACCOUNT_CREATED;
            return Tasks.forResult(status);
        });
    }

    public Task<Void> createConnectAccount() {
        if (currentUser == null) {
            return TaskUtils.forIllegalStateException("Current user is null");
        }
        return fetchUserAddresses(currentUser.getUserId()).continueWithTask(task -> {
            if (!task.isSuccessful()) {
                return TaskUtils.forTaskException(task, "Failed to retrieve user addresses");
            }
            List<Address> addressList = task.getResult();
            Address address = null;

            for (int i = 0; i < addressList.size(); i++) {
                int index = (int) (Math.random() * addressList.size());
                
                address = addressList.get(index);
            }
            if (address == null) {
                address = addressList.get(0);
            }
            return providerRepo.createStripeConnectAccount(currentUser, address);
        });
    }

    public Task<String> startOnboarding() {
        if (currentUser.getStripeAccountId() == null) {
            return TaskUtils.forIllegalStateException("Stripe account ID is missing");
        }
        return providerRepo.createAccountLink(currentUser.getStripeAccountId());
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void updateOnboardingStatus(OnboardingStatus status) {
        _onboardingStatus.postValue(status);
    }
}
