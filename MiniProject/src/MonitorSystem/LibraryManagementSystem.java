package MonitorSystem;

import java.util.*;
import java.time.LocalDate;
 
//================= BOOK CLASS ================= 
class Book {
    String id;
    String title;
    String author;
    boolean issued;
    String issuedTo;
    LocalDate issueDate;
    LocalDate dueDate;
 
    Book(String id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.issued = false;
    }
 
    void showBook() {
        System.out.println(id + " | " + title + " | " + author +
                " | " + (issued ? "Issued to " + issuedTo : "Available"));
    }
}
 
// ================= LIBRARIAN CLASS ================= 
class Librarian {
    String name;
 
    Librarian(String name) {
        this.name = name;
    }
 
    // Librarian approves the issue request
    boolean approveIssue() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Librarian (" + name + ") approval needed. Approve? (yes/no): ");
        String response = sc.nextLine();
        return response.equalsIgnoreCase("yes");
    }
 
    // Librarian collects the fine
    void collectFine(String student, double amount) {
        System.out.println("Librarian " + name + " collected Rs." + amount + " from " + student);
    }
}
 
//================= LIBRARY CLASS ================= 
class Library {
 
    ArrayList<Book> books = new ArrayList<>();        // all books in library
    Map<String, Book> issuedBooks = new HashMap<>();   // currently issued books (key = bookId)
 
    static final int LOAN_DAYS = 14;
    static final double FINE_PER_DAY = 5.0;
 
    Librarian librarian;
 
    Library(Librarian librarian) {
        this.librarian = librarian;
    }
 
    // ---------- Add Book ----------
    void addBook(String id, String title, String author) {
        books.add(new Book(id, title, author));
        System.out.println("Book added: " + title);
    }
 
    /* ---------- Search Book ---------- */
    void searchBook(String keyword) {
        System.out.println("\n--- Search results for \"" + keyword + "\" ---");
        boolean found = false;
        for (Book b : books) {
            if (b.title.toLowerCase().contains(keyword.toLowerCase())
                    || b.author.toLowerCase().contains(keyword.toLowerCase())
                    || b.id.equalsIgnoreCase(keyword)) {
                b.showBook();
                found = true;
            }
        }
        if (!found) System.out.println("No matching book found.");
    }
 
    /* ---------- Find Book Helper ---------- */
    Book findBook(String id) {
        for (Book b : books) {
            if (b.id.equalsIgnoreCase(id)) return b;
        }
        return null;
    }
 
    /* ---------- Book Availability ---------- */
    void checkAvailability(String id) {
        Book b = findBook(id);
        if (b == null) {
            System.out.println("Book not found.");
        } else if (b.issued) {
            System.out.println("Not available. Issued to " + b.issuedTo + " | Due: " + b.dueDate);
        } else {
            System.out.println("Book is available.");
        }
    }
 
    // ---------- Issue Book (needs Librarian approval) ----------
    void issueBook(String id, String studentName) {
        Book book = findBook(id);
 
        if (book == null) {
            System.out.println("Book not found.");
            return;
        }
        if (book.issued) {
            System.out.println("Book already issued to " + book.issuedTo);
            return;
        }
 
        // Ask librarian to approve
        if (!librarian.approveIssue()) {
            System.out.println("Issue request rejected by librarian.");
            return;
        }
 
        book.issued = true;
        book.issuedTo = studentName;
        book.issueDate = LocalDate.now();
        book.dueDate = book.issueDate.plusDays(LOAN_DAYS);
 
        issuedBooks.put(id, book);
 
        System.out.println("Book issued successfully!");
        System.out.println("Issue Date : " + book.issueDate);
        System.out.println("Due Date   : " + book.dueDate);
    }
 
    //---------- Fine Calculator ----------
    double calculateFine(LocalDate dueDate, LocalDate returnDate) {
        // toEpochDay() converts a date into a single day-count number
        // subtracting gives number of days between two dates (no ChronoUnit needed)
        long lateDays = returnDate.toEpochDay() - dueDate.toEpochDay();
 
        if (lateDays > 0) {
            return lateDays * FINE_PER_DAY;
        }
        return 0;
    }
 
    // ---------- Return Book ---------- 
    void returnBook(String id) {
        Book book = issuedBooks.get(id);
 
        if (book == null) {
            System.out.println("This book was not issued.");
            return;
        }
 
        LocalDate returnDate = LocalDate.now();
        double fine = calculateFine(book.dueDate, returnDate);
 
        System.out.println("Book returned: " + book.title);
 
        if (fine > 0) {
            System.out.println("Returned late! Fine = Rs." + fine);
            librarian.collectFine(book.issuedTo, fine);
        } else {
            System.out.println("Returned on time. No fine.");
        }
 
        // reset book status
        book.issued = false;
        book.issuedTo = null;
        book.issueDate = null;
        book.dueDate = null;
 
        issuedBooks.remove(id);
    }
 
    //---------- Due Date Reminder ----------
    void dueDateReminder() {
        System.out.println("\n--- Due Date Reminders ---");
 
        if (issuedBooks.isEmpty()) {
            System.out.println("No books are currently issued.");
            return;
        }
 
        LocalDate today = LocalDate.now();
 
        for (Book b : issuedBooks.values()) {
            long daysLeft = b.dueDate.toEpochDay() - today.toEpochDay();
 
            if (daysLeft < 0) {
                System.out.println(b.issuedTo + " -> \"" + b.title + "\" is OVERDUE by "
                        + (-daysLeft) + " day(s)!");
            } else if (daysLeft <= 3) {
                System.out.println(b.issuedTo + " -> \"" + b.title + "\" is due in "
                        + daysLeft + " day(s).");
            }
        }
    }
 
    // ---------- Generate Reports ---------- 
    void generateReport() {
        System.out.println("\n===== LIBRARY REPORT =====");
        System.out.println("Total Books     : " + books.size());
        System.out.println("Books Issued    : " + issuedBooks.size());
        System.out.println("Books Available : " + (books.size() - issuedBooks.size()));
 
        System.out.println("\n--- All Books ---");
        for (Book b : books) {
            b.showBook();
        }
        System.out.println("===========================");
    }
}
 
// ================= MAIN CLASS =================

public class LibraryManagementSystem {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
        Scanner sc = new Scanner(System.in);
        Librarian librarian = new Librarian("Mrs. Kavitha");
        Library library = new Library(librarian);
 
        // Preload a few books (anime titles)
        library.addBook("B1", "Naruto", "Masashi Kishimoto");
        library.addBook("B2", "One Piece", "Eiichiro Oda");
        library.addBook("B3", "Attack on Titan", "Hajime Isayama");
        library.addBook("B4", "Death Note", "Tsugumi Ohba");
 
        int choice;
 
        do {
            System.out.println("\n===== LIBRARY MANAGEMENT SYSTEM =====");
            System.out.println("1. Add Book");
            System.out.println("2. Search Book");
            System.out.println("3. Check Book Availability");
            System.out.println("4. Issue Book (Librarian Approval)");
            System.out.println("5. Return Book");
            System.out.println("6. Due Date Reminder");
            System.out.println("7. Generate Report");
            System.out.println("8. Exit");
            System.out.print("Enter choice: ");
            choice = sc.nextInt();
            sc.nextLine();
 
            switch (choice) {
 
                case 1:
                    System.out.print("Book ID: ");
                    String id = sc.nextLine();
                    System.out.print("Title: ");
                    String title = sc.nextLine();
                    System.out.print("Author: ");
                    String author = sc.nextLine();
                    library.addBook(id, title, author);
                    break;
 
                case 2:
                    System.out.print("Enter keyword to search: ");
                    library.searchBook(sc.nextLine());
                    break;
 
                case 3:
                    System.out.print("Enter Book ID: ");
                    library.checkAvailability(sc.nextLine());
                    break;
 
                case 4:
                    System.out.print("Enter Book ID to issue: ");
                    String issueId = sc.nextLine();
                    System.out.print("Enter Student Name: ");
                    String student = sc.nextLine();
                    library.issueBook(issueId, student);
                    break;
 
                case 5:
                    System.out.print("Enter Book ID to return: ");
                    library.returnBook(sc.nextLine());
                    break;
 
                case 6:
                    library.dueDateReminder();
                    break;
 
                case 7:
                    library.generateReport();
                    break;
 
                case 8:
                    System.out.println("Exiting... Thank you!");
                    break;
 
                default:
                    System.out.println("Invalid choice. Try again.");
            }
 
        } while (choice != 8);
 
        sc.close();

	}

}
