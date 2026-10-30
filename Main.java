import java.util.ArrayList;
import java.util.List;

// ---------- Req 8: custom exception for low battery ----------
class LowBatteryException extends Exception {
    public LowBatteryException(String message) {
        super(message);
    }
}

// ---------- Req 1: base interface ----------
interface Robot {
    void start() throws LowBatteryException;
    void stop();
    String status();
}

// ---------- Req 2: abstract class with shared attributes + constructor ----------
abstract class BaseRobot implements Robot {
    protected static final int MIN_BATTERY = 10;

    protected final String id;
    protected int batteryLevel;   // 0 - 100
    protected boolean running;

    public BaseRobot(String id, int batteryLevel) {
        this.id = id;
        this.batteryLevel = Math.max(0, Math.min(100, batteryLevel));
        this.running = false;
    }

    @Override
    public void start() throws LowBatteryException {
        if (batteryLevel < MIN_BATTERY) {
            throw new LowBatteryException(id + ": battery too low to start (" + batteryLevel + "%)");
        }
        running = true;
        System.out.println(id + " started.");
    }

    @Override
    public void stop() {
        running = false;
        System.out.println(id + " stopped.");
    }

    @Override
    public String status() {
        return id + " | battery: " + batteryLevel + "% | " + (running ? "running" : "stopped");
    }

    // Shared helper: every action drains battery and can fail
    protected void consumeBattery(int amount) throws LowBatteryException {
        if (!running) {
            System.out.println(id + " is not running. Call start() first.");
            return;
        }
        if (batteryLevel - amount < 0) {
            running = false;
            throw new LowBatteryException(id + ": not enough battery for this action (" + batteryLevel + "%)");
        }
        batteryLevel -= amount;
    }

    public String getId() {
        return id;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }
}

// ---------- Req 3 + 4: FlyingRobot ----------
class FlyingRobot extends BaseRobot {
    private int altitude;

    public FlyingRobot(String id, int batteryLevel) {
        super(id, batteryLevel);
        this.altitude = 0;
    }

    public FlyingRobot(String id, int batteryLevel, int startAltitude) {   // constructor overloading
        super(id, batteryLevel);
        this.altitude = startAltitude;
    }

    // Method overloading: move()
    public void move() throws LowBatteryException {
        move(10);
    }

    public void move(int altitudeChange) throws LowBatteryException {
        consumeBattery(5);
        if (running) {
            altitude = Math.max(0, altitude + altitudeChange);
            System.out.println(id + " changed altitude to " + altitude + "m.");
        }
    }

    public void move(int altitudeChange, String direction) throws LowBatteryException {
        move(altitudeChange);
        if (running) {
            System.out.println(id + " flying " + direction + ".");
        }
    }

    public void fly() throws LowBatteryException {
        consumeBattery(8);
        if (running) {
            System.out.println(id + " is performing aerial maneuvers at " + altitude + "m.");
        }
    }

    @Override
    public void stop() {            // override: must land first
        altitude = 0;
        System.out.println(id + " landing...");
        super.stop();
    }

    @Override
    public String status() {        // override
        return "[Flying] " + super.status() + " | altitude: " + altitude + "m";
    }
}

// ---------- Req 3 + 4: MovingRobot ----------
class MovingRobot extends BaseRobot {
    private int wheels;

    public MovingRobot(String id, int batteryLevel, int wheels) {
        super(id, batteryLevel);
        this.wheels = wheels;
    }

    public void move() throws LowBatteryException {
        move(5);
    }

    public void move(int speed) throws LowBatteryException {
        move(speed, "forward");
    }

    public void move(int speed, String direction) throws LowBatteryException {
        consumeBattery(3);
        if (running) {
            System.out.println(id + " moving " + direction + " at " + speed + " km/h on " + wheels + " wheels.");
        }
    }

    public void drive() throws LowBatteryException {
        consumeBattery(6);
        if (running) {
            System.out.println(id + " is driving across the terrain.");
        }
    }

    @Override
    public String status() {
        return "[Moving] " + super.status() + " | wheels: " + wheels;
    }
}

// ---------- Req 3 + 4: WalkingRobot ----------
class WalkingRobot extends BaseRobot {
    private int steps;

    public WalkingRobot(String id, int batteryLevel) {
        super(id, batteryLevel);
        this.steps = 0;
    }

    public void move() throws LowBatteryException {
        move(10);
    }

    public void move(int numberOfSteps) throws LowBatteryException {
        consumeBattery(2);
        if (running) {
            steps += numberOfSteps;
            System.out.println(id + " walked " + numberOfSteps + " steps (total " + steps + ").");
        }
    }

    public void move(int numberOfSteps, String direction) throws LowBatteryException {
        move(numberOfSteps);
        if (running) {
            System.out.println(id + " heading " + direction + ".");
        }
    }

    public void walk() throws LowBatteryException {
        consumeBattery(4);
        if (running) {
            System.out.println(id + " is walking on two legs.");
        }
    }

    public void stepOver(String obstacle) throws LowBatteryException {
        consumeBattery(5);
        if (running) {
            System.out.println(id + " stepped over a " + obstacle + ".");
        }
    }

    @Override
    public String status() {
        return "[Walking] " + super.status() + " | steps: " + steps;
    }
}

// ---------- Req 7: controller using polymorphism (depends on the Robot interface) ----------
class RobotController {
    private final List<Robot> robots = new ArrayList<>();

    public void addRobot(Robot robot) {
        robots.add(robot);
    }

    public void startAll() {
        for (Robot r : robots) {
            try {
                r.start();
            } catch (LowBatteryException e) {
                System.out.println("WARNING: " + e.getMessage());
            }
        }
    }

    public void stopAll() {
        for (Robot r : robots) {
            r.stop();
        }
    }

    public void printStatus() {
        for (Robot r : robots) {
            System.out.println(r.status());
        }
    }
}

// ---------- Req 5 + 6: main ----------
public class Main {
    public static void main(String[] args) {
        // Constructors with custom attributes
        FlyingRobot drone = new FlyingRobot("FLY-01", 80, 5);
        MovingRobot rover = new MovingRobot("MOV-01", 60, 4);
        WalkingRobot biped = new WalkingRobot("WLK-01", 8);   // battery too low to start

        RobotController controller = new RobotController();
        controller.addRobot(drone);
        controller.addRobot(rover);
        controller.addRobot(biped);

        System.out.println("=== Starting all robots ===");
        controller.startAll();     // biped triggers LowBatteryException

        System.out.println("\n=== Unique behaviours & overloaded move() ===");
        try {
            drone.fly();
            drone.move();                 // default
            drone.move(20);               // altitude
            drone.move(15, "north");      // altitude + direction

            rover.drive();
            rover.move();
            rover.move(30);
            rover.move(45, "east");

            biped.walk();                 // not running -> message
        } catch (LowBatteryException e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        System.out.println("\n=== Draining battery to show exception handling ===");
        try {
            for (int i = 0; i < 20; i++) {
                rover.drive();
            }
        } catch (LowBatteryException e) {
            System.out.println("ERROR: " + e.getMessage());
        } finally {
            System.out.println("Rover check complete.");
        }

        System.out.println("\n=== Status (polymorphism) ===");
        controller.printStatus();

        System.out.println("\n=== Stopping all robots ===");
        controller.stopAll();
    }
}

/*
 * SOLID principles used (Req 9):
 * S - Single Responsibility: each class has one job (robot behaviour vs. RobotController managing robots,
 *     LowBatteryException only represents the error).
 * O - Open/Closed: new robot types (e.g. SwimmingRobot) can be added by extending BaseRobot
 *     without changing RobotController.
 * L - Liskov Substitution: any BaseRobot subclass can be used wherever a Robot is expected.
 * I - Interface Segregation: Robot has only the three methods every robot needs; unique actions
 *     (fly, drive, walk) stay in the specific subclasses.
 * D - Dependency Inversion: RobotController depends on the Robot interface, not on concrete classes.
 */
