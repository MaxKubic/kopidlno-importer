package cz.smartform.importer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "obec")
public class Obec {

    @Id
    @Column(name = "kod")
    private Integer kod;

    @Column(name = "nazev", nullable = false)
    private String nazev;

    public Obec() {
    }

    public Obec(Integer kod, String nazev) {
        this.kod = kod;
        this.nazev = nazev;
    }

    public Integer getKod() {
        return kod;
    }

    public void setKod(Integer kod) {
        this.kod = kod;
    }

    public String getNazev() {
        return nazev;
    }

    public void setNazev(String nazev) {
        this.nazev = nazev;
    }
}