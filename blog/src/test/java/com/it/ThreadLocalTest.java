package com.it;

import org.junit.jupiter.api.Test;

public class ThreadLocalTest {

    @Test
    public void testThreadLocalSetAndGet() {
        ThreadLocal tl = new ThreadLocal();
        new Thread(()->{
            tl.set( "test1" );
            System.out.println(Thread.currentThread().getName()+" : "+tl.get());
        },"t1").start();

        new Thread(()->{
            tl.set( "test2" );
            System.out.println(Thread.currentThread().getName()+" : "+tl.get());
        },"t2").start();
    }
}
