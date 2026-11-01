import java.io.BufferedWriter;
import java.io.IOException;

public class ExpressCourier extends Courier {
    private int deliveryHours;
    private double priorityFee;

    public static double fastDeliverySurchargePercent = 25;
    public static int fastDeliveryLimitHours = 24;

    public ExpressCourier() {
        super();
        this.deliveryHours = fastDeliveryLimitHours;
        this.priorityFee = 0;
    }

    public ExpressCourier(String id, double wt, double dist, boolean ins, int hrs, double fee)
            throws InvalidWeightException, InvalidDistanceException,
                   InvalidPriorityFeeException, InvalidDeliveryHoursException {
        super(id, wt, dist, ins);
        try {
            setDeliveryHours(hrs);
            setPriorityFee(fee);
        } catch (InvalidDeliveryHoursException | InvalidPriorityFeeException ex) {
            uncount();
            throw ex;
        }
    }

    public int getDeliveryHours() { return deliveryHours; }
    public void setDeliveryHours(int hrs) throws InvalidDeliveryHoursException {
        if (hrs < 1) {
            throw new InvalidDeliveryHoursException("Delivery hours must be at least 1 but was " + hrs);
        }
        this.deliveryHours = hrs;
    }

    public double getPriorityFee() { return priorityFee; }
    public void setPriorityFee(double fee) throws InvalidPriorityFeeException {
        if (!(fee >= 0) || Double.isInfinite(fee)) {
            throw new InvalidPriorityFeeException("Priority fee must not be negative but was " + fee);
        }
        this.priorityFee = fee;
    }

    @Override
    public double calculateCharge() {
        double basePart = super.calculateCharge();
        double answer = basePart + priorityFee;
        if (deliveryHours < fastDeliveryLimitHours) {
            answer = answer + (basePart * fastDeliverySurchargePercent / 100.0);
        }
        return answer;
    }

    public double calculateRefund(int actualHours) throws InvalidDeliveryHoursException {
        if (actualHours < 0) {
            throw new InvalidDeliveryHoursException("Actual hours must not be negative but was " + actualHours);
        }
        if (actualHours > deliveryHours) {
            return priorityFee * 0.5;
        }
        return 0.0;
    }

    @Override
    public void display(BufferedWriter bw) throws IOException {
        super.display(bw);
        bw.write("Delivery hours : " + deliveryHours);
        bw.newLine();
        bw.write("Priority fee   : " + String.format("%.2f", priorityFee));
        bw.newLine();
    }
}
