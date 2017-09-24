package helper;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Outil pour parser les fichiers texte
 * 
 * @author Karim Vindas
 *
 */
public class Parser {

    /**
     * Parse le fichier .txt contentant le
     * graphe.
     * @param filePath chemin du fichier à parser
     * @return lists la liste des listes d'entiers présents à chaque ligne et séparés par des espaces
     */
    public static List<List<Integer>> parseFile(String filePath){
        
        BufferedReader bufferedReader = null;
        List<List<Integer>> lists = new ArrayList<List<Integer>>(); // on initialise la grande liste à une liste vide
        List<Integer> currentList = null;
        
        try {
        	bufferedReader = new BufferedReader(new FileReader(filePath)); // on ouvre un flux vers le fichier en s'aidant d'un tampon
        	String line = null;
           	while (true){
                currentList = new ArrayList<Integer>();		// on initialise la liste des entiers (liste courante) à une liste vide
        		line = bufferedReader.readLine();			// on lit la ligne suivante
                if (line == null) break;					// On sort de la boucle une fois que la tête de lecture a atteint la fin du fichier .txt
                int i = 0;									// On initialise un pointeur qui va parcourir la liste (indice du début de la fenêtre de lecture)
                while (i < line.length()){					// Cette étape s'arrête une fois qu'on arrive à la fin de la ligne.
                	int j = 1;								// On initialise la longueur de la fenêtre de lecture à 1.
                	// On va analyser le prochain caractère (s'il existe) pour repérer les espaces et bien séparer les voisins.
                    while ((i+j+1 < line.length()+1) && (!line.substring(i+j,i+j+1).contentEquals(" "))){
                    	j++; // tant qu'on ne lit pas d'espace (donc a priori des chiffres), on agrandit la fenêtre de lecture
                	}
                	currentList.add(Integer.parseInt(line.substring(i,i+j))); // On transforme la String relative aux noeuds en entier indexant le noeud. 
                	i=i+j+1; // on décale le début de la fenêtre juste après le dernier espace détecté (si ce n'était pas la fin de la ligne)
                }
                lists.add(currentList); // on complète la grande liste avec la liste des entiers lus sur cette ligne
           	}               
          	            
        	

        } catch (FileNotFoundException ex) { // Gestion des exceptions : fichier non trouvé
            ex.printStackTrace();
            System.out.print("Veuillez vérifier votre premier paramètre de commande"
                    + " ainsi que le contenu du dossier 'data'.");
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) // s'il reste des données dans le buffer...
                    bufferedReader.close(); // ... vider le buffer
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return lists; // On renvoie la liste contenant la liste des voisins pour chaque noeud.
        
    }

} 


