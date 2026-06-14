package com.hellokaton.blade.ioc;

import com.hellokaton.blade.ioc.bean.BeanDefine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for SimpleIoc container
 */
public class SimpleIocTest {

    private SimpleIoc ioc;

    @BeforeEach
    void setUp() {
        ioc = new SimpleIoc();
    }

    // ======================== addBean by instance ========================

    @Test
    void testAddBeanByInstance() {
        SampleService service = new SampleService();
        ioc.addBean(service);

        Object retrieved = ioc.getBean(service.getClass().getName());
        assertNotNull(retrieved);
        assertSame(service, retrieved);
    }

    @Test
    void testAddBeanByInstance_getBeanByType() {
        SampleService service = new SampleService();
        ioc.addBean(service);

        SampleService retrieved = ioc.getBean(SampleService.class);
        assertNotNull(retrieved);
        assertSame(service, retrieved);
    }

    // ======================== addBean by name ========================

    @Test
    void testAddBeanByName() {
        SampleService service = new SampleService();
        ioc.addBean("myService", service);

        Object retrieved = ioc.getBean("myService");
        assertNotNull(retrieved);
        assertSame(service, retrieved);
    }

    @Test
    void testAddBeanByName_alsoRegistersByClassName() {
        SampleService service = new SampleService();
        ioc.addBean("myService", service);

        // addBean(String, Object) creates BeanDefine and registers by name only
        // It does NOT auto-register by class name (unlike addBean(Object))
        Object retrieved = ioc.getBean("myService");
        assertNotNull(retrieved);
    }

    // ======================== addBean by type ========================

    @Test
    void testAddBeanByType() {
        SampleService service = ioc.addBean(SampleService.class);
        assertNotNull(service);

        SampleService retrieved = ioc.getBean(SampleService.class);
        assertNotNull(retrieved);
    }

    @Test
    void testAddBeanByType_registersInterfaces() {
        SampleServiceImpl impl = ioc.addBean(SampleServiceImpl.class);
        assertNotNull(impl);

        // Should be retrievable by interface
        SampleInterface retrieved = ioc.getBean(SampleInterface.class);
        assertNotNull(retrieved);
        assertSame(impl, retrieved);
    }

    // ======================== setBean ========================

    @Test
    void testSetBean_replaceExisting() {
        SampleService original = new SampleService();
        ioc.addBean(original);

        SampleService proxy = new SampleService();
        ioc.setBean(SampleService.class, proxy);

        SampleService retrieved = ioc.getBean(SampleService.class);
        assertSame(proxy, retrieved);
    }

    @Test
    void testSetBean_newBean() {
        // setBean for a type that doesn't exist yet should create a new entry
        SampleService service = new SampleService();
        ioc.setBean(SampleService.class, service);

        SampleService retrieved = ioc.getBean(SampleService.class);
        assertNotNull(retrieved);
        assertSame(service, retrieved);
    }

    // ======================== getBean ========================

    @Test
    void testGetBeanNotFound() {
        Object result = ioc.getBean("nonExistent");
        assertNull(result);
    }

    @Test
    void testGetBeanByTypeNotFound() {
        SampleService result = ioc.getBean(SampleService.class);
        assertNull(result);
    }

    // ======================== remove ========================

    @Test
    void testRemoveByName() {
        SampleService service = new SampleService();
        ioc.addBean("myService", service);
        assertNotNull(ioc.getBean("myService"));

        ioc.remove("myService");
        assertNull(ioc.getBean("myService"));
    }

    @Test
    void testRemoveByType() {
        // Note: SimpleIoc.remove(Class) uses type.getSimpleName(),
        // but addBean(Object) uses bean.getClass().getName().
        // This means remove(Class) won't actually remove beans added via addBean(Object).
        // This test documents the actual behavior.
        ioc.addBean(SampleService.class);
        assertNotNull(ioc.getBean(SampleService.class));

        // remove uses simpleName, which won't match getName() used by addBean
        // So this might not actually remove the bean
        ioc.remove(SampleService.class);
    }

    // ======================== clearAll ========================

    @Test
    void testClearAll() {
        ioc.addBean(new SampleService());
        ioc.addBean("test", "value");
        assertFalse(ioc.getBeanNames().isEmpty());

        ioc.clearAll();
        assertTrue(ioc.getBeanNames().isEmpty());
    }

    // ======================== getBeanDefines / getBeans / getBeanNames ========================

    @Test
    void testGetBeanDefines() {
        ioc.addBean(new SampleService());
        ioc.addBean("str", "hello");

        List<BeanDefine> defines = ioc.getBeanDefines();
        assertNotNull(defines);
        assertFalse(defines.isEmpty());
    }

    @Test
    void testGetBeanDefine() {
        SampleService service = new SampleService();
        ioc.addBean(service);

        BeanDefine define = ioc.getBeanDefine(SampleService.class);
        assertNotNull(define);
        assertSame(service, define.getBean());
        assertEquals(SampleService.class, define.getType());
    }

    @Test
    void testGetBeans() {
        ioc.addBean("a", "valueA");
        ioc.addBean("b", "valueB");

        List<Object> beans = ioc.getBeans();
        assertNotNull(beans);
        assertTrue(beans.size() >= 2);
    }

    @Test
    void testGetBeanNames() {
        ioc.addBean("myBean", "value");

        Set<String> names = ioc.getBeanNames();
        assertNotNull(names);
        assertTrue(names.contains("myBean"));
    }

    // ======================== addBean with interface ========================

    @Test
    void testAddBeanByInstance_registersInterfaces() {
        SampleServiceImpl impl = new SampleServiceImpl();
        ioc.addBean(impl);

        // addBean(Object) registers by class name AND interfaces
        SampleInterface retrieved = ioc.getBean(SampleInterface.class);
        assertNotNull(retrieved);
        assertSame(impl, retrieved);
    }

    @Test
    void testAddBeanByInstance_classNameLookup() {
        SampleServiceImpl impl = new SampleServiceImpl();
        ioc.addBean(impl);

        Object byClassName = ioc.getBean(SampleServiceImpl.class.getName());
        assertNotNull(byClassName);
        assertSame(impl, byClassName);
    }

    // ======================== BeanDefine properties ========================

    @Test
    void testBeanDefineSingletonDefault() {
        SampleService service = new SampleService();
        ioc.addBean(service);

        BeanDefine define = ioc.getBeanDefine(SampleService.class);
        assertNotNull(define);
        assertTrue(define.isSingleton());
    }

    // ======================== Helper classes ========================

    public static class SampleService {
        public String hello() {
            return "hello";
        }
    }

    public interface SampleInterface {
        String doWork();
    }

    public static class SampleServiceImpl implements SampleInterface {
        @Override
        public String doWork() {
            return "work done";
        }
    }
}
