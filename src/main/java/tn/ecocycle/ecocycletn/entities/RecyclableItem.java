package tn.ecocycle.ecocycletn.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "recyclable_items")
public class RecyclableItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantityKg;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceTnd;

    @Column(nullable = false)
    private String location;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    protected RecyclableItem() {
    }

    public RecyclableItem(
            String title,
            String description,
            Category category,
            BigDecimal quantityKg,
            BigDecimal priceTnd,
            String location,
            User owner
    ) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.quantityKg = quantityKg;
        this.priceTnd = priceTnd;
        this.location = location;
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public BigDecimal getQuantityKg() {
        return quantityKg;
    }

    public BigDecimal getPriceTnd() {
        return priceTnd;
    }

    public String getLocation() {
        return location;
    }

    public User getOwner() {
        return owner;
    }

    public void updateDetails(
            String title,
            String description,
            Category category,
            BigDecimal quantityKg,
            BigDecimal priceTnd,
            String location
    ) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.quantityKg = quantityKg;
        this.priceTnd = priceTnd;
        this.location = location;
    }
}
