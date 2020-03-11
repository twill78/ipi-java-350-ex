package com.ipiecoles.java.java350.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;

@Entity
public class Employe {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String nom;

    private String prenom;

    private String matricule;

    private LocalDate dateEmbauche;

    private Double salaire = Entreprise.SALAIRE_BASE;

    private Integer performance = Entreprise.PERFORMANCE_BASE;

    private Double tempsPartiel = 1.0;

    public Employe() {
    }

    public Employe(String nom, String prenom, String matricule, LocalDate dateEmbauche, Double salaire, Integer performance, Double tempsPartiel) {
        this.nom = nom;
        this.prenom = prenom;
        this.matricule = matricule;
        this.dateEmbauche = dateEmbauche;
        this.salaire = salaire;
        this.performance = performance;
        this.tempsPartiel = tempsPartiel;
    }

    public Integer getNombreAnneeAnciennete() {
    	
    	if (dateEmbauche != null && dateEmbauche.isBefore(LocalDate.now())) {
    		return LocalDate.now().getYear() - dateEmbauche.getYear();
    	}
    	return 0;
        
    }

    public Integer getNbConges() {
        return Entreprise.NB_CONGES_BASE + this.getNombreAnneeAnciennete();
    }

    public Integer getNbRtt(){
        return getNbRtt(LocalDate.now());
    }

    /**
     * Méthode qui retourne le nombre de jours de RTT pour une année donnée
     * @param d : la date à considérer
     * @return : le nombre de jours de RTT trouvés sous forme d'integer
     */
    public Integer getNbRtt(LocalDate d){
        int nbJoursParAn = d.isLeapYear() ? 365 : 366;
        int nbJoursWeekEnd = 104;
        // En fonction du premier jour de l'année considérée, on change le nombre de jours de week-end
        switch (LocalDate.of(d.getYear(),1,1).getDayOfWeek()){
            case FRIDAY:
            	if(d.isLeapYear()) {
            		nbJoursWeekEnd = nbJoursWeekEnd + 2;
            	} else { 
            		nbJoursWeekEnd = nbJoursWeekEnd + 1;
            	}
            	break;
            case SATURDAY:
            	if(d.isLeapYear()) {
            		nbJoursWeekEnd = nbJoursWeekEnd + 1;
            	} else { 
            		nbJoursWeekEnd = nbJoursWeekEnd + 2;
            	}
            	break;
            case SUNDAY:
            	if(d.isLeapYear()) {
            		nbJoursWeekEnd = nbJoursWeekEnd - 3;
            	} else { 
            		nbJoursWeekEnd = nbJoursWeekEnd + 2;
            	}
            	break;
        	default:
	        	if(d.isLeapYear()) {
            		nbJoursWeekEnd = nbJoursWeekEnd - 1;
            	} else { 
            		nbJoursWeekEnd = nbJoursWeekEnd + 1;
            	}
            	break;
        }
        int nbJoursFeriesEnSemaine = (int) Entreprise.joursFeries(d).stream().filter(localDate -> localDate.getDayOfWeek().getValue() <= DayOfWeek.FRIDAY.getValue()).count();
        return (int) Math.ceil((nbJoursParAn - Entreprise.NB_JOURS_MAX_FORFAIT - nbJoursWeekEnd - Entreprise.NB_CONGES_BASE - nbJoursFeriesEnSemaine) * tempsPartiel);
    }

    /**
     * Calcul de la prime annuelle selon la règle :
     * Pour les managers : Prime annuelle de base bonnifiée par l'indice prime manager
     * Pour les autres employés, la prime de base plus éventuellement la prime de performance calculée si l'employé
     * n'a pas la performance de base, en multipliant la prime de base par un l'indice de performance
     * (égal à la performance à laquelle on ajoute l'indice de prime de base)
     *
     * Pour tous les employés, une prime supplémentaire d'ancienneté est ajoutée en multipliant le nombre d'année
     * d'ancienneté avec la prime d'ancienneté. La prime est calculée au pro rata du temps de travail de l'employé
     *
     * @return la prime annuelle de l'employé en Euros et cents
     */
    public Double getPrimeAnnuelle(){
        //Calcule de la prime d'ancienneté
        Double primeAnciennete = Entreprise.PRIME_ANCIENNETE * this.getNombreAnneeAnciennete();
        Double prime;
        //Prime du manager (matricule commençant par M) : Prime annuelle de base multipliée par l'indice prime manager
        //plus la prime d'anciennté.
        if(matricule != null && matricule.startsWith("M")) {
            prime = Entreprise.primeAnnuelleBase() * Entreprise.INDICE_PRIME_MANAGER + primeAnciennete;
        }
        //Pour les autres employés en performance de base, uniquement la prime annuelle plus la prime d'ancienneté.
        else if (this.performance == null || Entreprise.PERFORMANCE_BASE.equals(this.performance)){
            prime = Entreprise.primeAnnuelleBase() + primeAnciennete;
        }
        //Pour les employés plus performance, on bonnifie la prime de base en multipliant par la performance de l'employé
        // et l'indice de prime de base.
        else {
            prime = Entreprise.primeAnnuelleBase() * (this.performance + Entreprise.INDICE_PRIME_BASE) + primeAnciennete;
        }
        //Au pro rata du temps partiel.
        return prime * this.tempsPartiel;
    }


    /**
     * Méthode qui augmente le salaire d'un employé selon un pourcentage donné.
     * On part du principe qu'on ne peut qu'augmenter un salaire (pourcentage positif).
     * Si l'on doit diminuer un salaire, cela fera l'objet d'une nouvelle méthode (diminuerSalaire par ex.)
     * @param pourcentage : le pourcentage d'augmentation sous forme de double
     */
    public void augmenterSalaire(double pourcentage){
    	if (pourcentage >= 0 && salaire != null) {
    		this.setSalaire((this.getSalaire()*pourcentage)+this.getSalaire());
    	} else {
    		this.setSalaire(this.getSalaire());
    	}
    	
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the nom
     */
    public String getNom() {
        return nom;
    }

    /**
     * @param nom the nom to set
     */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /**
     * @return the prenom
     */
    public String getPrenom() {
        return prenom;
    }

    /**
     * @param prenom the prenom to set
     */
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    /**
     * @return the matricule
     */
    public String getMatricule() {
        return matricule;
    }

    /**
     * @param matricule the matricule to set
     */
    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    /**
     * @return the dateEmbauche
     */
    public LocalDate getDateEmbauche() {
        return dateEmbauche;
    }

    /**
     * @param dateEmbauche the dateEmbauche to set
     */
    public void setDateEmbauche(LocalDate dateEmbauche) {
        this.dateEmbauche = dateEmbauche;
    }

    /**
     * @return the salaire
     */
    public Double getSalaire() {
        return salaire;
    }

    /**
     * @param salaire the salaire to set
     */
    public void setSalaire(Double salaire) {
        this.salaire = salaire;
    }

    public Integer getPerformance() {
        return performance;
    }

    public void setPerformance(Integer performance) {
        this.performance = performance;
    }

    public Double getTempsPartiel() {
        return tempsPartiel;
    }

    public void setTempsPartiel(Double tempsPartiel) {
        this.tempsPartiel = tempsPartiel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employe)) return false;
        Employe employe = (Employe) o;
        return Objects.equals(id, employe.id) &&
                Objects.equals(nom, employe.nom) &&
                Objects.equals(prenom, employe.prenom) &&
                Objects.equals(matricule, employe.matricule) &&
                Objects.equals(dateEmbauche, employe.dateEmbauche) &&
                Objects.equals(salaire, employe.salaire) &&
                Objects.equals(performance, employe.performance);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom, prenom, matricule, dateEmbauche, salaire, performance);
    }
}
