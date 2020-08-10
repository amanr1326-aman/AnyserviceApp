package helper;

import java.io.Serializable;

public class ServiceDetails implements Serializable {
    private String name;
    private String company;
    private String category;
    private String icon;

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    boolean verified;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String description;
    private int unit=1,id,agent_id;
    private float rating;
    private double price;
    private double deliveryCost;

    public double getBalance() {
        return balance;
    }

    private double balance;
    private boolean selected=false,measurable=false,deliveryCostable=false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getAgent_id() {
        return agent_id;
    }

    public void setAgent_id(int agent_id) {
        this.agent_id = agent_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getUnit() {
        return unit;
    }

    public void setUnit(int unit) {
        this.unit = unit;
    }

    public double getTotal_price() {
        return unit*price;
    }

    public boolean isMeasurable() {
        return measurable;
    }

    public void setMeasurable(boolean measurable) {
        this.measurable = measurable;
    }

    public double getDeliveryCost() {
        return deliveryCost;
    }

    public void setDeliveryCost(double deliveryCost) {
        this.deliveryCost = deliveryCost;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public boolean isDeliveryCostable() {
        return deliveryCostable;
    }

    public void setDeliveryCostable(boolean deliveryCostable) {
        this.deliveryCostable = deliveryCostable;
    }
}
