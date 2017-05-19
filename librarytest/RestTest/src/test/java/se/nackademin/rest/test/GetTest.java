/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.nackademin.rest.test;

import org.junit.Test;

import com.jayway.restassured.response.Response;
import static org.junit.Assert.*;
import se.nackademin.rest.test.model.Book;
import se.nackademin.rest.test.model.AllAuthors;
import se.nackademin.rest.test.model.AllBooks;
import se.nackademin.rest.test.model.Author;
import org.junit.*;
import static org.junit.Assert.*;

import org.junit.rules.*;
import org.slf4j.*;
/**
 *
 * @author testautomatisering
 */
public class GetTest {
    
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
    
    
    
    @Test //this test tries to perform a get-request on the api for a list of all books in the system and then verifies that we get the right statuscode (200), the response body is not blank and our dummy book is in the response we get
    public void testGetAllBooks(){
        //this part fetches a list of all books
        Response response = new BookOperations().getAllBooks();
        assertEquals("The status code should be: 200",  200, response.statusCode());
        assertNotEquals("The book list should not be empty", "", response.body().asString());
        
        //the next parts fetches the Dummybook from the list as a book.class object and verifies that it has the right attributes
        AllBooks books = response.jsonPath().getObject("books", AllBooks.class);
        Book DummyBook = books.getBookfromBooks(GlobVar.aDummyBookId);
        
        assertEquals("The books description should be: " + GlobVar.aDummyBookDescription,  GlobVar.aDummyBookDescription, DummyBook.getDescription());       
        assertEquals("The books title should be: " + GlobVar.aDummyBookTitle,  GlobVar.aDummyBookTitle, DummyBook.getTitle());       
        assertEquals("The books isbn should be: " + GlobVar.aDummyBookIsbn,  GlobVar.aDummyBookIsbn, DummyBook.getIsbn());       
        assertEquals("The books page count should be: " + GlobVar.aDummyBookNbrPages,  GlobVar.aDummyBookNbrPages, DummyBook.getNbrPages());       
        
        //the part below gets the same Dummybook as a response and verifies that it contains the name of that book's author
        Response grabAuthorResponse = new BookOperations().getBookById(GlobVar.aDummyBookId);
        String authorName = GlobVar.dummyAuthorFirstName;
        assertNotEquals("The book list should not be empty", "", response.body().asString()); 
        assertTrue("The book list should contain the name of our test author", grabAuthorResponse.body().asString().contains(authorName)); 
         
    }
    
    @Test //this test uses my help class AllBooks to fetch all books in the list, then find a single book from the response and verify that it has the right title
    public void testFetchAllBooks(){
        AllBooks books = new BookOperations().fetchAllBooks();
        Book fetchedBook = books.getBookfromBooks(GlobVar.aDummyBookId);
        assertEquals("The books description should be: " + GlobVar.aDummyBookDescription,  GlobVar.aDummyBookDescription, fetchedBook.getDescription());       
        assertEquals("The books title should be: " + GlobVar.aDummyBookTitle,  GlobVar.aDummyBookTitle, fetchedBook.getTitle());       
        assertEquals("The books isbn should be: " + GlobVar.aDummyBookIsbn,  GlobVar.aDummyBookIsbn, fetchedBook.getIsbn());       
        assertEquals("The books page count should be: " + GlobVar.aDummyBookNbrPages,  GlobVar.aDummyBookNbrPages, fetchedBook.getNbrPages());       
        
    }
    
    
    
    @Test //this test fetches our dummy book from the system and verifies the response statuscode (200) and that we got the right book
    public void testGetBookById(){
        Response response = new BookOperations().getBookById(GlobVar.aDummyBookId);
        assertEquals("The status code should be: 200",  200, response.statusCode());        
        assertEquals("Book title should be: " + GlobVar.aDummyBookTitle,  GlobVar.aDummyBookTitle, response.body().jsonPath().getString("book.title"));
        assertEquals("The books description should be: " + GlobVar.aDummyBookDescription,  GlobVar.aDummyBookDescription, response.body().jsonPath().getString("book.description"));       
        assertEquals("The books title should be: " + GlobVar.aDummyBookTitle,  GlobVar.aDummyBookTitle, response.body().jsonPath().getString("book.title"));       
        assertEquals("The books isbn should be: " + GlobVar.aDummyBookIsbn,  GlobVar.aDummyBookIsbn, response.body().jsonPath().getString("book.isbn"));       
        assertEquals("The books page count should be: " + GlobVar.aDummyBookNbrPages,  GlobVar.aDummyBookNbrPages, (Integer)response.body().jsonPath().getInt("book.nbrPages"));
    }
    
    @Test //this test verifies that trying to retrieve a book that does not exist in the system returns the right statuscode (404) and a blank response body
    public void testInvalidGetBookByIdReturns404(){
        Response response = new BookOperations().getBookById(GlobVar.aDummyBookId + 1);
        assertEquals("status code returned should be: 404",  404, response.statusCode());
        assertEquals("response body should be blank", "", response.body().asString());
    }

    @Test //this test is essentially the same as testGetBookById but uses the book.class object instead of json style responses
    public void testFetchBook(){
        Book book = new BookOperations().fetchBookById(GlobVar.aDummyBookId);
        assertEquals("book id should be: "+GlobVar.aDummyBookId, GlobVar.aDummyBookId, book.getId());    
        assertEquals("The books description should be: " + GlobVar.aDummyBookDescription,  GlobVar.aDummyBookDescription, book.getDescription());       
        assertEquals("The books title should be: " + GlobVar.aDummyBookTitle,  GlobVar.aDummyBookTitle, book.getTitle());       
        assertEquals("The books isbn should be: " + GlobVar.aDummyBookIsbn,  GlobVar.aDummyBookIsbn, book.getIsbn());       
        assertEquals("The books page count should be: " + GlobVar.aDummyBookNbrPages,  GlobVar.aDummyBookNbrPages, book.getNbrPages());       
        
    }
    
    
    
    @Test  //this test gets a single book written by an author specified by their authorId, then verifies the response statuscode (200) and that we got the right book
    public void testGetBookByAuthor(){
        Response response = new BookOperations().getBookByAuthor(GlobVar.dummyAuthorId);
        assertEquals("The status code should be: 200",  200, response.statusCode());
        assertEquals("The books description should be: " + GlobVar.aDummyBookDescription,  GlobVar.aDummyBookDescription, response.body().jsonPath().getString("books.book.description"));       
        assertEquals("The books title should be: " + GlobVar.aDummyBookTitle,  GlobVar.aDummyBookTitle, response.body().jsonPath().getString("books.book.title"));       
        assertEquals("The books isbn should be: " + GlobVar.aDummyBookIsbn,  GlobVar.aDummyBookIsbn, response.body().jsonPath().getString("books.book.isbn"));       
        assertEquals("The books page count should be: " + GlobVar.aDummyBookNbrPages,  GlobVar.aDummyBookNbrPages, (Integer)response.body().jsonPath().getInt("books.book.nbrPages"));
        
    }
    
    /*
     *  detta test kommer inte ge rätt utslag då rest-api returnerar 200 när dett test körs, 
     *  api dokumentationen specifierar inte heller att man ska kunna få fel på detta vis, 
     *  men det verkar som ett misstag så behåller detta test så ni kan se hur det blir.
     */
    @Test //this test verifies that trying to retrieve a book using an authorId returns an appropriate response, the api does not define what responce would be appropriate so I have improvised
    public void testInvalidGetBookbyAuthor(){
        Response response = new BookOperations().getBookByAuthor(GlobVar.dummyAuthorId + 1);
        //assertEquals("The status code should be: 404",  404, response.statusCode());
        assertEquals("response body should be {\"books\":\"\"}", "{\"books\":\"\"}", response.body().asString());
    }
    

    
    @Test //this test gets a single author from a book retrieved by its bookId and verifies the response statuscode (200) and that we got the right author
    public void testGetAuthorByBook(){
        Response response = new BookOperations().getAuthorByBook(GlobVar.aDummyBookId);
        assertEquals("The status code should be: 200",  200, response.statusCode());
        assertEquals("Author name should be " + GlobVar.dummyAuthorFirstName,  GlobVar.dummyAuthorFirstName, response.body().jsonPath().getString("authors.author.firstName"));
    }
    
    @Test //this test verifies that trying to retrieve an author using a bookId that does not exist in the system returns the right statuscode (404) and a blank response body
    public void testInvalidGetAuthorByBook(){
        Response response = new BookOperations().getAuthorByBook(GlobVar.aDummyBookId + 1);
        assertEquals("The status code should be: 404",  404, response.statusCode());
        assertEquals("response body should be blank", "", response.body().asString());
    }
    
    
    
    @Test //this test tries to perform a get-request on the api for a list of all authors in the system and then verifies that we get the right statuscode (200), the response body is not blank and our dummy author is in the response we get
    public void testGetAllAuthors(){
        Response response = new AuthorOperations().getAllAuthors();
        assertEquals("The status code should be: 200",  200, response.statusCode());
        Response authorResponse = new AuthorOperations().getAuthor(GlobVar.dummyAuthorId);
        String authorFirstName = authorResponse.body().jsonPath().getString("author.firstName");
        assertNotEquals("The book list should not be empty", "", response.body().asString()); 
        assertTrue("The book list should contain the name of our test author", response.body().asString().contains(authorFirstName));
    }
    
    @Test //this test uses my help class AllAuthors to fetch all authors in the list, then find a single author from the response and verify that it has the right name
    public void testFetchAllAuthors(){
        AllAuthors  authors = new AuthorOperations().fetchAllAuthors();
        Author fetchedAuthor = authors.getAuthorfromAuthors(GlobVar.dummyAuthorId);
        assertEquals("Author name should be: " + GlobVar.dummyAuthorFirstName,  GlobVar.dummyAuthorFirstName, fetchedAuthor.getFirstName());
    }
    
    
    
    @Test //this test fetches our dummy author from the system and verifies the response statuscode (200) and that we got the right author
    public void testGetAuthorById(){
        Response response = new AuthorOperations().getAuthor(GlobVar.dummyAuthorId);
        assertEquals("The status code should be: 200",  200, response.statusCode());
        assertEquals("Author Name should be: " + GlobVar.dummyAuthorFirstName,  GlobVar.dummyAuthorFirstName, response.body().jsonPath().getString("author.firstName"));
    }
    
    @Test //this test verifies that trying to retrieve an author using an authorId that does not exist in the system returns the right statuscode (404) and a blank response body
    public void testInvalidGetAuthorById(){
        Response response = new AuthorOperations().getAuthor(GlobVar.dummyAuthorId + 1);
        assertEquals("The status code should be: 404",  404, response.statusCode());
        assertEquals("response body should be blank", "", response.body().asString());
    }
    @Test //this test uses my Author.class helpclass to fetch a specific author based on their authorId and verifies that we get the right author.
    public void testFetchAuthorById(){
        Author author = new AuthorOperations().fetchAuthor(GlobVar.dummyAuthorId);
        assertEquals("Author name should be: " + GlobVar.dummyAuthorFirstName,  GlobVar.dummyAuthorFirstName, author.getFirstName());
    }
    
    
    
}
