package otaku.info.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AopAll {

    // Aroundのサンプル
    @Around("execution(* *..*.*Controller.*(..))")
    public Object aroundSample(ProceedingJoinPoint joinPoint) throws Throwable {

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
}
