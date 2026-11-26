import java.io.*;
import java.util.*;

/**
 * Simple Event Registration System (PS No. 47)
 * Concepts: ArrayList, HashMap, Queue, Methods, File Handling
 */
public class EventRegistration {

    static final int SEATS = 2;
    static final String FILE = "events.txt";
    static final String[] EVENTS = {"Tech Fest", "Music Night", "Sports Day"};

    // Registration record
    static class Reg {
        String id, name, event;
        Reg(String id, String name, String event) { this.id = id; this.name = name; this.event = event; }
    }

    // event name -> confirmed registrations
    static Map<String, List<Reg>> confirmed = new HashMap<>();
    // event name -> FIFO waiting queue
    static Map<String, Queue<Reg>> waiting = new HashMap<>();
    static int nextId = 101;   // first ID is REG101
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        for (String e : EVENTS) {
            confirmed.put(e, new ArrayList<>());
            waiting.put(e, new LinkedList<>());
        }
        loadData();

        while (true) {
            System.out.println("\n===== EVENT REGISTRATION SYSTEM =====");
            System.out.println("1. Register");
            System.out.println("2. Cancel Registration");
            System.out.println("3. Display Registrations");
            System.out.println("4. Show Waiting List");
            System.out.println("5. Exit");
            System.out.print("Choose option: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1": register(); break;
                case "2": cancel(); break;
                case "3": displayRegistrations(); break;
                case "4": showWaitingList(); break;
                case "5":
                    saveData();
                    System.out.println("Data saved. Goodbye!");
                    return;
                default: System.out.println("Invalid option. Please choose 1-5.");
            }
        }
    }

    // ---------- helpers ----------
    static String findEvent(String input) {
        for (String e : EVENTS) if (e.equalsIgnoreCase(input.trim())) return e;
        return null;
    }

    static String availableEvents() {
        return String.join(", ", EVENTS);
    }

    // ---------- features ----------
    static void register() {
        System.out.print("Name: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) { System.out.println("Name cannot be empty."); return; }
        System.out.print("Event: ");
        String input = sc.nextLine().trim();

        String event = findEvent(input);
        if (event == null) {
            System.out.println("Event " + input + " not found. Available: " + availableEvents());
            return;
        }

        Reg r = new Reg("REG" + nextId++, name, event);
        List<Reg> list = confirmed.get(event);
        if (list.size() < SEATS) {
            list.add(r);
            System.out.println("Registered. ID: " + r.id + " | " + r.name + " | " + r.event + " | Status: Confirmed");
            if (list.size() == SEATS) System.out.println("Seats left: 0");
        } else {
            waiting.get(event).add(r);
            System.out.println("Event full. ID: " + r.id + " | " + r.name + " | " + r.event + " | Status: Waiting");
        }
    }

    static void cancel() {
        System.out.print("Enter registration ID: ");
        String id = sc.nextLine().trim().toUpperCase();

        for (String event : EVENTS) {
            List<Reg> list = confirmed.get(event);
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).id.equals(id)) {
                    list.remove(i);
                    String msg = id + " cancelled.";
                    Reg next = waiting.get(event).poll();   // FIFO promotion
                    if (next != null) {
                        list.add(next);
                        msg += " " + next.id + " | " + next.name + " moved to Confirmed";
                    }
                    System.out.println(msg);
                    return;
                }
            }
            // also allow cancelling from waiting list
            Iterator<Reg> it = waiting.get(event).iterator();
            while (it.hasNext()) {
                if (it.next().id.equals(id)) {
                    it.remove();
                    System.out.println(id + " cancelled (removed from waiting list).");
                    return;
                }
            }
        }
        System.out.println("Registration ID " + id + " not found.");
    }

    static void displayRegistrations() {
        for (String event : EVENTS) {
            List<Reg> list = confirmed.get(event);
            System.out.println("\n" + event + " (" + list.size() + "/" + SEATS + " confirmed)");
            if (list.isEmpty()) System.out.println("  No registrations.");
            for (Reg r : list) System.out.println("  " + r.id + " | " + r.name);
        }
    }

    static void showWaitingList() {
        int total = 0;
        for (String event : EVENTS) {
            Queue<Reg> q = waiting.get(event);
            if (q.isEmpty()) continue;
            System.out.println("\n" + event + " - Waiting list: " + q.size());
            int n = 1;
            for (Reg r : q) System.out.println("  " + (n++) + ". " + r.id + " | " + r.name + " | " + r.event);
            total += q.size();
        }
        if (total == 0) System.out.println("Waiting list: 0");
    }

    // ---------- file handling ----------
    // Line format: ID|Name|Event|Status
    static void saveData() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            for (String event : EVENTS) {
                for (Reg r : confirmed.get(event))
                    pw.println(r.id + "|" + r.name + "|" + r.event + "|Confirmed");
                for (Reg r : waiting.get(event))
                    pw.println(r.id + "|" + r.name + "|" + r.event + "|Waiting");
            }
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    static void loadData() {
        File f = new File(FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length < 4 || !confirmed.containsKey(p[2])) continue;
                Reg r = new Reg(p[0], p[1], p[2]);
                if (p[3].equals("Confirmed")) confirmed.get(p[2]).add(r);
                else waiting.get(p[2]).add(r);
                try {
                    int num = Integer.parseInt(p[0].substring(3));
                    if (num >= nextId) nextId = num + 1;
                } catch (NumberFormatException ignored) {}
            }
        } catch (IOException e) {
            System.out.println("Error loading data: " + e.getMessage());
        }
    }
}
