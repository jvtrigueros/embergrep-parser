(ns ember-grep.core
  (:gen-class)
  (:require [clojure.data.json :as json]
            [clj-http.client :as client])
  (:use clojure.pprint)
  (:import (java.io File)))

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

(defn- mkdir-p
  [path]
  (when-let [filtered-path (not-empty (drop-last path))]
    (.mkdirs (File. (clojure.string/join (File/separator) filtered-path)))))

(defn save-lesson
  ""
  [data files]
  (let [{:keys [lessonNotes video slug]} data]
    (println "Creating " slug " directory.")
    (.mkdir (File. slug))

    (println slug "Saving notes.")
    (spit (File. (str slug "/notes.md")) lessonNotes)

    (println slug "Saving files.")
    (doseq [{:keys [name contents]} files]
      (mkdir-p (conj (seq (clojure.string/split name #"/")) slug))
      (spit (File. (str slug "/" name)) contents))

    (print slug "Saving video.")
    (with-open [in (clojure.java.io/input-stream (:sourceMp4Hd video))
                out (clojure.java.io/output-stream (File. (str slug "/lesson.mp4")))]
      (clojure.java.io/copy in out))
    ))

(defn download
  "Download all the content from the EmberGrep website."
  [username password]
  (let [token (:access_token (authenticate username password))
        course "zero-to-prototype"                          ;TODO: This would be dynamically calculated.
        [courses lessons lesson-files] (map #(partial % token) [courses lessons lesson-files])
        lesson-data (lessons (courses course))]
    (doseq [lesson lesson-data]
      (future (save-lesson lesson (lesson-files (:files lesson)))))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println (authenticate (get-input "Username: ") (get-input "Password:"))))
