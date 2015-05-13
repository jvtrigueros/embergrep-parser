(ns ember-grep.core
  (:gen-class)
  (:require [clj-http.client :as client])
  (:require [clojure.data.json :as json]))

(def ^:private base-url "https://embergrep.com/")

(defn- get-input [prompt]
  (println prompt)
  (read-line))

(defn authenticate
  "Takes a username and password and returns an authentication token"
  [username password]
  (let [url (str base-url "/oauth/access_token")
        form-params {:grant_type "password"
                     :username username
                     :password password}
        response (client/post url {:form-params form-params})
        body (json/read-str (:body response) :key-fn keyword)]
    (select-keys body [:access_token :refresh_token])))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println (authenticate (get-input "Username: ") (get-input "Password:"))))
