/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.nackademin.rest.test;

import org.junit.Test;
import static com.jayway.restassured.RestAssured.*;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import static com.jayway.restassured.path.json.JsonPath.*;
import org.junit.*;
import static org.junit.Assert.*;

import org.junit.rules.*;
import org.slf4j.*;
/**
 *
 * @author testautomatisering
 */
public class DeleteTest {
    private static Logger _logger;
    
    @Rule
    public TestName testName = new TestName();
    
     
    @BeforeClass //this method creates a dummy book, dummy author and adds the author to the book to be used during test executions
    public static void MaketheDummyBookAndDummyAuthor(){
        _logger = LoggerFactory.getLogger(LoansTest.class);
        
        Response makeDummysResponse = BeforeAndAfterOperations.makeDummyBookAndDummyAuthor();
        assertEquals("The status code should be: 201",  201, makeDummysResponse.statusCode());
        assertEquals("response body should be blank",  "", makeDummysResponse.body().asString());
        
        Response addDummyAuthorToDummyBook = BeforeAndAfterOperations.addDummyAuthorToDummyBook();
        assertEquals("The status code should be: 200",  200, addDummyAuthorToDummyBook.statusCode());
        assertEquals("response body should be blank",  "", addDummyAuthorToDummyBook.body().asString());
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
    public static void RemovetheDummyBookAndDummyAuthor(){
        Response removeResponse = BeforeAndAfterOperations.removeDummyBookAndDummyAuthor();
        assertEquals("The status code should be: 204",  204, removeResponse.statusCode());  
        assertEquals("response body should be blank",  "", removeResponse.body().asString());
    }
    
    
    
    @Test //this test verifies that you cannot perform a delete request to delete all books
    public void testForbiddenDeleteBooks(){
        String resourceName = "books";
        Response response = given().accept(ContentType.JSON).delete(GlobVar.BASE_URL+resourceName);
        assertEquals("The status code should be: 405, method not allowed",  405, response.statusCode());  
        assertEquals("response body should be blank",  "", response.body().asString());
    }
    
    
    
    @Test //this test attempts to create a new dummy book with dummy author attatched and then deletes the book, it then verifies that we got the right response statuscode (204), a blank response body and then checks that the book is no longer in the system but the author still is
    public void testDeleteBookWithAuthor(){
        BookOperations bookOperations = new BookOperations();
        AuthorOperations authorOperations = new AuthorOperations();
        
        Response authorResponse = authorOperations.getAuthor(GlobVar.dummyAuthorId); 
        String authorBiography = authorResponse.body().jsonPath().getString("author.bio");
        String authorCountry = authorResponse.body().jsonPath().getString("author.country");
        String authorFirstName = authorResponse.body().jsonPath().getString("author.firstName");
        String authorLastName = authorResponse.body().jsonPath().getString("author.lastName");
        Integer authorId = authorResponse.body().jsonPath().getInt("author.id");
        
        Response postResponse = bookOperations.createBookWithAuthor(authorId, authorBiography, authorCountry, authorFirstName, authorLastName, GlobVar.bSecondDummyBookDescription, GlobVar.bSecondDummyBookIsbn, GlobVar.bSecondDummyBookNbrPages, GlobVar.bSecondDummyBookPublicationDate, GlobVar.bSecondDummyBookTitle, GlobVar.bSecondDummyBookTotalNbrCopies);
        assertEquals("status code should be 201",  201, postResponse.statusCode());
        assertEquals("response body should be blank", "", postResponse.body().asString());
        
        String expectedTitle = from(bookOperations.getLatestJsonString()).getString("book.title");
        String expectedDescription = from(bookOperations.getLatestJsonString()).getString("book.description");
        String expectedIsbn = from(bookOperations.getLatestJsonString()).getString("book.isbn");
        Integer expectedNbrPages = from(bookOperations.getLatestJsonString()).getInt("book.nbrPages");
        String expectedAuthorName = from(bookOperations.getLatestJsonString()).getString("book.author.name");
        Integer expectedAuthorId = from(bookOperations.getLatestJsonString()).getInt("book.author.id");
        
        Response verifyResponse = new BookOperations().getAllBooks();
        String verifyTitle = verifyResponse.jsonPath().getString("books.book[-1].title");
        String verifyDescription = verifyResponse.jsonPath().getString("books.book[-1].description");
        String verifyIsbn = verifyResponse.jsonPath().getString("books.book[-1].isbn");
        Integer verifyNbrPages = verifyResponse.jsonPath().getInt("books.book[-1].nbrPages");
        String verifyAuthorName = verifyResponse.jsonPath().getString("books.book[-1].author.name");
        Integer verifyAuthorId = verifyResponse.jsonPath().getInt("books.book[-1].author.id");
        
        assertEquals(expectedTitle, verifyTitle);
        assertEquals(expectedDescription, verifyDescription);
        assertEquals(expectedIsbn, verifyIsbn);
        assertEquals(expectedNbrPages, verifyNbrPages); 
        assertEquals(expectedAuthorName, verifyAuthorName); 
        assertEquals(expectedAuthorId, verifyAuthorId); 
        
        //everything before this line creates and verifies that we have created a new book with attatched author that we can then try to delete.
        
        //the part below tries to delete the book if one was created
        if(postResponse.statusCode() == 201){
            Integer verifyBookId = verifyResponse.jsonPath().getInt("books.book[-1].id");
            Response deleteResponse = new BookOperations().deleteBook(verifyBookId);
            assertEquals("The status code should be: 204",  204, deleteResponse.statusCode());
            assertEquals("response body should be blank",  "", deleteResponse.body().asString());
        
            //and this part veirfies that the book is gone
            Response postDeleteBookResponse = new BookOperations().getBookById(verifyBookId);
            assertEquals("The status code should be: 404",  404, postDeleteBookResponse.statusCode());
            assertEquals("response body should be blank",  "", postDeleteBookResponse.body().asString());
        
            //and this part verifies that the author was not deleted from the system along with the book!
            Response postDeleteBookButNotAuthorResponse = authorOperations.getAuthor(GlobVar.dummyAuthorId); 
            assertEquals("The status code should be: 200",  200, postDeleteBookButNotAuthorResponse.statusCode());
            assertEquals("The authors first name should still be: " + authorFirstName,  authorFirstName, postDeleteBookButNotAuthorResponse.body().jsonPath().getString("author.firstName"));
            Integer postDeleteBookButNotAuthorId = postDeleteBookButNotAuthorResponse.body().jsonPath().getInt("author.id");
            assertEquals("The authors id should still be: " + authorId,  authorId, postDeleteBookButNotAuthorId);
        }
    }
    
    @Test //this test attempts to create a new dummy book with no author attatched and then deletes the book, it then verifies that we got the right response statuscode (204), a blank response body and then checks that the book is no longer in the system
    public void testDeleteBookWithNoAuthor(){
        BookOperations bookOperations = new BookOperations();
        
        Response postResponse = bookOperations.createBookWithInput(GlobVar.bSecondDummyBookDescription, GlobVar.bSecondDummyBookIsbn, GlobVar.bSecondDummyBookNbrPages, GlobVar.bSecondDummyBookPublicationDate, GlobVar.bSecondDummyBookTitle, GlobVar.bSecondDummyBookTotalNbrCopies);
        assertEquals("status code should be 201",  201, postResponse.statusCode());
        assertEquals("response body should be blank", "", postResponse.body().asString());
        
        String expectedTitle = from(bookOperations.getLatestJsonString()).getString("book.title");
        String expectedDescription = from(bookOperations.getLatestJsonString()).getString("book.description");
        String expectedIsbn = from(bookOperations.getLatestJsonString()).getString("book.isbn");
        Integer expectedNbrPages = from(bookOperations.getLatestJsonString()).getInt("book.nbrPages");
        
        Response verifyResponse = new BookOperations().getAllBooks();
        String verifyTitle = verifyResponse.jsonPath().getString("books.book[-1].title");
        String verifyDescription = verifyResponse.jsonPath().getString("books.book[-1].description");
        String verifyIsbn = verifyResponse.jsonPath().getString("books.book[-1].isbn");
        Integer verifyNbrPages = verifyResponse.jsonPath().getInt("books.book[-1].nbrPages");
        
        assertEquals(expectedTitle, verifyTitle);
        assertEquals(expectedDescription, verifyDescription);
        assertEquals(expectedIsbn, verifyIsbn);
        assertEquals(expectedNbrPages, verifyNbrPages); 
        
        //everything before this line creates and verifies that we have created a new book that we can then try to delete
        
        //the part below tries to delete the book if one was created
        if(postResponse.statusCode() == 201){
            Integer verifyBookId = verifyResponse.jsonPath().getInt("books.book[-1].id");
            Response deleteResponse = new BookOperations().deleteBook(verifyBookId);
            assertEquals("The status code should be: 204",  204, deleteResponse.statusCode());
            assertEquals("response body should be blank",  "", deleteResponse.body().asString());
        
            //and this part veirfies that the book is gone
            Response postDeleteBookResponse = new BookOperations().getBookById(verifyBookId);
            assertEquals("The status code should be: 404",  404, postDeleteBookResponse.statusCode());
            assertEquals("response body should be blank",  "", postDeleteBookResponse.body().asString());
        }
    }
    
    @Test //this test verifies that the book we are trying to delete does not exist in the system, that we cannot delete a book that does not exist in the system, that we get the appropriate status code (404) for trying along with a blank response body
    public void testInvalidDeleteBookWithNoExistingBook(){
        
        //this part veirfies that the book we are about to delete does not exist in the system
        Response bookResponse = new BookOperations().getAllBooks();
        Integer nonExistingBookId = bookResponse.jsonPath().getInt("books.book[-1].id") + 100;
        Response verifyNoBookResponse = new BookOperations().getBookById(nonExistingBookId);
        assertEquals("The status code should be: 404",  404, verifyNoBookResponse.statusCode());
        assertEquals("response body should be blank",  "", verifyNoBookResponse.body().asString());
        
        //the part below tries to delete a book if that book does not exist in the system
        if(verifyNoBookResponse.statusCode() == 404){
            Response deleteResponse = new BookOperations().deleteBook(nonExistingBookId);
            assertEquals("The status code should be: 404",  404, deleteResponse.statusCode());
            assertEquals("response body should be blank",  "", deleteResponse.body().asString());
        }
    }
    
    
    @Test //this test verifies that we cannot perform a delete request to delete a book by its author's authorId
    public void testForbiddenDeleteBooksByAuthorId(){
        String resourceName = "books/byauthor/"+GlobVar.dummyAuthorId;
        Response response = given().accept(ContentType.JSON).delete(GlobVar.BASE_URL+resourceName);
        assertEquals("The status code should be: 405, method not allowed",  405, response.statusCode());
        assertEquals("response body should be blank",  "", response.body().asString());
    }
    
    @Test //this test verifies that we cannot perform a delete request to delete an author by (one of) his book's bookId
    public void testForbiddenDeleteAuthorByBookId(){
        String resourceName = "books/"+GlobVar.dummyAuthorId +"/authors";
        Response response = given().accept(ContentType.JSON).delete(GlobVar.BASE_URL+resourceName);
        assertEquals("The status code should be: 405, method not allowed",  405, response.statusCode());
        assertEquals("response body should be blank",  "", response.body().asString());
    }
    
    
    @Test //this test verifies that we cannot perform a delete request to delete all authors
    public void testForbiddenDeleteAuthors(){
        String resourceName = "authors";
        Response response = given().accept(ContentType.JSON).delete(GlobVar.BASE_URL+resourceName);
        assertEquals("The status code should be: 405, method not allowed",  405, response.statusCode());  
        assertEquals("response body should be blank",  "", response.body().asString());
    }
    
    
    
    @Test // this test attempts to create a dummy author and then delete that author specified by its authorId if one was created. it also verifies that we get the appropriate response statuscode (204) and blank response body
    public void testDeleteAuthorById(){
        AuthorOperations authorOperations = new AuthorOperations();
        
        Response postResponse = authorOperations.createAuthor(GlobVar.secondDummyAuthorBio, GlobVar.secondDummyAuthorCountry, GlobVar.secondDummyAuthorFirstName, GlobVar.secondDummyAuthorLastName);
        assertEquals("status code should be 201",  201, postResponse.statusCode());
        assertEquals("response body should be blank", "", postResponse.body().asString());
        
        String expectedFirstName = from(authorOperations.getLatestJsonString()).getString("author.firstName");
        
        Response getResponse = authorOperations.getAllAuthors();
        String fetchedFirstName = getResponse.body().jsonPath().getString("authors.author[-1].firstName");
        assertEquals(expectedFirstName, fetchedFirstName);
        
        
        //this part above verifies that we have created an author and, if so, the part below tries to delete it
        if(postResponse.statusCode() == 201){
            Integer verifyAuthorId = getResponse.body().jsonPath().getInt("authors.author[-1].id");
            Response delResponse = authorOperations.deleteAuthor(verifyAuthorId);
            assertEquals("status code should be 204",  204, delResponse.statusCode());
            assertEquals("response body should be blank", "", delResponse.body().asString());
        
            //the part below verifies that the author is gone
            Response postDeleteAuthorResponse = new AuthorOperations().getAuthor(verifyAuthorId);
            assertEquals("The status code should be: 404",  404, postDeleteAuthorResponse.statusCode());
            assertEquals("response body should be blank",  "", postDeleteAuthorResponse.body().asString());
        }
    }
    
    @Test //this test verifies that the author we are about to try to delete does not exist in the system already, then tries to delete that nonexistant author and verifies that we get the right response statuscode (404) and a blank response body
    public void testInvalidDeleteAuthorById(){
        //this part veirfies that the Author we are about to delete does not exist in the system
        Response authorResponse = new AuthorOperations().getAllAuthors();
        Integer nonExistingAuthorId = authorResponse.jsonPath().getInt("authors.author[-1].id") + 100;
        Response verifyNoAuthorResponse = new AuthorOperations().getAuthor(nonExistingAuthorId);
        assertEquals("The status code should be: 404",  404, verifyNoAuthorResponse.statusCode());
        assertEquals("response body should be blank",  "", verifyNoAuthorResponse.body().asString());
        
        //the part below tries to delete a book if that book does not exist in the system
        if(verifyNoAuthorResponse.statusCode() == 404){
            Response deleteResponse = new AuthorOperations().deleteAuthor(nonExistingAuthorId);
            assertEquals("The status code should be: 404",  404, deleteResponse.statusCode());
            assertEquals("response body should be blank",  "", deleteResponse.body().asString());
        }
    }
    
    
    
}
    


