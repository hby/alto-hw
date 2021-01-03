(ns alto-hw.model-test
  (:require [clojure.test :as t :refer [deftest testing is]]
            [alto-hw.model :as m]))

(defn test-state-a [] (m/mk-state-a))

(defn c-state
  [c]
  {:coins c})

(defn i-data
  [i0 i1 i2]
  {"0" i0 "1" i1 "2" i2})

(defn i-state
  [i0 i1 i2]
  {:inventory (i-data i0 i1 i2)})

(defn test-s
  [c i0 i1 i2]
  (merge (c-state c)
         (i-state i0 i1 i2)))

(defn test-r
  [r c i0 i1 i2]
  [r (test-s c i0 i1 i2)])

(defn doto-state
  [state-a fs]
  (reduce (fn [_ [f a]] (if a
                          (f state-a a)
                          (f state-a)))
          nil
          fs))

(deftest starting-inventory-test
  (testing "starting-inventory-test"
    (is (= (doto-state (test-state-a)
                       [[m/inventory]])
           (i-data 5 5 5)))))

(deftest happy-selection-test
  (testing "happy-selection-test"
    (let [s-a (test-state-a)
          r   (doto-state s-a
                          [[m/add-coins! 1]
                           [m/add-coins! 1]
                           [m/make-selection! "0"]])]
      (is (= [r @s-a]
             (test-r [0 4] 0 4 5 5))))))

(deftest add-coins-state-test
  (testing "add-coins-state-test"
    (let [s-a (test-state-a)]
      (doto-state s-a
                  [[m/add-coins! 1]
                   [m/add-coins! 1]])
      (is (= @s-a
             (test-s 2 5 5 5))))))

(deftest return-coins-full-test
  (testing "return-coins-full-test"
    (let [s-a (test-state-a)
          c   (doto-state s-a
                          [[m/add-coins! 1]
                           [m/add-coins! 1]
                           [m/add-coins! 1]
                           [m/add-coins! 1]
                           [m/return-coins!]])]
      (is (= [c @s-a]
             (test-r 4 0 5 5 5))))))

(defn mk-selection [id] [m/make-selection! (str id)])

(deftest deplete-inventory-test
  (testing "deplete-inventory-test"
    (let [s-a (test-state-a)
          r   (doto-state s-a
                          (take (* 3 3 5)
                                (cycle
                                  (mapcat #(vector [m/add-coins! 1]
                                                   [m/add-coins! 1]
                                                   (mk-selection %)) [0 1 2]))))]
      (is (= [r @s-a]
             (test-r [0 0] 0 0 0 0))))))
