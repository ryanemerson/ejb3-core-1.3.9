package org.jboss.ejb3.core.test.jbpapp11239;

import javax.ejb.ConcurrentAccessException;
import javax.ejb.EJBException;
import javax.ejb.Local;
import javax.ejb.SessionSynchronization;
import javax.ejb.Stateful;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Ryan Emerson
 */
@Stateful
@Local(SessionSynchBean.class)
public class SessionSynchBeanImpl implements SessionSynchronization {

    public static final AtomicInteger afterBegin = new AtomicInteger(0);
    public static final AtomicInteger afterCompletion = new AtomicInteger(0);

    private final AtomicInteger currentlyExecutedMethods = new AtomicInteger();
    private volatile String previousMethod = "";

    @Override
    public void afterBegin() throws EJBException {
        afterBegin.incrementAndGet();
        methodBlock("afterBegin");
    }

    @Override
    public void beforeCompletion() throws EJBException {
        methodBlock("beforeCompletion");
    }

    @Override
    public void afterCompletion(boolean committed) throws EJBException {
        afterCompletion.incrementAndGet();
        methodBlock("afterCompletion (" + committed + ")");
    }

    public void method1() {
        methodBlock("method1");
    }

    private void methodBlock(String methodName) {
        int i = currentlyExecutedMethods.getAndIncrement();
        if (i > 0)
            throw new ConcurrentAccessException(getExceptionMessage(i, methodName));

        try {
            Thread.sleep(5000);
        } catch (InterruptedException ignore) {
        }

        i = currentlyExecutedMethods.decrementAndGet();
        if (i > 0)
            throw new ConcurrentAccessException(getExceptionMessage(i, methodName));

        previousMethod = methodName;
    }

    private String getExceptionMessage(int count, String currentMethod) {
        String className = this.getClass().getSimpleName() + ".";
        return "Concurrent access detected: " + count + " method(s) were already in execution." + " Method 1 := "
                + className + previousMethod + ", Method 2 := " + className + currentMethod;
    }
}