/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package helper;

/**
 * Modélisateur de forces
 * <p>
 * Il calcule des forces d'interaction en fonction de la distance
 * entre les deux noeuds considérés.
 * </p>
 * <p>
 * Les classes implémentant cette interface représentent chacune un modèle de forces,
 * dont il faut construire une instance pour pouvoir calculer les forces associées.
 * </p>
 * @author Long Nguyen Huu
 */
public interface ForceModelizer {
    
    /**
     * Calcule la valeur de la force d'attraction du modèle en fonction de la distance <i>dist</i>
     * et des paramètres du modèle qui sont fournis en attribut des classes l'implémentant
     * @param dist Distance entre les deux noeuds en interaction considérés
     * @return La valeur de la force d'attraction entre les deux noeuds
     */
    public double calculateAttrForce(double dist);
    
    /**
     * Calcule la valeur de la force de répulsion du modèle en fonction de la distance <i>dist</i>
     * et des paramètres du modèle qui sont fournis en attribut des classes l'implémentant
     * @param dist Distance entre les deux noeuds en interaction considérés
     * @return La valeur de la force de répulsion entre les deux noeuds
     */
    public double calculateRepulsForce(double dist);
    
}
