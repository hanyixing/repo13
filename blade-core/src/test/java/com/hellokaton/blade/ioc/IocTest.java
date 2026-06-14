package com.hellokaton.blade.ioc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the default IOC container {@link SimpleIoc}.
 *
 * @author biezhi
 * @date 2017/9/18
 */
public class IocTest {

    private Ioc ioc;

    @BeforeEach
    public void setUp() {
        ioc = new SimpleIoc();
    }

    interface Hello {
        String hi();
    }

    public static class HelloImpl implements Hello {
        @Override
        public String hi() {
            return "hi";
        }
    }

    @Test
    public void testAddBeanInstanceAndGetByType() {
        HelloImpl bean = new HelloImpl();
        ioc.addBean(bean);

        assertSame(bean, ioc.getBean(HelloImpl.class));
        // the implemented interface is registered to the pool as well
        assertSame(bean, ioc.getBean(Hello.class));
    }

    @Test
    public void testAddBeanByClassIsSingleton() {
        HelloImpl bean = ioc.addBean(HelloImpl.class);

        assertNotNull(bean);
        assertSame(bean, ioc.getBean(HelloImpl.class));
        assertSame(ioc.getBean(HelloImpl.class), ioc.getBean(HelloImpl.class));
    }

    @Test
    public void testAddBeanByName() {
        HelloImpl bean = new HelloImpl();
        ioc.addBean("myHello", bean);

        assertSame(bean, ioc.getBean("myHello"));
    }

    @Test
    public void testGetBeanDefineAndCollections() {
        ioc.addBean(new HelloImpl());

        assertNotNull(ioc.getBeanDefine(HelloImpl.class));
        assertFalse(ioc.getBeans().isEmpty());
        assertTrue(ioc.getBeanNames().contains(HelloImpl.class.getName()));
    }

    @Test
    public void testRemoveAndClear() {
        ioc.addBean("myHello", new HelloImpl());
        ioc.remove("myHello");
        assertNull(ioc.getBean("myHello"));

        ioc.addBean(new HelloImpl());
        ioc.clearAll();
        assertTrue(ioc.getBeanNames().isEmpty());
    }

}
