(ns ember-grep.core
  (:gen-class)
  (:require [clojure.data.json :as json]
            [clj-http.client :as client])
  (:use clojure.pprint))

(def ^:private base-url "https://embergrep.com/")

(defn- get-input [prompt]
  (println prompt)
  (read-line))

(defn- json->map
  "Converts a clj-http.client response's JSON body into a Clojure map."
  [response]
  (json/read-str (:body response) :key-fn keyword))

(defn authenticate
  "Takes a username and password and returns an authentication token"
  [username password]
  (let [url (str base-url "/oauth/access_token")
        form-params {:grant_type "password"
                     :username username
                     :password password}
        response (client/post url {:form-params form-params})
        body (json->map response)]
    (select-keys body [:access_token :refresh_token])))

(defn courses
  "Given a course slug it returns the course's metadata."
  [access_token course-slug]
  (let [url (str base-url "/api/courses/" course-slug "?access_token=" access_token)
        response (client/get url)
        body (json->map response)]
    {:lessons (get-in body [:course 0 :lessons])}))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println (authenticate (get-input "Username: ") (get-input "Password:"))))
