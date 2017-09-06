package android.os.CaseType;

public class ChannelType {

    public static final String BRABDS_KEY = "Brands";
    public static final String LEAF_KEY = "Leaf";
    public static final String NAME_KEY = "Name";
    public static final String Category_KEY = "Category";
    public static final String CHANNEL_KEY = "Channel";
    public static final String PARENT_KEY = "Parent";
    
    String brands;
    String leaf;
    String name;
    String category;
    String channel;
    String parent;
    
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getBrands() {
        return brands;
    }
    public void setBrands(String brands) {
        this.brands = brands;
    }
    public String getLeaf() {
        return leaf;
    }
    public void setLeaf(String leaf) {
        this.leaf = leaf;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getChannel() {
        return channel;
    }
    public void setChannel(String channel) {
        this.channel = channel;
    }
    public String getParent() {
        return parent;
    }
    public void setParent(String parent) {
        this.parent = parent;
    }
    @Override
    public String toString() {
        return "ChannelType [brands=" + brands + ", leaf=" + leaf + ", name=" + name + ", category=" + category + ", channel=" + channel + ", parent=" + parent + "]";
    }

}
