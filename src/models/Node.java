/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import helper.Vector;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Modèle de noeud
 * 
 * Un noeud est défini par son indice et des propriétés 2D.
 * De plus, il connaît ses voisins.
 * 
 * @author Long Nguyen Huu
 */
public class Node {
    
    /**
     * Indice courant
     * 
     * Compte le nombre de noeuds déjà créés jusqu'à présent.
     * Permet d'indexer les noeuds.
     */
    private static int currentIndex = 0;

    /**
     * Indice / index / n° du noeud
     */
    private int index;
    
    /**
     * position absolue dans le plan
     */
    private Vector position;
    
    /**
     * vitesse dans le plan
     */
    private Vector speed;
    
    /**
     * liste des voisins
     */
    private ArrayList<Node> neighboursList;
    
    /**
     * construit un noeud dépourvu de propriétés 2D, et sans voisin
     */
    public Node(){
        
        // on attribue au noeud l'indice courant, puis on incrémente ce dernier
        // pour le prochain noeud
        index = currentIndex;
        currentIndex++;
        
        // en attendant le reset()
        position = null;
        speed = null;
        
        // on commence avec une liste de voisins vide
        neighboursList = new ArrayList<Node>();
        
    }
    
    /**
     * réinitialise le noeud (bouton Reset)
     */
    public void reset() {
        
        /*
         * on attribue des positions aléatoirement aux noeuds dans la zone de travail (du modèle et non de la vue)
         * on est presque sûr que deux noeuds ne seront pas au même endroit,
         * mais on pourrait toujours s'assurer que le noeuds assurent une distance minimale au départ,
         * par exemple en les plaçant sur un quadrillage, en ajoutant une légère variation
         * dx et dy pour éviter que les noeuds soient alignés
         */
        position = new Vector(Math.random()*Math.sqrt(Graph.AREA),Math.random()*Math.sqrt(Graph.AREA));
        
        // vitesse initiale du noeud nulle
        speed = new Vector(0, 0);
        
    }
    
    /**
     * @return l'index du noeud
     */
    public int getIndex() {
        return index;
    }
    
    /**
     * @return le vecteur position du noeud
     */
    public Vector getPosition() {
        return position;
    }
    
    /**
     * 
     * @return la liste des voisins du noeud
     */
    public ArrayList<Node> getNeighbours() {
        return neighboursList;
    }

    /**
     * Ajoute {@code node} à la liste des voisins du noeud. Ne sert qu'à {@link #bindWith binidWith}.
     * @param node le voisin à ajouter
     */
    private void addNeighbour(Node node) {
        neighboursList.add(node);
    }
    
    /**
     * Lie le noeud à <i>node</i>
     * i.e. ajoute l'un à la liste des voisins de l'autre et inversement.
     * <p>Utilisé dans le constructeur de {@link Graph}</p>
     * @param node le voisin à relier
     */
    public void bindWith(Node node) {
        addNeighbour(node);
        node.addNeighbour(this);
    }
    
    /**
     * 
     * @return la vitesse du noeud
     */
    public Vector getSpeed() {
        return speed;
    }
    
    /**
     * applique le TAM élémentaire au noeud avec la force <i>force</i>,
     * le pas temporel <i>timestep</i> et le facteur de ralentissement <i>damping</i>
     * @param force force totale appliquée au noeud sur l'intervalle de temps élémentaire
     * @param timestep pas temporel ou longueur de l'intervalle de temps élémentaire
     * @param damping facteur de ralentissement, entre 0 et 1
     * @return nouvelle position du noeud
     */
    public Vector applyForce(Vector force, double timestep, double damping) {
        force.multiply(timestep);
        speed.add(force);
        speed.multiply(damping);
        Vector displ = speed; // TODO améliorer le code
        displ.multiply(timestep);
        move(displ);
        //            System.out.println("speed of " + u.getIndex() + ": " + u.getSpeed());
        return position;
    }
    
    /**
     * déplace le noeud selon displ. Ne sert qu'à {@link #applyForce applyForce}.
     * @param displ le vecteur déplacement
     */
    private void move(Vector displ) {
        position.add(displ);
    }
    
    /**
     * 
     * @return une chaîne contenant l'indice, la position, les voisins du noeuds
     */
    @Override
    public String toString() {
        
        // initialisation / allocation
        String nodeStr = "";
        Node currentNeighbour = null;
        
        // indice du noeud
        nodeStr += Integer.toString(getIndex());
        // + position
        nodeStr += getPosition();
        nodeStr += "[";
        
        // pour chaque voisin...
        for (Iterator<Node> neighbourIter = getNeighbours().listIterator(); neighbourIter.hasNext(); ) {
            currentNeighbour = neighbourIter.next();
            
            /* 
             * seulement si l'indice est plus grand que le nôtre
             * (juste pour n'indiquer les voisins qu'une fois)
             */
            if (currentNeighbour.getIndex() > getIndex()) {
                // + n° voisin
                nodeStr += Integer.toString(currentNeighbour.getIndex());
                // ajoute un espace seulement s'il reste d'autres voisins
                if (neighbourIter.hasNext()) nodeStr += " ";
            }

        }
        
        nodeStr += "]";
        
        return nodeStr;
        
    }
    
}
