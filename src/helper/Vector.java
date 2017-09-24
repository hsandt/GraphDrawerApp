/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package helper;

/**
 * Outil de modélisation des vecteurs force, vitesse, position
 *
 * @author Long Nguyen Huu
 */
public class Vector {
    private double x;
    private double y;
    
    /**
     * initialise le vecteur avec les composantes <i>x</i> et <i>y</i>
     * @param x 1ère composante
     * @param y 2ème composante
     */
    public Vector(double x, double y) {
        setComponents(x,y);
    }
    
    /**
     * 
     * @return composante x
     */
    public double getX() {
        return x;
    }
    
    /**
     * 
     * @return composante y
     */
    public double getY() {
        return y;
    }
    
    /**
     * setter pour les composantes x et y
     * @param x 1ère composante
     * @param y 2ème composante
     */
    public void setComponents(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * affecte le vecteur avec le vecteur (v1,v2)
     * @param v1 vecteur de référence
     * @param v2 vecteur d'arrivée
     */
    public void setComponents(Vector v1, Vector v2) {
        this.x = v2.x - v1.x;
        this.y = v2.y - v1.y;
    }
    
    /**
     * affecte le vecteur avec {@code scale*v}
     * @param v vecteur à mettre à l'échelle
     * @param scale paramètre d'échelle
     */
    public void setComponents(Vector v, double scale) {
        this.x = v.x * scale;
        this.y = v.y * scale;
    }
    
    /**
     * ajoute le vecteur otherVector au vecteur this (sur place)
     * @param otherVector vecteur à ajouter
     */
    public void add(Vector otherVector) {
        x += otherVector.x;
        y += otherVector.y;
    }
    
    /**
     * multiple le vecteur sur place par {@code scale}
     * @param scale
     */
    public void multiply(double scale) {
        setComponents(this, scale);
    }

    /**
     * calcule la norme au carré du vecteur
     * 
     * @return double norme du vecteur au carré
     */
    public double sqNorm() {
        return x*x + y*y;
    }
    
    /**
     * calcule la norme du vecteur
     * 
     * @return double norme du vecteur
     */
    public double norm() {
        return Math.sqrt(sqNorm());
    }
    /**
     * renvoie la chaîne "({@code x},{@code y})" des composantes du vecteur
     */
    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
    
}
