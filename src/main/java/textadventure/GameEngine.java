package textadventure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GameEngine {

    private GameState currentState;
    private StandIn target;

    public GameEngine(GameState state, StandIn target) {
        currentState = state;
        this.target = target;
    }

    public Iterable<Thing> getVisibleThings() {

        if (currentState.getPlayerStandsAt() != null){
            //if player stands at a thing then he focuses just on that thing
            if (currentState.getPlayerStandsAt() instanceof Container)
                //if that thing is something more complex then player sees its content
                return  ((Container) currentState.getPlayerStandsAt()).getThings();
            //if the thing itself is just something simple enough then the player sees the whole thing only
            return Arrays.asList((Thing)currentState.getPlayerStandsAt());
        }
        if (currentState.getPlayerStandsIn() != null) {
            //if player stands in a container then he can see anything in that container
            return currentState.getPlayerStandsIn().getThings();
        }
        //weird state - return nothing
        return null;
    }

    public void printCurrentState() {
        if (currentState.getPlayerStandsIn() != null) {
            message("You are in "+currentState.getPlayerStandsIn().toString());
        } else if (currentState.getPlayerStandsAt() != null) {
            message("You are at "+currentState.getPlayerStandsAt().toString());
        }
    }

    public void printActions(List<GameAction> actions) {
        message("What do you want to do?");
        for (int i = 1; i <= actions.size();i++) {
            message(i +"- "+ actions.get(i-1).toString());
        }
    }

    private Container getCurrentContainer() {
        if (currentState.getPlayerStandsIn() != null) return currentState.getPlayerStandsIn();
        if (currentState.getPlayerStandsAt() instanceof Container)
            return (Container) currentState.getPlayerStandsAt();
        return null;
    }

    private void message(String message) {
        System.out.println(message);
    }

    public List<GameAction> getAvailableActions() {
        Iterable<Thing> visibleThings = getVisibleThings();

        List<GameAction> result = new ArrayList<>();
        for (Thing thing : visibleThings) {
            thing.getActions(this).forEach(result::add);
        }
        for (Key key : currentState.getKeys()) {
            result.add(new Drop(key));
        }
        return result;
    }

    public Iterable<GameAction> getActions(Door door) {
        List<GameAction> result = new ArrayList<>();
        if (door.getState() == DoorState.Closed) {
            result.add(new OpenDoor(door));
            if (door.getLockState() == DoorLockState.Locked)
                result.add(new UnlockDoor(door, door.getCompatibleKey()));
            if (door.getLockState() == DoorLockState.Unlocked)
                result.add(new LockDoor(door, door.getCompatibleKey()));
        }
        if (door.getState() == DoorState.Open) {
            result.add(new CloseDoor(door));
            result.add(new GoThrough(door));
        }

        return result;
    }

    public Iterable<GameAction> getActions(Room room) {
        List<GameAction> result = new ArrayList<>();
        if (currentState.getPlayerStandsIn() != room)
            result.add(new Goto());
        return result;
    }

    public Iterable<GameAction> getActions(Table table) {
        List<GameAction> result = new ArrayList<>();
        if (currentState.getPlayerStandsAt() == table)
            result.add(new Goto());
        return result;
    }

    public Iterable<GameAction> getActions(Key key) {
        List<GameAction> result = new ArrayList<>();
        if (!currentState.getKeys().contains(key)) {
            result.add(new Grab(key));
        }
        return result;
    }

    public Iterable<GameAction> getActions(LightSwitch lightSwitch) {
        List<GameAction> result = new ArrayList<>();
        return result;
    }

    public Iterable<GameAction> getActions(Light light) {
        List<GameAction> result = new ArrayList<>();
        return result;
    }

    public void apply(OpenDoor openDoor) {
        if (openDoor.getDoor().getLockState()== DoorLockState.Locked) {
            message("The door is locked");
            return;
        }
        openDoor.getDoor().setState(DoorState.Open);
        message("You have opened the door");
    }

    public void apply(CloseDoor closeDoor) {
        closeDoor.getDoor().setState(DoorState.Closed);
        message("You have closed the door");
    }

    public void apply(GoThrough goThrough) {
        if (currentState.getPlayerStandsIn() == goThrough.getDoor().getTo()) {
            currentState.setPlayerStandsIn(goThrough.getDoor().getFrom());
            message("You have gone through the door");
            return;
        }
        if (currentState.getPlayerStandsIn() == goThrough.getDoor().getFrom()) {
            currentState.setPlayerStandsIn(goThrough.getDoor().getTo());
            message("You have gone through the door");
            return;
        }
        message("You can't go through this door");
    }

    public void apply(LockDoor lockDoor) {
        if (currentState.getKeys().contains(lockDoor.getDoor().getCompatibleKey())) {
            lockDoor.getDoor().setLockState(DoorLockState.Locked);
            message("You have locked the door");
        } else {
            message("You don't have a key for this door");
        }
    }

    public void apply(UnlockDoor unlockDoor) {
        if (currentState.getKeys().contains(unlockDoor.getDoor().getCompatibleKey())) {
            unlockDoor.getDoor().setLockState(DoorLockState.Unlocked);
            message("You have unlocked the door");
        } else {
            message("You need a key");
        }
    }

    public void apply(Goto aGoto) {

    }

    public void apply(Grab grab) {
        if (grab.getThing() instanceof Key) {
            if (getCurrentContainer() != null){
                getCurrentContainer().removeThing(grab.getThing());
            }
            currentState.getKeys().add((Key)grab.getThing());
            message("You have grabbed the key");
        } else {
            message("You can't grab this thing");
        }
    }

    public void apply(Drop drop) {
        Container container = getCurrentContainer();
        if (container == null) {
            message("You can't drop "+drop.thing+ " here");
            return;
        }
        container.addThing(drop.thing);
        if (drop.thing instanceof Key) {
            currentState.getKeys().remove(drop.thing);
        }
    }

    public Boolean checkVictory() {
        if (currentState.getPlayerStandsIn() == target)
            return true;
        return false;
    }
}
