package allurosi.housebuddy.shoppingList;

public class ShoppingItem {
    private String name;
    private String info;
    private boolean done;

    public ShoppingItem(String name, String info) {
        this.name = name;
        this.info = info;
        this.done = true;
    }

    public ShoppingItem(String name, String info, boolean done) {
        this.name = name;
        this.info = info;
        this.done = done;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public boolean isDone() {
        return done;
    }
}
