public class InvalidWeightException extends Exception {
    private final double rejectedValue;

    public InvalidWeightException(String message) {
        super(message);
        this.rejectedValue = Double.NaN;
    }
    public InvalidWeightException(String message, Throwable cause) {
        super(message, cause);
        this.rejectedValue = Double.NaN;
    }
    public InvalidWeightException(String message, double rejectedValue) {
        super(message);
        this.rejectedValue = rejectedValue;
    }
    public InvalidWeightException(String message, Throwable cause, double rejectedValue) {
        super(message, cause);
        this.rejectedValue = rejectedValue;
    }
    public double getRejectedValue() { return rejectedValue; }
}
