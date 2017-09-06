package entity;

public class LaboratoryReport {

    private int id;
    private String name;
    private String sort;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public LaboratoryReport(int id, String name, String sort) {

        this.id = id;
        this.name = name;
        this.sort = sort;

    }

    public LaboratoryReport() {

    }

}
