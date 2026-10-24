package com.example.spendingmanagerment.data.local.excel;

import com.example.spendingmanagerment.core.utils.DateUtils;
import com.example.spendingmanagerment.data.local.entity.TransactionEntity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/** Reads/writes the seven transaction columns as standard .xlsx using Android's XML/ZIP APIs. */
public class TransactionExcel {
    private static final String[] HEADERS = {"ID", "Type", "Category", "Account", "Note", "Date", "Amount"};
    private static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static final String SHEET_NS = "http://schemas.openxmlformats.org/spreadsheetml/2006/main";
    private static final long MAX_BYTES = 32L * 1024 * 1024;
    private static final int MAX_ROWS = 50_000;

    @Inject public TransactionExcel() { }

    public void write(OutputStream output, List<TransactionEntity> transactions) throws IOException {
        if (transactions.size() > MAX_ROWS) throw new IOException("Too many rows");
        try (ZipOutputStream zip = new ZipOutputStream(new FilterOutputStream(output) {
            @Override public void close() throws IOException { flush(); }
        })) {
            entry(zip, "[Content_Types].xml", XML + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                    + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                    + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
                    + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
                    + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/></Types>");
            entry(zip, "_rels/.rels", XML + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/></Relationships>");
            entry(zip, "xl/workbook.xml", XML + "<workbook xmlns=\"" + SHEET_NS + "\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                    + "<sheets><sheet name=\"Transactions\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>");
            entry(zip, "xl/_rels/workbook.xml.rels", XML + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                    + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/></Relationships>");
            zip.putNextEntry(new ZipEntry("xl/worksheets/sheet1.xml"));
            writeXml(zip, XML + "<worksheet xmlns=\"" + SHEET_NS + "\"><sheetData><row r=\"1\">");
            for (int column = 0; column < HEADERS.length; column++) cell(zip, column, 1, HEADERS[column], false);
            writeXml(zip, "</row>");
            int row = 2;
            for (TransactionEntity transaction : transactions) {
                writeXml(zip, "<row r=\"" + row + "\">");
                cell(zip, 0, row, Long.toString(transaction.id), false);
                cell(zip, 1, row, transaction.type, false);
                cell(zip, 2, row, transaction.category, false);
                cell(zip, 3, row, transaction.account, false);
                cell(zip, 4, row, transaction.note, false);
                cell(zip, 5, row, DateUtils.fromMillis(transaction.date).toString(), false);
                cell(zip, 6, row, Long.toString(transaction.amount), true);
                writeXml(zip, "</row>");
                row++;
            }
            writeXml(zip, "</sheetData></worksheet>");
            zip.closeEntry();
        }
    }

    public List<TransactionEntity> read(InputStream input) throws IOException {
        Map<String, byte[]> files = unzip(input);
        Document workbook = parse(files.get("xl/workbook.xml"));
        NodeList sheets = workbook.getElementsByTagNameNS("*", "sheet");
        if (sheets.getLength() == 0) throw new IOException("Workbook has no sheet");
        String relationship = ((Element) sheets.item(0)).getAttributeNS(
                "http://schemas.openxmlformats.org/officeDocument/2006/relationships", "id");
        Document relationships = parse(files.get("xl/_rels/workbook.xml.rels"));
        NodeList links = relationships.getElementsByTagNameNS("*", "Relationship");
        String sheetPath = null;
        for (int i = 0; i < links.getLength(); i++) {
            Element link = (Element) links.item(i);
            if (relationship.equals(link.getAttribute("Id"))) {
                String target = link.getAttribute("Target");
                sheetPath = target.startsWith("/") ? target.substring(1) : "xl/" + target;
            }
        }
        List<String> strings = new ArrayList<>();
        if (files.containsKey("xl/sharedStrings.xml")) {
            NodeList items = parse(files.get("xl/sharedStrings.xml")).getElementsByTagNameNS("*", "si");
            for (int i = 0; i < items.getLength(); i++) strings.add(textRuns((Element) items.item(i)));
        }
        NodeList properties = workbook.getElementsByTagNameNS("*", "workbookPr");
        boolean date1904 = properties.getLength() > 0 && ("1".equals(((Element) properties.item(0)).getAttribute("date1904"))
                || "true".equals(((Element) properties.item(0)).getAttribute("date1904")));
        NodeList rows = parse(files.get(sheetPath)).getElementsByTagNameNS("*", "row");
        if (rows.getLength() > MAX_ROWS + 1) throw new IOException("Too many rows");
        List<TransactionEntity> transactions = new ArrayList<>();
        Set<Long> ids = new HashSet<>();
        boolean hasHeader = false;
        for (int i = 0; i < rows.getLength(); i++) {
            String[] values = rowValues((Element) rows.item(i), strings);
            boolean blank = true;
            for (String value : values) if (!value.trim().isEmpty()) blank = false;
            if (blank) continue;
            if (!hasHeader) {
                for (int column = 0; column < HEADERS.length; column++) {
                    if (!HEADERS[column].equalsIgnoreCase(values[column].trim())) throw new IOException("Unexpected columns");
                }
                hasHeader = true;
                continue;
            }
            try {
                long id = new BigDecimal(values[0].trim()).longValueExact();
                String type = values[1].trim().toUpperCase(Locale.ROOT);
                long amount = new BigDecimal(values[6].trim()).longValueExact();
                // The original Spending Management workbook stores expenses as negative values.
                if (TransactionEntity.EXPENSE.equals(type) && amount < 0) amount = Math.negateExact(amount);
                if (id <= 0 || !ids.add(id) || amount <= 0 || amount > 999_999_999_999L
                        || !(TransactionEntity.INCOME.equals(type) || TransactionEntity.EXPENSE.equals(type))
                        || values[2].trim().isEmpty() || values[3].trim().isEmpty() || values[4].length() > 500) {
                    throw new IllegalArgumentException("Invalid transaction");
                }
                LocalDate date = readDate(values[5].trim(), date1904);
                transactions.add(new TransactionEntity(id, amount, DateUtils.startOfDay(date), type,
                        categoryCode(values[2].trim()), accountCode(values[3].trim()), values[4]));
            } catch (RuntimeException exception) {
                throw new IOException("Invalid transaction at row " + (i + 1), exception);
            }
        }
        if (!hasHeader) throw new IOException("Missing header");
        return transactions;
    }

    private static LocalDate readDate(String value, boolean date1904) {
        try { return LocalDate.parse(value); }
        catch (java.time.format.DateTimeParseException ignored) { }
        try {
            return ZonedDateTime.parse(value, DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)).toLocalDate();
        } catch (java.time.format.DateTimeParseException ignored) { }
        long serial = new BigDecimal(value).setScale(0, java.math.RoundingMode.FLOOR).longValueExact();
        if (!date1904 && serial == 60) throw new IllegalArgumentException("Excel's fictitious leap day");
        LocalDate base = date1904 ? LocalDate.of(1904, 1, 1)
                : serial < 60 ? LocalDate.of(1899, 12, 31) : LocalDate.of(1899, 12, 30);
        return base.plusDays(serial);
    }

    private static String categoryCode(String value) {
        String[] english = {"Salary", "Business", "Investment", "Loan", "Rent", "Other"};
        String[] vietnamese = {"Lương", "Kinh doanh", "Đầu tư", "Khoản vay", "Thuê nhà", "Khác"};
        return optionCode(value, english, vietnamese, com.example.spendingmanagerment.core.utils.TransactionOptions.CATEGORIES);
    }

    private static String accountCode(String value) {
        String[] english = {"Cash", "Bank", "MB Bank", "Viettel Money", "Other"};
        String[] vietnamese = {"Tiền mặt", "Ngân hàng", "MB Bank", "Viettel Money", "Khác"};
        if ("Viettel Monney".equalsIgnoreCase(value)) return "viettel_money";
        return optionCode(value, english, vietnamese, com.example.spendingmanagerment.core.utils.TransactionOptions.ACCOUNTS);
    }

    private static String optionCode(String value, String[] english, String[] vietnamese, String[] codes) {
        for (int i = 0; i < codes.length; i++) {
            if (value.equalsIgnoreCase(codes[i]) || value.equalsIgnoreCase(english[i]) || value.equalsIgnoreCase(vietnamese[i])) return codes[i];
        }
        return value;
    }

    private static String[] rowValues(Element row, List<String> strings) throws IOException {
        String[] values = {"", "", "", "", "", "", ""};
        NodeList cells = row.getElementsByTagNameNS("*", "c");
        for (int i = 0; i < cells.getLength(); i++) {
            Element cell = (Element) cells.item(i);
            String reference = cell.getAttribute("r");
            if (!reference.matches("[A-G][0-9]+")) continue;
            int column = reference.charAt(0) - 'A';
            if (cell.getElementsByTagNameNS("*", "f").getLength() > 0) throw new IOException("Formula cells are unsupported");
            NodeList number = cell.getElementsByTagNameNS("*", "v");
            String raw = number.getLength() == 0 ? "" : number.item(0).getTextContent();
            if ("inlineStr".equals(cell.getAttribute("t"))) values[column] = textRuns(cell);
            else if ("s".equals(cell.getAttribute("t"))) {
                try { values[column] = strings.get(Integer.parseInt(raw)); }
                catch (RuntimeException exception) { throw new IOException("Invalid shared string", exception); }
            } else values[column] = raw;
        }
        return values;
    }

    private static String textRuns(Element element) {
        StringBuilder text = new StringBuilder();
        NodeList runs = element.getElementsByTagNameNS("*", "t");
        for (int i = 0; i < runs.getLength(); i++) text.append(runs.item(i).getTextContent());
        return text.toString();
    }

    private static Map<String, byte[]> unzip(InputStream input) throws IOException {
        Map<String, byte[]> files = new HashMap<>();
        long total = 0;
        int count = 0;
        byte[] buffer = new byte[8192];
        try (ZipInputStream zip = new ZipInputStream(new FilterInputStream(input) {
            @Override public void close() { }
        })) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (++count > 2048) throw new IOException("Too many ZIP entries");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                int length;
                while ((length = zip.read(buffer)) != -1) {
                    total += length;
                    if (total > MAX_BYTES) throw new IOException("Workbook is too large");
                    if ((entry.getName().startsWith("xl/") && entry.getName().endsWith(".xml"))
                            || "xl/_rels/workbook.xml.rels".equals(entry.getName())) bytes.write(buffer, 0, length);
                }
                if (bytes.size() > 0) files.put(entry.getName(), bytes.toByteArray());
                zip.closeEntry();
            }
        }
        return files;
    }

    private static Document parse(byte[] data) throws IOException {
        if (data == null) throw new IOException("Missing workbook part");
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));
            return builder.parse(new ByteArrayInputStream(data));
        } catch (Exception exception) { throw new IOException("Invalid workbook XML", exception); }
    }

    private static void entry(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        writeXml(zip, content);
        zip.closeEntry();
    }

    private static void cell(OutputStream output, int column, int row, String value, boolean numeric) throws IOException {
        String reference = String.valueOf((char) ('A' + column)) + row;
        writeXml(output, numeric ? "<c r=\"" + reference + "\"><v>" + value + "</v></c>"
                : "<c r=\"" + reference + "\" t=\"inlineStr\"><is><t xml:space=\"preserve\">" + escape(value) + "</t></is></c>");
    }

    private static String escape(String value) {
        StringBuilder clean = new StringBuilder();
        value.codePoints().filter(c -> c == 9 || c == 10 || c == 13 || c >= 32 && c <= 0xD7FF
                || c >= 0xE000 && c <= 0xFFFD || c >= 0x10000 && c <= 0x10FFFF).forEach(clean::appendCodePoint);
        return clean.toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\r", "&#13;");
    }

    private static void writeXml(OutputStream output, String xml) throws IOException {
        output.write(xml.getBytes(StandardCharsets.UTF_8));
    }
}
