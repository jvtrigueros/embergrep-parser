(ns ember-grep.core
  (:gen-class)
  (:require [clojure.data.json :as json]
            [clj-http.client :as client])
  (:use clojure.pprint))

(def ^:private base-url "https://embergrep.com")

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
  "Given a course slug it returns the course's lessons."
  [access-token course-slug]
  (let [url (str base-url "/api/courses/" course-slug "?access_token=" access-token)
        response (client/get url)
        body (json->map response)]
    (get-in body [:course 0 :lessons])))

(defn lessons
  "Given a list of lesson ids, returns a list of expanded lesson content."
  [access-token lesson-ids]
  (let [ids (reduce #(str %1 "&ids[]=" %2) "" lesson-ids)
        url (str base-url "/api/lessons" "?access_token=" access-token ids)
        response (client/get url)
        body (json->map response)]
    (map #(select-keys % [:lessonNotes :video :slug :files]) (:lessons body))))

(defn lesson-files
  "Give a list of file ids, return a list of their actual content."
  [access-token file-ids]
  (let [ids (reduce #(str %1 "&ids[]=" %2) "" file-ids)
        url (str base-url "/api/lesson-files" "?access_token=" access-token ids)
        response (client/get url)
        body (json->map response)]
    (map #(select-keys % [:name :contents]) (:lesson-files body))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println (authenticate (get-input "Username: ") (get-input "Password:"))))
