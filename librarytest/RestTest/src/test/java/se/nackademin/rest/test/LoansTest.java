/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.nackademin.rest.test;

import static com.jayway.restassured.RestAssured.given;
import com.jayway.restassured.http.ContentType;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.rules.*;
import org.slf4j.*;
import com.jayway.restassured.response.Response;
import se.nackademin.rest.test.model.AllLoans;
import se.nackademin.rest.test.model.SingleLoan;
import se.nackademin.rest.test.model.Loan;
import se.nackademin.rest.test.model.Book;
import se.nackademin.rest.test.model.User;

/**
 *
 * @author testautomatisering
 */
public class LoansTest {
    private static Logger _logger;
    
    @Rule
    public TestName testName = new TestName();
    
    @BeforeClass //this method creates a pair of dummy books, dummy users and then uses those to create two dummy loans to be used during test executions
    public static void CreateTwoDummyBooksUsersAndLoans(){
        _logger = LoggerFactory.getLogger(LoansTest.class);
        
        Response makeADummyBookResponse = BeforeAndAfterOperations.createTwoDummyBooksUsersAndLoans();
        assertEquals("The status code should be: 201",  201, makeADummyBookResponse.statusCode());
        assertEquals("response body should be blank",  "", makeADummyBookResponse.body().asString());
        
    }
    
    @Before
    public void before(){
        String  name = testName.getMethodName();
        _logger.info("Startar testfallet " +name+".");
    }
    
    @After
    public void after(){
        String  name = testName.getMethodName();
        _logger.info("Avslutar testfallet " +name+".");
    }
    
    @AfterClass //this method removes the dummies created by the previous method
    public static void RemoveTwoDummyBooksUsersAndLoans(){
        
        Response removeADummyLoanResponse = BeforeAndAfterOperations.removeTwoDummyBooksUsersAndLoans();
        assertEquals("The status code should be: 204",  204, removeADummyLoanResponse.statusCode());
        assertEquals("response body should be blank",  "", removeADummyLoanResponse.body().asString());
        
    }
    
    
    @Test //this test tries to perform a get-request on the api for a list of all loans in the system and then verifies that we get the right statuscode (200), the response body is not blank and our dummy loan is in the response we get
    public void testGetAllLoans(){
        
        //this part fetches a list of all books
        Response response = new LoanOperations().getAllLoans();
        assertEquals("The status code should be: 200",  200, response.statusCode());
        assertNotEquals("The resulting list should not be empty", "", response.body().asString());
        
        Response grabloanResponse = new LoanOperations().getLoan(GlobVar.aDummyLoanId);
        String loanId = GlobVar.aDummyLoanId.toString();
        assertNotEquals("The list should not be empty", "", grabloanResponse.body().asString()); 
        assertTrue("The list should contain the id sequence of our loan", grabloanResponse.body().asString().contains(loanId)); 
        
    }
    
    @Test //this test uses my help class AllBooks to fetch all books in the list, then find a single specific book from the response and verify that it has the correct data
    public void testFetchAllLoans(){
        AllLoans loans = new LoanOperations().fetchAllLoans();
        Loan dummyLoan = loans.getLoanfromLoans(GlobVar.aDummyLoanId);
        String isNotEquals = dummyLoan.EqualsADummyLoan(dummyLoan);
        assertEquals("The String isNotEquals should be empty", "", isNotEquals);
        
    }
    
    @Test //this test creates a new alternate dummy loan using my loan class, verifies that we get the right response code (201), a blank response body and then fetches the resulting loan to verify that its data matches what we entered. It then deletes the new loan to keep things tidy
    public void testPostLoan(){
        Loan loan = new Loan();
        loan.setBook(GlobVar.bDummyLoanBook);
        loan.setDateBorrowed(GlobVar.aDummyDateBorrowed);
        loan.setDateDue(GlobVar.aDummyDateDue);
        loan.setUser(GlobVar.aDummyLoanUser);
        SingleLoan singleLoan = new SingleLoan(loan);
        Response response = given().contentType(ContentType.JSON).body(singleLoan).post(GlobVar.BASE_URL+"loans");
        assertEquals("The status code should be: 201",  201, response.statusCode());
        assertEquals("response body should be blank", "", response.body().asString());
        
        Loan postedloan = new LoanOperations().fetchLastLoan();
        String isNotEquals = postedloan.EqualsMixedADummyLoanWithBBook(loan);
        assertEquals("The String isNotEquals should be empty", "", isNotEquals);
        
        //this part removes our alternate dummy loan to keep things tidy if we have successfully created a loan to remove
        if(response.statusCode() == 201){
            Response removeResponse = new LoanOperations().deleteLastLoan();
            assertEquals("The status code should be: 204",  204, removeResponse.statusCode());
            assertEquals("response body should be blank", "", response.body().asString());
        }
    }
    
    
           //this test currently returns 201 and creates a second copy of a loan with the same user and book (also identical dateBorrowed and dateDue, though those hardly need to be unique) this should NOT happen according to the api. If you want to verify that dupplicate loans are indeed created, either do so manually with httprequester or just comment out the AfterClass cleanup methods though then you have to clean up the 2 dummybooks and dummy authors yourself (the dummyloans are removed automatically if you remove the associated books and/or authors)
    @Test //this test tries to post a loan using the same data as has already been entered as part of test-setup. This should not work and we should recieve a 409 status code. 
    public void testInvalidPostSameLoanTwice(){
        Loan loan = new Loan();
        loan.setBook(GlobVar.aDummyLoanBook);
        loan.setDateBorrowed(GlobVar.aDummyDateBorrowed);
        loan.setDateDue(GlobVar.aDummyDateDue);
        loan.setUser(GlobVar.aDummyLoanUser);
        SingleLoan singleLoan = new SingleLoan(loan);
        Response response = given().contentType(ContentType.JSON).body(singleLoan).post(GlobVar.BASE_URL+"loans");
        assertEquals("The status code should be: 409",  409, response.statusCode());
        // assertEquals("response body should be blank", "", response.body().asString()); the appropriate response message for this invalid post should probably be along the lines of "this loan already exists", but 
    }
    @Test //this test tries to post two loans, the first leaves our dummyBook at 0 copies available, the second verifies that trying to loan that book again with a different user returns the appropriate statuscode (409) and response message. it then removes the one successful loan and the extra dummy user that were needed for this test as part of cleanup.
    public void testInvalidPostLoanWithRemainingCopiesOfBookExhausted(){
        Response makeCDummyUserResponse = BeforeAndAfterOperations.makeCDummyUser();
        assertEquals("The status code should be: 201",  201, makeCDummyUserResponse.statusCode());
        assertEquals("response body should be blank",  "", makeCDummyUserResponse.body().asString());
        
        Loan loan = new Loan();
        loan.setBook(GlobVar.aDummyLoanBook);
        loan.setDateBorrowed(GlobVar.aDummyDateBorrowed);
        loan.setDateDue(GlobVar.aDummyDateDue);
        loan.setUser(GlobVar.bDummyLoanUser);
        SingleLoan singleLoan = new SingleLoan(loan);
        Response response = given().contentType(ContentType.JSON).body(singleLoan).post(GlobVar.BASE_URL+"loans");
        assertEquals("The status code should be: 201",  201, response.statusCode());
        assertEquals("response body should be blank", "", response.body().asString());
        
        Loan loanTwo = new Loan();
        loanTwo.setBook(GlobVar.aDummyLoanBook);
        loanTwo.setDateBorrowed(GlobVar.aDummyDateBorrowed);
        loanTwo.setDateDue(GlobVar.aDummyDateDue);
        loanTwo.setUser(new UserOperations().fetchLastUser());
        SingleLoan singleLoanTwo = new SingleLoan(loanTwo);
        Response responseTwo = given().contentType(ContentType.JSON).body(singleLoanTwo).post(GlobVar.BASE_URL+"loans");
        assertEquals("The status code should be: 409",  409, responseTwo.statusCode());
        assertEquals("response body should be: No copies of the book left in inventory.", "No copies of the book left in inventory.", responseTwo.body().asString());
        
        if(response.statusCode() == 201){
            Response removeResponse = new LoanOperations().deleteLastLoan();
            assertEquals("The status code should be: 204",  204, removeResponse.statusCode());
            assertEquals("response body should be blank", "", response.body().asString());
        }
        if(makeCDummyUserResponse.statusCode() == 201){
            Response removeCDummyUserResponse = BeforeAndAfterOperations.removeCDummyUser();
            assertEquals("The status code should be: 204",  204, removeCDummyUserResponse.statusCode());
            assertEquals("response body should be blank",  "", removeCDummyUserResponse.body().asString());
        }
    }
    
    @Test //this test verifies that we cannot create a loan without providing a book, that we get the right response code (400) and response message
    public void testInvalidPostLoanWithoutBook(){
        Loan loan = new Loan();
        loan.setDateBorrowed(GlobVar.aDummyDateBorrowed);
        loan.setDateDue(GlobVar.aDummyDateDue);
        loan.setUser(GlobVar.aDummyLoanUser);
        SingleLoan singleLoan = new SingleLoan(loan);
        Response response = given().contentType(ContentType.JSON).body(singleLoan).post(GlobVar.BASE_URL+"loans");
        assertEquals("The status code should be: 400",  400, response.statusCode());
        assertEquals("response body should be Book does not exist.", "Book does not exist.", response.body().asString());
    }
    
    
    
    
    @Test //this test updates the aDummyLoan with cDummy values for dateBorrowed and dateDue, it verifies that we get the right responsecode (200) and blank response body, then verifies that the loan has had its dates updated and finally restores the dummyloan to its prior status for other tests to work on
    public void testPutALoan(){
        Loan loan = new Loan();
        loan.setId(GlobVar.aDummyLoanId);
        loan.setBook(GlobVar.aDummyLoanBook);
        loan.setDateBorrowed(GlobVar.cDummyDateBorrowed);
        loan.setDateDue(GlobVar.cDummyDateDue);
        loan.setUser(GlobVar.aDummyLoanUser);
        SingleLoan singleLoan = new SingleLoan(loan);
        Response response = given().contentType(ContentType.JSON).body(singleLoan).put(GlobVar.BASE_URL+"loans");
        assertEquals("The status code should be: 200",  200, response.statusCode());
        assertEquals("response body should be blank", "", response.body().asString());
        
        Loan putLoan = new LoanOperations().fetchLoan(GlobVar.aDummyLoanId);
        String isNotEquals = putLoan.EqualsADummyLoanWithCDates(loan);
        assertEquals("The String isNotEquals should be empty", "", isNotEquals);
        
        Response removeResponse = new LoanOperations().unPutALoan();
        assertEquals("The status code should be: 200",  200, removeResponse.statusCode());
        assertEquals("response body should be blank", "", response.body().asString());
    }
    
    @Test //this test verifies that we cannot update a loan without a loan id set, not that this is in defiance of the api, though logically there's no better way to tell the program what loan to update without it!
    public void testInvalidPutLoanWithoutLoanId(){
        Loan loan = new Loan();
        
        loan.setBook(GlobVar.aDummyLoanBook);
        loan.setDateBorrowed(GlobVar.cDummyDateBorrowed);
        loan.setDateDue(GlobVar.cDummyDateDue);
        loan.setUser(GlobVar.aDummyLoanUser);
        SingleLoan singleLoan = new SingleLoan(loan);
        Response response = given().contentType(ContentType.JSON).body(singleLoan).put(GlobVar.BASE_URL+"loans");
        // if the above put attempt succeeds against expectations, this if-clause will unput the dummyLoan so it still works for other tests
        if(!(response.statusCode()== 400)){
            Response removeResponse = new LoanOperations().unPutALoan();
        }
        assertEquals("The status code should be: 400",  400, response.statusCode());
        assertEquals("response body should be No id set in loan.", "No id set in loan.", response.body().asString());
    }
            // this test currently returns statuscode 200 and a blank response body, the api says this should not be allowed. On the other hand dateBorrowed is not listed as an obligatory field. 
    @Test //this test verifies that we cannot update a loan without a loan dateBorrowed set, that we get the right response code (400) and message
    public void testInvalidPutLoanWithoutDateBorrowed(){
        Loan loan = new Loan();
        loan.setId(GlobVar.aDummyLoanId);
        loan.setBook(GlobVar.aDummyLoanBook);
        
        loan.setDateDue(GlobVar.aDummyDateDue);
        loan.setUser(GlobVar.aDummyLoanUser);
        SingleLoan singleLoan = new SingleLoan(loan);
        Response response = given().contentType(ContentType.JSON).body(singleLoan).put(GlobVar.BASE_URL+"loans");
        // if the above put attempt succeeds against expectations, this if-clause will unput the dummyLoan so it still works for other tests
        if(!(response.statusCode()== 400)){
            Response removeResponse = new LoanOperations().unPutALoan();
        }
        assertEquals("The status code should be: 400",  400, response.statusCode());
        assertEquals("response body should be No dateBorrowed set in loan.", "No dateBorrowed set in loan.", response.body().asString());
    }
    
            // this test currently returns statuscode 200 and a blank response body, the api says this should not be allowed. On the other hand dateDue is not listed as an obligatory field. 
    @Test //this test verifies that we cannot update a loan without a loan dateDue set, that we get the right response code (400) and message
    public void testInvalidPutLoanWithoutDateDue(){
        Loan loan = new Loan();
        loan.setId(GlobVar.aDummyLoanId);
        loan.setBook(GlobVar.aDummyLoanBook);
        loan.setDateBorrowed(GlobVar.aDummyDateBorrowed);
        
        loan.setUser(GlobVar.aDummyLoanUser);
        SingleLoan singleLoan = new SingleLoan(loan);
        Response response = given().contentType(ContentType.JSON).body(singleLoan).put(GlobVar.BASE_URL+"loans");
        // if the above put attempt succeeds against expectations, this if-clause will unput the dummyLoan so it still works for other tests
        if(!(response.statusCode()== 400)){
            Response removeResponse = new LoanOperations().unPutALoan();
        }
        assertEquals("The status code should be: 400",  400, response.statusCode());
        assertEquals("response body should be No dateDue set in loan.", "No dateDue set in loan.", response.body().asString());
    }
    
    @Test // this test verifies that we cannot update a loan and change what book is being loaned with it, that we get the right status response message and statuscode
    public void testInvalidPutLoanWithDifferentBook(){
        Loan loan = new Loan();
        loan.setId(GlobVar.aDummyLoanId);
        loan.setBook(GlobVar.bDummyLoanBook);
        loan.setDateBorrowed(GlobVar.aDummyDateBorrowed);
        loan.setDateDue(GlobVar.aDummyDateDue);
        loan.setUser(GlobVar.aDummyLoanUser);
        SingleLoan singleloan = new SingleLoan(loan);
        
        Response response = given().contentType(ContentType.JSON).body(singleloan).put(GlobVar.BASE_URL+"loans");
        // if the above put attempt succeeds against expectations, this if-clause will unput the dummyLoan so it still works for other tests
        if(!(response.statusCode()== 400)){
            Response removeResponse = new LoanOperations().unPutALoan();
        }
        assertEquals("The status code should be: 400",  400, response.statusCode());
        assertEquals("response body should be Can't update loan with new book.", "Can't update loan with new book.", response.body().asString());
        
    }
        
    @Test // this test verifies that we cannot update a loan and change what book is being loaned with it, that we get the right status response message and statuscode (400)
    public void testInvalidPutLoanWithDifferentUser(){
        Loan loan = new Loan();
        loan.setId(GlobVar.aDummyLoanId);
        loan.setBook(GlobVar.aDummyLoanBook);
        loan.setDateBorrowed(GlobVar.aDummyDateBorrowed);
        loan.setDateDue(GlobVar.aDummyDateDue);
        loan.setUser(GlobVar.bDummyLoanUser);
        SingleLoan singleloan = new SingleLoan(loan);
        
        Response response = given().contentType(ContentType.JSON).body(singleloan).put(GlobVar.BASE_URL+"loans");
        // if the above put attempt succeeds against expectations, this if-clause will unput the dummyLoan so it still works for other tests
        if(!(response.statusCode()== 400)){
            Response removeResponse = new LoanOperations().unPutALoan();
        }
        assertEquals("The status code should be: 400",  400, response.statusCode());
        assertEquals("response body should be Can't update loan with new user.", "Can't update loan with new user.", response.body().asString());
    }
    
    
    
    
    @Test //this test verifies that you cannot perform a delete request to delete all loans
    public void testForbiddenDeleteLoans(){
        String resourceName = "loans";
        Response response = given().accept(ContentType.JSON).delete(GlobVar.BASE_URL+resourceName);
        assertEquals("The status code should be: 405, method not allowed",  405, response.statusCode());  
        assertEquals("response body should be blank",  "", response.body().asString());
    }
    
    
    @Test //this test simply retrieves our dummyloan by their loanId and verifies that it has the correct, pairing of Book and User without using my Loan-class. it also verifies that we got the appropriate responce statuscode.
    public void testGetLoan(){
        Response getLoanResponse = new LoanOperations().getLoan(GlobVar.aDummyLoanId);
        assertEquals("The status code should be: 200",  200, getLoanResponse.statusCode());
        assertNotEquals("The response body should not be empty", "", getLoanResponse.body().asString());
        assertEquals("The loaned book's title should be " + GlobVar.aDummyLoanBook.getTitle(),  GlobVar.aDummyLoanBook.getTitle(), getLoanResponse.body().jsonPath().getObject("loan.book", Book.class).getTitle()); 
        assertEquals("The loan's User's displayName should be " + GlobVar.aDummyLoanUser.getDisplayName(),  GlobVar.aDummyLoanUser.getDisplayName(), getLoanResponse.body().jsonPath().getObject("loan.user", User.class).getDisplayName());
    }
    
    @Test //this test retrieves our dummyloan by their loanId and verifies that it has all the correct data, aka that we got the right one
    public void testFetchLoan(){
        Loan loan = new LoanOperations().fetchLoan(GlobVar.aDummyLoanId);
        String isNotEquals = loan.EqualsADummyLoan(loan);
        assertEquals("The String isNotEquals should be empty", "", isNotEquals);
        
    }
    
    
    @Test //this test verifies that you cannot perform a post request to a given loan by their loanId at "loans/{loanId}"
    public void testForbiddenPostToLoanId(){
        String resourceName = "loans/"+GlobVar.aDummyLoanId;
        Response response = given().accept(ContentType.JSON).post(GlobVar.BASE_URL+resourceName);
        assertEquals("The status code should be: 405, method not allowed",  405, response.statusCode());  
        assertEquals("response body should be blank",  "", response.body().asString());
    }
    
    @Test //this test verifies that you cannot perform a put request to a given loan by their loanId at "loans/{loanId}"
    public void testForbiddenPutToLoanId(){
        String resourceName = "loans/"+GlobVar.aDummyLoanId;
        Response response = given().accept(ContentType.JSON).put(GlobVar.BASE_URL+resourceName);
        assertEquals("The status code should be: 405, method not allowed",  405, response.statusCode());  
        assertEquals("response body should be blank",  "", response.body().asString());
    }
    
    
    @Test
    public void testDeleteLoan(){
        // this part creates an alternate dummy loan for us to try to delete
        Loan loan = new Loan();
        loan.setBook(GlobVar.aDummyLoanBook);
        loan.setDateBorrowed(GlobVar.aDummyDateBorrowed);
        loan.setDateDue(GlobVar.aDummyDateDue);
        loan.setUser(GlobVar.bDummyLoanUser);
        SingleLoan singleLoan = new SingleLoan(loan);
        Response response = given().contentType(ContentType.JSON).body(singleLoan).post(GlobVar.BASE_URL+"loans");
        assertEquals("The status code should be: 201",  201, response.statusCode());
        assertEquals("response body should be blank", "", response.body().asString());
        
        Loan postedloan = new LoanOperations().fetchLastLoan();
        String isNotEquals = postedloan.EqualsMixedADummyLoanWithBUser(loan);
        assertEquals("The String isNotEquals should be empty", "", isNotEquals);
        
        //this part removes our alternate dummy loan to keep things tidy if we have successfully created a loan to remove
        if(response.statusCode() == 201){
            Response removeResponse = new LoanOperations().deleteLastLoan();
            assertEquals("The status code should be: 204",  204, removeResponse.statusCode());
            assertEquals("response body should be blank", "", response.body().asString());
        }
    }
    
    
    @Test //this test retrieves our dummyloan by their loanId and verifies that it has all the correct data, aka that we got the right one
    public void testFetchLoanOfUserById(){
        Loan loan = new LoanOperations().fetchLoanOfUser(GlobVar.aDummyUserId);
        String isNotEquals = loan.EqualsADummyLoan(loan);
        assertEquals("The String isNotEquals should be empty", "", isNotEquals);
        
    }

    @Test //this test simply retrieves our dummyloan by their loanId and verifies that it has the correct, pairing of Book and User without using my Loan-class. it also verifies that we got the appropriate responce statuscode.
    public void testGetLoanOfUserById(){
        Response getLoanResponse = new LoanOperations().getLoanOfUser(GlobVar.aDummyUserId);
        assertEquals("The status code should be: 200",  200, getLoanResponse.statusCode());
        assertNotEquals("The response body should not be empty", "", getLoanResponse.body().asString());
        assertEquals("The loaned book's title should be " + GlobVar.aDummyLoanBook.getTitle(),  GlobVar.aDummyLoanBook.getTitle(), getLoanResponse.body().jsonPath().getObject("loans.loan.book", Book.class).getTitle()); 
        assertEquals("The loan's User's displayName should be " + GlobVar.aDummyLoanUser.getDisplayName(),  GlobVar.aDummyLoanUser.getDisplayName(), getLoanResponse.body().jsonPath().getObject("loans.loan.user", User.class).getDisplayName());
    }
    
    @Test //this test verifies that you cannot perform a post request to a given loan by their loanId at "loans/{loanId}"
    public void testForbiddenPostToLoanOfUserById(){
        String resourceName = "loans/ofuser/"+GlobVar.aDummyUserId;
        Response response = given().accept(ContentType.JSON).post(GlobVar.BASE_URL+resourceName);
        assertEquals("The status code should be: 405, method not allowed",  405, response.statusCode());  
        assertEquals("response body should be blank",  "", response.body().asString());
    }
    
    @Test //this test verifies that you cannot perform a put request to a given loan by their loanId at "loans/{loanId}"
    public void testForbiddenPutToLoanOfUserById(){
        String resourceName = "loans/ofuser/"+GlobVar.aDummyUserId;
        Response response = given().accept(ContentType.JSON).put(GlobVar.BASE_URL+resourceName);
        assertEquals("The status code should be: 405, method not allowed",  405, response.statusCode());  
        assertEquals("response body should be blank",  "", response.body().asString());
    }
    
    @Test //this test verifies that you cannot perform a put request to a given loan by their loanId at "loans/{loanId}"
    public void testForbiddenDeleteToLoanOfUserById(){
        String resourceName = "loans/ofuser/"+GlobVar.aDummyUserId;
        Response response = given().accept(ContentType.JSON).delete(GlobVar.BASE_URL+resourceName);
        assertEquals("The status code should be: 405, method not allowed",  405, response.statusCode());  
        assertEquals("response body should be blank",  "", response.body().asString());
    }
    
    @Test //this test retrieves our dummyloan by their loanId and verifies that it has all the correct data, aka that we got the right one
    public void testFetchLoanOfBookById(){
        Loan loan = new LoanOperations().fetchLoanOfBook(GlobVar.aDummyBookId);
        String isNotEquals = loan.EqualsADummyLoan(loan);
        assertEquals("The String isNotEquals should be empty", "", isNotEquals);
        
    }

    @Test //this test simply retrieves our dummyloan by their loanId and verifies that it has the correct, pairing of Book and User without using my Loan-class. it also verifies that we got the appropriate responce statuscode.
    public void testGetLoanOfBookById(){
        Response getLoanResponse = new LoanOperations().getLoanOfBook(GlobVar.aDummyBookId);
        assertEquals("The status code should be: 200",  200, getLoanResponse.statusCode());
        assertNotEquals("The response body should not be empty", "", getLoanResponse.body().asString());
        assertEquals("The loaned book's title should be " + GlobVar.aDummyLoanBook.getTitle(),  GlobVar.aDummyLoanBook.getTitle(), getLoanResponse.body().jsonPath().getObject("loans.loan.book", Book.class).getTitle()); 
        assertEquals("The loan's User's displayName should be " + GlobVar.aDummyLoanUser.getDisplayName(),  GlobVar.aDummyLoanUser.getDisplayName(), getLoanResponse.body().jsonPath().getObject("loans.loan.user", User.class).getDisplayName());
    }
    
    @Test //this test verifies that you cannot perform a post request to a given loan by their loanId at "loans/{loanId}"
    public void testForbiddenPostToLoanOfBookById(){
        String resourceName = "loans/ofbook/"+GlobVar.aDummyBookId;
        Response response = given().accept(ContentType.JSON).post(GlobVar.BASE_URL+resourceName);
        assertEquals("The status code should be: 405, method not allowed",  405, response.statusCode());  
        assertEquals("response body should be blank",  "", response.body().asString());
    }
    
    @Test //this test verifies that you cannot perform a put request to a given loan by their loanId at "loans/{loanId}"
    public void testForbiddenPutToLoanOfBookById(){
        String resourceName = "loans/ofbook/"+GlobVar.aDummyBookId;
        Response response = given().accept(ContentType.JSON).put(GlobVar.BASE_URL+resourceName);
        assertEquals("The status code should be: 405, method not allowed",  405, response.statusCode());  
        assertEquals("response body should be blank",  "", response.body().asString());
    }
    
    @Test //this test verifies that you cannot perform a put request to a given loan by their loanId at "loans/{loanId}"
    public void testForbiddenDeleteToLoanOfBookById(){
        String resourceName = "loans/ofbook/"+GlobVar.aDummyBookId;
        Response response = given().accept(ContentType.JSON).delete(GlobVar.BASE_URL+resourceName);
        assertEquals("The status code should be: 405, method not allowed",  405, response.statusCode());  
        assertEquals("response body should be blank",  "", response.body().asString());
    }
    
    
    
    @Test //this test retrieves our dummyloan by their loanId and verifies that it has all the correct data, aka that we got the right one
    public void testFetchLoanOfUserOfBookById(){
        Loan loan = new LoanOperations().fetchLoanOfUserOfBook(GlobVar.aDummyUserId, GlobVar.aDummyBookId);
        String isNotEquals = loan.EqualsADummyLoan(loan);
        assertEquals("The String isNotEquals should be empty", "", isNotEquals);
        
    }

    @Test //this test simply retrieves our dummyloan by their loanId and verifies that it has the correct, pairing of Book and User without using my Loan-class. it also verifies that we got the appropriate responce statuscode.
    public void testGetLoanOfUserOfBookById(){
        Response getLoanResponse = new LoanOperations().getLoanOfUserOfBook(GlobVar.aDummyUserId, GlobVar.aDummyBookId);
        assertEquals("The status code should be: 200",  200, getLoanResponse.statusCode());
        assertNotEquals("The response body should not be empty", "", getLoanResponse.body().asString());
        assertEquals("The loaned book's title should be " + GlobVar.aDummyLoanBook.getTitle(),  GlobVar.aDummyLoanBook.getTitle(), getLoanResponse.body().jsonPath().getObject("loan.book", Book.class).getTitle()); 
        assertEquals("The loan's User's displayName should be " + GlobVar.aDummyLoanUser.getDisplayName(),  GlobVar.aDummyLoanUser.getDisplayName(), getLoanResponse.body().jsonPath().getObject("loan.user", User.class).getDisplayName());
    }
    
    @Test //this test verifies that you cannot perform a post request to a given loan by their loanId at "loans/{loanId}"
    public void testForbiddenPostToLoanOfUserOfBookById(){
        String resourceName = "loans/ofuser/"+GlobVar.aDummyUserId;
        Response response = given().accept(ContentType.JSON).post(GlobVar.BASE_URL+resourceName);
        assertEquals("The status code should be: 405, method not allowed",  405, response.statusCode());  
        assertEquals("response body should be blank",  "", response.body().asString());
    }
    
    @Test //this test verifies that you cannot perform a put request to a given loan by their loanId at "loans/{loanId}"
    public void testForbiddenPutToLoanOfUserOfBookById(){
        String resourceName = "loans/ofuser/"+GlobVar.aDummyUserId;
        Response response = given().accept(ContentType.JSON).put(GlobVar.BASE_URL+resourceName);
        assertEquals("The status code should be: 405, method not allowed",  405, response.statusCode());  
        assertEquals("response body should be blank",  "", response.body().asString());
    }
    
    @Test //this test verifies that you cannot perform a put request to a given loan by their loanId at "loans/{loanId}"
    public void testForbiddenDeleteToLoanOfUserOfBookById(){
        String resourceName = "loans/ofuser/"+GlobVar.aDummyUserId;
        Response response = given().accept(ContentType.JSON).delete(GlobVar.BASE_URL+resourceName);
        assertEquals("The status code should be: 405, method not allowed",  405, response.statusCode());  
        assertEquals("response body should be blank",  "", response.body().asString());
    }
}
