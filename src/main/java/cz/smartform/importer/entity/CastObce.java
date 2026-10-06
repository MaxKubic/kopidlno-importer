package cz.smartform.importer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cast_obce")
public class CastObce {

    @Id
    @Column(name = "kod")
    private Integer kod;

    @Column(name = "nazev", nullable = false)
    private String nazev;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "obec_kod", nullable = false)
    private Obec obec;

    public CastObce() {
    }

    public CastObce(Integer kod, String nazev, Obec obec) {
        this.kod = kod;
        this.nazev = nazev;
        this.obec = obec;
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

    public Obec getObec() {
        return obec;
    }

    public void setObec(Obec obec) {
        this.obec = obec;
    }
}