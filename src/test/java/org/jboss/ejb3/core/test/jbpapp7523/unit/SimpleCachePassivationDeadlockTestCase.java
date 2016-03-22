package org.jboss.ejb3.core.test.jbpapp7523.unit;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jboss.ejb3.cache.simple.SimpleStatefulCache;
import org.jboss.ejb3.core.test.common.AbstractEJB3TestCase;
import org.jboss.ejb3.core.test.jbpapp7523.SimpleSFSB;
import org.jboss.ejb3.session.SessionContainer;
import org.jboss.ejb3.stateful.StatefulBeanContext;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:cdewolf@redhat.com">Carlo de Wolf</a>
 */
public class SimpleCachePassivationDeadlockTestCase extends AbstractEJB3TestCase {
    private static SessionContainer container;

    private static SimpleStatefulCache cache;

    private static final CyclicBarrier barrier = new CyclicBarrier(2);

    private ExecutorService service;

    @After
    public void after() {
        service.shutdown();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        // Do not try to stop the cache, it might be dead-locked
        //cache.stop();

        undeployEjb(container);

        AbstractEJB3TestCase.afterClass();
    }

    private static int await(final CyclicBarrier barrier, final long timeout, final TimeUnit unit) {
        try {
            return barrier.await(timeout, unit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (BrokenBarrierException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Before
    public void before() {
        service = Executors.newFixedThreadPool(2);
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
       AbstractEJB3TestCase.beforeClass();

       // Deploy the test SLSB
       container = deploySessionEjb(SimpleSFSB.class);

       cache = new SimpleStatefulCache() {
           protected void passivate(final StatefulBeanContext ctx) {
               if (!barrier.isBroken())
                   await(barrier, 10, SECONDS);
               //await(barrier, 10, SECONDS);
               super.passivate(ctx);
           }
       };
       cache.initialize(container);
       cache.start();
    }

    @Test
    public void testSmallCacheCreates() throws Exception {
        // Fill the cache.
        final StatefulBeanContext bean1 = cache.create();
        cache.release(bean1);
        final Future<StatefulBeanContext> futureBean2 = service.submit(new Callable<StatefulBeanContext>() {
            @Override
            public StatefulBeanContext call() throws Exception {
                final StatefulBeanContext ctx = cache.create();
                cache.release(ctx);
                return ctx;
            }
        });
        // Force bean1 in the first cacheMap sync.
        final Future<StatefulBeanContext> futureBean1 = service.submit(new Callable<StatefulBeanContext>() {
            @Override
            public StatefulBeanContext call() throws Exception {
                return cache.get(bean1.getId());
            }
        });
        // Setup another create run to lock the cacheMap after bean1 proceeds.
        final Future<StatefulBeanContext> futureBean3 = service.submit(new Callable<StatefulBeanContext>() {
            @Override
            public StatefulBeanContext call() throws Exception {
                final StatefulBeanContext ctx = cache.create();
                cache.release(ctx);
                return ctx;
            }
        });
        // Go bean 2! This will make either bean1 or bean3 move forward as well.
        barrier.await(10, SECONDS);
        // bean1 should be at SimpleStatefulCachemap:492 (or bean3 moved).
        // Since there is nothing in SimpleStatefulCache.get where we can control movement, we can only hope.
        /*
        final Future<StatefulBeanContext> futureBean4 = service.submit(new Callable<StatefulBeanContext>() {
            @Override
            public StatefulBeanContext call() throws Exception {
                final StatefulBeanContext ctx = cache.create();
                cache.release(ctx);
                return ctx;
            }
        });
        */
        // Go bean 3!
        barrier.await(10, SECONDS);
        // Break the barrier (bean3 needs to be passivated to make room for bean1).
        try {
            barrier.await(0, SECONDS);
        } catch (TimeoutException e) {
            // good
        }
        // Now we should have a response within 10 seconds.
        try {
            futureBean1.get(10, SECONDS);
        } catch (TimeoutException e) {
            fail("JBPAPP-7523: cache failed to respond in time");
        }
        futureBean2.get(10, SECONDS);
        futureBean3.get(10, SECONDS);
        //futureBean4.get(10, SECONDS);
    }
}
