package uk.gov.dwp.uc.pairtest;

/**
 * Should only have private methods other than the one below.
 */
import java.util.Arrays;
import java.util.List;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {

    /**
     * Should only have private methods other than the one below.
     */
    private static final int MAX_TICKETS_PER_ORDER = 20;
    private static final int ADULT_TICKET_PRICE = 20;
    private static final int CHILD_TICKET_PRICE = 10;
    private static final int INFANT_TICKET_PRICE = 0;

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {

        TicketPaymentServiceImpl ticketPaymentServiceImpl = new TicketPaymentServiceImpl();
        SeatReservationServiceImpl seatReservationServiceImpl = new SeatReservationServiceImpl();

        TicketTypeRequest[] req = ticketTypeRequests;
        List<TicketTypeRequest> list = Arrays.asList(req);
        if (!isValidTicketPurchaseRequest(list)) {
            throw new InvalidPurchaseException();
        }

        // calculate total amount and number of seats
        int totalAmount = 0;
        int numSeats = 0;
        for (TicketTypeRequest request : list) {
            int numTickets = request.getNoOfTickets();
            switch (request.getTicketType()) {
                case ADULT -> {
                    totalAmount += ADULT_TICKET_PRICE * numTickets;
                    numSeats += numTickets;
                }
                case CHILD -> {
                    totalAmount += CHILD_TICKET_PRICE * numTickets;
                    numSeats += numTickets;
                }
                case INFANT -> {
//                    numSeats += numTickets; // no cost for infants,  though can seat on an Adult's lap 
                }
                default -> {
                    throw new InvalidPurchaseException();
                }
            }
        }

        // check if total number of tickets is within limit
        if (numSeats > MAX_TICKETS_PER_ORDER) {
            throw new InvalidPurchaseException();
        }

        // make payment request and seat reservation request
        ticketPaymentServiceImpl.makePayment(accountId, totalAmount);
        seatReservationServiceImpl.reserveSeat(accountId, numSeats);
    }

    private static boolean isValidTicketPurchaseRequest(List<TicketTypeRequest> ticketTypeRequests) {

        int numAdults = 0;
        int numChildren = 0;
        int numInfants = 0;

        for (TicketTypeRequest request : ticketTypeRequests) {
            int numTickets = request.getNoOfTickets();
            switch (request.getTicketType()) {
                case ADULT ->
                    numAdults += numTickets;
                case CHILD ->
                    numChildren += numTickets;
                case INFANT ->
                    numInfants += numTickets;
                default -> {
                    return false; // invalid ticket type
                }
            }
        }

        if (numInfants > numAdults) {
            return false; // infants cannot be purchased without an adult ticket
        }

        if (numAdults == 0 && numChildren > 0) {
            return false; // children cannot be purchased without an adult ticket
        }

        return true;
    }

}
