(ns account.db-test
  "db空间测试"
  (:require [clojure.test :refer :all]
            [datomic.api :as d]
            [account.db :refer :all]))

(def conn (mk-conn "datomic:mem:acc-db-test" account-schema))

(deftest 组装账户数据
  (is (= [{:account/username "foo" :account/password "123456"}]
         (gen-account {:username "foo" :password "123456"}))))

(deftest 多次组装账户数据
  (are [expect username password]
    (= expect
       (gen-account {:username username :password password}))
    [{:account/username "foo" :account/password "123456"}] "foo" "123456"
    [{:account/username "bar" :account/password "123"}] "bar" "123"))

(deftest 查询账户数据
  (update-data conn (gen-account {:username "foo" :password "123456"}))
  (is (= {:account/username "foo" :account/password "123456"}
         (-> (find-account (d/db conn) "foo")
             (select-keys [:account/username :account/password])))))
