package edu.ndsu.cs.estimate.pages;

import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;

import java.util.ArrayList;
import java.util.List;

import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.SimpleByteSource;
import org.apache.tapestry5.alerts.AlertManager;
import org.apache.tapestry5.alerts.Duration;
import org.apache.tapestry5.alerts.Severity;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.SessionAttribute;
import org.apache.tapestry5.beanmodel.BeanModel;
import org.apache.tapestry5.beanmodel.services.BeanModelSource;
import org.apache.tapestry5.commons.Messages;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.tynamo.security.services.SecurityService;

import edu.ndsu.cs.estimate.cayenne.persistent.User;
import edu.ndsu.cs.estimate.entities.interfaces.RoleInterface;
import edu.ndsu.cs.estimate.entities.interfaces.UserAccount;
import edu.ndsu.cs.estimate.pages.Index;
import edu.ndsu.cs.estimate.services.database.interfaces.UserAccountDatabaseService;

public class CreateAccount
{
	@Inject
	private SecurityService securityService;
	
	@Inject
	private UserAccountDatabaseService userAccountDatabaseService;

    @Inject
	AlertManager alertManager;
    
	@Property
	@Persist
	private UserAccount userAccount; 

	@Property
	private List<? extends RoleInterface> roles;
	
	@Property
	private RoleInterface role;
	
	@Inject
	private Messages message;
	
	@Inject
	private BeanModelSource beanModelSource;
	
	@Property
	private BeanModel<UserAccount> userAccountBeanModel;
	
	@Property
	private String userName;
	
	@Property
	private String passWord;
    
	@InjectComponent
	private Form createAccountForm;
	
	//Property for username errors
	@Property
	private List<String> usernameExistsErrors = new ArrayList<>();
	
	//Property for password errors
	@Property
	private List<String> passwordErrors = new ArrayList<>();
	

    void setupRender() {
	   if (userAccount == null || userAccount.getPK() == null || userAccount.getPK() == -1) {
	        userAccount = userAccountDatabaseService.getNewUserAccount();
	    }
    }
	
    // Set password method that collects errors created from validation of password
    // The salt and hash are created here for security.
    void setPassword() {
    	passwordErrors = userAccount.validatePassword(passWord);
    	if(passwordErrors.isEmpty()) {
			//Create a salt and hash for the userAccount
			String salt = new SecureRandomNumberGenerator().nextBytes().toHex();
			userAccount.setPasswordSalt(salt);
			
			String hash = new Sha512Hash(passWord, salt).toHex(); 
			userAccount.setPasswordHash(hash);
//			
//			//Clear the plaintext password for security
//			passWord = null;
		}
    }
    
    void setUsername() {
    	//Query the database for user entries with the same username
    	boolean existingUsername = userAccountDatabaseService.isUsernameTaken(userName);
    	if(!existingUsername) {
    		//Needed to clear usernameErrors if multiple attempts fail.
    		usernameExistsErrors.remove("Username is taken.");
    		userAccount.setUserName(userName);
    	}
    	else {
    		usernameExistsErrors.add("Username is taken.");
    	}
    }
    
    void onValidateFromCreateAccountForm()
    {
		
    	//Call the setUsername method.
    	setUsername();
    	//Call the setPassword method.
		setPassword();
		List<String> errors = userAccount.validate();
		//Add errors found for username existence. Remove the 
		//"Name cannot be empty" error created in the validate method.
		//(Done because the interface doesn't currently have access to
		//the database/isExistingUsername method.)
		if(!usernameExistsErrors.isEmpty()) {
			errors.remove("Name cannot be empty.");
			errors.addAll(usernameExistsErrors);
		}
		errors.addAll(passwordErrors);
		
		//Record errors detected.
		for(String error : errors)
		{
			createAccountForm.recordError(error);
		}
		//Update the user account if there are no errors.
		if(!createAccountForm.getHasErrors())
		{
			userAccountDatabaseService.updateUserAccount(userAccount);
		}
		
	}

    Object onSuccessFromCreateAccountForm()
    {
    	if(!createAccountForm.getHasErrors()) {
            userAccountDatabaseService.updateUserAccount(userAccount);
            return performAutoLogin(userName, passWord, true);
        }
        return null;
	}
    
    private Object performAutoLogin(String username, String password, boolean rememberMe) {
        Subject currentUser = securityService.getSubject();
        if (currentUser == null) {
            throw new IllegalStateException("Subject can't be null");
        }
        
     // Retrieve the salt for the user and hash the password with it
        UserAccount account = userAccountDatabaseService.getUserAccount(username);
        if (account == null) {
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "User account not found.");
            return null;
        }

        String saltedHash = new Sha512Hash(password, new SimpleByteSource(account.getPasswordSalt())).toHex();
        UsernamePasswordToken token = new UsernamePasswordToken(username, saltedHash);
        token.setRememberMe(rememberMe);

        try {
            currentUser.login(token);
            alertManager.alert(Duration.SINGLE, Severity.SUCCESS, "Account created and logged in successfully.");
            return Index.class;
        } catch (AuthenticationException e) {
            alertManager.alert(Duration.SINGLE, Severity.ERROR, "Automatic login failed. Please try to log in manually. Hash: "
            		+ userAccount.getPasswordHash() + ", Salt: " + userAccount.getPasswordSalt());
            return null;
        }
    }
}
