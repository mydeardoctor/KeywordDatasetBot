SELECT file_unique_id, file_id, voice.audio_class_id
  FROM telegram_user
       INNER JOIN voice
       ON telegram_user.most_recent_voice_id = voice.file_unique_id
 WHERE telegram_user.user_id = ?
   AND telegram_user.most_recent_voice_id IS NOT NULL;