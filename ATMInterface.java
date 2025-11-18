import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * ATM demo with:
 *  - Login (user id + pin)
 *  - Transaction history
 *  - Withdraw
 *  - Deposit
 *  - Transfer
 *  - Quit
 *
 * Five classes: Main (public), ATM, Account, Transaction, Bank
 */

public class Main {
    public static void main(String[] args) {
        Bank bank = new Bank();
        // create demo accounts
        bank.addAccount(new Account("1001", "1234", 5000.00));
        bank.addAccount(new Account("1002", "4321", 3500.00));
        bank.addAccount(new Account("1003", "0000", 1000.00));

        ATM atm = new ATM(bank);
        atm.start(); // run the ATM console
    }
}

/*------------------ ATM ------------------*/
class ATM {
    private final Bank bank;
    private final Scanner sc = new Scanner(System.in);
    private Account currentAccount = null;

    public ATM(Bank bank) {
        this.bank = bank;
    }

    public void start() {
        System.out.println("==== Welcome to the Java ATM Interface ====");
        if (!login()) {
            System.out.println("Too many failed attempts. Exiting.");
            return;
        }

        while (true) {
            showMenu();
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> printTransactionHistory();
                case "2" -> withdraw();
                case "3" -> deposit();
                case "4" -> transfer();
                case "5" -> {
                    System.out.println("Thank you. Logging out.");
                    currentAccount = null;
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private boolean login() {
        int attempts = 0;
        while (attempts < 3) {
            System.out.print("Enter User ID: ");
            String id = sc.nextLine().trim();
            System.out.print("Enter PIN: ");
            String pin = sc.nextLine().trim();

            Account acc = bank.findAccountById(id);
            if (acc != null && acc.checkPin(pin)) {
                currentAccount = acc;
                System.out.println("Login successful. Welcome, User " + id + "!");
                return true;
            } else {
                attempts++;
                System.out.println("Invalid credentials. Attempts left: " + (3 - attempts));
            }
        }
        return false;
    }

    private void showMenu() {
        System.out.println("\n--- ATM Menu ---");
        System.out.println("1. Transaction History");
        System.out.println("2. Withdraw");
        System.out.println("3. Deposit");
        System.out.println("4. Transfer");
        System.out.println("5. Quit");
        System.out.print("Choose an option: ");
    }

    private void printTransactionHistory() {
        System.out.println("\n--- Transaction History ---");
        currentAccount.printTransactions();
    }

    private void withdraw() {
        System.out.print("Enter amount to withdraw: ");
        String s = sc.nextLine().trim();
        double amt;
        try {
            amt = Double.parseDouble(s);
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
            return;
        }
        if (amt <= 0) {
            System.out.println("Amount must be positive.");
            return;
        }
        boolean ok = currentAccount.withdraw(amt);
        if (ok) System.out.printf("Withdrawn %.2f. New balance: %.2f%n", amt, currentAccount.getBalance());
        else System.out.println("Insufficient balance.");
    }

    private void deposit() {
        System.out.print("Enter amount to deposit: ");
        String s = sc.nextLine().trim();
        double amt;
        try {
            amt = Double.parseDouble(s);
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
            return;
        }
        if (amt <= 0) {
            System.out.println("Amount must be positive.");
            return;
        }
        currentAccount.deposit(amt);
        System.out.printf("Deposited %.2f. New balance: %.2f%n", amt, currentAccount.getBalance());
    }

    private void transfer() {
        System.out.print("Enter recipient User ID: ");
        String toId = sc.nextLine().trim();
        Account toAcc = bank.findAccountById(toId);
        if (toAcc == null) {
            System.out.println("Recipient account not found.");
            return;
        }
        System.out.print("Enter amount to transfer: ");
        String s = sc.nextLine().trim();
        double amt;
        try {
            amt = Double.parseDouble(s);
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
            return;
        }
        if (amt <= 0) {
            System.out.println("Amount must be positive.");
            return;
        }
        if (currentAccount.transferTo(toAcc, amt)) {
            System.out.printf("Transferred %.2f to %s. New balance: %.2f%n", amt, toId, currentAccount.getBalance());
        } else {
            System.out.println("Transfer failed (insufficient funds).");
        }
    }
}

/*------------------ Bank ------------------*/
class Bank {
    private final Map<String, Account> accounts = new HashMap<>();

    public void addAccount(Account acc) {
        accounts.put(acc.getUserId(), acc);
    }

    public Account findAccountById(String id) {
        return accounts.get(id);
    }
}

/*------------------ Account ------------------*/
class Account {
    private final String userId;
    private final String pin;
    private double balance;
    private final List<Transaction> transactions = new ArrayList<>();

    public Account(String userId, String pin, double initialBalance) {
        this.userId = userId;
        this.pin = pin;
        this.balance = initialBalance;
        transactions.add(new Transaction("Account Opened", initialBalance, "Initial balance"));
    }

    public String getUserId() {
        return userId;
    }

    public boolean checkPin(String attemptedPin) {
        return this.pin.equals(attemptedPin);
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        balance += amount;
        transactions.add(new Transaction("Deposit", amount, "Self deposit"));
    }

    public boolean withdraw(double amount) {
        if (amount <= balance) {
            balance -= amount;
            transactions.add(new Transaction("Withdraw", amount, "Cash withdrawal"));
            return true;
        }
        return false;
    }

    public boolean transferTo(Account toAccount, double amount) {
        if (amount <= balance) {
            balance -= amount;
            toAccount.balance += amount;
            transactions.add(new Transaction("Transfer Out", amount, "To user " + toAccount.userId));
            toAccount.transactions.add(new Transaction("Transfer In", amount, "From user " + this.userId));
            return true;
        }
        return false;
    }

    public void printTransactions() {
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }
        System.out.printf("%-20s %-12s %-10s %-30s%n", "Date & Time", "Type", "Amount", "Details");
        System.out.println("--------------------------------------------------------------------------------");
        for (Transaction t : transactions) {
            System.out.printf("%-20s %-12s %-10.2f %-30s%n",
                    t.getTimestamp(), t.getType(), t.getAmount(), t.getDetails());
        }
        System.out.printf("Current balance: %.2f%n", balance);
    }
}

/*------------------ Transaction ------------------*/
class Transaction {
    private final String type;
    private final double amount;
    private final String details;
    private final String timestamp;

    public Transaction(String type, double amount, String details) {
        this.type = type;
        this.amount = amount;
        this.details = details;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getDetails() { return details; }
    public String getTimestamp() { return timestamp; }
}
