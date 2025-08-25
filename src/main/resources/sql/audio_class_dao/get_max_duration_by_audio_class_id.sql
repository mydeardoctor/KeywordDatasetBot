SELECT max_duration_seconds
  FROM audio_class
 WHERE audio_class_id = ?
   AND audio_class_id IS NOT NULL;