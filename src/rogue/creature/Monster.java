package rogue.creature;

import java.util.Arrays;
import jade.util.Dice;
import jade.util.datatype.ColoredChar;
import jade.util.datatype.Direction;

public class Monster extends Creature {

    private String name;

    public Monster(ColoredChar face) {
        super(face);
    }

    public Monster(ColoredChar face, String name) {
        super(face);
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public void act() {
        boolean fight = false;
        for (Direction dir : Arrays.asList(Direction.values())) {
            Player player = world().getActorAt(Player.class, x() + dir.dx(), y() + dir.dy());
            if (player != null) {
                fight(player);
                move(dir);
                fight = true;
                break;

            }
        }
        if (!fight) {

            move(Dice.global.choose(Arrays.asList(Direction.values())));
        }
    }

    private void fight(Player opponentplayer) {
        System.out.println("der" + name + "greift dich an");
    }
}
