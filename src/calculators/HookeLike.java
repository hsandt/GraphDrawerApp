/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package calculators;

import helper.ForceModelizer;

/**
 *
 * @author Long Nguyen Huu
 */
public class HookeLike implements ForceModelizer {

	/**
	 * distance idéale entre deux noeuds voisins
	 */
    private double k;
    
    /**
     * constructeur
     * crée une instance de l'algorithme semblable à Hooke avec le paramètre k
     * 
     * @param k distance idéale entre deux noeuds voisins
     */
    public HookeLike(double k) {
        this.k = k;
    }
    
    @Override
    public double calculateAttrForce(double dist) {
        return dist/k;
    }

    @Override
    public double calculateRepulsForce(double dist) {
        return -k/dist;
    }
    
}