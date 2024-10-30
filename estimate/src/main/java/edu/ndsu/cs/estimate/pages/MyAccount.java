package edu.ndsu.cs.estimate.pages;

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

    void setupRender() {
        String principal = securityService.getSubject().getPrincipal().toString();
        userAccount = userAccountDatabaseService.getUserAccount(principal);
    }

    public BeanModel<UserAccount> getUserAccountBeanModel() {
        return userAccount.getBeanModel(beanModelSource, messages);
    }

    void onValidateFromPasswordForm() {
        if (!newPassword.equals(confirmNewPassword)) {
            passwordForm.recordError("New password and confirm password do not match.");
            return;
        }

        boolean passwordChanged = userAccount.changePassword(oldPassword, newPassword, userAccount.getUserID().toString());
        if (!passwordChanged) {
            passwordForm.recordError("Incorrect old password.");
        }
    }

    void onSuccessFromPasswordForm() {
        userAccountDatabaseService.updateUserAccount(userAccount);
        alertManager.info("Password successfully changed.");
    }
}
