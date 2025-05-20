
package com.planner.travel.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ErrorHandler {

    /**
     * Transforms any exception into a user-friendly message while logging the full error details
     *
     * @param e The exception that occurred
     * @param contextMessage Additional context about where the error occurred
     * @return A user-friendly error message
     */
    public static String getUserFriendlyErrorMessage(Exception e, String contextMessage) {
        // Log the full exception with stack trace for debugging
        log.error("{}: {}", contextMessage, e.getMessage(), e);

        // Return a generic message to the user
        return "Sorry, we encountered a problem processing your request.";
    }

    public static String getUserFriendlyErrorMessage(Exception e, String contextMessage, boolean provideMoreDetails) {
        // Log the full exception with stack trace for debugging
        log.error("{}: {}", contextMessage, e.getMessage(), e);

        if (!provideMoreDetails) {
            return "Sorry, we encountered a problem processing your request.";
        }

        // Check for CompletionException with MaximumIterationsReached
        if (e instanceof java.util.concurrent.CompletionException && e.getCause() instanceof IllegalStateException) {
            if (e.getMessage() != null && e.getMessage().contains("Maximum number of iterations")) {
                return "Sorry, your request is too complex for our system to process. Please try a simpler request or break it down into smaller parts.";
            }
        }

        // Provide slightly more specific messages based on an exception type
        if (e instanceof NullPointerException) {
            return "Sorry, we couldn't process your request due to missing information.";
        } else if (e instanceof IllegalArgumentException) {
            return "Sorry, one of the values you provided isn't valid for this operation.";
        } else if (e.getCause() != null && e.getCause() instanceof java.net.ConnectException) {
            return "Sorry, we're having trouble connecting to one of our services. Please try again later.";
        }

        // Default generic message
        return "Sorry, we encountered a problem processing your request.";
    }
}