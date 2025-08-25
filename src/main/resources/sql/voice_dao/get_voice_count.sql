SELECT audio_class_id, COUNT(audio_class_id) AS count
  FROM voice
 WHERE user_id = ?
 GROUP BY audio_class_id;