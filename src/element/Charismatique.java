package element;

import interaction.Actions;
import interaction.Deplacements;
import interfaceGraphique.VueElement;

import java.awt.Point;
import java.rmi.RemoteException;
import java.util.Hashtable;

import utilitaires.Calculs;

public class Charismatique extends Personnage{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Charismatique() {
		super("Charismatique", 0, 99, 1, 4, 0);
	}
	
	public void fuir(VueElement ve, VueElement cible, Deplacements deplacements){
		int x,y;
		if(ve.getPoint().x > cible.getPoint().x)
			x = ve.getPoint().x+10;
		else
			x = ve.getPoint().x-10;
		if(ve.getPoint().y > cible.getPoint().y)
			y = ve.getPoint().y+10;
		else
			y = ve.getPoint().y-10;
		
		deplacements.seDirigerVers(new Point(x,y)); // fuir
	}
	
	public void strategie(VueElement ve, Hashtable<Integer, VueElement> voisins, Integer refRMI) throws RemoteException {
		Actions actions = new Actions(ve, voisins); // je recupere les voisins
													// (distance < 10)
		Deplacements deplacements = new Deplacements(ve, voisins);

		if (0 == voisins.size()) { // je n'ai pas de voisins, j'erre
			parler("J'erre...", ve);
			deplacements.seDirigerVers(0); // errer

		} else {
			VueElement cible = Calculs.chercherElementProche(ve, voisins);

			int distPlusProche = Calculs.distanceChebyshev(ve.getPoint(),
					cible.getPoint());

			int refPlusProche = cible.getRef();
			Element elemPlusProche = cible.getControleur().getElement();

			// dans la meme equipe ?
			boolean memeEquipe = false;

			if (elemPlusProche instanceof Personnage) {
				memeEquipe = (getLeader() != -1 && getLeader() == ((Personnage) elemPlusProche)
						.getLeader()) || // meme leader
						getLeader() == refPlusProche || // cible est le leader
														// de this
						((Personnage) elemPlusProche).getLeader() == refRMI; // this
																				// est
																				// le
																				// leader
																				// de
																				// cible
			}

			if (distPlusProche <= 2) { // si suffisamment proches
				if (elemPlusProche instanceof Potion) { // potion
					parler("J'erre...", ve);
					deplacements.seDirigerVers(0); // errer

				} else { // personnage
					if (!memeEquipe) { // duel seulement si pas dans la meme
										// equipe (pas de coup d'etat possible
										// dans ce cas)
						// On se combat que si on a un charisme superieur
						if (this.getCaract("charisme") > ve.getControleur()
								.getArene().consoleFromRef(refPlusProche)
								.getElement().getCaract("charisme")) {
							// duel
							parler("Je fais un duel avec " + refPlusProche, ve);
							actions.interaction(refRMI, refPlusProche, ve
									.getControleur().getArene());
						} else {
							parler("Je fuis " + refPlusProche, ve);
							this.fuir(ve, cible, deplacements);
						}
					} else {
						parler("J'erre...", ve);
						deplacements.seDirigerVers(0); // errer
					}
				}
			} else { // si voisins, mais plus eloignes
				if (!memeEquipe) { // potion ou enemmi
					if (this.getCaract("charisme") > ve.getControleur()
							.getArene().consoleFromRef(refPlusProche)
							.getElement().getCaract("charisme")) {
						// je vais vers le plus proche
						parler("Je vais vers mon voisin " + refPlusProche, ve);
						deplacements.seDirigerVers(refPlusProche);
					} else {
						parler("Je fuis " + refPlusProche, ve);
						this.fuir(ve, cible, deplacements);
					}

				} else {
					parler("J'erre...", ve);
					deplacements.seDirigerVers(0); // errer
				}
			}
		}
	}

}

