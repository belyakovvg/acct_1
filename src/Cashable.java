public interface Cashable {
    public void setName(String name);
    public void setAmounts(Currencies cur, Integer val);
    public void add(Currencies cur, Integer val);
    public Boolean isMultyVal();
    public void sayAbout();



    public Double rub();
    public Double usd();
}

