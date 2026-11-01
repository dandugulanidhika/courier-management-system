import java.io.BufferedWriter;
import java.io.IOException;

public class Courier {
    private String parcelId;
    private double weightKg;
    private double distanceKm;
    private boolean insured;

    public static String courierCompany = "SwiftLine Couriers";
    public static double baseRatePerKg = 50;
    public static double ratePerKm = 2;
    public static double insuranceRate = 5;
    private static int parcelCount;

    public Courier() {
        parcelCount++;
        this.parcelId = "UNKNOWN";
    }

    public Courier(String id, double wt, double dist)
            throws InvalidWeightException, InvalidDistanceException {
        this();
        try {
            setParcelId(id);
            setWeightKg(wt);
            setDistanceKm(dist);
        } catch (InvalidWeightException | InvalidDistanceException ex) {
            parcelCount--;
            throw ex;
        }
    }

    public Courier(String id, double wt, double dist, boolean ins)
            throws InvalidWeightException, InvalidDistanceException {
        this(id, wt, dist);
        setInsured(ins);
    }

    public String getParcelId() { return parcelId; }
    public void setParcelId(String id) { this.parcelId = id; }

    public double getWeightKg() { return weightKg; }
    public void setWeightKg(double w) throws InvalidWeightException {
        if (!(w > 0) || Double.isInfinite(w)) {
            throw new InvalidWeightException("Weight must be greater than 0 but was " + w, w);
        }
        this.weightKg = w;
    }

    public double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(double d) throws InvalidDistanceException {
        if (!(d > 0) || Double.isInfinite(d)) {
            throw new InvalidDistanceException("Distance must be greater than 0 but was " + d);
        }
        this.distanceKm = d;
    }

    public boolean isInsured() { return insured; }
    public void setInsured(boolean flag) { this.insured = flag; }

    public static int getParcelCount() { return parcelCount; }
    protected static void uncount() { parcelCount--; }

    public double calculateCharge() {
        double total = (baseRatePerKg * weightKg) + (distanceKm * ratePerKm);
        if (insured) {
            total = total + (total * insuranceRate / 100.0);
        }
        return total;
    }

    public double calculateCharge(double expressSurcharge) {
        return calculateCharge() + expressSurcharge;
    }

    public String compareCharge(Courier other) {
        if (other.calculateCharge() > this.calculateCharge()) {
            return other.parcelId;
        }
        return this.parcelId;
    }

    public void display(BufferedWriter bw) throws IOException {
        bw.write("Parcel ID      : " + parcelId);
        bw.newLine();
        bw.write("Weight (kg)    : " + String.format("%.2f", weightKg));
        bw.newLine();
        bw.write("Distance (km)  : " + String.format("%.2f", distanceKm));
        bw.newLine();
        bw.write("Insured        : " + insured);
        bw.newLine();
    }

    public static Courier searchParcel(Courier[] list, String key) {
        if (list == null || key == null) return null;
        String wanted = key.trim();
        for (int i = 0; i < list.length; i++) {
            Courier c = list[i];
            if (c == null || c.parcelId == null) continue;
            if (c.parcelId.equalsIgnoreCase(wanted)) return c;
        }
        return null;
    }
}
