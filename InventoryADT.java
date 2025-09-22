import java.util.*;

class InventoryItem {
    private int itemID;
    private String itemName;
    private int quantity;
    private double price;

    public InventoryItem(int itemID, String itemName, int quantity, double price) {
        this.itemID = itemID;
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
    }

    public int getItemID() {
        return itemID;
    }
    
    public String getItemName() {
        return itemName;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public double getPrice() {
        return price;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }
    
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    @Override
    public String toString() {
        return String.format("ID: %d, Name: %s, Quantity: %d, Price: $%.2f", 
                           itemID, itemName, quantity, price);
    }
}

public class InventoryADT {
    private ArrayList<InventoryItem> items;
    private int nextID;

    public InventoryADT() {
        this.items = new ArrayList<>();
        this.nextID = 1;
    }

    public boolean insertItem(Map<String, Object> data) {
        try {
        
            if (!data.containsKey("itemName") || !data.containsKey("quantity") || !data.containsKey("price")) {
                System.out.println("Error: Missing required fields (itemName, quantity, price)");
                return false;
            }

            String itemName = (String) data.get("itemName");
            if (itemName == null || itemName.trim().isEmpty()) {
                System.out.println("Error: itemName cannot be null or empty");
                return false;
            }
            
            int quantity;
            if (data.get("quantity") instanceof Integer) {
                quantity = (Integer) data.get("quantity");
            } else {
                System.out.println("Error: quantity must be an integer");
                return false;
            }
            
            if (quantity < 0) {
                System.out.println("Error: quantity must be non-negative");
                return false;
            }
            
            double price;
            if (data.get("price") instanceof Number) {
                price = ((Number) data.get("price")).doubleValue();
            } else {
                System.out.println("Error: price must be a number");
                return false;
            }
            
            if (price < 0) {
                System.out.println("Error: price must be non-negative");
                return false;
            }

            int itemID;
            if (data.containsKey("itemID")) {
                if (!(data.get("itemID") instanceof Integer)) {
                    System.out.println("Error: itemID must be an integer");
                    return false;
                }
                itemID = (Integer) data.get("itemID");

                if (searchItem(itemID) != null) {
                    System.out.println("Error: Item with ID " + itemID + " already exists");
                    return false;
                }

                if (itemID >= nextID) {
                    nextID = itemID + 1;
                }
            } else {
                itemID = nextID++;
            }

            InventoryItem newItem = new InventoryItem(itemID, itemName, quantity, price);
            items.add(newItem);
            System.out.println("Successfully added item: " + newItem);
            return true;
            
        } catch (Exception e) {
            System.out.println("Error inserting item: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteItem(int itemID) {
        try {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getItemID() == itemID) {
                    InventoryItem deletedItem = items.remove(i);
                    System.out.println("Successfully deleted item: " + deletedItem);
                    return true;
                }
            }
            
            System.out.println("Error: Item with ID " + itemID + " not found");
            return false;
            
        } catch (Exception e) {
            System.out.println("Error deleting item: " + e.getMessage());
            return false;
        }
    }

    public InventoryItem searchItem(Object searchKey) {
        try {
            for (InventoryItem item : items) {
                // Search by ItemID
                if (searchKey instanceof Integer && item.getItemID() == (Integer) searchKey) {
                    return item;
                }
                // Search by ItemName (case-insensitive)
                else if (searchKey instanceof String && 
                        item.getItemName().equalsIgnoreCase((String) searchKey)) {
                    return item;
                }
            }
            return null;
            
        } catch (Exception e) {
            System.out.println("Error searching item: " + e.getMessage());
            return null;
        }
    }

    public void displayAllItems() {
        if (items.isEmpty()) {
            System.out.println("Inventory is empty");
            return;
        }
        
        System.out.println("\n=== INVENTORY ITEMS ===");
        for (InventoryItem item : items) {
            System.out.println(item);
        }
        System.out.println("Total items: " + items.size());
    }

    public double getTotalValue() {
        double totalValue = 0.0;
        for (InventoryItem item : items) {
            totalValue += item.getQuantity() * item.getPrice();
        }
        return totalValue;
    }

    public boolean updateQuantity(int itemID, int newQuantity) {
        InventoryItem item = searchItem(itemID);
        if (item != null) {
            if (newQuantity >= 0) {
                int oldQuantity = item.getQuantity();
                item.setQuantity(newQuantity);
                System.out.println("Updated item " + itemID + " quantity from " + 
                                 oldQuantity + " to " + newQuantity);
                return true;
            } else {
                System.out.println("Error: Quantity must be non-negative");
                return false;
            }
        } else {
            System.out.println("Error: Item with ID " + itemID + " not found");
            return false;
        }
    }

    public int getItemCount() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public static void main(String[] args) {

        InventoryADT inventory = new InventoryADT();
        
        System.out.println("=== INVENTORY ADT DEMONSTRATION ===\n");

        System.out.println("1. Testing insertItem method:");
        
        Map<String, Object> laptopData = new HashMap<>();
        laptopData.put("itemName", "Laptop");
        laptopData.put("quantity", 10);
        laptopData.put("price", 999.99);
        inventory.insertItem(laptopData);
        
        Map<String, Object> mouseData = new HashMap<>();
        mouseData.put("itemName", "Mouse");
        mouseData.put("quantity", 50);
        mouseData.put("price", 25.50);
        inventory.insertItem(mouseData);
        
        Map<String, Object> keyboardData = new HashMap<>();
        keyboardData.put("itemID", 100);
        keyboardData.put("itemName", "Keyboard");
        keyboardData.put("quantity", 30);
        keyboardData.put("price", 75.00);
        inventory.insertItem(keyboardData);

        Map<String, Object> invalidData = new HashMap<>();
        invalidData.put("itemName", "Invalid Item");
        invalidData.put("quantity", -5);
        invalidData.put("price", 10.00);
        inventory.insertItem(invalidData);

        System.out.println("\n2. Display all items:");
        inventory.displayAllItems();

        System.out.println("\n3. Testing searchItem method:");
        InventoryItem foundItem = inventory.searchItem(1);
        if (foundItem != null) {
            System.out.println("Found by ID: " + foundItem);
        }
        
        foundItem = inventory.searchItem("mouse");
        if (foundItem != null) {
            System.out.println("Found by name: " + foundItem);
        }
        
        foundItem = inventory.searchItem(999);
        if (foundItem == null) {
            System.out.println("Item with ID 999 not found");
        }

        System.out.println("\n4. Testing deleteItem method:");
        inventory.deleteItem(2); 
        inventory.deleteItem(999);
        
        System.out.println("\n5. Inventory after deletion:");
        inventory.displayAllItems();

        System.out.printf("\n6. Total inventory value: $%.2f\n", inventory.getTotalValue());
        
        System.out.println("\n7. Testing quantity update:");
        inventory.updateQuantity(1, 15);
        
        System.out.println("\n8. Final inventory state:");
        inventory.displayAllItems();
        
        System.out.println("\n9. Inventory statistics:");
        System.out.println("Total items: " + inventory.getItemCount());
        System.out.println("Is empty: " + inventory.isEmpty());
    }
}