package cz.muni.exceptions.listener;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import cz.muni.exceptions.listener.classifier.ExceptionReportClassifier;
import cz.muni.exceptions.listener.db.TicketRepository;
import cz.muni.exceptions.listener.db.model.Ticket;
import cz.muni.exceptions.listener.db.model.TicketClass;
import cz.muni.exceptions.listener.db.model.TicketOccurence;
import cz.muni.exceptions.listener.duplication.SimilarityChecker;
import cz.muni.exceptions.source.ExceptionReport;


import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jan Ferko
 */
public class DatabaseExceptionListener implements ExceptionListener {

    private static final Pattern STACKTRACE_PATTERN = Pattern.compile("at (.+)\\((.+?)\\:*(\\d*)\\)");

    private final TicketRepository ticketRepository;

    private final ExceptionReportClassifier classifier;

    private final SimilarityChecker similarityChecker;

    public DatabaseExceptionListener(TicketRepository ticketRepository, ExceptionReportClassifier classifier,
                                     SimilarityChecker similarityChecker) {
        if (ticketRepository == null) {
            throw new IllegalArgumentException("[TicketRepository] is required and should not be null.");
        }
        if (classifier == null) {
            throw new IllegalArgumentException("[Classifier] is required and should not be null.");
        }
        if (similarityChecker == null) {
            throw new IllegalArgumentException("[Similarity] is required and should not be null.");
        }
        this.ticketRepository = ticketRepository;
        this.classifier = classifier;
        this.similarityChecker = similarityChecker;
    }

    @Override
    public void onThrownException(ExceptionReport report) {
        String detailMessage = report.getMessage();

        // create new ticket timestamp
        TicketOccurence ticketOccurence = new TicketOccurence();
        ticketOccurence.setTimestamp(new Timestamp(new Date().getTime()));

        // find duplicate
        Optional<Ticket> duplicate = findDuplicate(report, ticketRepository.all());

        if (duplicate.isPresent()) {
            Ticket ticket = duplicate.get();
            ticket.getOccurences().add(ticketOccurence);
            ticketRepository.update(ticket);
        } else {
            // classify exception here
            TicketClass ticketClass = classifier.classify(report);
            String stackTrace = prepareStackTrace(report);

            // store new ticket or update existing
            Ticket ticket = new Ticket(detailMessage, stackTrace, ticketClass, Arrays.asList(ticketOccurence));
            ticketRepository.add(ticket);

        }
    }

    private String prepareStackTrace(ExceptionReport report) {
        StringBuilder builder = new StringBuilder();
        if (report.getMessage() != null) {
            builder.append(report.getMessage()).append("\n");
        }

        for (StackTraceElement elem : report.getStackTrace()) {
            builder.append("at ").append(elem.toString()).append("\n");
        }

        if (report.getCause() != null) {
            builder.append(prepareStackTrace(report.getCause()));
        }

        return builder.toString();
    }

    private Optional<Ticket> findDuplicate(ExceptionReport report, Set<Ticket> existingTickets) {
        Optional<Ticket> result = Optional.absent();
        for (Ticket ticket : existingTickets) {
            List<Character> reportMessage = report.getMessage() == null 
                    ? Lists.<Character>newArrayList() : Lists.charactersOf(report.getMessage());
            List<Character> ticketMessage = ticket.getDetailMessage() == null 
                    ? Lists.<Character>newArrayList() : Lists.charactersOf(ticket.getDetailMessage());

            int messageScore = similarityChecker.checkSimilarity(reportMessage, ticketMessage);

            List<StackTraceElement> ticketStackTrace = buildStackTrace(ticket.getStackTrace());
            int stackTraceScore = similarityChecker.checkSimilarity(report.getStackTrace(), ticketStackTrace);

            if (messageScore == 0 && stackTraceScore == 0) {
                result = Optional.of(ticket);
                break;
            }
        }
        return result;
    }

    private List<StackTraceElement> buildStackTrace(String stackTrace) {
        List<StackTraceElement> elements = new ArrayList<>();

        if (stackTrace == null && stackTrace.isEmpty()) {
            return elements;
        }

        Matcher matcher = STACKTRACE_PATTERN.matcher(stackTrace);

        while (matcher.find()) {
            String classNameAndMethod = matcher.group(1);
            String[] split = classNameAndMethod.split("\\.");

            String method = split[split.length - 1];
            String[] classNames = Arrays.copyOfRange(split, 0, split.length - 1);
            StringBuilder classNameBuilder = new StringBuilder();
            for (String fragment : classNames) {
                classNameBuilder.append(fragment).append(".");
            }

            String fileName = matcher.group(2);
            String lineNumberValue = matcher.group(3);
            int lineNumber = 0;
            if (!lineNumberValue.isEmpty()) {
                Integer.parseInt(lineNumberValue);
            }

            StackTraceElement element = new StackTraceElement(classNameBuilder.toString(), method, fileName, lineNumber);
            elements.add(element);
        }

        return elements;
    }
}
