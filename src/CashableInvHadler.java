import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.lang.System;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Arrays.*;

public class CashableInvHadler implements InvocationHandler {

        CashableInvHadler(Object obj){
            this.obj = obj;
            this.cash = new ConcurrentHashMap<>();
            clearCashTime = java.lang.System.currentTimeMillis();
        }
        private Object obj;
          private static ConcurrentHashMap<String, HashMap<String,Object>> cash;
//        private static HashMap<String, HashMap<String,Object>> cash; // кэш для <состояние объекта, Hashmap<Имя  метода, значение>> время храним как <#time, TimeMillis>
        private static long clearCashTime;
        private static final Semaphore sem = new Semaphore(1);
        private Thread ch;
        private static void clearCash() {
            System.out.println("Очистка кэша  cashSize before= " + cash.size());

            for (String key : cash.keySet()) {
                if (java.lang.System.currentTimeMillis() - (long) cash.get(key).get("theTimeStamp") > 1000 ) {
                    try {
                        sem.acquire();
                        cash.remove(key);
                        sem.release();
                    } catch (InterruptedException e) {}
                }
            }
            System.out.println("              cashSize  after= " + cash.size());
        }
        private String theAccount(){
            final String[] res = {""};
            stream(obj.getClass().getDeclaredFields()).
                    filter(x -> x.isAnnotationPresent(ObjectStateField.class)).
                    forEach(y -> {
                        y.setAccessible(true);
                        try {
                           res[0] = y.get(obj).toString();
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    });
            return res[0];
        };
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Method m = obj.getClass().getMethod(method.getName(), method.getParameterTypes());
            boolean haveCash = m.isAnnotationPresent(Cashe.class);
            Object res;

            // очистка кэша
            if ((java.lang.System.currentTimeMillis() -  clearCashTime) > 2000 ) {
                clearCashTime= java.lang.System.currentTimeMillis();
                new Thread(() -> clearCash()).start();
            }

            //метод кешируемый

            if (haveCash) {
                try { sem.acquire();
                //   System.out.println("-------CashAble! " + theAccount());
                    if(!cash.containsKey(theAccount()) || (java.lang.System.currentTimeMillis() - (long)cash.get(theAccount()).get("theTimeStamp") > 1000)) {
                   // такого состояния объекта нет или оно устарело
                        HashMap<String, Object> h = new HashMap<>();
                        h.put("theTimeStamp", java.lang.System.currentTimeMillis());
                        h.put(m.getName().toString(), method.invoke(obj, args));
                        cash.put(theAccount(), h);
                        sem.release();
                        return method.invoke(obj, args);
                    }else if(!cash.get(theAccount()).containsKey(m.getName())) { // состооянмие объекта актуальное, значения для метода нет в кэше
                        HashMap<String,Object> h = cash.get(theAccount());
                        h.put(m.getName(), method.invoke(obj, args));
                        cash.put(theAccount(), h);
                        sem.release();
                        return method.invoke(obj, args);
                    }else {  // нашли в кэше
                        res = cash.get(theAccount()).get(m.getName());
                        if (res instanceof Boolean) {
                            sem.release();
                            return (Boolean) res;
                        } else if (res instanceof Double) {
                            sem.release();
                            return (Double) res;
                        } else if (res instanceof Integer) {
                            sem.release();
                            return (Integer) res;
                        } else if (res instanceof Long) {
                            sem.release();
                            return (Long) res;
                        }
                    }
                }catch(InterruptedException e){}
            }

            //метод  не кэшируемый
            return method.invoke(obj, args);
        }

}
