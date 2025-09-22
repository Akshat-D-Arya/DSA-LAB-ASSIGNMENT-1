import java.util.*;

class SparseElement {
    private int row;
    private int col;
    private double value;
    
    public SparseElement(int row, int col, double value) {
        this.row = row;
        this.col = col;
        this.value = value;
    }
    
    public int getRow() { return row; }
    public int getCol() { return col; }
    public double getValue() { return value; }
    
    public void setValue(double value) { this.value = value; }
    
    @Override
    public String toString() {
        return String.format("(%d,%d,%.2f)", row, col, value);
    }
}

public class InventoryManagementSystem {
    
    private Object[][] itemArray;           
    private List<SparseElement> sparseMatrix;  
    private double[][] priceQuantityTable;  

    private int maxItems;
    private int currentItemCount;
    private boolean useRowMajor;  
    private Map<String, Integer> itemNameToIndex;
    private Map<Integer, String> indexToItemName;
    
    private static final int ID_COL = 0;
    private static final int NAME_COL = 1;
    private static final int QUANTITY_COL = 2;
    private static final int PRICE_COL = 3;
    private static final int RESTOCK_FREQ_COL = 4;
    

    public InventoryManagementSystem(int maxItems, boolean useRowMajor) {
        this.maxItems = maxItems;
        this.currentItemCount = 0;
        this.useRowMajor = useRowMajor;
        this.itemArray = new Object[maxItems][5]; 
        this.sparseMatrix = new ArrayList<>();
        this.priceQuantityTable = new double[maxItems][2]; 
        this.itemNameToIndex = new HashMap<>();
        this.indexToItemName = new HashMap<>();
        
        System.out.println("Inventory Management System initialized with " + maxItems + 
                         " slots using " + (useRowMajor ? "Row-Major" : "Column-Major") + " ordering");
    }
    
    public boolean addItemRecord(int id, String name, int quantity, double price, int restockFreq) {
        try {
            if (currentItemCount >= maxItems) {
                System.out.println("Error: Inventory is full");
                return false;
            }

            if (searchByItem(id) != -1 || searchByItem(name) != -1) {
                System.out.println("Error: Item with ID " + id + " or name '" + name + "' already exists");
                return false;
            }

            if (name == null || name.trim().isEmpty()) {
                System.out.println("Error: Item name cannot be null or empty");
                return false;
            }
            if (quantity < 0 || price < 0 || restockFreq < 0) {
                System.out.println("Error: Quantity, price, and restock frequency must be non-negative");
                return false;
            }
            
            int index = currentItemCount;

            itemArray[index][ID_COL] = id;
            itemArray[index][NAME_COL] = name;
            itemArray[index][QUANTITY_COL] = quantity;
            itemArray[index][PRICE_COL] = price;
            itemArray[index][RESTOCK_FREQ_COL] = restockFreq;

            itemNameToIndex.put(name.toLowerCase(), index);
            indexToItemName.put(index, name);

            managePriceQuantity(index, price, quantity);

            if (restockFreq > 90) {
                optimizeSparseStorage(index, price, quantity);
            }
            
            currentItemCount++;
            System.out.println("Successfully added item: " + name + " (ID: " + id + ")");
            return true;
            
        } catch (Exception e) {
            System.out.println("Error adding item: " + e.getMessage());
            return false;
        }
    }

    public boolean removeItemRecord(Object identifier) {
        try {
            int index = searchByItem(identifier);
            if (index == -1) {
                System.out.println("Error: Item not found");
                return false;
            }
            
            String itemName = (String) itemArray[index][NAME_COL];
            int itemId = (Integer) itemArray[index][ID_COL];

            sparseMatrix.removeIf(element -> element.getRow() == index);

            for (int i = index; i < currentItemCount - 1; i++) {
            
                for (int j = 0; j < 5; j++) {
                    itemArray[i][j] = itemArray[i + 1][j];
                }

                priceQuantityTable[i][0] = priceQuantityTable[i + 1][0];
                priceQuantityTable[i][1] = priceQuantityTable[i + 1][1];
            }

            for (int j = 0; j < 5; j++) {
                itemArray[currentItemCount - 1][j] = null;
            }
            priceQuantityTable[currentItemCount - 1][0] = 0;
            priceQuantityTable[currentItemCount - 1][1] = 0;

            itemNameToIndex.remove(itemName.toLowerCase());
            indexToItemName.remove(index);

            for (int i = index; i < currentItemCount - 1; i++) {
                String name = (String) itemArray[i][NAME_COL];
                itemNameToIndex.put(name.toLowerCase(), i);
                indexToItemName.put(i, name);
            }

            for (SparseElement element : sparseMatrix) {
                if (element.getRow() > index) {
                
                    SparseElement newElement = new SparseElement(element.getRow() - 1, 
                                                               element.getCol(), element.getValue());
                    sparseMatrix.set(sparseMatrix.indexOf(element), newElement);
                }
            }
            
            currentItemCount--;
            System.out.println("Successfully removed item: " + itemName + " (ID: " + itemId + ")");
            return true;
            
        } catch (Exception e) {
            System.out.println("Error removing item: " + e.getMessage());
            return false;
        }
    }
    
    public int searchByItem(Object identifier) {
        try {
            if (identifier instanceof Integer) {

                int id = (Integer) identifier;
                for (int i = 0; i < currentItemCount; i++) {
                    if (itemArray[i][ID_COL] != null && (Integer) itemArray[i][ID_COL] == id) {
                        return i;
                    }
                }
            } else if (identifier instanceof String) {
               
                String name = ((String) identifier).toLowerCase();
                return itemNameToIndex.getOrDefault(name, -1);
            }
            return -1;
        } catch (Exception e) {
            System.out.println("Error searching item: " + e.getMessage());
            return -1;
        }
    }

    public void managePriceQuantity(int index, double price, double quantity) {
        try {
            if (useRowMajor) {
            
                priceQuantityTable[index][0] = price;
                priceQuantityTable[index][1] = quantity;
            } else {

                priceQuantityTable[index][0] = quantity;
                priceQuantityTable[index][1] = price;
            }
        } catch (Exception e) {
            System.out.println("Error managing price-quantity data: " + e.getMessage());
        }
    }
    

    public void optimizeSparseStorage(int itemIndex, double price, double quantity) {
        try {

            if (price > 0) {
                sparseMatrix.add(new SparseElement(itemIndex, 0, price));
            }
            if (quantity > 0) {
                sparseMatrix.add(new SparseElement(itemIndex, 1, quantity));
            }
            
            System.out.println("Added item " + indexToItemName.get(itemIndex) + " to sparse storage");
        } catch (Exception e) {
            System.out.println("Error optimizing sparse storage: " + e.getMessage());
        }
    }
    
    public void displayAllItems() {
        System.out.println("\n=== INVENTORY ITEMS ===");
        if (currentItemCount == 0) {
            System.out.println("Inventory is empty");
            return;
        }
        
        System.out.printf("%-5s %-15s %-10s %-10s %-12s\n", "ID", "Name", "Quantity", "Price", "RestockFreq");
        System.out.println("--------------------------------------------------------");
        
        for (int i = 0; i < currentItemCount; i++) {
            System.out.printf("%-5d %-15s %-10d $%-9.2f %-12d\n",
                (Integer) itemArray[i][ID_COL],
                (String) itemArray[i][NAME_COL],
                (Integer) itemArray[i][QUANTITY_COL],
                (Double) itemArray[i][PRICE_COL],
                (Integer) itemArray[i][RESTOCK_FREQ_COL]);
        }
        System.out.println("Total items: " + currentItemCount);
    }

    public void displayPriceQuantityTable() {
        System.out.println("\n=== PRICE-QUANTITY TABLE (" + 
                         (useRowMajor ? "Row-Major" : "Column-Major") + ") ===");
        
        if (useRowMajor) {
            System.out.printf("%-15s %-10s %-10s\n", "Item", "Price", "Quantity");
        } else {
            System.out.printf("%-15s %-10s %-10s\n", "Item", "Quantity", "Price");
        }
        System.out.println("---------------------------------------");
        
        for (int i = 0; i < currentItemCount; i++) {
            String itemName = indexToItemName.get(i);
            System.out.printf("%-15s $%-9.2f %-10.0f\n", 
                            itemName, priceQuantityTable[i][0], priceQuantityTable[i][1]);
        }
    }

    public void displaySparseMatrix() {
        System.out.println("\n=== SPARSE MATRIX (Rarely Restocked Items) ===");
        if (sparseMatrix.isEmpty()) {
            System.out.println("No items in sparse storage");
            return;
        }
        
        System.out.println("Format: (row, col, value) where col 0=price, col 1=quantity");
        for (SparseElement element : sparseMatrix) {
            String itemName = indexToItemName.get(element.getRow());
            String colType = element.getCol() == 0 ? "price" : "quantity";
            System.out.println(element + " -> " + itemName + " (" + colType + ")");
        }
    }

    public String getItemDetails(Object identifier) {
        int index = searchByItem(identifier);
        if (index == -1) {
            return null;
        }
        
        return String.format("ID: %d, Name: %s, Quantity: %d, Price: $%.2f, RestockFreq: %d days",
            (Integer) itemArray[index][ID_COL],
            (String) itemArray[index][NAME_COL],
            (Integer) itemArray[index][QUANTITY_COL],
            (Double) itemArray[index][PRICE_COL],
            (Integer) itemArray[index][RESTOCK_FREQ_COL]);
    }

    public boolean updateQuantity(Object identifier, int newQuantity) {
        int index = searchByItem(identifier);
        if (index == -1) {
            System.out.println("Error: Item not found");
            return false;
        }
        
        if (newQuantity < 0) {
            System.out.println("Error: Quantity must be non-negative");
            return false;
        }
        
        int oldQuantity = (Integer) itemArray[index][QUANTITY_COL];
        itemArray[index][QUANTITY_COL] = newQuantity;

        double price = (Double) itemArray[index][PRICE_COL];
        managePriceQuantity(index, price, newQuantity);

        for (SparseElement element : sparseMatrix) {
            if (element.getRow() == index && element.getCol() == 1) {
                element.setValue(newQuantity);
                break;
            }
        }
        
        System.out.println("Updated quantity for " + itemArray[index][NAME_COL] + 
                         " from " + oldQuantity + " to " + newQuantity);
        return true;
    }

    public void displaySystemStats() {
        System.out.println("\n=== SYSTEM STATISTICS ===");
        System.out.println("Total capacity: " + maxItems);
        System.out.println("Current items: " + currentItemCount);
        System.out.println("Available slots: " + (maxItems - currentItemCount));
        System.out.println("Memory organization: " + (useRowMajor ? "Row-Major" : "Column-Major"));
        System.out.println("Sparse matrix entries: " + sparseMatrix.size());
        System.out.println("Items in sparse storage: " + (sparseMatrix.size() / 2)); // price + quantity per item
        
        double totalValue = 0;
        for (int i = 0; i < currentItemCount; i++) {
            totalValue += (Integer) itemArray[i][QUANTITY_COL] * (Double) itemArray[i][PRICE_COL];
        }
        System.out.printf("Total inventory value: $%.2f\n", totalValue);
    }

    public static void main(String[] args) {
        System.out.println("=== INVENTORY MANAGEMENT SYSTEM DEMONSTRATION ===\n");

        InventoryManagementSystem inventory = new InventoryManagementSystem(10, true);
        
        System.out.println("\n1. Testing addItemRecord method:");
        inventory.addItemRecord(101, "Laptop", 25, 999.99, 30);
        inventory.addItemRecord(102, "Mouse", 100, 25.50, 60);
        inventory.addItemRecord(103, "Keyboard", 50, 75.00, 45);
        inventory.addItemRecord(104, "Vintage Monitor", 5, 299.99, 120); // Rarely restocked
        inventory.addItemRecord(105, "Antique Printer", 2, 150.00, 180); // Rarely restocked
        
        System.out.println("\n2. Display all items:");
        inventory.displayAllItems();
        
        System.out.println("\n3. Display Price-Quantity Table:");
        inventory.displayPriceQuantityTable();
        
        System.out.println("\n4. Display Sparse Matrix:");
        inventory.displaySparseMatrix();
        
        System.out.println("\n5. Testing searchByItem method:");
        int index = inventory.searchByItem(102);
        if (index != -1) {
            System.out.println("Found by ID: " + inventory.getItemDetails(102));
        }
        
        index = inventory.searchByItem("keyboard");
        if (index != -1) {
            System.out.println("Found by name: " + inventory.getItemDetails("keyboard"));
        }
        
        System.out.println("\n6. Testing updateQuantity:");
        inventory.updateQuantity("Mouse", 80);
        
        System.out.println("\n7. Testing removeItemRecord:");
        inventory.removeItemRecord(103); 
        
        System.out.println("\n8. Inventory after removal:");
        inventory.displayAllItems();
        
        System.out.println("\n9. Updated Price-Quantity Table:");
        inventory.displayPriceQuantityTable();
        
        System.out.println("\n10. Updated Sparse Matrix:");
        inventory.displaySparseMatrix();
        
        inventory.displaySystemStats();
        
        System.out.println("\n=== Testing Column-Major System ===");
        InventoryManagementSystem colMajorInventory = new InventoryManagementSystem(5, false);
        colMajorInventory.addItemRecord(201, "Tablet", 15, 599.99, 25);
        colMajorInventory.addItemRecord(202, "Headphones", 30, 199.99, 40);
        
        System.out.println("\nColumn-Major Price-Quantity Table:");
        colMajorInventory.displayPriceQuantityTable();
    }
}