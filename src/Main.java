import java.lang.reflect.*;
import java.util.Arrays;
import static java.lang.Thread.sleep;
import static java.util.concurrent.Executors.newFixedThreadPool;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService es = newFixedThreadPool(20);
        System.out.println("Begin");
        Account a = new Account("Иванов А.А.");
        Cashable a_cashable = (Cashable) a.getProxy();
        a_cashable.setAmounts(Currencies.RUB,0);
        a_cashable.setAmounts(Currencies.USD,0);
        System.out.println("rub= "+a_cashable.rub());
        System.out.println("usd= "+a_cashable.usd());
        int m=3;
        for (int j=1; j<=m;j++) {
          //  sleep(10);
            int n = 200000;
            for (int i = 1; i <= n; i++) {
                //изменяем состояние  0 -> N
                a_cashable.add(Currencies.RUB, 1);
                a_cashable.add(Currencies.USD, 2);
                // вызываем кэшируемый метод
                double r = a_cashable.rub();
                double d = a_cashable.usd();
                // проверяем корректность вызова
                if (r != (double) i / 100) System.out.println("r= " + r);
                if (d != (double) 2 * i / 100) System.out.println("d= " + d);
             //   sleep(1);
            }
            for (int i = 1; i <= n; i++) {
                //изменяем состояние n -> 0
                a_cashable.add(Currencies.RUB, -1);
                a_cashable.add(Currencies.USD, -2);
                // вызываем кэшируемый метод
                double r = a_cashable.rub();
                double d = a_cashable.usd();
                // проверяем корректность вызова
                if (r != (double) (n - i) / 100) System.out.println("r= " + r);
                if (d != (double) (2 * n - 2 * i) / 100) System.out.println("d= " + d);
             //  sleep(1);
            }
        }

        System.out.println("Результат, должен быть равен исходному значению");
        System.out.println("rub= "+a_cashable.rub());
        System.out.println("usd= "+a_cashable.usd());

        System.out.println("Ждем, пока кэш протухнет ");
        sleep(2000);
        a_cashable.sayAbout();
    }
}