(ns alto-hw.server-test
  (:require [clojure.test :as t :refer [deftest testing is]]
            [alto-hw.server :as svr]
            [alto-hw.model :as m]
            [ring.mock.request :as mock]
            [cheshire.core :as json]))

(defn test-app [] (svr/mk-app (m/mk-state-a)))

(defn pb
  [body]
  (json/parse-string body))

(defn headers
  ([code] (headers code nil nil))
  ([code coins remaining]
   (cond-> (if (= 200 code)
             {"Content-Type" "application/json; charset=utf-8"}
             {})
           coins (assoc "X-Coins" (str coins))
           remaining (assoc "X-Inventory-Remaining" (str remaining)))))

(defn request
  [op route & [body]]
  (cond-> (mock/request op route)
          body (mock/json-body body)))

(defn do-requests
  [app rs]
  (let [res (reduce (fn [_ r] (app (apply request r)))
                    {}
                    rs)]
    (if (and (:body res) (not= "" (:body res)))
      (update res :body pb)
      (dissoc res :body))))

(def r-add-coin [:put "/" {:coin 1}])
(defn r-make-selection [id] [:put (str "/inventory/" id)])
(def r-inventory [:get "/inventory"])

(defn r-purchase
  [n id]
  (take (* 3 n) (cycle [r-add-coin r-add-coin (r-make-selection id)])))

(deftest inventory-test
  (testing "inventory-test"
    (is (= (do-requests (test-app)
                        [r-inventory])
           {:status  200
            :headers (headers 200)
            :body    [5 5 5]}))))

(deftest happy-purchase-test
  (testing "happy-purchase-test"
    (is (= (do-requests (test-app)
                        (r-purchase 1 0))
           {:status  200
            :headers (headers 200 "0" "4")
            :body    {"quantity" 1}}))))

(deftest reduce-inventory-test
  (testing "reduce-inventory-test"
    (is (= (do-requests (test-app)
                        (concat (r-purchase 1 0)
                                [r-inventory]))
           {:status  200
            :headers (headers 200)
            :body    [4 5 5]}))))

(deftest deplete-stock-test
  (testing "deplete-stock-test"
    (is (= (do-requests (test-app)
                        (concat
                          (r-purchase 5 0)
                          [r-inventory]))
           {:status  200
            :headers (headers 200)
            :body    [0 5 5]}))))

(deftest out-of-stock-1-test
  (testing "out-of-stock-1-test"
    (is (= (do-requests (test-app)
                        (concat
                          (r-purchase 5 0)
                          [(r-make-selection 0)]))
           {:status  404
            :headers (headers 404 0 nil)}))))

(deftest out-of-stock-2-test
  (testing "out-of-stock-2-test"
    (is (= (do-requests (test-app)
                        (concat
                          (r-purchase 5 0)
                          [r-add-coin
                           (r-make-selection 0)]))
           {:status  404
            :headers (headers 404 1 nil)}))))

(deftest out-of-stock-3-test
  (testing "out-of-stock-3-test"
    (is (= (do-requests (test-app)
                        (concat
                          (r-purchase 5 0)
                          [r-add-coin
                           r-add-coin
                           (r-make-selection 0)]))
           {:status  404
            :headers (headers 404 2 nil)}))))

(deftest insufficient-funds-0-test
  (testing "insufficient-funds-0-test"
    (is (= (do-requests (test-app)
                        [(r-make-selection 0)])
           {:status  403
            :headers (headers 403 0 nil)}))))

(deftest insufficient-funds-1-test
  (testing "insufficient-funds-1-test"
    (is (= (do-requests (test-app)
                        [r-add-coin
                         (r-make-selection 0)])
           {:status  403
            :headers (headers 403 1 nil)}))))