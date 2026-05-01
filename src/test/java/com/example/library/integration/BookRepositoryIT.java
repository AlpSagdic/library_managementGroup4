package com.example.library.integration;

import com.example.library.model.Book;
import com.example.library.model.Genre;
import com.example.library.repository.BookRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * INTEGRATION TEST - Repository Layer
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
    }

    private Book createBook(String isbn, String title, String author, int copies, Genre genre) {
        Book book = new Book(isbn, title, author, copies, genre);
        book.setPublishedDate(LocalDate.of(2020, 1, 1));
        return bookRepository.save(book);
    }

    // =========================================================================
    // EXAMPLE: Basic CRUD and custom query tests — filled in
    // =========================================================================

    @Nested
    @DisplayName("Basic CRUD operations")
    class CrudTests {

        @Test
        @DisplayName("should save and retrieve a book by ID")
        void shouldSaveAndFindById() {
            Book saved = createBook("978-0-13-468599-1", "Clean Code", "Robert C. Martin", 3, Genre.TECHNOLOGY);

            Optional<Book> found = bookRepository.findById(saved.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getTitle()).isEqualTo("Clean Code");
            assertThat(found.get().getIsbn()).isEqualTo("978-0-13-468599-1");
        }

        @Test
        @DisplayName("should find book by ISBN")
        void shouldFindByIsbn() {
            createBook("978-0-13-468599-1", "Clean Code", "Robert C. Martin", 3, Genre.TECHNOLOGY);

            Optional<Book> found = bookRepository.findByIsbn("978-0-13-468599-1");

            assertThat(found).isPresent();
            assertThat(found.get().getTitle()).isEqualTo("Clean Code");
        }

        @Test
        @DisplayName("should return empty when ISBN not found")
        void shouldReturnEmpty_WhenIsbnNotFound() {
            Optional<Book> found = bookRepository.findByIsbn("non-existent");

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("Custom query methods")
    class CustomQueryTests {

        @Test
        @DisplayName("should search books by keyword in title or author (case insensitive)")
        void shouldSearchByKeyword() {
            createBook("978-1", "Clean Code", "Robert C. Martin", 3, Genre.TECHNOLOGY);
            createBook("978-2", "Clean Architecture", "Robert C. Martin", 2, Genre.TECHNOLOGY);
            createBook("978-3", "Design Patterns", "Gang of Four", 5, Genre.TECHNOLOGY);

            List<Book> results = bookRepository.searchBooks("clean");

            assertThat(results).hasSize(2);
            assertThat(results).extracting(Book::getTitle)
                    .containsExactlyInAnyOrder("Clean Code", "Clean Architecture");
        }

        @Test
        @DisplayName("should find available books (copies > 0)")
        void shouldFindAvailableBooks() {
            Book available = createBook("978-1", "Available Book", "Author A", 3, Genre.FICTION);
            Book unavailable = createBook("978-2", "Unavailable Book", "Author B", 1, Genre.FICTION);
            unavailable.setAvailableCopies(0);
            bookRepository.save(unavailable);

            List<Book> results = bookRepository.findAvailableBooks();

            assertThat(results).hasSize(1);
            assertThat(results.get(0).getTitle()).isEqualTo("Available Book");
        }
    }

    // =========================================================================
    // TODO: Students should write these integration tests
    // =========================================================================

    @Nested
    @DisplayName("Genre and author queries")
    class FilterTests {
        // Seçkin
        @Test
        @DisplayName("should find books by genre")
        void shouldFindByGenre() {
            // TODO: Save books of different genres
            //       Query by Genre.SCIENCE and verify only matching books are returned
            createBook("978-1", "SCIENCE BOOK TEST 1", "SCIENCE BOOK WRITER 1", 10, Genre.SCIENCE);
            createBook("978-2", "SCIENCE BOOK TEST 2", "SCIENCE BOOK WRITER 2", 10, Genre.SCIENCE);
            createBook("978-3", "TECHNOLOGY BOOK TEST 1", "TECHNOLOGY BOOK WRITER 1", 10, Genre.TECHNOLOGY);
            createBook("978-4", "HISTORY BOOK TEST 1", "HISTORY BOOK WRITER 1", 10, Genre.HISTORY);
            createBook("978-5", "NON-FICTION BOOK TEST 1", "NON-FICTION BOOK WRITER 1", 10, Genre.NON_FICTION);

            List<Book> genreControl = bookRepository.findByGenre(Genre.SCIENCE);

            assertThat(genreControl).hasSize(2);
            assertThat(genreControl).extracting(Book::getGenre).containsOnly(Genre.SCIENCE);

        }
        //Ahmet Seçkin Büyükavcu
        @Test
        @DisplayName("should find books by author (case insensitive, partial match)")
        void shouldFindByAuthor() {
            // TODO: Save books by different authors
            //       Search by partial author name and verify results
            createBook("978-1", "Book One", "Stephen King", 5, Genre.FICTION);
            createBook("978-2", "Book Two", "Stephen Hawking", 5, Genre.SCIENCE);
            createBook("978-3", "Book Three", "George Orwell", 5, Genre.FICTION);

            List<Book> results = bookRepository.findByAuthorContainingIgnoreCase("stephen");

            assertThat(results).hasSize(2);
            assertThat(results).extracting(Book::getAuthor).containsExactlyInAnyOrder("Stephen King", "Stephen Hawking");
        }
        // Baha
        @Test
        @DisplayName("should search by author name using searchBooks()")
        void shouldSearchByAuthorKeyword() {
            bookRepository.save(createBook("978-2", "Available Book", "Author A", 1, Genre.FICTION));
            assertThat(bookRepository.searchBooks("Author A")).hasSize(1);
        }

        @Test
        @DisplayName("should return empty list when no books match search")
        void shouldReturnEmpty_WhenNoMatch() {
            assertThat(bookRepository.searchBooks("nothing")).isEmpty();
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {
        // Seçkin
        @Test
        @DisplayName("should enforce unique ISBN constraint")
        void shouldEnforceUniqueIsbn() {
            // TODO: Try to save two books with the same ISBN
            //       Verify a DataIntegrityViolationException is thrown
            //       Hint: Use assertThrows() and flush the persistence context
            createBook("978-1", "First Book", "Author A", 5, Genre.FICTION);

            assertThrows(DataIntegrityViolationException.class, () -> {
                createBook("978-1", "Second Book", "Author B", 3, Genre.SCIENCE);
            });
        }
        // Baha
        @Test
        @DisplayName("should handle deleting a book")
        void shouldDeleteBook() {
            Book available = createBook("978-1", "Available Book", "Author A", 3, Genre.FICTION);
            bookRepository.save(available);
            bookRepository.delete(available);
            assertThat(bookRepository.findByIsbn("978-1")).isEmpty();
        }
    }
}
