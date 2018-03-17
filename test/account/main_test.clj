(ns account.main-test
  "集成测试"
  (:require [clojure.test :refer :all]
            [account.main :refer :all]
            [cheshire.core :as json]
            [clj-http.client :as client]))

(def cfg {:web-opts {:host "0.0.0.0" :port 8080 :join? false}
          :datomic-url "datomic:mem:db-name"})

(def server (start-server cfg))

(deftest 查询不存在的用户名
  (is (= (json/generate-string {:exist? false})
         (:body (client/get "http://127.0.0.1:8080/api/exist/foo")))))

(deftest 注册
  (is (= (json/generate-string {:result true :username "foo"})
         (:body (client/post "http://127.0.0.1:8080/api/reg"
                             {:headers {"Content-Type" "application/json; charset=utf-8"}
                              :body (json/generate-string {:username "foo"
                                                           :password "123456"})})))))

(deftest 查询已经存在的用户名
  (is (= (json/generate-string {:exist? true})
         (:body (client/get "http://127.0.0.1:8080/api/exist/foo")))))

(deftest 对已经存在的用户名尝试注册
  (is (= (json/generate-string {:result false :error "user exist"})
         (:body (client/post "http://127.0.0.1:8080/api/reg"
                             {:headers {"Content-Type" "application/json; charset=utf-8"}
                              :body (json/generate-string {:username "foo"
                                                           :password "123456"})})))))
