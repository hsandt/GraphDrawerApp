/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calculators;

import helper.ForceModelizer;

/**
 * Modèle d'Eades
 * <p>
 * Calcule des forces d'attraction logarithmique
 * </p>
 * 
 * @author Long Nguyen Huu
 */
public class Eades implements ForceModelizer {
	
	/**
	 * distance idéale entre deux noeuds voisins
	 */
    private double k;
    /**
     * coefficient pour la force d'attraction
     */
    private double cAttr;
    /**
     * coefficient pour la force de répulsion
     */
    private double cRepuls;
    
    /**
     * constructeur
     * crée une instance de l'algorithme semblable à Hooke avec le paramètre k
     * 
     * @param   k       distance idéale entre deux noeuds voisins
     * @param   cAttr   coefficient pour la force d'attraction
     * @param   cRepuls coefficient pour la force de répulsion
     */
    public Eades(double k, double cAttr, double cRepuls) {
        this.k = k;
        this.cAttr = cAttr;
        this.cRepuls = cRepuls;
    }
    
    @Override
    public double calculateAttrForce(double dist) {
        return cAttr*Math.log(dist/k);
    }

    @Override
    public double calculateRepulsForce(double dist) {
        return -cRepuls/(dist*dist);
    }
            
}