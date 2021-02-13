package client1;


import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.PurchaseApi;
import io.swagger.client.model.Purchase;
import io.swagger.client.model.PurchaseItems;
import java.io.IOException;

public class ShopRunnable implements Runnable {
  private int shopId;
  private int numCustomersPerStore;
  private int maxItemId;
  private int numPurchases;
  private int numItemsPurPurchase;
  private String date;
  private String ipWithPort;
  private String basePath = "";
  private MultithreadedClient multithreadedClient;

  public ShopRunnable(
      int shopId,
      int numCustomersPerStore,
      int maxItemId,
      int numPurchases,
      int numItemsPurPurchase,
      String date,
      String ipWithPort,
      MultithreadedClient multithreadedClient) {

    this.shopId = shopId;
    this.numCustomersPerStore = numCustomersPerStore;
    this.maxItemId = maxItemId;
    this.numPurchases = numPurchases;
    this.numItemsPurPurchase = numItemsPurPurchase;
    this.date = date;
    this.ipWithPort = ipWithPort;
    this.basePath = "http://" + this.ipWithPort + "/server_war";
    this.multithreadedClient = multithreadedClient;
  }

  public void run() {
    ApiClient shop = new ApiClient();
    shop.setConnectTimeout(20000);
    shop.setBasePath(this.basePath);
    PurchaseApi apiInstance = new PurchaseApi(shop);
    for (int i = 0; i < 9 * this.numPurchases; i++) {
      Purchase purchase = new Purchase();
      for (int j = 0 ; j < this.numItemsPurPurchase; j++) {
        PurchaseItems item = new PurchaseItems();
        String randomItemId = String
            .valueOf(1 + (int)(Math.random() * (this.maxItemId)));
        item.setItemID(randomItemId);
        item.setNumberOfItems(1);
        purchase.addItemsItem(item);
      }
      Integer storeID = this.shopId;
      int randomCustId = this.shopId * 1000 + (int)(Math.random() * (this.numCustomersPerStore));
      Integer custID = randomCustId;
      String date = this.date;

      try {
        multithreadedClient.incrementNumRequests();
        ApiResponse<Void> res = apiInstance.newPurchaseWithHttpInfo(purchase, storeID, custID, date);
        if (res.getStatusCode() == 201) {
          multithreadedClient.incrementNumSuccessfulRequests();
        } else {
          multithreadedClient.incrementNumUnsuccessfulRequests();
        }
      } catch (ApiException e) {
        System.err.println("Exception when calling PurchaseApi#newPurchase");
        e.printStackTrace();
        multithreadedClient.incrementNumUnsuccessfulRequests();
      }
    }
  }
}
