/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import calculators.*;
import graphdrawerapp.GraphDrawerApp;
import helper.ForceModelizer;
import helper.Vector;
import helper.Parser;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Modèle de graphe
 * @author Long Nguyen Huu
 * @author Karim Vindas
 */
public class Graph {
    
    /**
     * aire ciblée pour le modèle de graphe (différente de l'aire de dessin)
     */
    public static int AREA = 20000;
    
    /**
     * seuil d'énergie cinétique pour le critère d'arrêt
     * 
     */
    public static double ENERGY_THRESHOLD = 1.6 * (GraphDrawerApp.timestep*GraphDrawerApp.timestep);
    // variation au carré du pas temporel, de sorte que quand il vaut 0.25, on ait un seuil à 0.1
    // = 10 : le graph3 n'atteint pas une forme tout à fait symétrique mais prend déjà son apparence finale
    // = 1 : le graph3 atteint presque la symétrie
    // = 0.1 : le graph3 atteint la symétrie
    
    /**
     * le calculateur de force associé au graphe. Dépend du modèle choisi.
     */
    private ForceModelizer calculator;
    
    /**
     * ensemble des noeuds du graphe 
     */
    private ArrayList<Node> nodes;
    
    /**
     * distance idéale entre deux noeuds voisins 
     */
    private double k;
    
    /**
     * énergie cinétique du système
     */
    private double kineticEnergy;
    
    
    /*
     * Les 4 attributs suivants sont les coordonnées extrêmes du graphe,
     * i.e. le maximum et le minimum parmi les abscisses et les
     * ordonnées de chacun des noeuds
     * 
     * Une fenêtre dont les frontières seraient définies à l'aide de ces valeurs
     * contiendrait donc l'ensemble de la représentation du graphe.
     * Nous allons utiliser cette propriété pour zoomer et positionner correctement
     * le graphe dans la zone de dessin.
     */
    
    /**
     * coordonnée verticale la plus petite parmi les noeuds (frontière du HAUT)
     */
    private double verticalLowerBound;
    
    /**
     * coordonnée verticale la plus grande parmi les noeuds (frontière du BAS)
     * 
     */
    private double verticalUpperBound;
    
    /**
     * coordonnée horizontale la plus petite parmi les noeuds (frontière de GAUCHE)
     * 
     */
    private double horizontalLowerBound;
    
    /**
     * coordonnée horizontale la plus grande parmi les noeuds (frontière de DROITE)
     * 
     */
    private double horizontalUpperBound;
    
    
    /**
     * Construit le modèle de graphe à partir d'un fichier
     * contenant ses caractéristiques
     * 
     * @param filePath nom du fichier ressource
     * @param modelLetter initiale du nom du modèle choisi
     */
    public Graph(String filePath, char modelLetter) {
        
        // *** création et association des noeuds
        
        // initialiser liste vide
        nodes = new ArrayList<Node>();
        
        // lit le fichier et le parse en une liste profonde lists
        List<List<Integer>> lists = Parser.parseFile(filePath);
        if (GraphDrawerApp.DEBUG_MODE) System.out.println(lists);
        int nbNodes = (lists.remove(0).remove(0));
        System.out.println("Le nombre de noeuds dans ce graphe est : " + nbNodes);

        /*
         * on transforme lists en une véritable structure de graphe
         * 
         */
        
        // on crée d'abord les noeuds et on les associe au graphe
        for (int i = 0; i<nbNodes; i++){
                Node node = new Node(); // on donnera des positions aléatoires plus tard (reset())
                addNode(node);          // on ajoute le noeud au graphe
        }
        if (GraphDrawerApp.DEBUG_MODE) System.out.println(lists);

        // on associe ensuite les noeuds entre eux
        int j = 0;
        Node node = null;
        while (!lists.isEmpty()) {                          // tant qu'il reste un noeud à étudier
            List<Integer> listeVoisins = lists.remove(0);   // on 'pop' la prochaine liste de voisins
            System.out.println(listeVoisins);               // permet de décrire le graphe (peut-être différemment du fichier)
            node = getNodes().get(j);                       // on prend le noeud courant
            while (!listeVoisins.isEmpty()) {               // tant qu'il lui reste un voisin à lier
                int b = (listeVoisins.remove(0));           // on 'pop' ce voisin
                Node newNeighbour = getNodes().get(b);      // on récupère ce voisin
                node.bindWith(newNeighbour);                // on lie les deux noeuds (dans les deux sens à la fois)
            }
            j++;		
        }
        
        // *** fin création et associaion des noeuds
        
        // calcule distance idéale
        k = Math.sqrt(AREA/nbNodes); // k = racine(aire de travail / |V|)
        if (GraphDrawerApp.DEBUG_MODE) System.out.println("k is: " + k);
        
        reset(); // fait office d'initialisation ici (placement des noeuds et Ec)
        
        switch (modelLetter){ // seul le premier caractère importe
            case 'e':
                calculator = new Eades(k,20,30000);
                // remarque : on aurait pu mettre les 2 derniers arguments en paramètres de commande également
                break;
            case 'h':
                calculator = new HookeLike(k);
                break;
            case 'f':
            default:
                calculator = new FruchtermanReingold(k);
                break;
        }
    }
    
    /**
     *  (Ré)initialise les propriétés 2D du graphe à partir de la même disposition de noeuds.
     * <p>
     * Les propriétés concernées sont la position/vitesse des noeuds et l'énergie cinétique.
     * </p>
     */
    public void reset() {
        
        /*
         * on attribue des positions aléatoirement aux noeuds
         * on est presque sûr que deux noeuds ne seront pas au même endroit,
         * mais on pourrait toujours s'assurer que le noeuds assurent une distance minimale au départ,
         * par exemple en les plaçant sur un quadrillage, en ajoutant une légère variation
         * dx et dy pour éviter que les noeuds soient alignés
         */
        Node currentNode = null;
        for (Iterator<Node> nodeIter = nodes.listIterator(); nodeIter.hasNext(); ) {
            currentNode = nodeIter.next();
            currentNode.reset();
        }
        refreshBoundaries(); // initialiser les frontières pour la première représentation du graphe
        kineticEnergy = ENERGY_THRESHOLD; // initialiser énergie cinétique pour que l'équilibrage puisse démarrer
        // attention, comme les vitesses des noeuds sont, elles, nulles, ce n'est pas la véritable énergie cinétique
    }
    
    /**
     * 
     * @return la liste de noeuds du graphe
     */
    public ArrayList<Node> getNodes() {
        return nodes;
    }
    
    /**
     * @return l'abscisse la plus petite parmi les noeuds du graphe
     */
    public double getHorizontalLowerBound() {
        return horizontalLowerBound;
    }
    
    /**
     * @return l'abscisse la plus grande parmi les noeuds du graphe
     */
    public double getHorizontalUpperBound() {
        return horizontalUpperBound;
    }
    
    /**
     * @return l'ordonnée la plus petite parmi les noeuds du graphe
     */
    public double getVerticalLowerBound() {
        return verticalLowerBound;
    }
    
    /**
     * @return l'ordonnée la plus grande parmi les noeuds du graphe
     */
    public double getVerticalUpperBound() {
        return verticalUpperBound;
    }
    

    
    /**
     * Ajoute le noeud <i>node</i> au graphe.
     * @param node 
     */
    private void addNode(Node node) {
        nodes.add(node);
    }
    
    /**
     * Applique une itération de l'algorithme d'équilibrage avec le modèle et les paramètres choisi.
     * 
     * @param timestep pas temporel de l'itération
     * @param damping facteur d'amortissement
     */
    public void iterateBalance(double timestep, double damping) {
        
        // allocations
        Node u = null;
        Node v = null;
        // initialisation des forces (on settera les composantes afin de ne pas créer de nouveaux objets)
        Vector totalForce = new Vector(0,0);        // force totale appliquée à un noeud
        // les deux forces suivantes ont surtout servi à débugguer (on pourrait s'en passer)
        Vector attractiveForce = new Vector(0,0);   // total des forces attractives appliquées à un noeud
        Vector repulsiveForce = new Vector(0,0);    // total des forces répulsives appliquées à un noeud
        Vector currentForce = new Vector(0,0);      // force muette pour calculer
        Vector uv = new Vector(0,0);                // idem mais utilisée pour le vecteur (u,v)
        
        // initialisation du calcul de l'énergie cinétique du système
        kineticEnergy = 0;
        
        /*
         * on va appliquer le TAM élémentaire à chaque noeud
         */
        
        // pour chaque noeud...
        for (Iterator<Node> nodeIter = nodes.listIterator(); nodeIter.hasNext(); ) {
            
            // réinitialisation forces
            attractiveForce.setComponents(0, 0);
            repulsiveForce.setComponents(0, 0);
            totalForce.setComponents(0, 0);
            
            // prendre le noeud suivant
            u = nodeIter.next();
            if (GraphDrawerApp.DEBUG_MODE) System.out.println("Noeud considéré : " + u.getIndex());
            
            /*
             * sommer les forces attractives avec tous les VOISINS
             */
            
            // pour chaque voisin...
            for (Iterator<Node> neighbourIter = u.getNeighbours().listIterator(); neighbourIter.hasNext(); ) {
                // prendre le voisin suivant
                v = neighbourIter.next();
                if (GraphDrawerApp.DEBUG_MODE) System.out.println("Voisin : " + v.getIndex());
                // calculer le vecteur (u,v)
                uv.setComponents(u.getPosition(), v.getPosition());
                // calculer la force d'attraction associée
                // remarque : comme on a la valeur à multiplier ave le vecteur unitaire, on divise par norm(uv)
                currentForce.setComponents(uv,calculator.calculateAttrForce(uv.norm())/uv.norm());
                if (GraphDrawerApp.DEBUG_MODE) System.out.println("applique la force attractive : " + currentForce);
                // ajouter la force d'attraction à la somme correspondante
                attractiveForce.add(currentForce);
            }
            
            /*
             * pour tous les AUTRES noeuds : sommer les forces de répulsion
             * 
             */
            
            // pour tout noeud...
            for (Iterator<Node> otherNodeIter = nodes.listIterator(); otherNodeIter.hasNext(); ) {
                // prendre le voisin suivant
                v = otherNodeIter.next();
                if (v == u) continue; // exclure soi-même
                if (GraphDrawerApp.DEBUG_MODE) System.out.println("Voisin : " + v.getIndex());
                uv.setComponents(u.getPosition(), v.getPosition()); // calcul u->v
                // calculer la force de répulsion associée
                currentForce.setComponents(uv,calculator.calculateRepulsForce(uv.norm())/uv.norm());
                if (GraphDrawerApp.DEBUG_MODE) System.out.println("applique la force répulsive : " + currentForce);
                // ajouter la force de répulsion à la somme
                repulsiveForce.add(currentForce);
            }
            
            totalForce.add(attractiveForce);
            totalForce.add(repulsiveForce);
            
            u.applyForce(totalForce,timestep,damping); // TAM élémentaire sur le noeud courant

            kineticEnergy += u.getSpeed().sqNorm(); // on incrémente la somme flottante calculant l'Ec
            if (GraphDrawerApp.DEBUG_MODE) System.out.println("vitesse de noeud " + u.getIndex() + " : " + u.getSpeed());
            if (GraphDrawerApp.DEBUG_MODE) System.out.println("Ec de noeud " + u.getIndex() + " : " + u.getSpeed().sqNorm());
            
        } // fin itération sur les noeuds
        
        // mise à jour des coordonnées extrêmes du graphe
        refreshBoundaries();
        
        System.out.println("Ec totale : " + kineticEnergy);
        if (GraphDrawerApp.DEBUG_MODE) System.out.println("Graphe :\n" + this);
        
    }
    
    public double getEk() {
        return kineticEnergy;
    }
    
    /**
     * Met à jour les coordonnées extrêmes du graphe
     */
    private void refreshBoundaries() {
        
        // Pour évaluer un max ou un min on initialise la valeur de travail à un min ou un max respectivement
        horizontalLowerBound = Double.MAX_VALUE;
        horizontalUpperBound = -Double.MAX_VALUE;
        verticalLowerBound = Double.MAX_VALUE;
        verticalUpperBound = -Double.MAX_VALUE;
        
        // Pour chaque noeud, on regarde si ses coordonnées repoussent les frontières
        Node currentNode = null;
        for (Iterator<Node> nodeIter = nodes.listIterator(); nodeIter.hasNext(); ) {
            currentNode = nodeIter.next();
            double x = currentNode.getPosition().getX();
            double y = currentNode.getPosition().getY();
            /* on compare la coordonnée à la frontière actuelle et on la repousse si besoin
             * attention, ne pas essayer de mettre des else if car on a initalisé
             * les frontières flottantes à des valeurs non logiques (lower > upper)
             * 
             */
            if (x < horizontalLowerBound) horizontalLowerBound = x;
            if (x > horizontalUpperBound) horizontalUpperBound = x;
            if (y < verticalLowerBound) verticalLowerBound = y;
            if (y > verticalUpperBound) verticalUpperBound = y;
        }
        
        if (GraphDrawerApp.DEBUG_MODE) System.out.println("Boundaries: " + horizontalLowerBound+","+horizontalUpperBound+","+verticalLowerBound+","+verticalUpperBound);
        
    }
    
    
    /**
     * Conversion en chaîne de caractères
     * <p>
     * Principalement pour le debug. La forme obtenue n'est pas celle
     * du fichier ressource, d'une part car on précise les positions des noeuds,
     * d'autre part car l'utilisateur a pu écrire les voisins d'index plus bas
     * à certaines lignes. En outre, le n° du noeud considéré est rappelé et il
     * y a des crochets.
     * </p>
     * @return la représentation textuelle du graphe
     */
    @Override
    public String toString() {
        
        // initialisation représentation textuelle du graphe
        String graphRep = "";
        // allocations
        Node currentNode = null;
        
        // pour chaque noeud
        for (Iterator<Node> nodeIter = nodes.listIterator(); nodeIter.hasNext(); ) {
            currentNode = nodeIter.next();
            graphRep += currentNode; // ajouter la représentation du noeud même
            if (nodeIter.hasNext()) graphRep += "\n"; // à la ligne
        }

        return graphRep;
    }
    
}
