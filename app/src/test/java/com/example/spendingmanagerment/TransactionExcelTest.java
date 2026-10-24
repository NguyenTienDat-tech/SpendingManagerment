package com.example.spendingmanagerment;

import com.example.spendingmanagerment.core.utils.DateUtils;
import com.example.spendingmanagerment.data.local.entity.TransactionEntity;
import com.example.spendingmanagerment.data.local.excel.TransactionExcel;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.junit.Test;
import static org.junit.Assert.*;

public class TransactionExcelTest {
    private final TransactionExcel excel = new TransactionExcel();

    @Test public void roundTripPreservesIdsVndUnicodeAndXmlCharacters() throws Exception {
        TransactionEntity original = new TransactionEntity(9_007_199_254_740_993L, 999_999_999_999L,
                DateUtils.startOfDay(LocalDate.of(2028, 2, 29)), "EXPENSE", "rent", "cash", "Thú cưng 🐱 & <ghi chú>\nDòng 2");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        excel.write(output, Collections.singletonList(original));
        List<TransactionEntity> imported = excel.read(new ByteArrayInputStream(output.toByteArray()));
        assertEquals(1, imported.size());
        TransactionEntity actual = imported.get(0);
        assertEquals(original.id, actual.id);
        assertEquals(original.amount, actual.amount);
        assertEquals(original.date, actual.date);
        assertEquals(original.note, actual.note);
        assertEquals(original.type, actual.type);
        assertEquals(original.category, actual.category);
    }

    @Test public void emptyWorkbookExportsHeaderAndCanBeImported() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        excel.write(output, Collections.emptyList());
        assertTrue(excel.read(new ByteArrayInputStream(output.toByteArray())).isEmpty());
    }

    @Test public void importsSharedStringsAndNegativeExpenseFromOriginalProject() throws Exception {
        byte[] workbook = legacyWorkbook("-250000", "Fri Oct 23 00:00:00 GMT+07:00 2026");
        TransactionEntity actual = excel.read(new ByteArrayInputStream(workbook)).get(0);
        assertEquals(7, actual.id);
        assertEquals(250000, actual.amount);
        assertEquals("rent", actual.category);
        assertEquals("cash", actual.account);
        assertEquals(LocalDate.of(2026, 10, 23), DateUtils.fromMillis(actual.date));
    }

    @Test public void importsExcelSerialDates() throws Exception {
        TransactionEntity actual = excel.read(new ByteArrayInputStream(legacyWorkbook("1000", "1"))).get(0);
        assertEquals(LocalDate.of(1900, 1, 1), DateUtils.fromMillis(actual.date));
    }

    @Test public void fractionalVndAndDuplicateIdsAreRejected() throws Exception {
        assertThrows(IOException.class, () -> excel.read(new ByteArrayInputStream(legacyWorkbook("1.25", "2026-10-23"))));
        TransactionEntity transaction = new TransactionEntity(1, 100, 0, "INCOME", "salary", "cash", "");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        excel.write(output, Arrays.asList(transaction, transaction));
        assertThrows(IOException.class, () -> excel.read(new ByteArrayInputStream(output.toByteArray())));
    }

    @Test public void arbitraryFileIsRejected() {
        assertThrows(IOException.class, () -> excel.read(new ByteArrayInputStream("not an xlsx file".getBytes(StandardCharsets.UTF_8))));
    }

    private byte[] legacyWorkbook(String amount, String date) throws Exception {
        ByteArrayOutputStream base = new ByteArrayOutputStream();
        excel.write(base, Collections.emptyList());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(output); ZipInputStream input = new ZipInputStream(new ByteArrayInputStream(base.toByteArray()))) {
            ZipEntry part;
            while ((part = input.getNextEntry()) != null) {
                zip.putNextEntry(new ZipEntry(part.getName()));
                ByteArrayOutputStream content = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int length;
                while ((length = input.read(buffer)) != -1) content.write(buffer, 0, length);
                String text = content.toString("UTF-8");
                if (part.getName().endsWith("sheet1.xml")) {
                    String row = "<row r=\"2\"><c r=\"A2\"><v>7</v></c><c r=\"B2\" t=\"s\"><v>0</v></c>"
                            + "<c r=\"C2\" t=\"s\"><v>1</v></c><c r=\"D2\" t=\"s\"><v>2</v></c>"
                            + "<c r=\"F2\" t=\"inlineStr\"><is><t>" + date + "</t></is></c><c r=\"G2\"><v>" + amount + "</v></c></row>";
                    text = text.replace("</sheetData>", row + "</sheetData>");
                }
                zip.write(text.getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
            zip.putNextEntry(new ZipEntry("xl/sharedStrings.xml"));
            zip.write(("<sst xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
                    + "<si><t>EXPENSE</t></si><si><t>Rent</t></si><si><t>Cash</t></si></sst>").getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        return output.toByteArray();
    }
}
