import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;


class InvalidWeightException extends Exception {
    private double rejectedValue;

    public InvalidWeightException(String message) {
        super(message);
    }

    public InvalidWeightException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidWeightException(String message, double rejectedValue) {
        super(message);
        this.rejectedValue = rejectedValue;
    }

    public double getRejectedValue() {
        return rejectedValue;
    }
}

class InvalidDistanceException extends Exception {
    public InvalidDistanceException(String message) {
        super(message);
    }

    public InvalidDistanceException(String message, Throwable cause) {
        super(message, cause);
    }
}

class InvalidPriorityFeeException extends Exception {
    public InvalidPriorityFeeException(String message) {
        super(message);
    }

    public InvalidPriorityFeeException(String message, Throwable cause) {
        super(message, cause);
    }
}

class InvalidDeliveryHoursException extends Exception {
    public InvalidDeliveryHoursException(String message) {
        super(message);
    }

    public InvalidDeliveryHoursException(String message, Throwable cause) {
        super(message, cause);
    }
}

class InvalidRecordException extends Exception {
    public InvalidRecordException(String message) {
        super(message);
    }

    public InvalidRecordException(String message, Throwable cause) {
        super(message, cause);
    }
}

class EmptyInputFileException extends Exception {
    public EmptyInputFileException(String message) {
        super(message);
    }

    public EmptyInputFileException(String message, Throwable cause) {
        super(message, cause);
    }
}


class ParcelNotFoundException extends RuntimeException {
    public ParcelNotFoundException(String message) {
        super(message);
    }

    public ParcelNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}



class Courier {
    private String parcelId;
    private double weightKg;
    private double distanceKm;
    private boolean insured;

    static String courierCompany = "Swift Courier";
    static double baseRatePerKg = 50;
    static double ratePerKm = 2;
    static double insuranceRate = 5;
    static int parcelCount = 0;

   
    public Courier() {
        parcelCount++;
    }

    public Courier(String parcelId, double weightKg, double distanceKm)
            throws InvalidWeightException, InvalidDistanceException {
        this();
        try {
            this.parcelId = parcelId;
            setWeightKg(weightKg);
            setDistanceKm(distanceKm);
        } catch (InvalidWeightException e) {
            parcelCount--; 
            throw e;
        } catch (InvalidDistanceException e) {
            parcelCount--;
            throw e;
        }
    }

    public Courier(String parcelId, double weightKg, double distanceKm, boolean insured)
            throws InvalidWeightException, InvalidDistanceException {
        this(parcelId, weightKg, distanceKm);
        this.insured = insured;
    }

    public String getParcelId() {
        return parcelId;
    }

    public void setParcelId(String parcelId) {
        this.parcelId = parcelId;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(double weightKg) throws InvalidWeightException {
        if (weightKg <= 0) {
            throw new InvalidWeightException("Weight must be more than 0 but was " + weightKg, weightKg);
        }
        this.weightKg = weightKg;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) throws InvalidDistanceException {
        if (distanceKm <= 0) {
            throw new InvalidDistanceException("Distance must be more than 0 but was " + distanceKm);
        }
        this.distanceKm = distanceKm;
    }

    public boolean isInsured() {
        return insured;
    }

    public void setInsured(boolean insured) {
        this.insured = insured;
    }

    public double calculateCharge() {
        double charge = (baseRatePerKg * weightKg) + (distanceKm * ratePerKm);
        if (insured == true) {
            charge = charge + (charge * insuranceRate / 100);
        }
        return charge;
    }

    public double calculateCharge(double expressSurcharge) {
        return calculateCharge() + expressSurcharge;
    }

    public String compareCharge(Courier other) {
        if (other.calculateCharge() > this.calculateCharge()) {
            return other.parcelId;
        } else {
            return this.parcelId;
        }
    }

    public void display(BufferedWriter bw) throws IOException {
        bw.write("Parcel ID     : " + parcelId);
        bw.newLine();
        bw.write("Weight (kg)   : " + weightKg);
        bw.newLine();
        bw.write("Distance (km) : " + distanceKm);
        bw.newLine();
        bw.write("Insured       : " + insured);
        bw.newLine();
    }

    public static Courier searchParcel(Courier[] parcels, String parcelId) {
        for (int i = 0; i < parcels.length; i++) {
            if (parcels[i] != null) {
                if (parcels[i].parcelId.equalsIgnoreCase(parcelId)) {
                    return parcels[i];
                }
            }
        }
        return null;
    }
}



class ExpressCourier extends Courier {
    private int deliveryHours;
    private double priorityFee;

    static double fastDeliverySurchargePercent = 25;
    static int fastDeliveryLimitHours = 24;

    public ExpressCourier() {
        super();
    }

    public ExpressCourier(String parcelId, double weightKg, double distanceKm, boolean insured,
            int deliveryHours, double priorityFee)
            throws InvalidWeightException, InvalidDistanceException,
            InvalidDeliveryHoursException, InvalidPriorityFeeException {
        super(parcelId, weightKg, distanceKm, insured);
        try {
            setDeliveryHours(deliveryHours);
            setPriorityFee(priorityFee);
        } catch (InvalidDeliveryHoursException e) {
            parcelCount--;
            throw e;
        } catch (InvalidPriorityFeeException e) {
            parcelCount--;
            throw e;
        }
    }

    public int getDeliveryHours() {
        return deliveryHours;
    }

    public void setDeliveryHours(int deliveryHours) throws InvalidDeliveryHoursException {
        if (deliveryHours < 1) {
            throw new InvalidDeliveryHoursException("Delivery hours must be at least 1 but was " + deliveryHours);
        }
        this.deliveryHours = deliveryHours;
    }

    public double getPriorityFee() {
        return priorityFee;
    }

    public void setPriorityFee(double priorityFee) throws InvalidPriorityFeeException {
        if (priorityFee < 0) {
            throw new InvalidPriorityFeeException("Priority fee cannot be negative but was " + priorityFee);
        }
        this.priorityFee = priorityFee;
    }

    public double calculateCharge() {
        double base = super.calculateCharge();
        double result = base + priorityFee;
        if (deliveryHours < fastDeliveryLimitHours) {
            result = result + (base * fastDeliverySurchargePercent / 100);
        }
        return result;
    }

    public double calculateRefund(int actualHours) throws InvalidDeliveryHoursException {
        if (actualHours < 0) {
            throw new InvalidDeliveryHoursException("Actual hours cannot be negative but was " + actualHours);
        }
        if (actualHours > deliveryHours) {
            return priorityFee * 0.5;
        } else {
            return 0;
        }
    }

    public void display(BufferedWriter bw) throws IOException {
        super.display(bw);
        bw.write("Promised hours: " + deliveryHours);
        bw.newLine();
        bw.write("Priority fee  : " + String.format("%.2f", priorityFee));
        bw.newLine();
    }
}



public class PSPJPROJECT {

    static int lastActualHours = 0; 

    public static void main(String[] args) {
        int recordsRead = 0;
        int accepted = 0;
        int rejected = 0;

        File outFolder = new File("output");
        outFolder.mkdirs();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("output/charge_slips.txt"));
                BufferedWriter err = new BufferedWriter(new FileWriter("output/errors.txt", true))) {

            err.write("=== Run at " + new Date() + " ===");
            err.newLine();

            try {
                
                File parcelFile = new File("input/parcels.txt");
                if (!parcelFile.exists() || !parcelFile.isFile() || !parcelFile.canRead()) {
                    throw new FileNotFoundException("Parcels file is missing: input/parcels.txt");
                }
                if (parcelFile.length() == 0) {
                    throw new EmptyInputFileException("Parcels file is empty");
                }

                Courier[] parcels = null;
                int[] actualHours = null;
                int count = 0;
                int n = 0;
                boolean gotN = false;
                int lineNo = 0;
                int done = 0;

                try (BufferedReader br = new BufferedReader(new FileReader(parcelFile))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = line.trim();
                        lineNo++;

                        if (line.isEmpty() || line.startsWith("#")) {
                            continue;
                        }

                       
                            try {
                                n = Integer.parseInt(line);
                            } catch (NumberFormatException e) {
                                throw new EmptyInputFileException("N is not a number", e);
                            }
                            if (n <= 0) {
                                throw new EmptyInputFileException("N is zero, no records to read");
                            }
                            parcels = new Courier[n];
                            actualHours = new int[n];
                            gotN = true;
                            continue;
                        }

                        if (done >= n) {
                            break;
                        }
                        done++;

                        try {
                            Courier c = makeParcel(line);
                            parcels[count] = c;
                            actualHours[count] = lastActualHours;
                            count++;
                            accepted++;
                        } catch (InvalidWeightException e) {
                            rejected++;
                            err.write("Line " + lineNo + " | " + line + " | " + e.getClass().getName() + " | " + e.getMessage());
                            err.newLine();
                        } catch (InvalidDistanceException e) {
                            rejected++;
                            err.write("Line " + lineNo + " | " + line + " | " + e.getClass().getName() + " | " + e.getMessage());
                            err.newLine();
                        } catch (InvalidPriorityFeeException e) {
                            rejected++;
                            err.write("Line " + lineNo + " | " + line + " | " + e.getClass().getName() + " | " + e.getMessage());
                            err.newLine();
                        } catch (InvalidDeliveryHoursException e) {
                            rejected++;
                            err.write("Line " + lineNo + " | " + line + " | " + e.getClass().getName() + " | " + e.getMessage());
                            err.newLine();
                        } catch (InvalidRecordException e) {
                            rejected++;
                            err.write("Line " + lineNo + " | " + line + " | " + e.getClass().getName() + " | " + e.getMessage());
                            err.newLine();
                        } finally {
                            recordsRead++;
                        }
                    }
                }

                if (gotN == false) {
                    throw new EmptyInputFileException("N is missing, file has no records");
                }

                
                bw.write("######## " + Courier.courierCompany + " : CHARGE SLIPS ########");
                bw.newLine();

                for (int i = 0; i < count; i++) {
                    Courier c = parcels[i];
                    bw.newLine();
                    bw.write("===== CHARGE SLIP =====");
                    bw.newLine();
                    c.display(bw);
                    bw.write("Charge        : Rs. " + String.format("%.2f", c.calculateCharge()));
                    bw.newLine();

                    if (c instanceof ExpressCourier) {
                        ExpressCourier x = (ExpressCourier) c;
                        try {
                            double refund = x.calculateRefund(actualHours[i]);
                            if (actualHours[i] > x.getDeliveryHours()) {
                                bw.write("Actual hours  : " + actualHours[i] + " (LATE)");
                            } else {
                                bw.write("Actual hours  : " + actualHours[i] + " (on time)");
                            }
                            bw.newLine();
                            bw.write("Refund        : Rs. " + String.format("%.2f", refund));
                            bw.newLine();
                        } catch (InvalidDeliveryHoursException e) {
                            err.write(x.getParcelId() + " | " + e.getClass().getName() + " | " + e.getMessage());
                            err.newLine();
                        }
                    } else {
                        bw.write("Express quote : Rs. " + String.format("%.2f", c.calculateCharge(150))
                                + " (upgrade, flat surcharge Rs. 150.00)");
                        bw.newLine();
                    }
                }

                
                bw.newLine();
                if (count >= 2) {
                    bw.write("Higher charge between " + parcels[0].getParcelId() + " and "
                            + parcels[1].getParcelId() + ": " + parcels[0].compareCharge(parcels[1]));
                } else {
                    bw.write("compareCharge skipped: fewer than two valid parcels.");
                }
                bw.newLine();

              
                bw.newLine();
                bw.write("===== PARCEL LOOKUPS =====");
                bw.newLine();

                File lookupFile = new File("input/lookup.txt");
                if (!lookupFile.exists() || !lookupFile.isFile() || !lookupFile.canRead() || lookupFile.length() == 0) {
                    bw.write("Lookups skipped: lookup file is missing or empty");
                    bw.newLine();
                    err.write("Lookup file is missing or empty");
                    err.newLine();
                } else {
                    try (BufferedReader br2 = new BufferedReader(new FileReader(lookupFile))) {
                        int lookupN = 0;
                        boolean gotLookupN = false;
                        int lookupLine = 0;
                        int lookupDone = 0;

                        String id;
                        while ((id = br2.readLine()) != null) {
                            id = id.trim();
                            lookupLine++;

                            if (id.isEmpty() || id.startsWith("#")) {
                                continue;
                            }

                            if (gotLookupN == false) {
                                try {
                                    lookupN = Integer.parseInt(id);
                                } catch (NumberFormatException e) {
                                    bw.write("Lookups skipped: N is not a number");
                                    bw.newLine();
                                    break;
                                }
                                gotLookupN = true;
                                continue;
                            }

                            if (lookupDone >= lookupN) {
                                break;
                            }
                            lookupDone++;

                            try {
                                Courier found = Courier.searchParcel(parcels, id);
                                if (found == null) {
                                    throw new ParcelNotFoundException("No parcel with ID " + id);
                                }
                                bw.write("Lookup " + id + ": FOUND");
                                bw.newLine();
                                found.display(bw);
                                bw.write("Charge        : Rs. " + String.format("%.2f", found.calculateCharge()));
                                bw.newLine();
                            } catch (ParcelNotFoundException e) {
                                bw.write("Lookup " + id + ": NOT FOUND (" + e.getMessage() + ")");
                                bw.newLine();
                                err.write("Line " + lookupLine + " | " + id + " | " + e.getClass().getName() + " | " + e.getMessage());
                                err.newLine();
                            }
                        }
                    }
                }

               
                bw.newLine();
                bw.write("Total objects created (parcelCount): " + Courier.parcelCount);
                bw.newLine();
                bw.write("Records read: " + recordsRead + ", accepted: " + accepted + ", rejected: " + rejected + ".");
                bw.newLine();

            } catch (FileNotFoundException e) {
                err.write("File error: " + e.getMessage());
                err.newLine();
                bw.write("Run stopped: " + e.getMessage());
                bw.newLine();
            } catch (EmptyInputFileException e) {
                err.write("File error: " + e.getMessage());
                err.newLine();
                bw.write("Run stopped: " + e.getMessage());
                bw.newLine();
            }

            bw.flush();
            err.flush();

        } catch (IOException e) {
            System.out.println("Problem with files: " + e.getMessage());
            return;
        }

        System.out.println("Processing complete. See output folder.");
    }

   
    static Courier makeParcel(String line) throws InvalidRecordException, InvalidWeightException,
            InvalidDistanceException, InvalidPriorityFeeException, InvalidDeliveryHoursException {

        String[] f = line.split(",", -1);
        for (int i = 0; i < f.length; i++) {
            f[i] = f[i].trim();
        }

        try {
            if (f[0].equals("S")) {
                if (f.length != 5) {
                    throw new InvalidRecordException("Type S needs 5 fields but found " + f.length);
                }
                double weight = Double.parseDouble(f[2]);
                double distance = Double.parseDouble(f[3]);
                boolean insured;
                if (f[4].equalsIgnoreCase("true")) {
                    insured = true;
                } else if (f[4].equalsIgnoreCase("false")) {
                    insured = false;
                } else {
                    throw new InvalidRecordException("insured must be true or false but was " + f[4]);
                }
                return new Courier(f[1], weight, distance, insured);

            } else if (f[0].equals("X")) {
                if (f.length != 8) {
                    throw new InvalidRecordException("Type X needs 8 fields but found " + f.length);
                }
                double weight = Double.parseDouble(f[2]);
                double distance = Double.parseDouble(f[3]);
                boolean insured;
                if (f[4].equalsIgnoreCase("true")) {
                    insured = true;
                } else if (f[4].equalsIgnoreCase("false")) {
                    insured = false;
                } else {
                    throw new InvalidRecordException("insured must be true or false but was " + f[4]);
                }
                int hours = Integer.parseInt(f[5]);
                double fee = Double.parseDouble(f[6]);
                int actual = Integer.parseInt(f[7]);
                if (actual < 0) {
                    throw new InvalidDeliveryHoursException("Actual hours cannot be negative but was " + actual);
                }
                ExpressCourier x = new ExpressCourier(f[1], weight, distance, insured, hours, fee);
                lastActualHours = actual;
                return x;

            } else {
                throw new InvalidRecordException("Unknown type code: " + f[0]);
            }
        } catch (NumberFormatException e) {
            throw new InvalidRecordException("Wrong number in record: " + e.getMessage(), e);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new InvalidRecordException("A field is missing in the record", e);
        }
    }
}
