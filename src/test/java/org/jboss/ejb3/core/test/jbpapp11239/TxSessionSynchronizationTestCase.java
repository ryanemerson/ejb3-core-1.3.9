package org.jboss.ejb3.core.test.jbpapp11239;

import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.common.tx.ControlledTransaction;
import org.jboss.ejb3.core.test.common.tx.ControlledTransactionManager;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.SystemException;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * @author Ryan Emerson
 */
public class TxSessionSynchronizationTestCase extends AbstractEJB3TestCase {

    private static final String BEAN_LOCATION = "SessionSynchBeanImpl/local";
    private ControlledTransactionManager tm = getTransactionManager();

    @Before
    public void init() {
        SessionSynchBeanImpl.afterBegin.set(0);
        SessionSynchBeanImpl.afterCompletion.set(0);
    }

    @BeforeClass
    public static void beforeClass() throws Exception
    {
        AbstractEJB3TestCase.beforeClass();
        deploy("controlled-transactionmanager-beans.xml");
        deploySessionEjb(SessionSynchBeanImpl.class);
    }

    @After
    public void tearDown() {
        try {
            tm.suspend(); // Dissasociate tx from this thread, occasionally required
        } catch (SystemException ignore){}
    }

    @Test
    public void testSerializationOfBeanMethodsAndCallbacks() throws Exception {
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        SessionSynchBean sfsb = lookup(BEAN_LOCATION, SessionSynchBean.class);
        tm.begin();

        final ControlledTransaction tx = tm.getControlledTransaction();
        scheduler.schedule(new Runnable() {
            @Override public void run() {
                try {
                    tx.rollback();
                } catch (Throwable ignore) {
                }
            }
        }, 5, TimeUnit.SECONDS);

        while (tx.getStatus() != Status.STATUS_ROLLEDBACK) {
            sfsb.method1();
        }

        assertEquals(1, SessionSynchBeanImpl.afterBegin.get());
        assertEquals(1, SessionSynchBeanImpl.afterCompletion.get());
    }

    protected static ControlledTransactionManager getTransactionManager() {
        try {
            return lookup("java:/TransactionManager", ControlledTransactionManager.class);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }
}
