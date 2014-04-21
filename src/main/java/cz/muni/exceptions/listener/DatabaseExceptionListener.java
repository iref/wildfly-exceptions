package cz.muni.exceptions.listener;

import com.google.common.base.Functions;
import com.google.common.base.Strings;
import cz.muni.exceptions.listener.db.TicketRepository;
import cz.muni.exceptions.listener.db.model.Ticket;
import cz.muni.exceptions.listener.db.model.TicketClass;
import cz.muni.exceptions.listener.db.model.TicketOccurence;
import cz.muni.exceptions.source.ExceptionReport;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;

/**
 * @author Jan Ferko
 */
public class DatabaseExceptionListener implements ExceptionListener {

    private final TicketRepository ticketRepository;

    public DatabaseExceptionListener(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Override
    public void onThrownException(ExceptionReport report) {
        String detailMessage = report.getMessage();
        // classify exception here
        TicketClass ticketClass = TicketClass.UNKNOWN;
        String stackTrace = prepareStackTrace(report);

        TicketOccurence ticketOccurence = new TicketOccurence();
        ticketOccurence.setTimestamp(new Timestamp(new Date().getTime()));

        // store new ticket or update existing
        Ticket ticket = new Ticket(detailMessage, stackTrace, ticketClass, Arrays.asList(ticketOccurence));
        ticketRepository.add(ticket);
    }

    private String prepareStackTrace(ExceptionReport report) {
        StringBuilder builder = new StringBuilder(report.getMessage()).append("\n");
        for (StackTraceElement elem : report.getStackTrace()) {
            builder.append("at ").append(elem.toString()).append("\n");
        }

        if (report.getCause() != null) {
            builder.append(prepareStackTrace(report.getCause()));
        }

        return builder.toString();
    }
}
