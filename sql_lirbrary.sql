-- Library Management System - Seed Data
-- Run AFTER the app has started once (so tables are created by Hibernate)

USE library_db;

-- ===================== USERS =====================
-- Password for all users: Admin@123 (BCrypt encoded)
INSERT IGNORE INTO users (username, email, password, full_name, role, phone, student_id, enabled, created_at, updated_at) VALUES
('manager1',  'manager@library.com',  '$2b$10$vTJgmgMLxn85Mh66CIWB1.1O6xxYCd.GXy1i6/eVTs/rCHzopjWf2', 'Nguyen Van Manager', 'MANAGER',   '0901111111', NULL,        true, NOW(), NOW()),
('librarian1','librarian@library.com','$2b$10$vTJgmgMLxn85Mh66CIWB1.1O6xxYCd.GXy1i6/eVTs/rCHzopjWf2', 'Tran Thi Librarian', 'LIBRARIAN', '0902222222', NULL,        true, NOW(), NOW()),
('student1',  'student1@iu.edu.vn',   '$2b$10$vTJgmgMLxn85Mh66CIWB1.1O6xxYCd.GXy1i6/eVTs/rCHzopjWf2', 'Le Van Student',     'STUDENT',   '0903333333', 'ITITWE001', true, NOW(), NOW()),
('student2',  'student2@iu.edu.vn',   '$2b$10$vTJgmgMLxn85Mh66CIWB1.1O6xxYCd.GXy1i6/eVTs/rCHzopjWf2', 'Pham Thi Student',   'STUDENT',   '0904444444', 'ITITWE002', true, NOW(), NOW()),
('lecturer1', 'lecturer@iu.edu.vn',   '$2b$10$vTJgmgMLxn85Mh66CIWB1.1O6xxYCd.GXy1i6/eVTs/rCHzopjWf2', 'Dr. Nguyen Minh',    'LECTURER',  '0905555555', 'LEC001',    true, NOW(), NOW()),
('lecturer2', 'lecturer2@iu.edu.vn',  '$2b$10$vTJgmgMLxn85Mh66CIWB1.1O6xxYCd.GXy1i6/eVTs/rCHzopjWf2', 'Dr. Tran Van Hai',   'LECTURER',  '0906666666', 'LEC002',    true, NOW(), NOW()),
('student3',  'student3@iu.edu.vn',   '$2b$10$vTJgmgMLxn85Mh66CIWB1.1O6xxYCd.GXy1i6/eVTs/rCHzopjWf2', 'Hoang Van Student',  'STUDENT',   '0907777777', 'ITITWE003', true, NOW(), NOW()),
('student4',  'student4@iu.edu.vn',   '$2b$10$vTJgmgMLxn85Mh66CIWB1.1O6xxYCd.GXy1i6/eVTs/rCHzopjWf2', 'Vo Thi Student',     'STUDENT',   '0908888888', 'ITITWE004', true, NOW(), NOW());

-- ===================== CATEGORIES =====================
INSERT IGNORE INTO categories (name, description, created_at) VALUES
('Programming',       'Software development, algorithms, and coding', NOW()),
('Computer Science',  'Theory of computation, data structures, networking', NOW()),
('Mathematics',       'Calculus, algebra, statistics, discrete math', NOW()),
('Database',          'SQL, NoSQL, database design and administration', NOW()),
('Web Development',   'HTML, CSS, JavaScript, frameworks', NOW()),
('Artificial Intelligence', 'Machine learning, deep learning, AI', NOW()),
('Software Engineering', 'Design patterns, architecture, best practices', NOW()),
('Operating Systems', 'Linux, Windows internals, system programming', NOW()),
('Networks',          'TCP/IP, protocols, network security', NOW()),
('General',           'General interest books', NOW());

-- ===================== AUTHORS =====================
INSERT IGNORE INTO authors (name, nationality, biography, created_at) VALUES
('Robert C. Martin',    'American', 'Author of Clean Code and other software engineering books', NOW()),
('Donald Knuth',        'American', 'The Art of Computer Programming series author', NOW()),
('Thomas H. Cormen',    'American', 'Introduction to Algorithms co-author', NOW()),
('Gang of Four',        'International', 'Design Patterns book authors', NOW()),
('Andrew Tanenbaum',    'American', 'Modern Operating Systems and Computer Networks author', NOW()),
('Jeff Ullman',         'American', 'Database and compiler textbook author', NOW()),
('Martin Fowler',       'British',  'Refactoring and enterprise patterns author', NOW()),
('Joshua Bloch',        'American', 'Effective Java author, Java API architect', NOW()),
('Erich Gamma',         'Swiss',    'Design Patterns co-author, Eclipse creator', NOW()),
('Stuart Russell',      'British',  'Artificial Intelligence: A Modern Approach co-author', NOW()),
('Kathy Sierra',        'American', 'Head First series author', NOW()),
('Brian Kernighan',     'Canadian', 'C Programming Language co-author', NOW());

-- ===================== BOOKS =====================
INSERT IGNORE INTO books (title, isbn, description, total_copies, available_copies, published_year, publisher, language, category_id, created_at, updated_at) VALUES
('Clean Code: A Handbook of Agile Software Craftsmanship', '978-0-13-468599-1', 'A guide to writing clean, maintainable code', 3, 3, 2008, 'Prentice Hall', 'English', 1, NOW(), NOW()),
('The Pragmatic Programmer', '978-0-13-595705-9', 'From journeyman to master programmer', 2, 2, 2019, 'Addison-Wesley', 'English', 7, NOW(), NOW()),
('Introduction to Algorithms', '978-0-26-204630-5', 'Comprehensive algorithms textbook', 4, 4, 2009, 'MIT Press', 'English', 2, NOW(), NOW()),
('Design Patterns: Elements of Reusable Object-Oriented Software', '978-0-20-163361-5', 'The classic GoF design patterns book', 2, 2, 1994, 'Addison-Wesley', 'English', 7, NOW(), NOW()),
('Modern Operating Systems', '978-0-13-359162-0', 'Comprehensive OS textbook by Tanenbaum', 3, 3, 2014, 'Pearson', 'English', 8, NOW(), NOW()),
('Database System Concepts', '978-0-07-352332-3', 'Fundamental database concepts and SQL', 5, 5, 2019, 'McGraw-Hill', 'English', 4, NOW(), NOW()),
('Artificial Intelligence: A Modern Approach', '978-0-13-468214-3', 'The leading AI textbook', 3, 3, 2020, 'Pearson', 'English', 6, NOW(), NOW()),
('Effective Java', '978-0-13-468599-1', 'Best practices for Java programming', 3, 3, 2018, 'Addison-Wesley', 'English', 1, NOW(), NOW()),
('Head First Java', '978-0-59-600712-6', 'Java learning through visuals and puzzles', 4, 4, 2005, "O''Reilly", 'English', 1, NOW(), NOW()),
('Computer Networks', '978-0-13-212695-3', 'Comprehensive computer networking textbook', 3, 3, 2011, 'Pearson', 'English', 9, NOW(), NOW()),
('The C Programming Language', '978-0-13-110362-7', 'The classic C language reference', 2, 2, 1988, 'Prentice Hall', 'English', 1, NOW(), NOW()),
('Refactoring: Improving the Design of Existing Code', '978-0-13-468599-2', 'How to refactor legacy code safely', 2, 2, 2018, 'Addison-Wesley', 'English', 7, NOW(), NOW()),
('Learning Python', '978-1-44-939890-3', 'Comprehensive Python programming guide', 4, 4, 2013, "O''Reilly", 'English', 1, NOW(), NOW()),
('Spring Boot in Action', '978-1-61-729314-1', 'Building Spring Boot applications', 3, 3, 2016, 'Manning', 'English', 5, NOW(), NOW()),
('HTML and CSS: Design and Build Websites', '978-1-11-899165-9', 'Visual guide to HTML and CSS', 5, 5, 2011, 'Wiley', 'English', 5, NOW(), NOW()),
('JavaScript: The Good Parts', '978-0-59-651774-8', 'The best features of JavaScript', 3, 3, 2008, "O''Reilly", 'English', 5, NOW(), NOW()),
('Discrete Mathematics and Its Applications', '978-1-25-966943-7', 'Discrete math for CS students', 4, 4, 2018, 'McGraw-Hill', 'English', 3, NOW(), NOW()),
('Deep Learning', '978-0-26-203561-3', 'The deep learning textbook by Goodfellow', 2, 2, 2016, 'MIT Press', 'English', 6, NOW(), NOW()),
('Structure and Interpretation of Computer Programs', '978-0-26-251087-5', 'Classic MIT programming textbook', 2, 2, 1996, 'MIT Press', 'English', 2, NOW(), NOW()),
('Code Complete', '978-0-73-561967-8', 'A practical handbook of software construction', 3, 3, 2004, 'Microsoft Press', 'English', 7, NOW(), NOW());

-- ===================== BOOK-AUTHOR RELATIONSHIPS =====================
INSERT IGNORE INTO book_authors (book_id, author_id) VALUES
(1, 1), (2, 1), (3, 3), (4, 4), (4, 9),
(5, 5), (6, 6), (7, 10), (8, 8), (9, 11),
(10, 5), (11, 12), (12, 7), (16, 3);
