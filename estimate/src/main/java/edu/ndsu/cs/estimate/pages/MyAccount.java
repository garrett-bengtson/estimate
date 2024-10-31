package edu.ndsu.cs.estimate.pages;

import java.util.ArrayList;
import java.util.List;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.beanmodel.BeanModel;
import org.apache.tapestry5.beanmodel.services.BeanModelSource;
import org.apache.tapestry5.commons.Messages;
import org.tynamo.security.services.SecurityService;
import edu.ndsu.cs.estimate.entities.interfaces.UserAccount;
import edu.ndsu.cs.estimate.services.database.interfaces.UserAccountDatabaseService;
import org.apache.tapestry5.corelib.components.BeanDisplay;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.PasswordField;
import org.apache.tapestry5.alerts.AlertManager;

@RequiresAuthentication
public class MyAccount {
    @Inject
    private SecurityService securityService;

    @Inject
    private UserAccountDatabaseService userAccountDatabaseService;

    @Inject
    private BeanModelSource beanModelSource;

    @Inject
    private AlertManager alertManager;

    @Inject
    private Messages messages;

    @Property
    @Persist
    private UserAccount userAccount;

    @InjectComponent
    private Form passwordForm;

    @Property
    private String oldPassword;

    @Property
    private String newPassword;

    @Property
    private String confirmNewPassword;
    
    //Property for password errors
  	@Property
  	private List<String> passwordErrors = new ArrayList<>();


    void setupRender() {
        String principal = securityService.getSubject().getPrincipal().toString();
        userAccount = userAccountDatabaseService.getUserAccount(principal);
    }

    public BeanModel<UserAccount> getUserAccountBeanModel() {
        return userAccount.getBeanModel(beanModelSource, messages);
    }

    void onValidateFromPasswordForm() {
    	//debug statement
    	//System.out.println("method called");
    	//Return if there is no input for the old password.
    	if(oldPassword == null) {
    		passwordForm.recordError("Please enter your old password.");
    		return;
    	}
    	
    	//Return if no password is inputted.
    	if(newPassword == null) {
    		passwordForm.recordError("Your new password can't be empty.");
    		return;
    	}
    	//Return if the new password and confirmation don't match.
    	else if (!newPassword.equals(confirmNewPassword)) {
            passwordForm.recordError("New password and confirm password do not match.");
            return;
        }
        
        passwordErrors = userAccount.validatePassword(newPassword);
        
        for (String error : passwordErrors) {
            passwordForm.recordError(error);
        }
        
        if (passwordErrors.isEmpty()) {
            boolean passwordChanged = userAccount.changePassword(oldPassword, newPassword, confirmNewPassword);
            if (!passwordChanged) {
                passwordForm.recordError("Incorrect old password.");
            }
        }
    }

    void onSuccessFromPasswordForm() {
        userAccountDatabaseService.updateUserAccount(userAccount);
        alertManager.info("Password successfully changed.");
    }
}
