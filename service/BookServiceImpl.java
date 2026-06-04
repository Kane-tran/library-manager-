package com.library.service;

import com.library.entity.Author;
import com.library.entity.Book;
import com.library.entity.Category;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import com.library.repository.CategoryRepository;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final AuthorRepository authorRepository;

    @Override
    public Book save(Book book, List<Long> authorIds) {
        if (authorIds != null && !authorIds.isEmpty()) {
            List<Author> authors = authorRepository.findAllById(authorIds);
            book.setAuthors(authors);
        }
        if (book.getId() == null) {
            book.setAvailableCopies(book.getTotalCopies());
        }
        return bookRepository.save(book);
    }

    @Override
    @Transactional(readOnly = true)
    public Book findById(Long id) {
        return bookRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Book not found: " + id));
    }

    @Override
    public void delete(Long id) {
        bookRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Book> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Book> search(String q, Long categoryId, Pageable pageable) {
        boolean hasQuery = q != null && !q.isBlank();
        boolean hasCategory = categoryId != null && categoryId > 0;

        if (hasQuery && hasCategory) {
            Category cat = findCategoryById(categoryId);
            return bookRepository.searchBooksInCategory(q, cat, pageable);
        } else if (hasQuery) {
            return bookRepository.searchBooks(q, pageable);
        } else if (hasCategory) {
            Category cat = findCategoryById(categoryId);
            return bookRepository.findByCategory(cat, pageable);
        }
        return findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Category findCategoryById(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));
    }

    @Override
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Author> findAllAuthors() {
        return authorRepository.findAll();
    }

    @Override
    public Author saveAuthor(Author author) {
        return authorRepository.save(author);
    }

    @Override
    @Transactional(readOnly = true)
    public Author findAuthorById(Long id) {
        return authorRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Author not found: " + id));
    }

    @Override
    public void deleteAuthor(Long id) {
        authorRepository.deleteById(id);
    }

    @Override
    public int importFromCsv(MultipartFile file) {
        // CSV format: title,isbn,description,totalCopies,publishedYear,publisher,language,categoryName,authorNames(semicolon separated)
        int count = 0;
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] header = reader.readNext(); // skip header
            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length < 4) continue;
                try {
                    Book book = new Book();
                    book.setTitle(line[0].trim());
                    if (line.length > 1 && !line[1].isBlank()) book.setIsbn(line[1].trim());
                    if (line.length > 2) book.setDescription(line[2].trim());
                    int copies = line.length > 3 ? Integer.parseInt(line[3].trim()) : 1;
                    book.setTotalCopies(copies);
                    book.setAvailableCopies(copies);
                    if (line.length > 4 && !line[4].isBlank()) book.setPublishedYear(Integer.parseInt(line[4].trim()));
                    if (line.length > 5) book.setPublisher(line[5].trim());
                    if (line.length > 6 && !line[6].isBlank()) book.setLanguage(line[6].trim());

                    // Category
                    if (line.length > 7 && !line[7].isBlank()) {
                        String catName = line[7].trim();
                        Category cat = categoryRepository.findByName(catName)
                            .orElseGet(() -> {
                                Category c = new Category();
                                c.setName(catName);
                                return categoryRepository.save(c);
                            });
                        book.setCategory(cat);
                    }

                    // Authors
                    if (line.length > 8 && !line[8].isBlank()) {
                        String[] authorNames = line[8].split(";");
                        List<Author> authors = new ArrayList<>();
                        for (String name : authorNames) {
                            String trimmed = name.trim();
                            if (!trimmed.isEmpty()) {
                                Author author = authorRepository.findAll().stream()
                                    .filter(a -> a.getName().equalsIgnoreCase(trimmed))
                                    .findFirst()
                                    .orElseGet(() -> {
                                        Author a = new Author();
                                        a.setName(trimmed);
                                        return authorRepository.save(a);
                                    });
                                authors.add(author);
                            }
                        }
                        book.setAuthors(authors);
                    }

                    bookRepository.save(book);
                    count++;
                } catch (Exception e) {
                    log.warn("Failed to import book row: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage(), e);
        }
        return count;
    }
}
