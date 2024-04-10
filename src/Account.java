import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class Account implements Cashable{
    private static final Semaphore sem = new Semaphore(1);
    public  Object getProxy()
    {
        Class cls = this.getClass();
        return  Proxy.newProxyInstance(cls.getClassLoader(),
                new Class[]{Cashable.class},
                new CashableInvHadler(this));
    }
    // конструкторы
    private Account() {}

    public Account(String name) {
        this.setName(name);
        this.amounts = new HashMap<>();
    }

    private String name;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException();
        this.name = name;
    }
    @ObjectStateField
    private HashMap<Currencies, Integer> amounts;

    public HashMap<Currencies, Integer> getAmounts() {
        return new HashMap<>(this.amounts);
    }

    public void setAmounts(Currencies cur, Integer val) {
        if (val < 0) throw new IllegalArgumentException();
        // put
        this.amounts.put(cur, val);
    }
    public void add(Currencies cur, Integer val){
        try {
            sem.acquire();
                setAmounts(cur,getAmounts().get(cur) + val);
            sem.release();
        }catch(InterruptedException e){}
    }
    @Cashe(cashTimer = 1000)
    public Boolean isMultyVal(){
        return this.getAmounts().size() >=2;
    }
    @Cashe
    public Double rub() {
        double result = 0;
        if (getAmounts().containsKey(Currencies.RUB)) {
            result = (double)getAmounts().get(Currencies.RUB) / 100;
        }
        return result;
    }
    @Cashe
    public Double usd() {
        double result = 0;
        if (getAmounts().containsKey(Currencies.USD)) {
            result = (double)getAmounts().get(Currencies.USD) / 100;
        }
        return result;
    }
    @Override
    public String toString() {
        return "Account{" +
                "name='" + name + '\'' +
                ", amounts=" + amounts +
                ", isMultyval=" + isMultyVal() +
//                ", rub=" + rub() +
                '}';
    }
    public void sayAbout(){
        System.out.println(this.toString());
    }
}
