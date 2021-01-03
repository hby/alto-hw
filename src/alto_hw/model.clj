(ns alto-hw.model)

(defn mk-state-a
  "State consists of
   the number of coins accepted and
   the current inventory of the 3 items."
  []
  (atom {:coins 0
         :inventory {"0" 5 "1" 5 "2" 5}}))

(defn coins-accepted
  "Returns the number of coins accepted."
  [state-a]
  (:coins @state-a))

(defn inventory
  "Returns either all of the inventory as a vector
  or the single inventory of the item given an id."
  ([state-a] (:inventory @state-a))
  ([state-a id] (get (inventory state-a) id)))

(defn add-coins!
  "Adding a coins changes the state to add to the
  number of coins accepted."
  [state-a n]
  (swap! state-a update :coins + n))

(defn return-coins!
  [state-a]
  "Returns the number of coins accepted and resets
  the state to have 0 coins accepted."
  (let [c (coins-accepted state-a)]
    (swap! state-a assoc :coins 0)
    c))

(defn sufficient-funds?
  "Returns true if the state has at least
  2 coins accepted."
  [state-a]
  (<= 2 (coins-accepted state-a)))

(defn in-stock?
  "Returns true if the state has at least
  1 item of the given id in the inventory."
  [state-a id]
  (<= 1 (inventory state-a id)))

(defn can-purchase?
  "Returns true if the state satisfies both
  sufficient-funds? and in-stock? for the
  given id."
  [state-a id]
  (and (sufficient-funds? state-a)
       (in-stock? state-a id)))

(defn make-selection!
  "Guarded by whether the state satisfies can-purchase?
  for the given id.

  The state is set to have 0 coins accepted and the
  inventory of the item of the given id is decremented.

  Returns the number of coins accepted - 2
  and the new item inventory as a 2 element vector."
  [state-a id]
  {:pre [(can-purchase? state-a id)]}
  (let [[o n] (swap-vals! state-a
                          (fn [cv]
                            (cond-> (assoc cv :coins 0)
                                    (< 0 (get-in cv [:inventory id])) (update-in [:inventory id] dec))))]
    [(- (:coins o) 2) (get-in n [:inventory id])]))
