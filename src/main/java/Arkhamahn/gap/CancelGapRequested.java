package Arkhamahn.gap;

/** Exception raised when the user presses the CANCEL GAP button during a run. */
public class CancelGapRequested extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CancelGapRequested(String message) {
        super(message);
    }
}