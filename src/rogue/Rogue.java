package rogue;

import jade.core.Actor;
import jade.core.World;
import jade.core.World.Tile;
import jade.ui.TiledTermPanel;
import jade.util.datatype.ColoredChar;
import java.awt.Color;
import java.util.Collection;
import rogue.creature.Dragon;
import rogue.creature.Monster;
import rogue.creature.Player;
import rogue.level.Level;
import rogue.level.Screen;
import rogue.system.Path;
import rogue.system.CharacterCreation;
import rogue.system.SystemHelper;

public class Rogue {
	public static void main(String[] args) throws InterruptedException {
		//if (args.length > 0) {
		//	if (args[0].compareTo("debug") == 0) {
		//		SystemHelper.debug=true;
		//		SystemHelper.debugMessage("Test.");
		//	}
		//}
		SystemHelper.getArgs(args);
		SystemHelper.debugMessage("Test");
		TiledTermPanel term = TiledTermPanel.getFramedTerminal("Jade Rogue");
		// How many rounds for next healing+
		final int hpCycle=10;
		int roundsToHpUp = hpCycle;
		// Nobody knows right now, what happens here
		term.registerTile("dungeon.png", 5, 59, ColoredChar.create('#'));
		term.registerTile("dungeon.png", 3, 60, ColoredChar.create('.'));
		term.registerTile("dungeon.png", 5, 20, ColoredChar.create('@'));
		term.registerTile("dungeon.png", 14, 30, ColoredChar.create('D', Color.red));

		// Create a new Player
		Player player = new Player(term);
		// Generate a new World
		World world = new Level(69, 24, player);
		player.setName(CharacterCreation.getCharacterName(term, world));
		Screen.printLine(player.getName(),term,world);
		term.getKey();
		// Show Splashscreen for Start
		Screen.showFile(Path.generateAbsolutePath("rogue/system/start.txt"),term,world);

		// Press any Key to continue
		term.getKey();

		// Who deleted this, and why?
		//world.addActor(new Monster(ColoredChar.create('D', Color.red),"roter Drache"));

		// Add a Dragon so we have an enemy
		world.addActor(new Dragon(ColoredChar.create('D',Color.red),"roter Drache"));
		// Add Minimap to left part in Window (Size given as Parameter), focus on Player
		term.registerCamera(player, 5, 5);

		// Play Game
		while(!player.expired()) { // Player is still living?

			// ? TODO Delete this Block if it is not needed anymore
			/*Collection<Monster> monsters = world.getActorsAt(Monster.class, player.pos());
			  if(!monsters.isEmpty()){
			  player.expire();
			  continue;
			  }*/

			// Finished hpCycle?
			if (roundsToHpUp == 0) { // Yes
				// Give Player a hitpoint
				player.regainHitpoint();
				// Reset Counter
				roundsToHpUp = hpCycle;
			}
			// Decrement hpCycle Counter
			roundsToHpUp--;

			// Generate a List of Monsters still on Map
			Collection<Monster> monsters = world.getActorsAt(Monster.class, player.pos());
			// Has every Monster been killed?
			if(!monsters.isEmpty()){ // Yes
				// Stop Game
				player.expire();
				continue;
			}

			// Redraw Windowcontents now
			term.clearBuffer();
			for(int x = 0; x < world.width(); x++)
				for(int y = 0; y < world.height(); y++)
					term.bufferChar(x + 11, y, world.look(x, y));
			term.bufferCameras();
			term.refreshScreen();

			// Give everyone else the chance to make his move
			world.tick();
		}
		term.clearBuffer();
		//Screen.showFile(normalizePath("src\\rogue\\system\\end.txt","rogue/system/end.txt"), term, world);
		Screen.showFile(Path.generatePath("rogue/system/end.txt"),term, world);
		term.getKey();
		System.exit(0);

	}
}
