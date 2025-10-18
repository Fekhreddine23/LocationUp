package com.mobility.mobility_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cities")
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "city_id")  // ✅ Ajouter cette annotation
    private Integer cityId;    // ✅ CHANGER Long → Integer

    @Column(nullable = false, unique = true)
    private String name;

    // Constructeurs
    public City() {}

    public City(String name) {
        this.name = name;
    }

    // Getters et Setters
    public Integer getCityId() {
        return cityId;
    }

    public void setId(Integer id) {
        this.cityId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
			return true;
		}
        if (!(o instanceof City)) {
			return false;
		}
        City city = (City) o;
        return cityId != null && cityId.equals(city.cityId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "City{" +
                "id=" + cityId +
                ", name='" + name + '\'' +
                '}';
    }
}
