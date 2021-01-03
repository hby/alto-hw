# Assumptions

 - Clojure is properly installed
   - to install with `brew`
     (Note: if need to install `brew` see [https://brew.sh](https://brew.sh))
    ```shell
    brew install clojure/tools/clojure
    ```

# Run

## Server
To start the server
```shell
cd <this project directory>
clojure -X:server
```
this runs on the default port of `8080`.

To run the sever on a specific port use:
```shell
clojure -X:server :port <port>
```


## Tests
To run tests
```shell
clojure -M:test:runner
```

# Notes and assumptions on reading and understanding requirements

"The purchase price of an item is 2 Coins."

"Machine only holds 3 unique items, and holds 5 of each item in its inventory."
 - How do you identify an item?
   Since the result of `GET /inventory` is "An array of remaining item quantities (an array of integers)"
   I'm going to assume the ids are zero based indices `0, 1, and 2`. 

Will use in memory map data structure to store inventory quantities
- `{"0" 5 "1" 5 "2" 5}`
  - String keys makes for an easy lookup based on request url parameter
    
"Machine will accept more than the purchase price of coins, but will only dispense a
single item per transaction."
 - Going to assume you can continue to put in coins until you select an item

"Upon transaction completion, all unused Coins must be returned to the customer."

### This leads to the following description of the machine
Machine starts with 5 of each item with id 0, 1, and 2.
As items are purchased the inventory runs down to zero for each item.
There is no route for replenishing inventory.

- At any moment you can do any of:
  - add a coin
  - return coins
  - ask for inventory
  - make a selection

- add a coin
  - increment coins state
  - return coins added up to now

- return coins
  - set coin state to 0
  - return coins added up to now

- ask for inventory
  - return all inventory counts

- make a selection
  - can-purchase?
    - return 
      - {“quantity”: 1}
      - coins added up to now - 2
      - new inventory
  - NOT in-stock?
    - return
      - 404, coins added up to now
  - Otherwise (not enough coins),
    - return
      - 403, coins added up to now (will be 0 or 1)
