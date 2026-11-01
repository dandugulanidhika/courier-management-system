import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    static final String IN_PARCELS = "input/parcels.txt";
    static final String IN_LOOKUP = "input/lookup.txt";
    static final String OUT_SLIPS = "output/charge_slips.txt";
    static final String OUT_ERRORS = "output/errors.txt";

    static Courier[] store = new Courier[0];
    static int[] realHours = new int[0];
    static int[] srcLine = new int[0];
    static String[] srcText = new String[0];

    static int seen = 0;
    static int okCount = 0;
    static int badCount = 0;

    public static void main(String[] args) {
        new File("output").mkdirs();

        try (BufferedWriter errLog = new BufferedWriter(new FileWriter(OUT_ERRORS, true));
             BufferedWriter report = new BufferedWriter(new FileWriter(OUT_SLIPS))) {

            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            errLog.write("===== Run at " + stamp + " =====");
            errLog.newLine();

            report.write("COURIER CHARGE REPORT - " + Courier.courierCompany);
            report.newLine();
            report.newLine();

            try {
                loadParcels(errLog);
                writeSlips(report, errLog);
            } catch (FileNotFoundException | EmptyInputFileException ex) {
                logError(errLog, "parcels.txt", 0, IN_PARCELS, ex);
                report.write("Parcel file could not be processed: " + ex.getMessage());
                report.newLine();
            }

            try {
                doLookups(report, errLog);
            } catch (FileNotFoundException | EmptyInputFileException ex) {
                logError(errLog, "lookup.txt", 0, IN_LOOKUP, ex);
                report.write("Lookup file could not be processed: " + ex.getMessage());
                report.newLine();
            }

            report.newLine();
            report.write("Final parcelCount: " + Courier.getParcelCount());
            report.newLine();
            report.write("Records read: " + seen + ", accepted: " + okCount + ", rejected: " + badCount);
            report.newLine();

            report.flush();
            errLog.flush();
            System.out.println("Processing complete. See output folder.");
        } catch (IOException ex) {
            System.out.println("Processing failed: " + ex.getMessage());
        }
    }

    static void verifyFile(File file) throws FileNotFoundException, EmptyInputFileException {
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            throw new FileNotFoundException("Input file missing or unreadable: " + file.getPath());
        }
        if (file.length() == 0) {
            throw new EmptyInputFileException("Input file is empty: " + file.getPath());
        }
    }

    static boolean ignorable(String s) {
        return s.isEmpty() || s.startsWith("#");
    }

    static int fetchN(BufferedReader reader, int[] counter) throws IOException, EmptyInputFileException {
        String raw;
        while ((raw = reader.readLine()) != null) {
            counter[0]++;
            String cleaned = raw.trim();
            if (ignorable(cleaned)) continue;
            try {
                int n = Integer.parseInt(cleaned);
                if (n <= 0) throw new EmptyInputFileException("N is zero or negative: " + n);
                return n;
            } catch (NumberFormatException ex) {
                throw new EmptyInputFileException("N is missing or not a number: '" + cleaned + "'", ex);
            }
        }
        throw new EmptyInputFileException("N is missing: no record count found");
    }

    static void loadParcels(BufferedWriter errLog) throws IOException, EmptyInputFileException {
        File src = new File(IN_PARCELS);
        verifyFile(src);

        try (BufferedReader reader = new BufferedReader(new FileReader(src))) {
            int[] ln = {0};
            int n = fetchN(reader, ln);
            store = new Courier[n];
            realHours = new int[n];
            srcLine = new int[n];
            srcText = new String[n];

            String raw;
            while (seen < n && (raw = reader.readLine()) != null) {
                ln[0]++;
                String rec = raw.trim();
                if (ignorable(rec)) continue;

                int slot = seen;
                try {
                    int[] tmp = new int[1];
                    store[slot] = buildFromRecord(rec, tmp);
                    realHours[slot] = tmp[0];
                    srcLine[slot] = ln[0];
                    srcText[slot] = rec;
                    okCount++;
                } catch (InvalidWeightException | InvalidDistanceException | InvalidPriorityFeeException
                         | InvalidDeliveryHoursException | InvalidRecordException ex) {
                    badCount++;
                    logError(errLog, "parcels.txt", ln[0], rec, ex);
                } catch (Exception ex) {
                    badCount++;
                    logError(errLog, "parcels.txt", ln[0], rec, ex);
                } finally {
                    seen++;
                }
            }
        }
    }

    static Courier buildFromRecord(String rec, int[] hoursOut)
            throws InvalidRecordException, InvalidWeightException, InvalidDistanceException,
                   InvalidPriorityFeeException, InvalidDeliveryHoursException {
        String[] parts = rec.split(",", -1);
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }

        try {
            switch (parts[0].toUpperCase()) {
                case "S":
                    needFields(parts, 5);
                    needId(parts[1]);
                    return new Courier(parts[1], Double.parseDouble(parts[2]),
                                       Double.parseDouble(parts[3]), toBool(parts[4]));
                case "X":
                    needFields(parts, 8);
                    needId(parts[1]);
                    hoursOut[0] = Integer.parseInt(parts[7]);
                    return new ExpressCourier(parts[1], Double.parseDouble(parts[2]),
                                              Double.parseDouble(parts[3]), toBool(parts[4]),
                                              Integer.parseInt(parts[5]), Double.parseDouble(parts[6]));
                default:
                    throw new InvalidRecordException("Unknown type code '" + parts[0] + "'");
            }
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            throw new InvalidRecordException("Malformed record: " + ex.getMessage(), ex);
        }
    }

    static void needFields(String[] parts, int want) throws InvalidRecordException {
        if (parts.length != want) {
            throw new InvalidRecordException("Expected " + want + " fields but found " + parts.length);
        }
    }

    static void needId(String id) throws InvalidRecordException {
        if (id.isEmpty()) throw new InvalidRecordException("Parcel ID is empty");
    }

    static boolean toBool(String text) throws InvalidRecordException {
        if (text.equalsIgnoreCase("true")) return true;
        if (text.equalsIgnoreCase("false")) return false;
        throw new InvalidRecordException("insured must be true or false but was '" + text + "'");
    }

    static void writeSlips(BufferedWriter report, BufferedWriter errLog) throws IOException {
        Courier one = null;
        Courier two = null;

        report.write("=========== CHARGE SLIPS ===========");
        report.newLine();

        for (int i = 0; i < store.length; i++) {
            Courier item = store[i];
            if (item == null) continue;

            if (one == null) {
                one = item;
            } else if (two == null) {
                two = item;
            }

            report.write("----------------------------------------");
            report.newLine();
            item.display(report);
            report.write("Total charge    : Rs. " + fmt(item.calculateCharge()));
            report.newLine();

            if (item instanceof ExpressCourier) {
                ExpressCourier exp = (ExpressCourier) item;
                try {
                    double back = exp.calculateRefund(realHours[i]);
                    report.write("Actual hours    : " + realHours[i]);
                    report.newLine();
                    report.write("Delivery status : " + (back > 0 ? "LATE" : "ON TIME"));
                    report.newLine();
                    report.write("Refund          : Rs. " + fmt(back));
                    report.newLine();
                } catch (InvalidDeliveryHoursException ex) {
                    report.write("Refund          : not calculated (" + ex.getMessage() + ")");
                    report.newLine();
                    logError(errLog, "parcels.txt", srcLine[i], srcText[i], ex);
                }
            } else {
                report.write("Express upgrade quote : Rs. " + fmt(item.calculateCharge(150)));
                report.newLine();
            }
        }

        report.write("----------------------------------------");
        report.newLine();
        report.newLine();
        if (one != null && two != null) {
            report.write("compareCharge(" + one.getParcelId() + ", " + two.getParcelId()
                         + ") -> higher charge: " + one.compareCharge(two));
        } else {
            report.write("compareCharge: fewer than two valid parcels");
        }
        report.newLine();
        report.newLine();
    }

    static void doLookups(BufferedWriter report, BufferedWriter errLog)
            throws IOException, EmptyInputFileException {
        File src = new File(IN_LOOKUP);
        verifyFile(src);

        report.write("=========== PARCEL LOOKUPS ===========");
        report.newLine();

        try (BufferedReader reader = new BufferedReader(new FileReader(src))) {
            int[] ln = {0};
            int n = fetchN(reader, ln);
            int done = 0;
            String raw;
            while (done < n && (raw = reader.readLine()) != null) {
                ln[0]++;
                String key = raw.trim();
                if (ignorable(key)) continue;
                done++;

                try {
                    Courier hit = Courier.searchParcel(store, key);
                    if (hit == null) {
                        throw new ParcelNotFoundException("Parcel not found: " + key);
                    }
                    report.write("Lookup " + key + ": FOUND");
                    report.newLine();
                    hit.display(report);
                    report.write("Total charge    : Rs. " + fmt(hit.calculateCharge()));
                    report.newLine();
                } catch (ParcelNotFoundException ex) {
                    report.write("Lookup " + key + ": NOT FOUND (" + ex.getMessage() + ")");
                    report.newLine();
                    logError(errLog, "lookup.txt", ln[0], key, ex);
                }
                report.write("----------------------------------------");
                report.newLine();
            }
        }
    }

    static String fmt(double amount) {
        return String.format("%.2f", amount);
    }

    static void logError(BufferedWriter errLog, String fileName, int lineNo, String text, Exception ex)
            throws IOException {
        String entry = "[" + fileName + "] Line " + lineNo + " | " + text + " | "
                + ex.getClass().getSimpleName() + " | " + ex.getMessage();
        if (ex.getCause() != null) {
            entry = entry + " | cause: " + ex.getCause().getClass().getSimpleName();
        }
        errLog.write(entry);
        errLog.newLine();
        errLog.flush();
    }
}
