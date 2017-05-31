/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.nackademin.librarytest.pages;

import static com.codeborne.selenide.Selenide.$$;
import com.codeborne.selenide.SelenideElement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.support.FindBy;

/**
 *
 * @author testautomatisering
 */
public class MyProfilePage extends MenuPage{
    private static final Logger LOG =  Logger.getLogger(MyProfilePage.class.getName());
    @FindBy(css = "#gwt-uid-5")
    private SelenideElement userName;
    @FindBy(css = "#gwt-uid-7")
    private SelenideElement userFirstName;
    @FindBy(css = "#gwt-uid-9")
    private SelenideElement userLastName;
    @FindBy(css = "#gwt-uid-11")
    private SelenideElement userPhone;
    @FindBy(css= "#gwt-uid-13")
    private SelenideElement userEmail;
    @FindBy(css = "#edit-user")
    private SelenideElement editUserButton;
    @FindBy(css = "#delete-user-button")
    private SelenideElement deleteUserButton;
    @FindBy(css = "#confirmdialog-ok-button")
    private SelenideElement okButton;
    @FindBy(css = "td.v-grid-cell:nth-child(1) > a:nth-child(1)")
    private SelenideElement firstBorrowedBookTitle;
    
    
    public String getUserName(){
        LOG.log(Level.INFO, "Getting the profile's username");
        return userName.getText();
    }
    public String getFirstName(){
        LOG.log(Level.INFO, "Getting the profile's first name");
        return userFirstName.getText();
    }
    public String getLastName(){
        LOG.log(Level.INFO, "Getting the profile's last name");
        return userLastName.getText();
    }
    public String getUserPhone(){
        LOG.log(Level.INFO, "Getting the profile's phone number");
        return userPhone.getText();
    }
    
    public String getUserEmail(){
        LOG.log(Level.INFO, "Getting the profile's email adress");
        return userEmail.getText();
    }
    
    public String getFirstBorrowedBookTitle(){
        LOG.log(Level.INFO, "Getting the title of the first book in the user's borrowed book list");
        return firstBorrowedBookTitle.getText();
    }
    
    public void clickFirstBorrowedBookTitle(){
        clickButton("title of the first book in the user's borrowed book list", firstBorrowedBookTitle);
    }
    public void clickEditUserButton(){
        clickButton("edit user button", editUserButton);
    }
    public void clickDeleteUserButton(){
        clickButton("delete user button", deleteUserButton);
        clickButton("yes button", okButton);
    }
    public int cantFindDeleteUserButton(){
        LOG.log(Level.INFO, "Checking that Delete User Button is not visible");
        int n = $$("#delete-user-button").size();
        return n;
    }
    
}
