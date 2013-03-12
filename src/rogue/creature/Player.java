package rogue.creature;

import jade.core.Actor;
import java.util.Collection;
import jade.fov.RayCaster;
import jade.fov.ViewField;
import jade.gen.Generator;
import jade.gen.map.World1;
import jade.ui.Camera;
import jade.ui.Terminal;
import jade.util.datatype.ColoredChar;
import jade.util.datatype.Coordinate;
import jade.util.datatype.Direction;
import rogue.level.Screen;
import rogue.creature.util.Inventory;
import rogue.creature.util.Item;
import rogue.creature.util.NotEnoughGoldException;
import rogue.creature.util.NotEnoughSpaceException;
import java.util.Random;
import java.lang.InterruptedException;
import jade.core.World;
import java.util.ArrayList;

/**
 * Represents Player
 */
public class Player extends Creature implements Camera {
	private Terminal term;
	private ViewField fov;
	private static final int maxHitpoints = 15;
	private int strength;
	private String name;
	private Inventory inventory;

	public Boolean worldchange = false;   // standardm��ig ist keine Map�nderung erfolgt
	/**
	 * Creates a new Player Object
	 * 
	 * @param term
	 *            Currently used Terminalobject
	 */
	public Player(Terminal term) {
		// Put Charactersymbol on Screen
		super(ColoredChar.create('@'));
		// Save Terminal
		this.term = term;
		fov = new RayCaster();
		// Initialise Hitpoints on Max
		hitpoints = maxHitpoints;
		strength = 5;
		inventory = new Inventory(5,50);
	}

	/**
	 * Sets Charactername. Should be only called on character Creation.
	 * 
	 * @param name
	 *            New Name of Character
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns Charactername.
	 * 
	 * @return Name of Character
	 */
	public String getName() {
		return name;
	}

	@Override
	/**
	 * Ask Player to do some action (passing him the baton). Reads input and moves Character accordingly.
	 */
	public void act() {
		try {
			// Get pressed char
			char key;
			key = term.getKey();
			switch (key) {
				case 'q': // User wants to quit
					expire(); // Leave let player die, so this application quits
					break;
				case 'i': // Show Inventory
					showInventoryScreen();
					break;
				default: // User pressed something else
					Direction dir = Direction.keyToDir(key); // Get direction
					// Something useful pressed?
					if (dir != null) { // Yes
						// Get list of all monsters on target Coordinates
						Collection<Monster> actorlist = world().getActorsAt(Monster.class, x() + dir.dx(), y() + dir.dy());
						// Is there a monster on TargetL
						if (!actorlist.isEmpty()) { // Yes
							// Fight first monster on coordinate.
							fight((Monster) actorlist.toArray()[0]);
						} else {
							if (world().tileAt(x() + dir.dx(), y() + dir.dy()) == ColoredChar.create('�')) {
								System.out.println("Level Up");  
								worldchange= true;					//Stellt fest, dass eine T�r gefunden wurde und somit eine Map�nderung erfolgt
								move(dir);
							} else {// No monster there

								move(dir);

								break;
							}
						}
					}
			}
		} catch (InterruptedException e) { // Something has happened here
			System.out.println("!Interrupted Exception");
			e.printStackTrace();
		}
	}

	@Override
	/**
	 * Get what is visible
	 *
	 * @return A collection of visible Items
	 */
	public Collection<Coordinate> getViewField() {
		return fov.getViewField(world(), pos(), 5);
	}

	/**
	 * Player fights the opponent. Causes random damage between 1 and strength
	 * 
	 * @param opponent
	 *            The opponent Monster
	 */
	// TODO Clean up Messages in Console, to use just a single line
	private void fight(Monster opponent) {
		System.out.println("Du kämpfst gegen " + opponent.name());
		// Get a randomizer
		Random random = new Random();
		// Get random Damage for Attack
		int damage = random.nextInt(strength) + 1;
		// Do Damage to Opponent
		opponent.loseHitpoints(damage);
		// Print result
		System.out.println("Du hast " + damage + " Schaden verursacht");
		System.out.println(opponent.name() + " hat noch " + opponent.hitpoints
				+ " HP");
		Screen.redrawEventLine("Du verursachst " + damage + " Schaden");
		try {
			term.getKey();

		} catch (InterruptedException e) {
			System.out.println("!InterruptedException");
			e.printStackTrace();
		}
	}

	/**
	 * Player regains 1 Hitpoint. Method should be used every x rounds in rogue
	 */
	public void regainHitpoint() {
		// Is there something to heal
		if (hitpoints < maxHitpoints) {
			// Gain Healthpoint back
			hitpoints++;
			// Print message to Console
			System.out.println("Du hast einen HP regeneriert, jetzt " + hitpoints+" HP");
			// Print Eventline
			Screen.redrawEventLine("Du regenerierst einen HP.");
			try{
				// Wait for pressed Key
				term.getKey();

			} catch (InterruptedException e) {
				System.out.println("!IOException");
				e.printStackTrace();
			}
		}
	}

	/**
	 *
	 */
	public int getHitpoints() {
		return hitpoints;
	}

	/**
	 * Creates and prints an Inventory Screen
	 */
	public void showInventoryScreen() {
		boolean loop = true;
		// Inventar geschlossen?
		while (loop) { // Nein.
			// Erstelle eine ArrayList von Strings um dort unser Inventarinterface zu puffern
			ArrayList<String> lines = new ArrayList<String>();
			// Erstelle eine Titelzeile
			// TODO In die Mitte Verschieben
			lines.add("Inventar");
			// Zeige was der Nutzer gerade angelegt hat
			lines.add("Du traegst: ");
			// Lade die Liste
			Item[] wornItems = inventory.getWornItems();
			// Generiere den Output fuer den aktuellen Helm
			lines.add("Kopf: "+wornItems[Item.ITEMTYPE_HEAD].getName()+" [+DMG: "+wornItems[Item.ITEMTYPE_HEAD].getDamageBonus()+", +HP: "+wornItems[Item.ITEMTYPE_HEAD].getHealthBonus()+"]");
			// Generiere den Output fuer das aktuelle Schwert
			lines.add("Schwert: "+wornItems[Item.ITEMTYPE_SWORD].getName()+" [+DMG: "+wornItems[Item.ITEMTYPE_HEAD].getDamageBonus()+", +HP: "+wornItems[Item.ITEMTYPE_HEAD].getHealthBonus()+"]");
			// TODO Zeige gesamt Bonus an
			// Zeige an, was sonst noch im Inventar liegt, aber nicht angelegt wurde (und somit keinen Bonus bringt)
			ArrayList<Item> backpack = inventory.listBackpack();
			lines.add("Du hast im Rucksack: ");
			for (int i = 0;i<backpack.size();i++) {
				// Zeige das Item an Stelle an i an
				lines.add("("+i+") "+backpack.get(i).getName()+"[+DMG: "+backpack.get(i).getDamageBonus()+", +HP: "+backpack.get(i).getHealthBonus()+"]");
			}
			// TODO Add lines here.
			Screen.putText(lines);
			try {
				// Erwarte eine Eingabe vom Nutzer.
				char key = term.getKey();
				switch (key) {
					case 'q':
					loop = false;
					break;
					case '0':
					break;
					case '1':
					break;
					case '2':
					break;
				}
			} catch (InterruptedException e) {
				System.out.println("!Exeception");
				e.printStackTrace();
			}
		}
		// Inventar verlassen, zeichne wieder die Karte.
		Screen.redrawMap();
	}
}
