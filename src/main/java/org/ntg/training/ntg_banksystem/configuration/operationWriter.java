package org.ntg.training.ntg_banksystem.configuration;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ntg.training.ntg_banksystem.entity.Account;
import org.ntg.training.ntg_banksystem.entity.Customer;
import org.ntg.training.ntg_banksystem.entity.Transaction;
import org.ntg.training.ntg_banksystem.repository.AccountRepository;
import org.ntg.training.ntg_banksystem.repository.CustomerRepository;
import org.ntg.training.ntg_banksystem.repository.TransactionRepository;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class operationWriter implements ItemWriter<Account> {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public void write(Chunk<? extends Account> accounts) throws Exception {
        accountRepository.saveAll(accounts.getItems());
        log.info("Updated balances for {} account(s).", accounts.getItems().size());

        List<Customer> customers = customerRepository.findAll();

        for (Customer customer : customers) {
            String middleName = customer.getMiddleName() != null ? customer.getMiddleName() : "";
            String fileName = customer.getFirstName() + middleName + customer.getLastName() + customer.getCustomerId() + "_statement.pdf";
            generateCustomerPdf(customer, fileName);
        }
    }

    private void generateCustomerPdf(Customer customer, String fileName) {
        try {
            String folderPath = "D:/NTG_BankSystem/src/main/resources/customerTXT";
            Files.createDirectories(Path.of(folderPath));
            String filePath = folderPath + "/" + fileName;

            Document document = new Document(PageSize.A4, 50, 50, 30, 30);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            try {
                PdfPTable table = new PdfPTable(2);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{1, 1});

                PdfPCell logoCell = new PdfPCell();
                logoCell.setBorder(Rectangle.NO_BORDER);
                Image logo = Image.getInstance("D:/NTG_BankSystem/src/main/resources/logo.jpg");
                logo.scaleToFit(100, 100);
                logo.setAlignment(Element.ALIGN_LEFT);
                logoCell.addElement(logo);
                table.addCell(logoCell);


                PdfPCell textCell = new PdfPCell();
                textCell.setBorder(Rectangle.NO_BORDER);
                textCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                Font textFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
                Paragraph bankInfo = new Paragraph("NTG Bank\n1060 West Addison St.\nChicago, IL 60613", textFont);
                bankInfo.setAlignment(Element.ALIGN_RIGHT);
                textCell.addElement(bankInfo);
                table.addCell(textCell);

                document.add(table);
            } catch (Exception e) {
                log.warn("Could not load logo or text: {}", e.getMessage());
            }

            Paragraph space = new Paragraph();
            space.setSpacingBefore(20f);
            document.add(space);

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, new BaseColor(140, 0, 0));
            Paragraph title = new Paragraph("Account Statement", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("\nCustomer Details:", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
            document.add(new Paragraph(" " + customer.getFirstName() + " " + customer.getLastName()));
            document.add(new Paragraph( customer.getAddress1()));
            document.add(new Paragraph(customer.getCity() + ", " + customer.getState() + " " + customer.getPostalCode()));
            document.add(new Paragraph("\n"));

            List<Account> customerAccounts = accountRepository.findByCustomerAccountId(customer.getCustomerId());

            for (Account account : customerAccounts) {
                document.add(new Paragraph("Account ID: " + account.getAccountId(), new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));

                LocalDate currentDate = LocalDate.now();
                LocalDate firstDateOfMonth = currentDate.withDayOfMonth(1);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                document.add(new Paragraph("Statement Period: " + firstDateOfMonth.format(formatter) + " - " + currentDate.format(formatter)));
                document.add(new Paragraph("\n"));

                PdfPTable table = new PdfPTable(3);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{30, 50, 20});

                BaseColor headerColor = new BaseColor(140, 0, 0);
                addTableHeader(table, new String[]{"Date", "Description", "Amount"}, headerColor);

                List<Transaction> transactions = transactionRepository.findByAccountId(account.getAccountId());
                for (Transaction transaction : transactions) {
                    addTransactionRow(table, transaction);
                }

                document.add(table);
                document.add(new Paragraph("\n"));

                document.add(createSummaryTable(transactions, account.getBalance()));

                document.add(new Paragraph("\n-------------------------------------------------------------------------------------------------------------------------\n"));
            }

            document.close();
            log.info("PDF file generated successfully at: {}", filePath);

        } catch (Exception e) {
            log.error("Error generating PDF file: {}", e.getMessage(), e);
        }
    }

    private void addTableHeader(PdfPTable table, String[] headers, BaseColor backgroundColor) {
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(backgroundColor);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            table.addCell(cell);
        }
    }

    private void addTransactionRow(PdfPTable table, Transaction transaction) {
        double amount = transaction.getCredit() - transaction.getDebit();

        table.addCell(new PdfPCell(new Phrase(transaction.getTimestamp().toString())));
        table.addCell(new PdfPCell(new Phrase(transaction.getDescription())));
        table.addCell(new PdfPCell(new Phrase(String.format("%.2f", amount))));
    }

    private PdfPTable createSummaryTable(List<Transaction> transactions, double balance) {
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(50);
        summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

        double totalDebit = transactions.stream().mapToDouble(Transaction::getDebit).sum();
        double totalCredit = transactions.stream().mapToDouble(Transaction::getCredit).sum();

        summaryTable.addCell(getSummaryCell("Total Debit:"));
        summaryTable.addCell(getSummaryCell(String.format("%.2f", totalDebit)));

        summaryTable.addCell(getSummaryCell("Total Credit:"));
        summaryTable.addCell(getSummaryCell(String.format("%.2f", totalCredit)));

        summaryTable.addCell(getSummaryCell("Balance:"));
        summaryTable.addCell(getSummaryCell(String.format("%.2f", balance)));

        return summaryTable;
    }
    private PdfPCell getSummaryCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

}



//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class operationWriter implements ItemWriter<Account> {
//
//    private final AccountRepository accountRepository;
//    private final CustomerRepository customerRepository;
//    private final TransactionRepository transactionRepository;
//
//    @Override
//    public void write(Chunk<? extends Account> accounts) throws Exception {
//        // Save all accounts
//        accountRepository.saveAll(accounts.getItems());
//        log.info("Updated balances for {} account(s).", accounts.getItems().size());
//        log.info("//////////////////////////////////////////////////////////////////");
//
//        // Get all customers
//        List<Customer> customers = customerRepository.findAll();
//
//        // Process each customer
//        for (Customer customer : customers) {
//            StringBuilder content = new StringBuilder();
//
//            // Add customer header to the file
//            content.append(String.format("%-20s%60s%n",
//                    customer.getFirstName() + " " + customer.getLastName(),
//                    "NTG Bank"));
//            content.append(String.format("%-20s%70s%n",
//                    customer.getAddress1(),
//                    "1060 West Addison St."));
//            content.append(String.format("%-20s%60s%n",
//                    customer.getCity() + ", " + customer.getState() + " " + customer.getPostalCode(),
//                    "Chicago, IL 60613"));
//            content.append("\n");
//
//            List<Account> customerAccounts = accountRepository.findByCustomerAccountId(customer.getCustomerId());
//
//            for (Account account : customerAccounts) {
//                content.append("Account ID: ").append(account.getAccountId()).append("\n");
//                content.append("Your Account Summary\n");
//
//                LocalDate currentDate = LocalDate.now();
//                LocalDate firstDateOfMonth = currentDate.withDayOfMonth(1);
//                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//                String formattedStartDate = firstDateOfMonth.format(formatter);
//                String formattedEndDate = currentDate.format(formatter);
//
//                content.append(String.format("%-20s%60s%n",
//                        "Statement Period: ",
//                        formattedStartDate + " - " + formattedEndDate));
//                content.append("\n");
//
//                List<Transaction> transactionList = transactionRepository.findByAccountId(account.getAccountId());
//
//                for (Transaction transaction : transactionList) {
//                    double debit = transaction.getDebit();
//                    double credit = transaction.getCredit();
//
//                    content.append(String.format("%-15s%-30s%-70s%n",
//                            transaction.getTimestamp(),
//                            "          " + transaction.getDescription(),
//                            debit > 0 ? -debit : credit));
//                }
//
//                double totalDebit = transactionList.stream()
//                        .mapToDouble(Transaction::getDebit)
//                        .sum();
//                double totalCredit = transactionList.stream()
//                        .mapToDouble(Transaction::getCredit)
//                        .sum();
//
//                content.append("\n");
//                content.append(String.format("%-20s%50f%n", "Total Debit: ", totalDebit));
//                content.append(String.format("%-20s%50f%n", "Total Credit: ", totalCredit));
//                content.append(String.format("%-20s%50f%n", "Balance: ", account.getBalance()));
//                content.append("\n");
//                content.append("------------------------------------------------------------\n");
//            }
//
//            String fileName = customer.getFirstName() + customer.getMiddleName() + customer.getLastName() + "_statement.txt";
//            writeToFile(fileName, content.toString());
//        }
//    }
//
//    private void writeToFile(String fileName, String content) {
//        try {
//            String folderPath = "D:/NTG_BankSystem/src/main/resources/customerTXT";
//            Path folder = Path.of(folderPath);
//            Files.createDirectories(folder);
//            String filePath = folderPath + "/" + fileName;
//            try (FileWriter writer = new FileWriter(filePath)) {
//                writer.write(content);
//            }
//
//            System.out.println("File written successfully to: " + filePath);
//
//        } catch (IOException e) {
//            System.err.println("An error occurred while writing to file: " + e.getMessage());
//        }
//    }
//}