package org.opendcs.maven.central.uploader;

import java.util.function.Consumer;

/**
 * Pair objects used for returning Successful and failed results for processing in a stream.
 * @param <SuccessType> Desired Object
 * @param <FailType> Object containing error details. Most commonly an Exception, but can be anything.
 */
public class FailableResult<SuccessType, FailType>
{
    private final SuccessType successResult;
    private final FailType failResult;

    private FailableResult(SuccessType successResult, FailType failResult)
    {
        this.successResult = successResult;
        this.failResult = failResult;
    }

    /**
     * @return true if this is successful
     */
    public boolean isSuccess()
    {
        return failResult == null;
    }

    /**
     * @return true if this is a failure
     */
    public boolean isFailure()
    {
        return failResult != null;
    }

    /**
     * Get the success object.
     * May be null.
     * @return The object containing the successful result.
     * @throws IllegalStateException if this result is not a success.
     */
    public SuccessType getSuccess()
    {
        if (isFailure())
        {
            throw new IllegalStateException("Attempt to retrieve 'success' result of a failure result.");
        }
        return successResult;
    }

    /**
     * Get the failure information.
     * @return The object containing failure information.
     * @throws IllegalStateException if this result is not a failure.
     */
    public FailType getFailure()
    {
        if (!isFailure())
        {
            throw new IllegalStateException("Attempt to retrieve 'failure' result of a succesfull result.");
        }
        return failResult;
    }

    /**
     * Do something with the error information if this a failure.
     * @param consumer Consumer function to operate on the failure result.
     */
    public void handleError(Consumer<FailType> consumer)
    {
        if (isFailure())
        {
            consumer.accept(failResult);
        }
    }

    /**
     * Create a new result with a successful value.
     * @param <SuccessType> Type of object to return for success.
     * @param <FailType> Type of object to return, most often derived from Throwable, but that is not required.
     * @param successResult The successful result.
     * @return a new FailableResult object
     */
    public static <SuccessType, FailType> FailableResult<SuccessType, FailType> success(SuccessType successResult)
    {
        return new FailableResult<>(successResult, null);
    }

    /**
     * Create a new result with a failure value.
     * @param <SuccessType> Type of object to return for success.
     * @param <FailType> Type of object to return, most often derived from Throwable, but that is not required.
     * @param failResult The cause of failure.
     * @return a new FailableResult object
     */
    public static <SuccessType, FailType> FailableResult<SuccessType, FailType> failure(FailType failResult)
    {
        return new FailableResult<>(null, failResult);
    }
}
