package graphdrawerapp;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import models.Graph;
import models.Node;

/**
 * Application de dessin de graphes.
 * <p>
 * A partir d'un fichier texte, un graphe est représenté dans une fenêtre et
 * l'utilisateur dispose de boutons lui permettant de clarifier se représentation
 * à l'aide d'un algorithme itératif selon un modèle de forces donnée.
 * </p>
 * <p>
 * Le nom du fichier et le modèle de forces peuvent être choisi par l'utilisateur
 * à l'aide de paramètres de commande (voir guide d'utilisation).
 * </p>
 * 
 * @author Long Nguyen Huu
 * @author Karim Vindas
 */
public class GraphDrawerApp {
    
    /**
     * booléen mis à true quand on veut débugguer (active plus de prints)
     * <p>attention, ralentit la mise en oeuvre de l'algorithme avec beaucoup de noeuds</p>
     */
    public static final boolean DEBUG_MODE = false;
    
    /**
     * longueur du côté de la zone de dessin qui est carrée
     */
    public static final int PANEL_SIZE = 400;
    
    /**
     * pas temporel de discrétisation
     */
    public static double timestep = 0.25;
          
    /**
     * facteur d'amortissement
     */
    public static double damping = 0.8;
            
    /**
     * graphe de travail
     */
    private static Graph graph;
    
    
    /**
     * Construit le graphe de travail et lance l'interface graphique
     * <p>
     * Le graphe de travail est une instance de {@link Graph} et
     * est construit selon les deux paramètres de commande (optionnels)
     * situés dans <i>args</i>. Le premier définit le nom du fichier ressource (sans extension)
     * et la première lettre du deuxième désigne le modèle de forces employé (son initiale)
     * </p>
     * <p>
     * La Graphical User Interface (GUI) est lancée dans un thread qui agit par petites tâches
     * en chaîne afin de ne pas empêcher les évènements de la bibliothèque AWT d'être traités
     * </p>
     * 
     * @param args arguments optionnels passés dans la commande, sous la forme : "nom_du_fichier" "initiale modèle"
     * @see Graph
     */
    public static void main(String[] args) {
        
        // valeurs par défaut (en cas d'arguments manquants ou incorrects)
        String fileName = "graph";  // nom du fichier ressource par défaut
        char modelLetter = 'f';     // lettre 'f' pour le modèle de Fruchterman et Reingold par défaut
        
        // affecter des valeurs utilisateur si besoin
        if (args.length >= 1) { // 1er argument passé : le nom du fichier ressource
            
            if (Pattern.matches("\\w{1,256}", args[0])) fileName = args[0]; // s'il est conforme, on le prend
            
            if (args.length >= 2) {                     // 2e argument passé : modèle de forces
                if (args[1].length() >= 1) {            // en tapant '""' en paramètre on pourrait avoir un paramètre vide
                    modelLetter = args[1].charAt(0);    // sinon on peut prendre la première lettre
                    // remarque : cette lettre peut ne pas être conforme mais on la traitera dans le cas "default" de switch
                }
            }
        
        }
        
        System.out.println("Fichier choisi : " + fileName + ".txt");
        System.out.println("Initiale modèle choisi : " + modelLetter);
        
        // construire le graphe en fonction de ces deux paramètres
        graph = new Graph("data/"+fileName+".txt", modelLetter);
        
        System.out.println("Graphe initialisé :\n" + graph);
        
        // lancer l'interface graphique en 'event dispatch thread'
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
        
    }
    
    /**
     * Crée l'interface graphique et l'affiche
     * <p>
     * Crée une fenêtre {@link JFrame} constituée d'un panneau {@link GraphPanel}
     * pour la représentation du graphe et de boutons {@link JButton} pour le modifier
     * </p>
     */
    private static void createAndShowGUI() {
        
        // affiche TRUE dans la console si la fonction est bien appelée en 'event dispatch thread'
        System.out.println("Created GUI on EDT? " + SwingUtilities.isEventDispatchThread());
        
        // initialisation fenêtre et obtention de son contenu
        JFrame f = new JFrame("Dessinateur de graphes");
        Container contenu = f.getContentPane();
        // Remarque : on va travailler avec le contenu ('content pane') de la fenêtre pour ajouter des éléments
        // cependant, depuis Java 1.5 on peut aussi travailler avec la JFrame directement
        
        /*
         * création du GraphPanel et ajout à la fenêtre
         * 
         * le GraphPanel doit être 'final' pour qu'une classe interne puisse l'utiliser
         * cela est dû à la façon dont Java gère les {} : l'instance de la classe interne
         * n'est pas au courant des changements possibles sur la référence graphPanel
         */
        final GraphPanel graphPanel = new GraphPanel(graph);
        contenu.add(graphPanel);  // ajoute l'instance créée comme enfant du contenu
        f.pack(); // la taille de la fenêtre s'adapte au 'panel'
        // on ajoutera de la place pour les boutons plus tard
        f.setLayout(null); // le layout manager va éviter les superpositions d'objets, etc.
        // si on ne fait pas de setLayout(), certains boutons n'apparaissent pas,
        // leur zone de click s'étend partout, etc.
        
        /*
         * Création du bouton Reset : réinitialisation du graphe (modèle + vue)
         * 
         */
        JButton resetButton = new JButton("Reset");             // le bouton est un JButton
        resetButton.setBounds(PANEL_SIZE, 0, 100, 50);          // les boutons mesurent 100*50, à droite de la zone de dessin
        resetButton.addActionListener(new ActionListener() {    // sur appui du bouton...
            public void actionPerformed(ActionEvent event) {    // on déclenche l'action...
                graph.reset();                                  // réinitialiser le modèle graphe
                graphPanel.repaint();                           // rafraîchir la vue pour qu'on le voie
                System.out.println("Graphe réinitialisé.");
           }
        });
        contenu.add(resetButton);                               // ajouter le bouton en enfant au contenu
        
        /*
         * Création du bouton Balance (équilibrage visible du graphe)
         * 
         */
        JButton balanceButton = new JButton("Balance");
        balanceButton.setBounds(PANEL_SIZE, 50, 100, 50);              // les boutons sont adjacents, donc 50 de plus en ordonnée
        balanceButton.addActionListener(new ActionListener() {
            
            
            public void actionPerformed(ActionEvent event) {

                /* pour 400 itérations maximum, tant que l'énergie cinétique est au-dessus du seuil
                 * par expérience, avec nos paramètres, 400 itérations suffisent à atteindre le seuil d'Ec
                 * si le timestep s'éloigne de la valeur par défaut 0.25, on arrange i en conséquence
                 * avec tout de même une limite absolue à 1000
                 * 
                 */
                int i;
                for (i = 0; i < Math.min(100 / timestep, 1000) && graph.getEk() >= Graph.ENERGY_THRESHOLD; i++) {
                    if (DEBUG_MODE) System.out.println("itération n°" + i + " :");
                    // on exécute une itération de l'algorithme
                    graph.iterateBalance(timestep,damping);
                    // on redessine immédiatement le graphe pour voir son évolution
                    graphPanel.paintImmediately(0, 0, PANEL_SIZE, PANEL_SIZE);
                }
                System.out.println("Arret de l'algorithme sur : " + (graph.getEk() < Graph.ENERGY_THRESHOLD ? "seuil energie atteint (" + i + " iterations)" : "max iterations atteint (" + i + ")"));
                
           }
        });
        contenu.add(balanceButton);
        
        /*
         * Création du bouton Quit
         * 
         */
        JButton quitButton = new JButton("Quit");
        quitButton.setBounds(PANEL_SIZE, 100, 100, 50);
        quitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                System.exit(0); // sortie sans erreur du programme
           }
        });
        contenu.add(quitButton);
        
        f.setSize(f.getWidth() + 100, f.getHeight());       // on ajoute 100px horizontaux pour les boutons
        f.setResizable(false);                              // la fenêtre ne sera pas redimensionnable
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);   // la fenêtre se ferme en cliquant sur la croix
        f.setLocationRelativeTo(null);                      // fenêtre centrée sur l'écran
        
        f.setVisible(true);                                 // la fenêtre devient visible
        
    }
    
}

/**
 * Panneau personnalisé héritant de {@link JPanel} et responsable du dessin du graphe de travail
 * @author Long Nguyen Huu
 * @author Karim Vindas
 */
class GraphPanel extends JPanel {
    
    /**
     * rayon de référence pour dessiner un noeud (sera mis à l'échelle)
     */
    private static final int RADIUS = 8;
    
    /**
     * marge entre le graphe représenté et les bords ou les boutons
     */
    private static final double MARGIN = 10;
    
    /**
     * graphe de travail
     */
    private Graph graph;
    
    /**
     * Construit une instance de GraphPanel en y associant le graphe <i>graph</i>.
     * @param graph le graphe de travail
     * @see Graph
     */
    public GraphPanel(Graph graph) {
        this.graph = graph;
    }
    
    /**
     * Renvoie la dimension voulue pour le panneau.
     * 
     * @return la dimension d'un carré de côté {@link GraphDrawerApp#PANEL_SIZE}
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(GraphDrawerApp.PANEL_SIZE,GraphDrawerApp.PANEL_SIZE);
    }
    
    /**
     * Dessine le graphe de travail composante par composante en protégeant
     * l'objet graphique {@code g} des altérations à l'aide du copie (voir la doc de JComponent)
     * @param g objet graphique à protéger
     * @see Graph
     */
    @Override
    public void paintComponent(Graphics g) {
        
        /* on laisse l'UI delegate peindre d'abord, sinon des problèmes graphiques surviennent
         * dans notre cas la zone de dessin de sera pas effacée avec de redessiner,
         * faisant apparaître une superposition de graphes...
         * 
         */
        super.paintComponent(g);
        
        /*
         * on va représenter le graphe de sorte qu'il rentre juste dans la zone de dessin
         * (avec tout de même une légère marge)
         * on va donc "zoomer" et décaler le point de vue sur le graphe
         * mais on ne touchera pas à sa structure, seule la "vue" change
         * 
         * on parlera de dessin "cadré"
         */
        // on calcule le rapport de zoom 'scale'
        double scale = getFittingScale();
        // mise à l'échelle
        int scaledRadius = (int) Math.floor(RADIUS * scale);
        if (scaledRadius < 2) scaledRadius = 2; // rayon minimum de 2 pour avoir des noeuds visibles
        
        // on récupère la liste des noeuds du graphe de travail
        ArrayList<Node> nodes = graph.getNodes();
        
        // allocations
        Node currentNode, currentNeighbour;
        
        // pour chaque noeud...
        for (Iterator<Node> nodeIter = nodes.listIterator(); nodeIter.hasNext(); ) {
            currentNode = nodeIter.next();
            
            // calcule la position du noeud à dessiner pour le dessin cadré
            int currentFittingX = getFittingDrawingX(currentNode.getPosition().getX(), scale);
            int currentFittingY = getFittingDrawingY(currentNode.getPosition().getY(), scale);
            
            g.setColor(Color.BLACK); // couleur des arêtes
            
            /*
             * dessin des arêtes
             * 
             * il précède celui des noeuds car les arêtes doivent passer "en-dessous" sur le dessin
             */
            // pour chacun de ses voisins
            for (Iterator<Node> neighbourIter = currentNode.getNeighbours().listIterator(); neighbourIter.hasNext(); ) {
            currentNeighbour = neighbourIter.next();
            
                // on ne doit dessiner les arêtes qu'une fois ; on choisit celle vers les voisins d'indices plus élevés
                if (currentNeighbour.getIndex() > currentNode.getIndex()) {
                    // dessin de l'arête entre le noeud et le voisin courant en cadré
                    g.drawLine(currentFittingX, currentFittingY, getFittingDrawingX(currentNeighbour.getPosition().getX(), scale), getFittingDrawingY(currentNeighbour.getPosition().getY(), scale));
                }
                
            } // fin "pour chaque voisin"
            
            /*
             * dessin du noeud courant
             */
            // couleur bleue pour l'intérieur d'un noeud
            g.setColor(Color.CYAN);
            // on dessine un ovale de mêmes dimensions x et y = un rond
            g.fillOval(currentFittingX - scaledRadius, currentFittingY - scaledRadius, 2*scaledRadius, 2*scaledRadius);
            // couleur noire pour le contour d'un noeud
            g.setColor(Color.BLACK);
            // on trace le cercle
            g.drawOval(currentFittingX - scaledRadius, currentFittingY - scaledRadius, 2*scaledRadius, 2*scaledRadius);
            // couleur rouge pour les numéros
            g.setColor(Color.RED);
            // on écrit le numéro du noeud si possible à l'intérieur, et à l'extérieur si le noeud est trop petit (trop de "dézoom")
            g.drawString("" + currentNode.getIndex(), currentFittingX + (scaledRadius > 6 ? - 6 : + 6), currentFittingY + 3);
            
            
        } // fin pour chaque noeud
        
    }
    
    /**
     * 
     * @return le facteur d'échelle pour un dessin cadré
     */
    private double getFittingScale() {
        return (GraphDrawerApp.PANEL_SIZE - 2*MARGIN) /
            (Math.max(graph.getHorizontalUpperBound() - graph.getHorizontalLowerBound(),
            graph.getVerticalUpperBound() - graph.getVerticalLowerBound()) + 2*RADIUS);
    }
    
    /**
     * 
     * @param x l'abscisse du noeud dans le modèle de graphe
     * @param scale le facteur d'échelle
     * @return l'abscisse cadrée du noeud
     */
    private int getFittingDrawingX(double x, double scale) {
        return getFittingDrawingCoord(x, graph.getHorizontalLowerBound(), scale);
    }
    
    /**
     * 
     * @param y l'ordonnée du noeud dans le modèle de graphe
     * @param scale le facteur d'échelle
     * @return l'ordonnée cadrée du noeud
     */
    private int getFittingDrawingY(double y, double scale) {
        return getFittingDrawingCoord(y, graph.getVerticalLowerBound(), scale);
    }
    
    /**
     * Cette fonction est une aide pour {@link #getFittingDrawingX getFittingDrawingX} et {@link #getFittingDrawingY getFittingDrawingY}
     * @param coord x ou y du noeud
     * @param coord0 position de référence, frontière du haut ou de gauche de la vue du graphe
     * @param scale facteur d'échelle
     * @return la coordonnée cadrée du noeud
     */
    private int getFittingDrawingCoord(double coord, double coord0, double scale) {
        return (int) Math.floor( MARGIN +  scale * (coord - (coord0 - RADIUS)) );
    }
    
}