package com.hmdp.utils;

public interface ILock {
    /*
      尝试获取锁
     */
    boolean tryLook(Long timeoutSec);

    /*
    释放锁
     */
    void unlock();

}
