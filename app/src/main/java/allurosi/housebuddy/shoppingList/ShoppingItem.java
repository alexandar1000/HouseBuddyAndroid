package allurosi.housebuddy.shoppingList;

public class ShoppingItem {
    private String name;
    private String info;
    private boolean done;
    private String shoppingItemId;

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

    public ShoppingItem(ShoppingItem shoppingItem) {
        this.name = shoppingItem.getName();
        this.info = shoppingItem.getInfo();
        this.done = shoppingItem.isDone();
        this.shoppingItemId = shoppingItem.getShoppingItemId();
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

    public String getShoppingItemId() {
        return shoppingItemId;
    }

    public void setShoppingItemId(String shoppingItemId) {
        this.shoppingItemId = shoppingItemId;
    }
}
