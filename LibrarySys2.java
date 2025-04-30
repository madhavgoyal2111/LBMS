import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

class Book implements Serializable {
    int id;
    String title, author;
    boolean isAvailable = true;
    LocalDateTime returnTime = null;

    public Book(int id, String title, String author) {
        this.id = id;
        this.title = title;
        this.author = author;
    }

    public void borrow() {
        if (isAvailable) {
            isAvailable = false;
            returnTime = LocalDateTime.now().plusDays(14);
        }
    }

    public void returnBook() {
        isAvailable = true;
        returnTime = null;
    }

    public String toString() {
        return id + ". " + title + " by " + author + " (" + (isAvailable ? "Available" : "Borrowed until " + returnTime) + ")";
    }
}

class Library implements Serializable {
    List<Book> books = new ArrayList<>();

    public void addBook(Book book) {
        for (Book b : books) {
            if (b.id == book.id) {
                throw new IllegalArgumentException("Book ID already exists! Choose a unique ID.");
            }
        }
        books.add(book);
    }

    public void removeBook(int id) {
        books.removeIf(book -> book.id == id);
    }

    public List<Book> getBooks() {
        return books;
    }

    public Book searchBook(int id) {
        for (Book book : books) {
            if (book.id == id) return book;
        }
        return null;
    }
}

class User implements Serializable {
    String username, password, role, name, email;

    public User(String username, String password, String role, String name, String email) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.name = name;
        this.email = email;
    }

    public boolean authenticate(String inputPassword) {
        return password.equals(inputPassword);
    }
}

public class LibrarySys2 extends Application {
    Library library = new Library();
    Map<String, User> users = new HashMap<>();
    final String USER_FILE = "users.dat";
    final String LIBRARY_FILE = "library.dat";
    Stage window;
    TextArea outputArea = new TextArea();
    User currentUser = null;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setTitle("Library Management System");
        loadUsers();
        loadLibrary();
        showLoginPage();
    }

    void saveUsers() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(USER_FILE))) {
            out.writeObject(users);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void loadUsers() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(USER_FILE))) {
            users = (Map<String, User>) in.readObject();
        } catch (Exception e) {
            users = new HashMap<>();
        }
    }

    void saveLibrary() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(LIBRARY_FILE))) {
            out.writeObject(library);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    void loadLibrary() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(LIBRARY_FILE))) {
            library = (Library) in.readObject();
        } catch (Exception e) {
            library = new Library();
        }
    }

    void showLoginPage() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        Button loginButton = new Button("Login");
        Button registerButton = new Button("Register");
        Label message = new Label();

        loginButton.setOnAction(e -> {
            String user = usernameField.getText();
            String pass = passwordField.getText();
            if (users.containsKey(user) && users.get(user).authenticate(pass)) {
                currentUser = users.get(user);
                if (currentUser.role.equals("librarian")) showLibrarianPage();
                else showBorrowerPage();
            } else {
                message.setText("Invalid credentials.");
            }
        });

        registerButton.setOnAction(e -> showRegistrationPage());

        layout.getChildren().addAll(new Label("Login"), usernameField, passwordField, loginButton, registerButton, message);
        window.setScene(new Scene(layout, 300, 250));
        window.show();
    }

    void showRegistrationPage() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        ChoiceBox<String> roleBox = new ChoiceBox<>();
        roleBox.getItems().addAll("librarian", "borrower");
        roleBox.setValue("borrower");
        Button registerButton = new Button("Register");
        Label message = new Label();

        registerButton.setOnAction(e -> {
            String username = usernameField.getText();
            if (!users.containsKey(username)) {
                users.put(username, new User(username, passwordField.getText(), roleBox.getValue(), nameField.getText(), emailField.getText()));
                saveUsers();
                showLoginPage();
            } else {
                message.setText("Username already exists.");
            }
        });

        layout.getChildren().addAll(new Label("Register"), nameField, emailField, usernameField, passwordField, roleBox, registerButton, message);
        window.setScene(new Scene(layout, 300, 350));
        window.show();
    }

    void showLibrarianPage() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        outputArea.setEditable(false);

        TextField idField = new TextField();
        idField.setPromptText("Book ID");
        TextField titleField = new TextField();
        titleField.setPromptText("Book Title");
        TextField authorField = new TextField();
        authorField.setPromptText("Author");

        Button addButton = new Button("Add Book");
        Button removeButton = new Button("Remove Book");
        Button listButton = new Button("List Books");
        Button logoutButton = new Button("Logout");

        addButton.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText());
                String title = titleField.getText();
                String author = authorField.getText();
                library.addBook(new Book(id, title, author));
                saveLibrary();
                outputArea.appendText("Book added.\n");
            } catch (IllegalArgumentException ex) {
                outputArea.appendText(ex.getMessage() + "\n");
            } catch (Exception ex) {
                outputArea.appendText("Invalid input.\n");
            }
        });

        removeButton.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText());
                library.removeBook(id);
                saveLibrary();
                outputArea.appendText("Book removed.\n");
            } catch (Exception ex) {
                outputArea.appendText("Invalid input.\n");
            }
        });

        listButton.setOnAction(e -> {
            outputArea.clear();
            for (Book book : library.getBooks()) {
                outputArea.appendText(book + "\n");
            }
        });

        logoutButton.setOnAction(e -> {
            currentUser = null;
            showLoginPage();
        });

        layout.getChildren().addAll(new Label("Librarian Dashboard"), idField, titleField, authorField,
                new HBox(5, addButton, removeButton, listButton, logoutButton), outputArea);
        window.setScene(new Scene(layout, 600, 400));
    }

    void showBorrowerPage() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));
        outputArea.setEditable(false);

        TextField idField = new TextField();
        idField.setPromptText("Book ID");
        Button borrowButton = new Button("Borrow Book");
        Button returnButton = new Button("Return Book");
        Button listButton = new Button("List Books");
        Button logoutButton = new Button("Logout");

        borrowButton.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText());
                Book book = library.searchBook(id);
                if (book != null && book.isAvailable) {
                    book.borrow();
                    saveLibrary();
                    outputArea.appendText("Borrowed until: " + book.returnTime + "\n");
                } else {
                    outputArea.appendText("Book unavailable.\n");
                }
            } catch (Exception ex) {
                outputArea.appendText("Invalid input.\n");
            }
        });

        returnButton.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText());
                Book book = library.searchBook(id);
                if (book != null && !book.isAvailable) {
                    outputArea.appendText("Returned on: " + LocalDateTime.now() + "\n");
                    book.returnBook();
                    saveLibrary();
                } else {
                    outputArea.appendText("Book not found or already available.\n");
                }
            } catch (Exception ex) {
                outputArea.appendText("Invalid input.\n");
            }
        });

        listButton.setOnAction(e -> {
            outputArea.clear();
            for (Book book : library.getBooks()) {
                outputArea.appendText(book + "\n");
            }
        });

        logoutButton.setOnAction(e -> {
            currentUser = null;
            showLoginPage();
        });

        layout.getChildren().addAll(new Label("Borrower Dashboard"), idField,
                new HBox(5, borrowButton, returnButton, listButton, logoutButton), outputArea);
        window.setScene(new Scene(layout, 600, 400));
    }
}