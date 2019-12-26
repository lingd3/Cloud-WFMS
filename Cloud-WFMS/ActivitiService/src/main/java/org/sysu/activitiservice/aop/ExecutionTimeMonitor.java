package org.sysu.activitiservice.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author: Gordan Lin
 * @create: 2019/12/25
 **/
@Component
@Aspect
public class ExecutionTimeMonitor {

    private final static Logger logger = LoggerFactory.getLogger(ExecutionTimeMonitor.class);

    private final static int EXECUTION_TIME = 800;

    private static double sum = 0;

    private static double count = 0;

    public static final String POINT = "execution (* org.sysu.activitiservice.service.impl.ActivitiServiceImpl.completeTask(..))";

    @Around(POINT)
    public Object doAround(ProceedingJoinPoint pjp) {
        long start = System.currentTimeMillis();
        Object object = null;
        try {
            object = pjp.proceed();
        } catch (Throwable e) {
            logger.error("统计方法执行时间出错", e.getMessage());
        }
        long end = System.currentTimeMillis();
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String methodName = signature.getDeclaringTypeName() + "." + signature.getName();
        logger.info(methodName + "执行时间: " + (end-start) + " ms");

//        sum++;
//        if (end-start > EXECUTION_TIME) count++;
//        System.out.println("sum: " + sum + ", count: " + count);
//        if (count/sum > 0.2) {
//            System.out.println("请新增实例");
//        }

        return object;
    }
}
