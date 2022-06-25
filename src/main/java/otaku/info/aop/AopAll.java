package otaku.info.aop;

//import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.After;
//import org.aspectj.lang.annotation.AfterReturning;
//import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AopAll {

    // PointcutのExecution指定サンプル

    // Beforeのサンプル
//    @Before("execution(* *..*.*Controller.*(..))")
//    public void beforeSample(JoinPoint joinPoint) {
//        System.out.println("[Before]メソッド開始:" + joinPoint.getSignature());
//    }

    // Afterのサンプル
//    @After("execution(* *..*.*Controller.*(..))")
//    public void afterSample(JoinPoint joinPoint) {
//        System.out.println("[After]メソッド終了:" + joinPoint.getSignature());
//    }

    // Aroundのサンプル
    @Around("execution(* *..*.*Controller.*(..))")
    public Object aroundSample(ProceedingJoinPoint joinPoint) throws Throwable {
//        System.out.println("[Around]メソッド開始:" + joinPoint.getSignature());
        // メソッドの引数の名前
        // 例. [id, name, age]
//        String[] methodArgNames = ((CodeSignature) joinPoint.getSignature()).getParameterNames();
//
//        // メソッドの引数の値
//        // 例. ["001", "Alice", 18]
//        Object[] methodArgValues = joinPoint.getArgs();
//
//        // 「メソッドの引数の名前=メソッドの引数の値」形式で表示する
//        // 例. id=001 name=Alice age=18
//        for (int i = 0; i < methodArgNames.length; i++) {
//            System.out.print(methodArgNames[i] + "=" + String.valueOf(methodArgValues[i]) + " ");
//        }
//        System.out.print("\n");

        try {
            // メソッド実行
            Object result = joinPoint.proceed();
            System.out.println("[Around]メソッド終了:" + joinPoint.getSignature());
            return result;
        } catch(Exception e) {
            System.out.println("[Around]メソッド異常終了:" + joinPoint.getSignature());
            // メソッドの引数の名前
            // 例. [id, name, age]
            String[] methodArgNames2 = ((CodeSignature) joinPoint.getSignature()).getParameterNames();

            // メソッドの引数の値
            // 例. ["001", "Alice", 18]
            Object[] methodArgValues2 = joinPoint.getArgs();

            // 「メソッドの引数の名前=メソッドの引数の値」形式で表示する
            // 例. id=001 name=Alice age=18
            for (int i = 0; i < methodArgNames2.length; i++) {
                System.out.print(methodArgNames2[i] + "=" + String.valueOf(methodArgValues2[i]) + " ");
            }
            System.out.print("\n");
            e.printStackTrace();
            throw e;
        }
    }

    // AfterReturningのサンプル
//    @AfterReturning("execution(* *..*.*Controller.*(..))")
//    public void afterReturningSample(JoinPoint joinPoint) {
//        System.out.println("[AfterReturning]メソッド正常終了:" + joinPoint.getSignature());
//    }

    // AfterThrowingのサンプル
//    @AfterThrowing("execution(* *..*.*Controller.*(..))")
//    public void afterThrowingSample(JoinPoint joinPoint) {
//        System.out.println("[AfterThrowing]メソッド異常終了:" + joinPoint.getSignature());
//    }
}
