package client1;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.regex.Pattern;

public class MultithreadedClient {
  private int numRequests = 0;
  private int numSuccessfulRequests = 0;
  private int numUnsuccessfulRequests = 0;

  synchronized public void incrementNumRequests() {
    this.numRequests += 1;
  }

  synchronized public void incrementNumSuccessfulRequests() {
    this.numSuccessfulRequests += 1;
  }

  synchronized public void incrementNumUnsuccessfulRequests() {
    this.numUnsuccessfulRequests += 1;
  }


  public static void main(String[] args) throws IOException, InterruptedException {
    HashMap<String, String> argsMap = parseArgs(args);
    if (argsMap == null) {
      System.out.println("The command line arguments are not valid...");
      return;
    }

    MultithreadedClient multithreadedClient = new MultithreadedClient();
    long start = System.currentTimeMillis();
    int maxStore = Integer.parseInt(argsMap.get("maxStore"));
    int numCustomersPerStore = Integer.parseInt(argsMap.get("numCustomersPerStore"));
    int maxItemId = Integer.parseInt(argsMap.get("maxItemId"));
    int numPurchases = Integer.parseInt(argsMap.get("numPurchases"));
    int numItemsPurPurchase = Integer.parseInt(argsMap.get("numItemsPurPurchase"));
    String date = argsMap.get("date");
    String ipWithPort = argsMap.get("ipWithPort");

    ShopRunnable[] shopRunnables = new ShopRunnable[maxStore];
    for (int i = 0; i < maxStore; i ++) {
      shopRunnables[i] = new ShopRunnable(
          i+1,
          numCustomersPerStore,
          maxItemId,
          numPurchases,
          numItemsPurPurchase,
          date,
          ipWithPort,
          multithreadedClient);
    }
    Thread[] shopThreads = new Thread[maxStore];
    for (int i = 0; i < maxStore; i ++) {
      shopThreads[i] = new Thread(shopRunnables[i]);
      shopThreads[i].start();
    }

    try {
      for (int i = 0; i < maxStore; i++) {
        shopThreads[i].join();
      }
    } catch (InterruptedException e) {
    }
    long end = System.currentTimeMillis();

    System.out.println("Multithreaded Client 1 Report:");
    System.out.println("Maximum number of stores: " + maxStore);
    System.out.println("Total number of requests sent: " + multithreadedClient.numRequests);
    System.out.println("Total number of successful requests sent: " + multithreadedClient.numSuccessfulRequests);
    System.out.println("Total number of unsuccessful requests sent: " + multithreadedClient.numUnsuccessfulRequests);
    System.out.println("The wall time (milliseconds): " + (end - start));
    System.out.println("The throughput (requests/second): " + (multithreadedClient.numSuccessfulRequests/((end-start)/1000)));
  }

  private static HashMap<String, String> parseArgs(String[] args) {
    HashMap<String, String> argsMap = new HashMap<>();
    argsMap.put("numCustomersPerStore", "1000");
    argsMap.put("maxItemId", "100000");
    argsMap.put("numPurchases", "300");
    argsMap.put("numItemsPurPurchase", "5");
    argsMap.put("date", "20210101");

    if (args.length == 0 || args.length % 2 != 0) {
      System.out.println("Missing parameters...");
      return null;
    }

    for (int i = 0; i < args.length; i += 2) {
      switch (args[i]) {
        case "-ms":
          if (!is32Int(args[i + 1])) {
            System.out.println("maxStore should be a valid 32-bit integer...");
            return null;
          }
          argsMap.put("maxStore", args[i + 1]);
          break;
        case "-ncps":
          if (!is32Int(args[i + 1])) {
            System.out.println("numCustomersPerStore should be a valid 32-bit integer...");
            return null;
          }
          argsMap.put("numCustomersPerStore", args[i + 1]);
          break;
        case "-mii":
          if (!is32Int(args[i + 1])) {
            System.out.println("maxItemId should be a valid 32-bit integer...");
            return null;
          }
          argsMap.put("maxItemId", args[i + 1]);
          break;
        case "-np":
          if (!is32Int(args[i + 1])) {
            System.out.println("numPurchases should be a valid 32-bit integer...");
            return null;
          }
          argsMap.put("numPurchases", args[i + 1]);
          break;
        case "-nipp":
          if (!is32Int(args[i + 1])) {
            System.out.println("numItemsPurPurchase should be a valid 32-bit integer...");
            return null;
          }
          argsMap.put("numItemsPurPurchase", args[i + 1]);
          break;
        case "-d":
          if (!isValidDate(args[i + 1])) {
            System.out.println("date should be a valid date...");
            return null;
          }
          argsMap.put("date", args[i + 1]);
          break;
        case "-iwp":
          if (!isValidIpWithPort(args[i + 1])) {
            System.out.println("ipWithPort should be a valid ip with port");
            return null;
          }
          argsMap.put("ipWithPort", args[i + 1]);
          break;
        default:
          System.out.println("Unknown option: " + args[i] + "...");
          return null;
      }
    }

    if (!argsMap.containsKey("maxStore")) {
      System.out.println("Missing maxStore...");
      return null;
    }
    if (!argsMap.containsKey("ipWithPort")) {
      System.out.println("Missing ipWithPort...");
      return null;
    }

    return argsMap;
  }

  private static boolean is32Int(String s) {
    try {
      Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

  private static boolean isValidDate(String s) {
    DateTimeFormatter dateFormatter = DateTimeFormatter.BASIC_ISO_DATE;
    try {
      LocalDate.parse(s, dateFormatter);
    } catch (DateTimeParseException e) {
      return false;
    }
    return true;
  }

  private static boolean isValidIpWithPort(String s) {
    Pattern p = Pattern.compile("^"
        + "(((?!-)[A-Za-z0-9-]{1,63}(?<!-)\\.)+[A-Za-z]{2,6}" // Domain name
        + "|"
        + "localhost" // localhost
        + "|"
        + "(([0-9]{1,3}\\.){3})[0-9]{1,3})" // Ip
        + ":"
        + "[0-9]{1,5}$"); // Port
    return p.matcher(s).matches();
  }
}
